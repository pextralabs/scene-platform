package br.ufes.inf.lprm.scene.base;


import br.ufes.inf.lprm.scene.model.impl.PartImpl;
import br.ufes.inf.lprm.scene.model.impl.Situation;
import br.ufes.inf.lprm.situation.model.SituationType;
import br.ufes.inf.lprm.scene.model.impl.SituationTypeImpl;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieSession;
import br.ufes.inf.lprm.situation.model.Part;
import br.ufes.inf.lprm.situation.annotations.part;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SceneApplication {

    private String name;
    private KieBase kbase;
    private KieSession ksession;
    private List<SituationTypeImpl> situationTypeImpls;
    private Map<String, SituationTypeImpl> mappedSituationTypes;

    public SceneApplication(String name, KieBase kbase) {
        this.name = name;
        this.kbase = kbase;
    }

    public SceneApplication(String name, KieSession ksession) {
        this.name = name;
        this.kbase = ksession.getKieBase();
        this.ksession = ksession;
        injectSceneMetamodel();
    }

    private List<SituationType> findSituationTypes(KieBase kbase) {
        List<SituationType> situationTypes = new ArrayList<SituationType>();
        for (KiePackage kpackage: kbase.getKiePackages()) {
            for (FactType ftype: kpackage.getFactTypes()) {
                Class clazz = ftype.getFactClass();
                if (Situation.class.isAssignableFrom(clazz) && clazz != Situation.class) {
                    situationTypes.add(new SituationTypeImpl(clazz, findPartsFromClass(clazz)));
                }
            }
        }
        return situationTypes;
    }

    private void injectSceneMetamodel() {
        List<SituationType> types = findSituationTypes(kbase);
        for (SituationType type: types) {
            for(Part part: type.getParts()) {
                ksession.insert(part);
            }
            ksession.insert(type);
        }
    }

    private KieSession newSession() {
        ksession = kbase.newKieSession();
        injectSceneMetamodel();
        return ksession;
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
}
