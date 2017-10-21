package br.ufes.inf.lprm.situation.model;

import br.ufes.inf.lprm.situation.model.events.SituationEvent;

import java.util.List;

public interface Situation {

    public long getUID();
    public SituationEvent getActivation();
    public SituationEvent getDeactivation();
    public void setDeactivation(SituationEvent deactivation);
    public boolean isActive();
    public List<? extends Participation> getParticipations();
    public SituationType getType();
}
