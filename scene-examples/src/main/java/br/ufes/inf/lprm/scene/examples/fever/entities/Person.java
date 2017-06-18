package br.ufes.inf.lprm.scene.examples.fever.entities;

public class Person {

    private int id;
    private String name;
    private Temperature temperature;

    public int getId() {
        return id;
    }

    public Person setId(int id) {
        this.id = id;
        temperature = new Temperature();
        return this;
    }

    public String getName() {
        return name;
    }

    public Person setName(String name) {
        this.name = name;
        return this;
    }

    public Temperature getTemperature() {
        return temperature;
    }

}
