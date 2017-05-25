package br.ufes.inf.lprm.scene.model;

import  br.ufes.inf.lprm.situation.model.Situation;
import  java.lang.reflect.Field;

public class Part implements br.ufes.inf.lprm.situation.model.bindings.Part {

    private String label;
    private Field field;
    private boolean isKey;

    public Part(String label, Field field, boolean isKey) {
        this.label = label;
        this.field = field;
        this.isKey = isKey;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public boolean isKey() {
        return isKey;
    }

    public Field getField() {
        return field;
    }

    public void set(Situation situation, Object participant) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(situation, participant);
        field.setAccessible(false);
    }

    public String toString() {
        return getLabel() + ": " + field.getType().getSimpleName();
    }

}
