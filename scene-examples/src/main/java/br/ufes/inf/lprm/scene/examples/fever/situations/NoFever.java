package br.ufes.inf.lprm.scene.examples.fever.situations;

import org.kie.api.definition.type.Key;
import br.ufes.inf.lprm.scene.examples.fever.entities.Person;
import br.ufes.inf.lprm.scene.model.Situation;
import br.ufes.inf.lprm.situation.bindings.part;

public class NoFever extends Situation {

    @Key
    @part(label = "f1")
    private Person nonFebrile;

    public Person getNonFebrile() {
        return nonFebrile;
    }

    public NoFever setNonFebrile(Person nonFebrile) {
        this.nonFebrile = nonFebrile;
        return this;
    }

}
