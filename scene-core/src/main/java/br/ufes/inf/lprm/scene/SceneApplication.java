package br.ufes.inf.lprm.scene;


import br.ufes.inf.lprm.scene.base.listeners.DeactivationListener;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import br.ufes.inf.lprm.scene.exceptions.NotCompatibleException;
import br.ufes.inf.lprm.scene.exceptions.NotInstantiatedException;
import br.ufes.inf.lprm.scene.model.Part;
import br.ufes.inf.lprm.scene.model.Snapshot;
import br.ufes.inf.lprm.scene.model.Situation;
import br.ufes.inf.lprm.scene.model.SituationType;
import br.ufes.inf.lprm.scene.serialization.JsonContext;
import br.ufes.inf.lprm.scene.serialization.JsonType;
import br.ufes.inf.lprm.situation.bindings.part;
import br.ufes.inf.lprm.situation.bindings.snapshot;
import br.ufes.inf.lprm.situation.model.bindings.SnapshotPolicy;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.type.Annotation;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieSession;
import org.reflections.Reflections;
import org.kie.api.definition.type.Key;

import java.lang.reflect.Field;
import java.util.*;



public class SceneApplication {

    private static final String __LSRECE_GET_TIMESTAMP_FROM_TUPLE =
            "{" +
                    "	org.drools.core.common.InternalFactHandle fh = $1.get( this.declaration );\n" +
                    "	if (fh instanceof org.drools.core.common.EventFactHandle) {\n" +
                    "		return ((org.drools.core.common.EventFactHandle) fh).getStartTimestamp();\n" +
                    "	} else {\n" +
                    "		Object obj = fh.getObject();\n" +
                    "   	if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
                    "			br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;  \n" +
                    "			return sit.getActivation().getTimestamp();\n" +
                    "		} else {\n" +
                    "			return Long.MAX_VALUE;\n" +
                    "		}\n" +
                    "	}\n" +
                    "}";
    private static final String __LSRECE_GET_TIMESTAMP_FROM_FACT_HANDLE =
            "{" +
                    "	org.drools.core.common.InternalFactHandle fh = $1;\n" +
                    "	if (fh instanceof org.drools.core.common.EventFactHandle) {\n" +
                    "		return ((org.drools.core.common.EventFactHandle) fh).getEndTimestamp();\n" +
                    "	} else {\n" +
                    "		Object obj = fh.getObject();\n" +
                    "   	if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
                    "			br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;  \n" +
                    "			return (!sit.isActive()) ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;\n" +
                    "		} else {\n" +
                    "			return Long.MAX_VALUE;\n" +
                    "		}\n" +
                    "	}\n" +
                    "}";

    private static final String __LERSCE_GET_TIMESTAMP_FROM_TUPLE =
            "{" +
                    "	org.drools.core.common.InternalFactHandle fh = $1.get( this.declaration );\n" +
                    "	if (fh instanceof org.drools.core.common.EventFactHandle) {\n" +
                    "		return ((org.drools.core.common.EventFactHandle) fh).getEndTimestamp();\n" +
                    "	} else {\n" +
                    "		Object obj = fh.getObject();\n" +
                    "   	if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
                    "			br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;  \n" +
                    "			return (!sit.isActive()) ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;" +
                    "		} else {\n" +
                    "			return Long.MAX_VALUE;\n" +
                    "		}\n" +
                    "	}\n" +
                    "}";
    private static final String __LERSCE_GET_TIMESTAMP_FROM_FACT_HANDLE =
            "{\n" +
            "	org.drools.core.common.InternalFactHandle fh = $1;\n" +
            "	if (fh instanceof org.drools.core.common.EventFactHandle) {\n" +
            "		return ((org.drools.core.common.EventFactHandle) fh).getStartTimestamp();\n" +
            "	} else {\n" +
            "		Object obj = fh.getObject();\n" +
            "   	if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
            "			br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;  \n" +
            "			return sit.getActivation().getTimestamp();\n" +
            "		} else {\n" +
            "			return Long.MAX_VALUE;\n" +
            "		}\n" +
            "	}\n" +
            "}";


    private static final String __TVCE_UPDATE_FROM_TUPLE =
                    "{\n" +
                    " this.workingMemory = $1;\n" +
                    " this.tuple = $2;\n" +
                    " if (this.declaration.getExtractor().isSelfReference()) {\n" +
                    "  Object obj = $2.getObject(this.declaration);\n" +
                    "  if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
                    "   br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;\n" +
                    "   this.startTS = sit.getActivation().getTimestamp();\n" +
                    "   this.endTS = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;\n" +
                    "  } else {\n" +
                    "   org.drools.core.common.EventFactHandle efh = ((org.drools.core.common.EventFactHandle) $2.get(this.declaration));\n" +
                    "   this.startTS = efh.getStartTimestamp();\n" +
                    "   this.endTS = efh.getEndTimestamp();\n" +
                    "  }\n" +
                    " } else {\n" +
                    "  this.leftNull = this.declaration.getExtractor().isNullValue($1, $2.getObject(this.declaration));\n" +
                    "  if (!leftNull) { // avoid a NullPointerException\n" +
                    "   this.startTS = this.declaration.getExtractor().getLongValue($1, $2.getObject(this.declaration));\n" +
                    "  } else {\n" +
                    "   this.startTS = 0;\n" +
                    "  }\n" +
                    "  endTS = startTS;\n" +
                    " }\n" +
                    "}";

    private static final String __TVCE_UPDATE_FROM_FACT_HANDLE =
                    "{\n" +
                    " this.workingMemory = $1;\n" +
                    " this.object = $2.getObject();\n" +
                    " if (this.extractor.isSelfReference()) {\n" +
                    "  if (this.object instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
                    "   br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) this.object;\n" +
                    "   this.startTS = sit.getActivation().getTimestamp();\n" +
                    "   this.endTS = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;\n" +
                    "  } else {\n" +
                    "   this.startTS = ((org.drools.core.common.EventFactHandle) $2).getStartTimestamp();\n" +
                    "   this.endTS = ((org.drools.core.common.EventFactHandle) $2).getEndTimestamp();\n" +
                    "  }\n" +
                    " } else {\n" +
                    "  this.rightNull = this.extractor.isNullValue($1, $2.getObject());\n" +
                    "  if (!rightNull) { // avoid a NullPointerException\n" +
                    "   this.startTS = this.extractor.getLongValue($1, $2.getObject());\n" +
                    "  } else {\n" +
                    "   this.startTS = 0;\n" +
                    "  }\n" +
                    "  endTS = startTS;\n" +
                    " }\n" +
                    "}";

    private String name;
    private String description;
    private KieSession ksession;
    private List<br.ufes.inf.lprm.situation.model.SituationType> situationTypes;
    private Map<String, br.ufes.inf.lprm.situation.model.SituationType> mappedSituationTypes;
    private JsonContext context;

    public SceneApplication(ClassPool pool, KieSession ksession, String name) {
        situationTypes = new ArrayList<br.ufes.inf.lprm.situation.model.SituationType>();
        mappedSituationTypes = new HashMap<String, br.ufes.inf.lprm.situation.model.SituationType>();
        this.name = name;
        this.ksession = ksession;

        ksession.addEventListener( new DeactivationListener() );

        CtMethod getTimestampFromTuple, getTimestampFromFactHandle, updateFromTuple, updateFromFactHandle;

        try{
            CtClass lersce = pool.get("org.drools.core.rule.VariableRestriction$LeftEndRightStartContextEntry");

            if (!lersce.isFrozen()) {
                getTimestampFromTuple 		= lersce.getDeclaredMethod("getTimestampFromTuple");
                getTimestampFromFactHandle = lersce.getDeclaredMethod("getTimestampFromFactHandle");
                getTimestampFromTuple.setBody(__LERSCE_GET_TIMESTAMP_FROM_TUPLE);
                getTimestampFromFactHandle.setBody(__LERSCE_GET_TIMESTAMP_FROM_FACT_HANDLE);
                lersce.toClass();
            }

            CtClass lsrece = pool.get("org.drools.core.rule.VariableRestriction$LeftStartRightEndContextEntry");
            if (!lsrece.isFrozen()) {
                getTimestampFromTuple 		= lsrece.getDeclaredMethod("getTimestampFromTuple");
                getTimestampFromFactHandle = lsrece.getDeclaredMethod("getTimestampFromFactHandle");
                getTimestampFromTuple.setBody(__LSRECE_GET_TIMESTAMP_FROM_TUPLE);
                getTimestampFromFactHandle.setBody(__LSRECE_GET_TIMESTAMP_FROM_FACT_HANDLE);
                lsrece.toClass();
            }

            CtClass tvce = pool.get("org.drools.core.rule.VariableRestriction$TemporalVariableContextEntry");
            if (!tvce.isFrozen()) {
                updateFromTuple 	   = tvce.getDeclaredMethod("updateFromTuple");
                updateFromFactHandle   = tvce.getDeclaredMethod("updateFromFactHandle");
                updateFromTuple.setBody(__TVCE_UPDATE_FROM_TUPLE);
                updateFromFactHandle.setBody(__TVCE_UPDATE_FROM_FACT_HANDLE);
                tvce.toClass();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        injectSceneMetamodel();
    }

    public SceneApplication() {
        situationTypes = new ArrayList<br.ufes.inf.lprm.situation.model.SituationType>();
        mappedSituationTypes = new HashMap<String, br.ufes.inf.lprm.situation.model.SituationType>();
        this.context = new JsonContext();
    }

    private List<br.ufes.inf.lprm.situation.model.SituationType> findSituationTypes(KieBase kbase) {

        for (KiePackage kpackage: kbase.getKiePackages()) {

            for (FactType ftype: kpackage.getFactTypes()) {
                Class clazz = ftype.getFactClass();
                if (Situation.class.isAssignableFrom(clazz) && clazz != Situation.class) {

                    //findPartsFromFactType(ftype);

                    SituationType situationType = new SituationType(clazz, findPartsFromClass(clazz), findSnapshotsFromClass(clazz));
                    situationTypes.add(situationType);
                    mappedSituationTypes.put(situationType.getName(), situationType);
                }
            }

            System.out.println("PACKAGE: " + kpackage.getName());

            Reflections reflections = new Reflections(kpackage.getName());

            Set<Class<? extends Situation>> situations = reflections.getSubTypesOf(Situation.class);

            for (Class situationClass: situations ) {
                System.out.println(situationClass.getName());
                if (mappedSituationTypes.get(situationClass.getName()) == null) {
                    SituationType situationType = new SituationType(situationClass, findPartsFromClass(situationClass), findSnapshotsFromClass(situationClass));
                    situationTypes.add(situationType);
                    mappedSituationTypes.put(situationType.getName(), situationType);
                }
            }
        }
        return situationTypes;
    }

    private void injectSceneMetamodel() {
        List<br.ufes.inf.lprm.situation.model.SituationType> types = findSituationTypes(ksession.getKieBase());
        for (br.ufes.inf.lprm.situation.model.SituationType type: types) {
            for(br.ufes.inf.lprm.situation.model.bindings.Part part: type.getParts()) {
                ksession.insert(part);
            }
            ksession.insert(type);
        }
    }

    private List<br.ufes.inf.lprm.situation.model.bindings.Part> findPartsFromFactType(FactType ftype) {
        List<br.ufes.inf.lprm.situation.model.bindings.Part> parts = new ArrayList<br.ufes.inf.lprm.situation.model.bindings.Part>();

        for(FactField field: ftype.getFields()) {
            for (Annotation a: field.getFieldAnnotations()) {
                System.out.println(field.getName() + ":"  + a);
            }

            //String meta = (String) field.getFieldAnnotations()
            System.out.println(field.getName() + ":"  + field.getMetaData().get("part"));

        }



        //Class clazz = ftype.getFactClass();
        //findPartsFromClass(clazz);
        return parts;

    }

    private List<br.ufes.inf.lprm.situation.model.bindings.Snapshot> findSnapshotsFromClass(Class<?> clazz) {
        List<br.ufes.inf.lprm.situation.model.bindings.Snapshot> snapshots = new ArrayList<br.ufes.inf.lprm.situation.model.bindings.Snapshot>();
        Class superclazz = clazz.getSuperclass();
        if (superclazz != null) {
            snapshots.addAll(findSnapshotsFromClass(superclazz));
        }
        for (Field field: clazz.getDeclaredFields()) {

            snapshot p = (snapshot) field.getAnnotation(snapshot.class);
            if (p != null) {
                String label = p.label().equals("") ? field.getName() : p.label();
                snapshots.add(new Snapshot(label, field, SnapshotPolicy.Shallow ));
            }
        }
        return snapshots;
    }

    private List<br.ufes.inf.lprm.situation.model.bindings.Part> findPartsFromClass(Class<?> clazz) {
        List<br.ufes.inf.lprm.situation.model.bindings.Part> parts = new ArrayList<br.ufes.inf.lprm.situation.model.bindings.Part>();
        Class superclazz = clazz.getSuperclass();
        if (superclazz != null) {
            parts.addAll(findPartsFromClass(superclazz));
        }
        for (Field field: clazz.getDeclaredFields()) {

            part p = (part) field.getAnnotation(part.class);
            if (p != null) {
                //Key key = (Key) field.getAnnotation(Key.class);
                String label = p.label().equals("") ? field.getName() : p.label();
                parts.add(new Part(label, field, true));
            }
        }
        return parts;
    }

    public void insertCode(String content) throws NotCompatibleException {
        context.compileCodeJson(content);
        name = context.getAppname();
        description = context.getDescription();
        ksession = context.getkSession();
        ksession.addEventListener(new SCENESessionListener());
        injectSceneMetamodel();
    }

    public void insertData(String content) throws NotInstantiatedException {
        context.compileDataJson(content, JsonType.INSERT);
    }

    public void updateData(String content) throws NotInstantiatedException {
        context.compileDataJson(content, JsonType.UPDATE);
    }

    public void deleteData(String content) throws NotInstantiatedException {
        context.compileDataJson(content, JsonType.DELETE);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public KieSession getKsession() {
        return ksession;
    }
}
