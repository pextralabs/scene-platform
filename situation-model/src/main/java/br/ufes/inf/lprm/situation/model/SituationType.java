package br.ufes.inf.lprm.situation.model;

import br.ufes.inf.lprm.situation.model.bindings.Part;
import br.ufes.inf.lprm.situation.model.bindings.Snapshot;

import java.lang.reflect.Type;
import java.util.List;

public interface SituationType extends Type {
    public String getName();
    public List<? extends Part> getParts();
    //public List<Bind> getBinds();
    public List<? extends Snapshot> getSnapshots();

}
