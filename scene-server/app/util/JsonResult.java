package util;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.model.impl.PartImpl;
import br.ufes.inf.lprm.scene.model.impl.ParticipationImpl;
import br.ufes.inf.lprm.scene.model.impl.Situation;
import br.ufes.inf.lprm.scene.model.impl.SituationTypeImpl;
import br.ufes.inf.lprm.scene.util.OnGoingSituation;
import br.ufes.inf.lprm.situation.model.Part;
import br.ufes.inf.lprm.situation.model.Participation;
import br.ufes.inf.lprm.situation.model.events.Activation;
import br.ufes.inf.lprm.situation.model.events.Deactivation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import play.libs.Json;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hborjaille on 10/26/16.
 */
public class JsonResult {

    public static ObjectNode appStatusSituations(SceneApplication app) {
        KieSession ksession = app.getKsession();
        ObjectNode answer = Json.newObject();
        ArrayNode sitArray = answer.putArray("situations");
        for (FactHandle fact: ksession.getFactHandles()) {
            Object obj = ksession.getObject(fact);
            if(obj instanceof Situation) {
                Situation sit = (Situation) obj;

                ObjectNode sitNode = Json.newObject();
                sitNode.put("name", sit.getClass().getName());
                boolean isActive = sit.isActive();
                sitNode.put("active", isActive);
                sitNode.put("activation", sit.getActivation().getTimestamp());
                if(!isActive) {
                    sitNode.put("deactivation", sit.getDeactivation().getTimestamp());
                }
                ArrayNode participationsArray = sitNode.putArray("participations");

                for (Participation p: sit.getParticipations()) {
                    ObjectNode participation = Json.newObject();
                    participation.put("kind", p.getActor().getClass().getName());
                    participation.put("label", p.getPart().getLabel());
                    HashMap<String, JsonNode> map = new HashMap<>();
                    Iterator<Map.Entry<String, JsonNode>> iter = Json.toJson(p.getActor()).fields();
                    while(iter.hasNext()) {
                        Map.Entry<String, JsonNode> entry = iter.next();
                        map.put(entry.getKey(), entry.getValue());
                    }
                    participation.setAll(map);
                    participationsArray.add(participation);
                }
                sitArray.add(sitNode);
            }
        }
        return answer;
    }

    public static ObjectNode appReturnModel(SceneApplication app) {
        KieSession ksession = app.getKsession();
        ObjectNode answer = Json.newObject();
        ArrayNode modelArray = answer.putArray("model");

        for (FactHandle fact: ksession.getFactHandles()) {
            Object obj = ksession.getObject(fact);
            if(obj instanceof SituationTypeImpl) {
                SituationTypeImpl objType = (SituationTypeImpl) obj;

                ObjectNode partial = Json.newObject();
                partial.put("name", objType.getName());

                ArrayNode array = partial.putArray("parts");

                for (Part p : objType.getParts()) {
                    ObjectNode partsNode = Json.newObject();
                    partsNode.put("label", p.getLabel());

                    PartImpl pi = ((PartImpl) p);
                    partsNode.put("kind", pi.getField().getType().getSimpleName());

                    ArrayNode fieldArray = partsNode.putArray("fields");

                    for (Field f: pi.getField().getType().getDeclaredFields()) {
                        ObjectNode fieldAux = Json.newObject();
                        fieldAux.put("name", f.getName());
                        fieldAux.put("type", f.getType().getSimpleName());
                        fieldArray.add(fieldAux);
                    }
                    array.add(partsNode);
                }
                modelArray.add(partial);
            }
        }
        return answer;
    }

    public static ObjectNode appDumpEveryObject(SceneApplication app) {
        KieSession ksession = app.getKsession();
        ObjectNode answer = Json.newObject();
        ArrayNode situations = answer.putArray("situations");

        for (JsonNode sit: appStatusSituations(app).get("situations")) {
            situations.add(sit);
        }

        ArrayNode objects = answer.putArray("objects");

        for (FactHandle fact: ksession.getFactHandles()) {
            Object obj = ksession.getObject(fact);
            if(!(obj instanceof SituationTypeImpl) && !(obj instanceof Situation)
                    && !(obj instanceof PartImpl) && !(obj instanceof OnGoingSituation)
                    && !(obj instanceof Activation) && !(obj instanceof Deactivation)
                    && !(obj instanceof ParticipationImpl)) {

                ObjectNode object = Json.newObject();
                object.put("kind", obj.getClass().getName());
                HashMap<String, JsonNode> map = new HashMap<>();
                Iterator<Map.Entry<String, JsonNode>> iter = Json.toJson(obj).fields();
                while(iter.hasNext()) {
                    Map.Entry<String, JsonNode> entry = iter.next();
                    map.put(entry.getKey(), entry.getValue());
                }
                object.setAll(map);
                objects.add(object);
            }
        }
        return answer;
    }
}
