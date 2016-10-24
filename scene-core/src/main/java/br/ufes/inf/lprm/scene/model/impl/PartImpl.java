package br.ufes.inf.lprm.scene.model.impl;

import  br.ufes.inf.lprm.situation.model.Part;
import  br.ufes.inf.lprm.situation.model.Situation;
import  java.lang.reflect.Field;

public class PartImpl implements Part {

    private String label;
    private Field field;
    public PartImpl(String label, Field field) {
        this.label = label;
        this.field = field;
    }

    @Override
    public String getLabel() {
        return label;
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
