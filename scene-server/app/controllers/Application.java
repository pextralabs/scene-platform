package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.Controller;
import play.mvc.Result;
import plugins.Scene;

import javax.inject.Inject;

public class Application extends Controller {

    @Inject
    Scene scene;

    public Result index() {
        return ok();
    }

    public Result processResult(JsonNode answer) {
        if(answer.has("error")) {
            return badRequest(answer);
        }
        return ok(answer);
    }

    public Result appInsert() {

        JsonNode node = request().body().asJson();

        if(!node.has("application")) return badRequest("There is no application field.");
        if(!node.has("files"))
            return badRequest("There is no files field.");
        else {
            JsonNode files = node.get("files");
            if(files.size() == 0)
                return badRequest("There is no described files inside files field.");
            else
                for(int i = 0; i < files.size(); i++) {
                    if(!files.get(i).has("name")) return badRequest("File number " + i + " has no name described.");
                    if(!files.get(i).has("package")) return badRequest("File number " + i + " has no package described.");
                    if(!files.get(i).has("content")) return badRequest("File number " + i + " has no content described.");
                }
        }

        JsonNode answer = scene.newApp(node.toString());
        return processResult(answer);
    }

    public Result getApps() {
        JsonNode answer = scene.getApps();
        return processResult(answer);
    }

    public Result compileData(Integer appId) {
        JsonNode answer = scene.compileData(appId, request().body());
        return processResult(answer);
    }

    public Result appStatusSituations(Integer appId) {
        JsonNode answer = scene.appStatusSituations(appId);
        return processResult(answer);
    }

    public Result appModel(Integer appId) {
        JsonNode answer = scene.appModel(appId);
        return processResult(answer);
    }

    public Result appDump(Integer appId) {
        JsonNode answer = scene.appDump(appId);
        return processResult(answer);
    }

}
