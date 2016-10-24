package br.ufes.inf.lprm.scene.model.impl;

import br.ufes.inf.lprm.situation.model.Situation;
import br.ufes.inf.lprm.situation.model.Part;
import br.ufes.inf.lprm.situation.model.Participation;

public class ParticipationImpl implements Participation {

    private Object actor;
    private Part part;
    private Situation situation;

    public ParticipationImpl(Situation situation, Part part, Object actor)  {

        this.situation = situation;
        this.part = part;
        this.actor = actor;

        try {
            ((PartImpl) part).set(situation, actor);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getActor() {
        return actor;
    }

    @Override
    public Part getPart() {
        return part;
    }

    @Override
    public Situation getSituation() {
        return situation;
    }
}
