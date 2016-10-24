package br.ufes.inf.lprm.situation.model;

import br.ufes.inf.lprm.situation.model.events.Activation;
import br.ufes.inf.lprm.situation.model.events.Deactivation;

import java.util.List;

public interface Situation {

    public long getUID();
    public Activation getActivation();
    public Deactivation getDeactivation();
    public void setDeactivation(Deactivation deactivation);
    public boolean isActive();
    public List<Participation> getParticipations();
    public SituationType getType();
}
