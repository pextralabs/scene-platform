package br.ufes.inf.lprm.scene;


import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import br.ufes.inf.lprm.scene.model.impl.PartImpl;
import br.ufes.inf.lprm.scene.model.impl.Situation;
import br.ufes.inf.lprm.scene.model.impl.SituationTypeImpl;
import br.ufes.inf.lprm.scene.serialization.JsonContext;
import br.ufes.inf.lprm.scene.serialization.JsonType;
import br.ufes.inf.lprm.situation.annotations.part;
import br.ufes.inf.lprm.situation.model.Part;
import br.ufes.inf.lprm.situation.model.SituationType;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieSession;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;

public class SceneApplication {

    private String name;
    private KieSession ksession;
    private List<SituationType> situationTypes;
    private Map<String, SituationType> mappedSituationTypes;
    private JsonContext context;

    public SceneApplication(String name, KieSession ksession) {
        situationTypes = new ArrayList<SituationType>();
        mappedSituationTypes = new HashMap<String, SituationType>();
        this.name = name;
        this.ksession = ksession;
        injectSceneMetamodel();
    }

    public SceneApplication() {
        situationTypes = new ArrayList<SituationType>();
        mappedSituationTypes = new HashMap<String, SituationType>();
        this.context = new JsonContext();
    }

    private List<SituationType> findSituationTypes(KieBase kbase) {

        for (KiePackage kpackage: kbase.getKiePackages()) {

            //System.out.println(kpackage.toString());
            for (FactType ftype: kpackage.getFactTypes()) {
                Class clazz = ftype.getFactClass();
                if (Situation.class.isAssignableFrom(clazz) && clazz != Situation.class) {
                    SituationTypeImpl situationType = new SituationTypeImpl(clazz, findPartsFromClass(clazz));
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
                    SituationTypeImpl situationType = new SituationTypeImpl(situationClass, findPartsFromClass(situationClass));
                    situationTypes.add(situationType);
                    mappedSituationTypes.put(situationType.getName(), situationType);
                }
            }
        }
        return situationTypes;
    }

    private void injectSceneMetamodel() {
        List<SituationType> types = findSituationTypes(ksession.getKieBase());
        for (SituationType type: types) {
            for(Part part: type.getParts()) {
                ksession.insert(part);
            }
            ksession.insert(type);
        }
    }

    private List<Part> findPartsFromClass(Class clazz) {
        List<Part> parts = new ArrayList<Part>();
        Class superclazz = clazz.getSuperclass();
        if (superclazz != null) {
            parts.addAll(findPartsFromClass(superclazz));
        }
        for (Field field: clazz.getDeclaredFields()) {
            part p = (part) field.getAnnotation(part.class);
            if (p != null) {
                String label = p.label().equals("") ? field.getName() : p.label();
                parts.add(new PartImpl(label, field));
            }
        }
        return parts;
    }

    public void insertCode(String content) {
        context.compileCodeJson(content);
        name = context.getAppname();
        ksession = context.getkSession();
        ksession.addEventListener(new SCENESessionListener());
        injectSceneMetamodel();
    }

    public void insertData(String content) {
        context.compileDataJson(content, JsonType.INSERT);
    }

    public void updateData(String content) {
        context.compileDataJson(content, JsonType.UPDATE);
    }

    public void deleteData(String content) {
        context.compileDataJson(content, JsonType.DELETE);
    }
}
