package plugins;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.SceneManager;
import br.ufes.inf.lprm.scene.exceptions.NotCompatibleException;
import br.ufes.inf.lprm.scene.exceptions.NotInstantiatedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Environment;
import play.inject.ApplicationLifecycle;
import play.libs.F;
import play.libs.Json;
import play.mvc.Http;
import util.JsonResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class Scene {

    private final SceneManager manager;

    @Inject
    public Scene(ApplicationLifecycle lifecycle, Environment environment) {
        this.manager = SceneManager.getInstance();

        lifecycle.addStopHook(() -> {
            return F.Promise.pure(null);
        });
    }

    public ObjectNode newApp(String content) {
        SceneApplication newApp = new SceneApplication();
        ObjectNode answer = Json.newObject();
        try {
            newApp.insertCode(content);
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

}
