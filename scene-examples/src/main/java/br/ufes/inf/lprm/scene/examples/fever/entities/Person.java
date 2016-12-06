package br.ufes.inf.lprm.scene.examples.fever.entities;

import br.ufes.inf.lprm.scene.model.impl.Actor;
import br.ufes.inf.lprm.situation.annotations.id;

public class Person extends Actor {

    @id
    private long identifier;
    private String name;
    private int temperature;

    public long getIdentifier() {
        return identifier;
    }

    public Person setIdentifier(int id) {
        this.identifier = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public int getTemperature() {
        return temperature;
    }

    public Person setTemperature(int temperature) {
        this.temperature = temperature;
        return this;
    }
}
