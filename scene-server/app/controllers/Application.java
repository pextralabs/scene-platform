package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import play.mvc.*;

import plugins.Scene;
import scene.fever.Person;

import javax.inject.Inject;

public class Application extends Controller {

    @Inject
    Scene scene;

    public Result index() {
        return ok("rules are running... check the console.");
    }


    public Result run() {

        JsonNode json = request().body().asJson();

        if (!json.has("name")) return badRequest("cadê o nome fdp?");

        try {
            FactType factType = scene.kieSession.getKieBase().getFactType("scene", "Person");

            Object p1 = factType.newInstance();

            FactField name = factType.getField("name");
            FactField temperature = factType.getField("temperature");

            name.set(p1, "john");
            temperature.set(p1, 39);

            scene.kieSession.insert(p1);


        } catch (Exception e) {
            e.printStackTrace();
        }

        scene.kieSession.fireAllRules();
        return ok("rules are running... check the console.");
    }

}
