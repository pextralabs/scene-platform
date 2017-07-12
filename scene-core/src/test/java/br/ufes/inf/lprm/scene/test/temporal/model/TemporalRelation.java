package br.ufes.inf.lprm.scene.test.temporal.model;

public class TemporalRelation {

    private Type type;
    private TemporalEntity a;
    private TemporalEntity b;

    public TemporalRelation(Type type, TemporalEntity a, TemporalEntity b) {
        this.type = type;
        this.a = a;
        this.b = b;
    }

    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        else {
            if ( !(obj instanceof TemporalRelation) ) {
                return false;
            }
            else {
                TemporalRelation relation = (TemporalRelation) obj;
                return (this.type == relation.getType()) && (this.a == relation.getA()) && (this.b == relation.getB());
            }
        }

    }

    public TemporalEntity getA() {
        return a;
    }

    public TemporalEntity getB() {
        return b;
    }

    public boolean validate() {

        return true;

    }

    public Type getType() {
        return type;
    }

    public enum Type {
        AFTER, BEFORE, MEETS, MET_BY, FINISHES, FINISHED_BY, INCLUDES, DURING, STARTS, STARTED_BY, COINCIDES, OVERLAPS, OVERLAPPED_BY
    }

}

