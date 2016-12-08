package plugins;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.SceneManager;
import br.ufes.inf.lprm.scene.exceptions.NotCompatibleException;
import br.ufes.inf.lprm.scene.exceptions.NotInstantiatedException;
import br.ufes.inf.lprm.scene.model.impl.Situation;
import br.ufes.inf.lprm.scene.util.SituationHelper;
import br.ufes.inf.lprm.situation.model.Actor;
import br.ufes.inf.lprm.situation.model.SituationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import play.Environment;
import play.inject.ApplicationLifecycle;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Http;
import util.JsonResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;


@Singleton
public class Scene {

    private final SceneManager manager;

    @Inject
    WSClient ws;

    @Inject
    public Scene(ApplicationLifecycle lifecycle, Environment environment) {
        this.manager = SceneManager.getInstance();

        lifecycle.addStopHook(() -> {
            return F.Promise.pure(null);
        });
    }

    public ObjectNode newApp(String content) {
        SceneApplication newApp = new SceneApplication();

        newApp.getKsession().setGlobal("ws", this);

        ObjectNode answer = Json.newObject();
        try {
            newApp.insertCode(content, "sceneServerKieBase");
        } catch (NotCompatibleException e) {
            answer.put("error", e.getMessage());
            return answer;
        }
        int hashCode = manager.putApp(newApp, newApp.hashCode());
        answer.put("app", newApp.getName());
        answer.put("id", hashCode);
        return answer;
    }

    public ObjectNode getApps() {
        ObjectNode answer = Json.newObject();
        ArrayNode appArray = answer.putArray("apps");
        Map<Integer, SceneApplication> map = manager.getApps();
        for (Integer appId: map.keySet()) {
            ObjectNode app = Json.newObject();
            app.put("id", appId);
            app.put("name", map.get(appId).getName());
            app.put("description", map.get(appId).getDescription());
            appArray.add(app);
        }
        return answer;
    }

    public ObjectNode compileData(int key, Http.RequestBody body) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();

        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return answer;
        }

        JsonNode node = body.asJson();
        if(!node.has("type")) {
            answer.put("error", "There is no type field.");
            answer.put("hint", "insert, update or delete.");
            return answer;
        }

        try {
            switch (node.get("type").textValue()) {
                case "insert":
                    app.insertData(node.toString());
                    break;
                case "update":
                    app.updateData(node.toString());
                    break;
                case "delete":
                    app.deleteData(node.toString());
                    break;
                default:
                    answer.put("error", "Invalid type.");
                    answer.put("hint", "insert, update or delete.");
                    return answer;
            }
        } catch (NotInstantiatedException e) {
            answer.put("error", e.getMessage());
            return answer;
        }

        answer.put("success", node.get("type").textValue() + " with success!");
        return answer;
    }

    public ObjectNode appStatusSituations(int key) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();
        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return answer;
        }

        return JsonResult.appStatusSituations(app);
    }

    public ObjectNode appModel(int key) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();
        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return answer;
        }

        return JsonResult.appReturnModel(app);
    }

    public ObjectNode appDump(int key) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();
        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return answer;
        }

        return JsonResult.appDumpEveryObject(app);
    }


    public ObjectNode appSubscribe(int key, JsonNode node) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();
        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return answer;
        }

        FactType subscriptionType = app.getKsession().getKieBase().getFactType("scene", "Subscription");
        Object subscription = null;
        try {
            subscription = subscriptionType.newInstance();

            String subscriberId = UUID.randomUUID().toString();
            subscriptionType.set(subscription, "id", subscriberId);

            if (!node.has("webhook")) return answer.put("error", "A 'webhook' field is required.");
            subscriptionType.set(subscription, "id", node.get("webhook").textValue());


            if (node.has("situation")) {
                SituationType situationType =  SituationHelper.getSituationType(app.getKsession(), node.get("situation").textValue());
                if (situationType == null) return answer.put("error", "There is no situation type '" + node.get("situation").textValue() + "' in this application.");
                subscriptionType.set(subscription, "situation", situationType);
            }

            if (node.has("actor")) {

                JsonNode actorNode = node.get("actor");

                if (!actorNode.has("type")) return answer.put("error", "There is no 'type' field for actor");
                if (!actorNode.has("id"))   return answer.put("error", "There is no 'id' field for actor");

                Actor actor = (Actor) app.getContext().getObject(actorNode.get("type").textValue(), actorNode.get("id").intValue());
                if (actor == null) return answer.put("error", "There is no actor from type '"+ actorNode.get("type").textValue() + "' and id '" + actorNode.get("id").intValue() + ".");
                subscriptionType.set(subscription, "actor", actor);
            }

            app.getKsession().insert(subscription);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        answer.put("subscriberId", (String) subscriptionType.get(subscription, "id"));
        return answer;
    }

    public ObjectNode appUnsubscribe(int key, String subscriberId) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();
        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return answer;
        }

        QueryResults results = app.getKsession().getQueryResults("SubscriptionQuery", new Object[] {subscriberId} );
        FactHandle factHandle = null;
        for (QueryResultsRow row: results ) {
            factHandle = row.getFactHandle("subscription");
        }

        if (factHandle == null) return answer.put("error", "There's no subscription with id '" + subscriberId + "'.");
        app.getKsession().delete(factHandle);
        return answer.put("subscriberId", subscriberId);

    }

}
