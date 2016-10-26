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
import org.kie.api.runtime.rule.FactHandle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SceneApplication {

    private String name;
    private KieSession ksession;
    private List<SituationTypeImpl> situationTypeImpls;
    private Map<String, SituationTypeImpl> mappedSituationTypes;
    private JsonContext context;

    public SceneApplication(String name, KieSession ksession) {
        this.name = name;
        this.ksession = ksession;
        injectSceneMetamodel();
    }

    public SceneApplication() {
        this.context = new JsonContext();
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

    public String appStatus() {
        String answer = new String();
        for (FactHandle fact: ksession.getFactHandles()) {
            Object obj = ksession.getObject(fact);
            answer += obj.getClass().getName() + "\n";
            for (Field field: obj.getClass().getDeclaredFields()) {
                //TODO How to get the value from field
                answer += field.getName() + ": " + "\n" ;
            }
            answer += "\n";
        }
        return answer;
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

    public String getName() {
        return name;
    }
}
