package br.ufes.inf.lprm.scene;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.kie.api.KieServices;
import org.kie.api.definition.type.FactField;
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
public class DataInsert {

    public static void compileDataJson(KieSession kSession, File file, JsonType type, String packagePath) {
        try {
            Gson gson = new Gson();
            Type jsonType = new TypeToken<Map<String, Object>>(){}.getType();

            Map<String, Object> myMap = gson.fromJson(new FileReader(file), jsonType);

            JsonReader jsonreader = new JsonReader(new FileReader(file));
            readDataJson(kSession, jsonreader, myMap, type, false, packagePath);
            if (type == JsonType.INSERT) {
                jsonreader = new JsonReader(new FileReader(file));
                readDataJson(kSession, jsonreader, myMap, type, true, packagePath);
            }
        } catch (IOException e) {
            System.out.println("Could not open " + file.getName());
        }

    }

    public static void readDataJson(KieSession kSession, JsonReader reader, Map<String, Object> map, JsonType type, boolean isInsertOrUpdate, String packagePath) {

        try {
            JsonToken token = reader.peek();
            if(token == JsonToken.NAME) {
                String name = reader.nextName();
                List list = (List) map.get(name);
                if(list != null) {
                    switch (type) {
                        case INSERT:
                            tryToInstantiateEveryAvailableData(kSession, name, list, packagePath, isInsertOrUpdate);
                            break;
                        case UPDATE:
                            tryToUpdateEveryAvailableData(kSession, name, list, packagePath);
                            break;
                        case DELETE:
                            tryToDeleteEveryAvailableData(kSession, name, list, packagePath);
                            break;
                    }

                } else {
                    reader.skipValue();
                }
            } else if(token == JsonToken.BEGIN_ARRAY) {
                reader.beginArray();
                while (reader.hasNext()) {
                    readDataJson(kSession, reader, map, type, isInsertOrUpdate, packagePath);
                }
                reader.endArray();
            } else if(token == JsonToken.BEGIN_OBJECT) {
                reader.beginObject();
                while (reader.hasNext()) {
                    readDataJson(kSession, reader, map, type, isInsertOrUpdate, packagePath);
                }
                reader.endObject();
            }
        } catch (IOException e) {
            System.out.println("Could not read the JsonReader.");
        }
    }

    public static FactType getClassByName(KieSession kSession, String classname, String packagePath) {
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

    public static void setFieldsNull(Object objClass) {
        Field[] fields = objClass.getClass().getDeclaredFields();

        for (Field f : fields) {
            f.setAccessible(true);
            try {
                f.set(objClass, null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            f.setAccessible(false);
        }
    }

    public static void setFields(Map<String, Object> objAux, Object objClass, FactType type, boolean chooser) {
        ServerContext context = ServerContext.getInstance();
        for (String str : objAux.keySet()) {
            Field[] fields = objClass.getClass().getDeclaredFields();

            for (Field f : fields) {
                if (f.getName().equals(str)) {
                    f.setAccessible(true);
                    try {
                        Object attribute = objAux.get(str);
                        if(f.getType().getName().contains("Integer")) {
                            f.set(objClass, ((Double)attribute).intValue());
                        } else if(f.getType().getName().contains("Float")) {
                            f.set(objClass, ((Double)attribute).floatValue());
                        } else if(f.getType().getName().contains("Short")) {
                            f.set(objClass, ((Double)attribute).shortValue());
                        } else if(f.getType().getName().contains("Long")) {
                            f.set(objClass, ((Double)attribute).longValue());
                        } else {
                            if(chooser) {
                                Map<String, Object> metadata = null;
                                for (FactField fieldType: type.getFields()) {
                                    if(fieldType.getName().equals(f.getName()))
                                        metadata = fieldType.getMetaData();
                                }

                                if(metadata != null) {
                                    List fieldData = (List) attribute;
                                    for(int i = 0; i < fieldData.size(); i++) {
                                        int id = ((Double)fieldData.get(i)).intValue();
                                        Object obj = context.getObj(metadata.get("type").toString(), id);
                                        fieldData.set(i, obj);
                                    }
                                    f.set(objClass, fieldData);
                                } else {
                                    f.set(objClass, attribute);
                                }
                            } else {
                                f.set(objClass, attribute);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        System.out.println("Could not set the attribute " + str);
                    }

                    f.setAccessible(false);
                }
            }
        }
    }

    private static void tryToInstantiateEveryAvailableData(KieSession kSession, String classname, List list, String packagePath, boolean chooser) {
        ServerContext context = ServerContext.getInstance();
        for (Object obj: list) {
            FactType type = getClassByName(kSession, classname, packagePath);
            Object objClass = tryToInstantiateClass(type);

            int id = 0;
            Map<String, Object> objAux = (Map<String, Object>) obj;

            id = ((Double)objAux.get("id")).intValue();


            setFields(objAux, objClass, type, chooser);

            context.putFact(objClass.getClass().getName(), id, kSession.insert(objClass));
            context.putObj(objClass.getClass().getSimpleName(), id, objClass);
        }
        kSession.fireAllRules();
    }

    private static void tryToUpdateEveryAvailableData(KieSession kSession, String classname, List list, String packagePath) {
        ServerContext context = ServerContext.getInstance();
        for (Object obj: list) {
            int id = 0;
            FactType type = getClassByName(kSession, classname, packagePath);

            Map<String, Object> objAux = (Map<String, Object>) obj;
            id = ((Double)objAux.get("id")).intValue();
            FactHandle fact = context.getFact(type.getName(), id);
            Object objClass = kSession.getObject(fact);

            setFieldsNull(objClass);
            setFields(objAux, objClass, type, true);

            kSession.update(fact, objClass);
        }
        kSession.fireAllRules();
    }

    private static void tryToDeleteEveryAvailableData(KieSession kSession, String classname, List list, String packagePath) {
        ServerContext context = ServerContext.getInstance();
        for (Object obj: list) {
            int id = 0;
            FactType type = getClassByName(kSession, classname, packagePath);

            Map<String, Object> objAux = (Map<String, Object>) obj;
            id = ((Double)objAux.get("id")).intValue();
            FactHandle fact = context.getFact(type.getName(), id);

            kSession.delete(fact);
            context.removeFact(type.getName(), fact);
        }
        kSession.fireAllRules();
    }
}
