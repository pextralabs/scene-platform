package br.ufes.inf.lprm.scene.serialization;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.drools.core.ClockType;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.FactHandle;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by hborjaille on 10/24/16.
 */
public class JsonContext {
    private Map<String, Map<Integer, FactHandle>> factHandleMap;
    private Map<String, Map<Integer, Object>> objectMap;
    private ArrayList<String> packages;
    private KieSession kSession;
    private String appname;

    public JsonContext() {
        factHandleMap = new HashMap<>();
        objectMap = new HashMap<>();
        packages = new ArrayList<>();
    }

    public void compileCodeJson(String content) {
        Gson gson = new Gson();
        Type jsonType = new TypeToken<Map<String, Object>>(){}.getType();

        Map<String, Object> myMap = gson.fromJson(content, jsonType);

        if(myMap.get("application") == null) {
            System.out.println("Json format not compatible.");
        } else {
            readCodeJson(myMap);
        }
    }

    private String refactorAppName(String name) {
        return name.toLowerCase().replace(" ", "-").replace(".", "-");
    }

    private void readCodeJson(Map<String, Object> map) {
        // Getting KieServices
        KieServices kServices = KieServices.Factory.get();
        // Instantiating a KieFileSystem and KieModuleModel
        KieFileSystem kFileSystem = kServices.newKieFileSystem();
        KieModuleModel kieModuleModel = kServices.newKieModuleModel();

        appname = refactorAppName((String) map.get("application"));

        ReleaseId releaseId = kServices.newReleaseId("br.ufes.inf.lprm.scene", appname, (String) map.get("version"));
        kFileSystem.generateAndWritePomXML(releaseId);

        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(appname);
        kieBaseModel.addInclude("sceneKieBase");

        List<Map<String, String>> files = (List<Map<String, String>>) map.get("files");

        for (Map<String, String> f: files) {
            String name = f.get("name");
            String pakage = f.get("package");

            packages.add(pakage);
            kieBaseModel.addPackage(pakage);

            String path = "src/main/resources/" + pakage.replace(".", "/") + "/" + name;

            Resource resource = kServices.getResources().newByteArrayResource(f.get("content").getBytes()).setResourceType(ResourceType.DRL);
            kFileSystem.write(path, resource);
        }

        KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel(appname + ".session");
        kieSessionModel.setClockType(ClockTypeOption.get(ClockType.REALTIME_CLOCK.getId()))
                .setType(KieSessionModel.KieSessionType.STATEFUL);

        kFileSystem.writeKModuleXML(kieModuleModel.toXML());
        KieBuilder kbuilder = kServices.newKieBuilder(kFileSystem);
        ArrayList<Resource> dependencies = new ArrayList();
        try {
            Enumeration<URL> e = JsonContext.class.getClassLoader().getResources("META-INF/kmodule.xml");
            while ( e.hasMoreElements() ) {
                URL url = e.nextElement();
                Path path = Paths.get(url.toURI());
                dependencies.add(kServices.getResources().newFileSystemResource(path.getParent().getParent().toString()));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        kbuilder.setDependencies(dependencies.toArray(new Resource[0]));
        kbuilder.buildAll();
        if (kbuilder.getResults().hasMessages()) {
            throw new IllegalArgumentException("Coudln't build knowledge module" + kbuilder.getResults());
        }

        KieModule kModule = kbuilder.getKieModule();
        KieContainer kContainer = kServices.newKieContainer(kModule.getReleaseId());

        kSession = kContainer.newKieSession(appname + ".session");

    }

    public void compileDataJson(String content, JsonType type) {
        Gson gson = new Gson();
        Type jsonType = new TypeToken<Map<String, Object>>(){}.getType();

        Map<String, Object> myMap = gson.fromJson(content, jsonType);

        JsonReader jsonreader = new JsonReader(new StringReader(content));
        readDataJson(jsonreader, myMap, type, false);
        if (type == JsonType.INSERT) {
            jsonreader = new JsonReader(new StringReader(content));
            readDataJson(jsonreader, myMap, type, true);
        }

    }

    private void readDataJson(JsonReader reader, Map<String, Object> map, JsonType type, boolean isInsertOrUpdate) {

        try {
            JsonToken token = reader.peek();
            if(token == JsonToken.NAME) {
                String name = reader.nextName();
                if(name.equals("type")) {
                    reader.skipValue();
                } else {
                    List list = (List) map.get(name);
                    if(list != null) {
                        switch (type) {
                            case INSERT:
                                tryToInstantiateEveryAvailableData(name, list, isInsertOrUpdate);
                                break;
                            case UPDATE:
                                tryToUpdateEveryAvailableData(name, list);
                                break;
                            case DELETE:
                                tryToDeleteEveryAvailableData(name, list);
                                break;
                        }

                    } else {
                        reader.skipValue();
                    }
                }
            } else if(token == JsonToken.BEGIN_ARRAY) {
                reader.beginArray();
                while (reader.hasNext()) {
                    readDataJson(reader, map, type, isInsertOrUpdate);
                }
                reader.endArray();
            } else if(token == JsonToken.BEGIN_OBJECT) {
                reader.beginObject();
                while (reader.hasNext()) {
                    readDataJson(reader, map, type, isInsertOrUpdate);
                }
                reader.endObject();
            }
        } catch (IOException e) {
            System.out.println("Could not read the JsonReader.");
        }
    }

    private FactType getClassByName(String classname, String packagePath) {
        FactType type = kSession.getKieBase().getFactType(packagePath, classname);
        return type;
    }

    private Object tryToInstantiateClass(FactType type) {
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

    private void setFieldsNull(Object objClass) {
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

    private void setFields(Map<String, Object> objAux, Object objClass, FactType type, boolean chooser) {
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
                                    if(fieldType.getName().equals(f.getName())) {
                                        metadata = fieldType.getMetaData();
                                    }
                                }

                                if(metadata != null) {
                                    List fieldData = (List) attribute;
                                    for(int i = 0; i < fieldData.size(); i++) {
                                        int id = ((Double)fieldData.get(i)).intValue();
                                        Object obj = getObj(metadata.get("type").toString(), id);
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

    private void tryToInstantiateEveryAvailableData(String classname, List list, boolean chooser) {
        for (Object obj: list) {
            FactType type = null;
            for (String pakage : packages) {
                type = getClassByName(classname, pakage);
                if(type != null) break;
            }
            Object objClass = tryToInstantiateClass(type);

            int id = 0;
            Map<String, Object> objAux = (Map<String, Object>) obj;

            id = ((Double)objAux.get("id")).intValue();


            setFields(objAux, objClass, type, chooser);

            putFact(objClass.getClass().getName(), id, kSession.insert(objClass));
            putObj(objClass.getClass().getSimpleName(), id, objClass);
        }
        kSession.fireAllRules();
    }

    private void tryToUpdateEveryAvailableData(String classname, List list) {
        for (Object obj: list) {
            int id = 0;
            FactType type = null;
            for (String pakage : packages) {
                type = getClassByName(classname, pakage);
                if(type != null) break;
            }

            Map<String, Object> objAux = (Map<String, Object>) obj;
            id = ((Double)objAux.get("id")).intValue();
            FactHandle fact = getFact(type.getName(), id);
            Object objClass = kSession.getObject(fact);

            setFieldsNull(objClass);
            setFields(objAux, objClass, type, true);

            kSession.update(fact, objClass);
        }
        kSession.fireAllRules();
    }

    private void tryToDeleteEveryAvailableData(String classname, List list) {
        for (Object obj: list) {
            int id = 0;
            FactType type = null;
            for (String pakage : packages) {
                type = getClassByName(classname, pakage);
                if(type != null) break;
            }

            Map<String, Object> objAux = (Map<String, Object>) obj;
            id = ((Double)objAux.get("id")).intValue();
            FactHandle fact = getFact(type.getName(), id);

            kSession.delete(fact);
            // TODO removeObj(type.getName(), );
            removeFact(type.getName(), fact);
        }
        kSession.fireAllRules();
    }

    private void putObj(String clazz, int id, Object obj) {
        Map<Integer, Object> item = objectMap.get(clazz);
        if(item != null) {
            item.put(id, obj);
        } else {
            item = new HashMap<Integer, Object>();
            item.put(id, obj);
            objectMap.put(clazz, item);
        }
    }

    private Object getObj(String clazz, int id) {
        return objectMap.get(clazz).get(id);
    }

    private void removeObj(String clazz, Object obj) {
        objectMap.get(clazz).remove(obj);
    }

    private void putFact(String clazz, int id, FactHandle fact) {
        Map<Integer, FactHandle> item = factHandleMap.get(clazz);
        if(item != null) {
            item.put(id, fact);
        } else {
            item = new HashMap<Integer, FactHandle>();
            item.put(id, fact);
            factHandleMap.put(clazz, item);
        }
    }

    private FactHandle getFact(String clazz, int id) {
        return factHandleMap.get(clazz).get(id);
    }

    private void removeFact(String clazz, FactHandle fact) {
        factHandleMap.get(clazz).remove(fact);
    }

    public String getAppname() {
        return appname;
    }

    public KieSession getkSession() {
        return kSession;
    }

}
