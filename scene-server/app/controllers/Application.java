package controllers;

import scene.Message;
import play.mvc.*;

import plugins.Drools;
import scene.fever.Person;

import javax.inject.Inject;

public class Application extends Controller {

    @Inject
    Drools drools;

    public Result index() {
        Person p = new Person("Isaac", 1);
        p.setTemperature(39);
        drools.kieSession.insert(p);
        drools.kieSession.fireAllRules();
        return ok("rules are running... check the console.");
    }

}
