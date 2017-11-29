package br.ufes.inf.lprm.scene.test.equality.model;

public class Person {
    private String name;
    private Double temperature;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Person setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public String toString() {
        return name + ": temperature(" + temperature + ")" ;
    }
}
