package br.ufes.inf.lprm.situation.model;

import br.ufes.inf.lprm.situation.model.bindings.Part;

public interface Participation {
    public Object getActor();
    public Part getPart();
    public Situation getSituation();
}
