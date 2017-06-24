package br.ufes.inf.lprm.scene.test.model;

public class Person {

    private long id;
    private String name;
    private int heartrate;

    public Person(long id, String name) {
        this.id = id;
        this.name = name;
        this.heartrate = 80;
    }

    public long getId() {
        return id;
    }

    public Person setId(long id) {
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

    public int getHeartrate() {
        return heartrate;
    }

    public Person setHeartrate(int heartrate) {
        this.heartrate = heartrate;
        return this;
    }
}
