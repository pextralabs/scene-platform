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
import play.mvc.Result;
import util.JsonResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

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

    public Result newApp(String content) {
        SceneApplication newApp = new SceneApplication();
        try {
            newApp.insertCode(content);
        } catch (NotCompatibleException e) {
            return badRequest(e.getMessage());
        }
        int hashCode = manager.putApp(newApp, newApp.hashCode());
        ObjectNode answer = Json.newObject();
        answer.put("app", newApp.getName());
        answer.put("id", hashCode);
        return ok(answer);
    }

    public Result getApps() {
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
        return ok(answer);
    }

    public Result compileData(int key, Http.RequestBody body) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();

        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return badRequest(answer);
        }

        JsonNode node = body.asJson();
        if(!node.has("type")) {
            answer.put("error", "There is no type field.");
            answer.put("hint", "insert, update or delete.");
            return badRequest();
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
                    return badRequest(answer);
            }
        } catch (NotInstantiatedException e) {
            answer.put("error", e.getMessage());
            return badRequest(answer);
        }

        answer.put("success", node.get("type").textValue() + " with success!");
        return ok(answer);
    }

    public Result appStatusSituations(int key) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();
        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return badRequest(answer);
        }

        return ok(JsonResult.appStatusSituations(app));
    }

    public Result appModel(int key) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();
        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return badRequest(answer);
        }

        return ok(JsonResult.appReturnModel(app));
    }

    public Result appDump(int key) {
        SceneApplication app = manager.getApp(key);

        ObjectNode answer = Json.newObject();
        if(app == null) {
            answer.put("error", "There is no application with key " + key);
            return badRequest(answer);
        }

        return ok(JsonResult.appDumpEveryObject(app));
    }

}
