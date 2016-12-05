package br.ufes.inf.lprm.scene.examples.fever.entities;

public class Person {

    private int id;
    private String name;
    private int temperature;

    public int getId() {
        return id;
    }

    public Person setId(int id) {
        this.id = id;
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
