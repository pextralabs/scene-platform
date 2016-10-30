package br.ufes.inf.lprm.scene.examples.fever.situations;

import br.ufes.inf.lprm.scene.examples.fever.entities.Person;
import br.ufes.inf.lprm.scene.model.impl.Situation;
import br.ufes.inf.lprm.situation.annotations.part;

public class NoFever extends Situation {


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
