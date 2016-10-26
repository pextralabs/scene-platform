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


    public Result appinsert() {

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

        return scene.newApp(node.toString());
    }

    public Result compiledata(Integer appId) {
        return scene.compileData(appId, request().body());
    }

    public Result appstatus(Integer appId) {
        return scene.appStatus(appId);
    }

}
