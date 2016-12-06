package br.ufes.inf.lprm.scene.model.impl;

import br.ufes.inf.lprm.scene.util.SituationCast;
import br.ufes.inf.lprm.situation.model.Part;
import br.ufes.inf.lprm.situation.model.Participation;
import br.ufes.inf.lprm.situation.model.SituationType;
import br.ufes.inf.lprm.situation.model.Actor;
import br.ufes.inf.lprm.situation.model.events.Activation;
import br.ufes.inf.lprm.situation.model.events.Deactivation;

import java.util.ArrayList;
import java.util.List;

public class Situation implements br.ufes.inf.lprm.situation.model.Situation {

    private Activation activation;
    private Deactivation deactivation;
    private Boolean active;
    private SituationType type;
    private List<Participation> participations;

    public Situation(SituationType type, Activation activation, SituationCast cast, boolean active) {
        this.type = type;
        this.activation = activation;
        this.active = active;

        participations = new ArrayList<Participation>();

        for(Part part: type.getParts()) {
            Actor participant = (Actor) cast.get(part.getLabel());
            if (participant != null) {
                Participation participation = new ParticipationImpl(this, part, participant);
                participations.add(participation);
            }
        }
    }

    public Situation() {
        this.active = true;
    }

    public Situation(SituationType type, SituationCast cast, Activation activation) {
        this(type, activation, cast, true);
    }

    @Override
    public long getUID() {
        return 0;
    }

    @Override
    public Activation getActivation() {
        return activation;
    }

    @Override
    public Deactivation getDeactivation() {
        return deactivation;
    }

    public void setDeactivation(Deactivation deactivation) {
        this.deactivation = deactivation;
        this.active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public List<Participation> getParticipations() {
        return participations;
    }

    @Override
    public SituationType getType() {
        return type;
    }


}
