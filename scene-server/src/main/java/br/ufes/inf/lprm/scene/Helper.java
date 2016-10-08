package br.ufes.inf.lprm.scene;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.kie.api.KieServices;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by hborjaille on 10/7/16.
 */
public class Helper {

    private enum JsonType {
        INSERT, UPDATE, DELETE;
    }

    private static final String packagePath = "br.ufes.inf.lprm.scene";

    public static void compileJson(KieSession kSession, File file, JsonType type) {
        try {
            Gson gson = new Gson();
            Type jsonType = new TypeToken<Map<String, Object>>(){}.getType();

            Map<String, Object> myMap = gson.fromJson(new FileReader(file), jsonType);

            JsonReader jsonreader = new JsonReader(new FileReader(file));
            readJson(kSession, jsonreader, myMap, type);
        } catch (IOException e) {
            System.out.println("Could not open " + file.getName());
        }

    }

    public static void readJson(KieSession kSession, JsonReader reader, Map<String, Object> map, JsonType type) {

        try {
            JsonToken token = reader.peek();
            if(token == JsonToken.NAME) {
                String name = reader.nextName();
                List list = (List) map.get(name);
                if(list != null) {
                    switch (type) {
                        case INSERT:
                            tryToInstantiateEverything(kSession, name, list, true);
                            tryToInstantiateEverything(kSession, name, list, false);
                            break;
                        case UPDATE:
                            tryToUpdateEverything(kSession, name, list);
                            break;
                        case DELETE:
                            tryToDeleteEverything(kSession, name, list);
                            break;
                    }

                } else {
                    reader.skipValue();
                }
            } else if(token == JsonToken.BEGIN_ARRAY) {
                reader.beginArray();
                while (reader.hasNext()) {
                    readJson(kSession, reader, map, type);
                }
                reader.endArray();
            } else if(token == JsonToken.BEGIN_OBJECT) {
                reader.beginObject();
                while (reader.hasNext()) {
                    readJson(kSession, reader, map, type);
                }
                reader.endObject();
            }
        } catch (IOException e) {
            System.out.println("Could not read the JsonReader.");
        }
    }

    public static FactType getClassByName(KieSession kSession, String classname) {
        FactType type = kSession.getKieBase().getFactType(packagePath, classname);
        return type;
    }

    public static Object tryToInstantiateClass(FactType type) {
        Object objClass = null;
        try {
            objClass = type.newInstance();
        } catch (InstantiationException e) {
            System.out.println("Could not instantiate the class " + objClass.getClass());
        } catch (IllegalAccessException e) {
            System.out.println("The Type passed to the function is null");
        }

        return objClass;
    }

    public static void setId(Object objClass, int id) {
        try {
            Field field = objClass.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(objClass, id);
            field.setAccessible(false);
        } catch (NoSuchFieldException e) {
            System.out.println("There is no id attribute.");
        } catch (IllegalAccessException e) {
            System.out.println("Could not set the id.");
        }
    }

    public static void setFields(Map<String, Object> objAux, Object objClass) {
        for (String str : objAux.keySet()) {
            Field[] fields = objClass.getClass().getDeclaredFields();

            for (Field f : fields) {
                if (f.getName().equals(str)) {
                    f.setAccessible(true);
                    try {
                        Object attribute = objAux.get(str);
                        if(f.getType().getName().contains("Integer")) {
                            f.set(objClass, ((Double)attribute).intValue());
                        } else {
                            // TODO LIST
                            f.set(objClass, attribute);
                        }
                    } catch (IllegalAccessException e) {
                        System.out.println("Could not set the attribute " + str);
                    }

                    f.setAccessible(false);
                }
            }
        }
    }

    private static void tryToInstantiateEverything(KieSession kSession, String classname, List list, boolean chooser) {
        ServerContext context = ServerContext.getInstance();
        for (Object obj: list) {
            FactType type = getClassByName(kSession, classname);
            Object objClass = tryToInstantiateClass(type);

            int id = 0;
            Map<String, Object> objAux = (Map<String, Object>) obj;

            id = ((Double)objAux.get("id")).intValue();

            if(chooser) {
                setId(objClass, id);
            } else {
                setFields(objAux, objClass);
            }

            context.put(objClass.getClass().getName(), id, kSession.insert(objClass));
        }
        kSession.fireAllRules();
    }

    private static void tryToUpdateEverything(KieSession kSession, String classname, List list) {
        ServerContext context = ServerContext.getInstance();
        for (Object obj: list) {
            int id = 0;
            FactType type = getClassByName(kSession, classname);

            Map<String, Object> objAux = (Map<String, Object>) obj;
            id = ((Double)objAux.get("id")).intValue();
            FactHandle fact = context.get(type.getName(), id);
            Object objClass = kSession.getObject(fact);

            setFields(objAux, objClass);

            kSession.update(fact, objClass);
        }
        kSession.fireAllRules();
    }

    private static void tryToDeleteEverything(KieSession kSession, String classname, List list) {
        ServerContext context = ServerContext.getInstance();
        for (Object obj: list) {
            int id = 0;
            FactType type = getClassByName(kSession, classname);

            Map<String, Object> objAux = (Map<String, Object>) obj;
            id = ((Double)objAux.get("id")).intValue();
            FactHandle fact = context.get(type.getName(), id);

            kSession.delete(fact);
        }
        kSession.fireAllRules();
    }

    public static final void main(String[] args) {
        // load up the knowledge base
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kSession = kContainer.newKieSession("br.ufes.inf.lprm.scene.test.session");

        File file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/insert.json");
        compileJson(kSession, file, JsonType.INSERT);
        kSession.fireAllRules();

        file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/update.json");
        compileJson(kSession, file, JsonType.UPDATE);
        kSession.fireAllRules();

        file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/delete.json");
        compileJson(kSession, file, JsonType.DELETE);
        kSession.fireAllRules();
    }

}
