package br.ufes.inf.lprm.scene.test.temporal.model;

public interface TemporalEntity {
    public String getId();
    public boolean isFinished();
    public long getStart();
    public long getEnd();
    public Type getTemporalType();

    public enum Type {
        EVENT, SITUATION
    }

}


