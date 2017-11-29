package br.ufes.inf.lprm.scene.model;

import br.ufes.inf.lprm.scene.model.events.Activation;
import br.ufes.inf.lprm.scene.model.events.Deactivation;
import br.ufes.inf.lprm.scene.util.SituationCast;
import br.ufes.inf.lprm.situation.model.bindings.Part;
import br.ufes.inf.lprm.situation.model.SituationType;
import br.ufes.inf.lprm.situation.model.events.SituationEvent;
import org.kie.api.definition.type.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//@Entity
public class Situation implements br.ufes.inf.lprm.situation.model.Situation {
    @Key
    private String internalKey;
    private int runningId;
    private Activation activation;
    private Deactivation deactivation;
    private Boolean active;
    private SituationType type;
    private List<br.ufes.inf.lprm.situation.model.Participation> participations;

    public Situation(SituationType type, Activation activation, SituationCast cast, boolean active) {

        this.runningId = ((br.ufes.inf.lprm.scene.model.SituationType) type).getTypeClass().hashCode() + cast.hashCode();
        this.type = type;
        this.activation = activation;
        this.active = active;

        participations = new ArrayList<>();

        for(Part part: type.getParts()) {
            Object participant = cast.get(part.getLabel());
            if (participant != null) {
                br.ufes.inf.lprm.situation.model.Participation participation = new Participation(this, part, participant);
                participations.add(participation);
            }
        }
    }

    public Situation() {
        this.active = true;
        this.internalKey = UUID.randomUUID().toString();
    }

    public Situation(SituationType type, SituationCast cast, Activation activation) {
        this(type, activation, cast, true);
    }

    @Override
    public long getUID() {
        return runningId;
    }

    @Override
    public SituationEvent getActivation() {
        return activation;
    }

    @Override
    public SituationEvent getDeactivation() {
        return deactivation;
    }

    public void setDeactivation(SituationEvent deactivation) {
        this.deactivation = (Deactivation) deactivation;
        this.active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public List<br.ufes.inf.lprm.situation.model.Participation> getParticipations() {
        return participations;
    }

    @Override
    public SituationType getType() {
        return type;
    }

    public String getInternalKey() {
        return internalKey;
    }
}
