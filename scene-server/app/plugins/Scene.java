package plugins;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.SceneManager;
import br.ufes.inf.lprm.scene.exceptions.NotInstantiatedException;
import com.fasterxml.jackson.databind.JsonNode;
import play.Environment;
import play.inject.ApplicationLifecycle;
import play.libs.F;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import util.JsonResult;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        newApp.insertCode(content);
        manager.putApp(newApp);
        return ok(Json.parse("{\"app\": \"" + newApp.getName() + "\",\"id\": \"" + newApp.getName().hashCode() + "\"}"));
    }

    public Result compileData(int key, Http.RequestBody body) {
        SceneApplication app = manager.getApp(key);

        if(app == null)
            return badRequest("There is no application with key " + key);

        JsonNode node = body.asJson();
        if(!node.has("type"))
            return badRequest("There is no type field.\nHint: insert, update or delete.");

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
                    return badRequest("Invalid type.\nHint: insert, update or delete.");
            }
        } catch (NotInstantiatedException e) {
            return badRequest(e.getMessage());
        }

        return ok("Data compiled with success!");
    }

    public Result appStatusSituations(int key) {
        SceneApplication app = manager.getApp(key);

        if(app == null)
            return badRequest("There is no application with key " + key);

        return ok(JsonResult.appStatusSituations(app));
    }

    public Result appModel(int key) {
        SceneApplication app = manager.getApp(key);

        if(app == null)
            return badRequest("There is no application with key " + key);

        return ok(JsonResult.appReturnModel(app));
    }

    public Result appDump(int key) {
        SceneApplication app = manager.getApp(key);

        if(app == null)
            return badRequest("There is no application with key " + key);

        return ok(JsonResult.appDumpEveryObject(app));
    }

}
