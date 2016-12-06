package br.ufes.inf.lprm.scene.model.impl;

import br.ufes.inf.lprm.situation.model.Situation;
import br.ufes.inf.lprm.situation.model.Part;
import br.ufes.inf.lprm.situation.model.Participation;
import br.ufes.inf.lprm.situation.model.Actor;

public class ParticipationImpl implements Participation {

    private Actor actor;
    private Part part;
    private Situation situation;

    public ParticipationImpl(Situation situation, Part part, Actor actor)  {

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
    public Actor getActor() {
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
