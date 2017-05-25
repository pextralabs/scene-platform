package br.ufes.inf.lprm.scene.model;

import br.ufes.inf.lprm.situation.model.Situation;

public class Participation implements br.ufes.inf.lprm.situation.model.Participation {

    private Object actor;
    private br.ufes.inf.lprm.situation.model.bindings.Part part;
    private Situation situation;

    public Participation(Situation situation, br.ufes.inf.lprm.situation.model.bindings.Part part, Object actor)  {

        this.situation = situation;
        this.part = part;
        this.actor = actor;

        try {
            ((Part) part).set(situation, actor);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getActor() {
        return actor;
    }

    @Override
    public br.ufes.inf.lprm.situation.model.bindings.Part getPart() {
        return part;
    }

    @Override
    public Situation getSituation() {
        return situation;
    }
}
