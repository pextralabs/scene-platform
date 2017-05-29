package br.ufes.inf.lprm.scene;


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

    private String name;
    private String description;
    private KieSession ksession;
    private List<br.ufes.inf.lprm.situation.model.SituationType> situationTypes;
    private Map<String, br.ufes.inf.lprm.situation.model.SituationType> mappedSituationTypes;
    private JsonContext context;

    public SceneApplication(String name, KieSession ksession) {
        situationTypes = new ArrayList<br.ufes.inf.lprm.situation.model.SituationType>();
        mappedSituationTypes = new HashMap<String, br.ufes.inf.lprm.situation.model.SituationType>();
        this.name = name;
        this.ksession = ksession;
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
                Key key = (Key) field.getAnnotation(Key.class);
                String label = p.label().equals("") ? field.getName() : p.label();
                parts.add(new Part(label, field, key != null ));
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
