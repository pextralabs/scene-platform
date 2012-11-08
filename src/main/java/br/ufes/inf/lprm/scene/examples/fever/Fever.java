package br.ufes.inf.lprm.scene.examples.fever;

import br.ufes.inf.lprm.scene.base.*;

public class Fever extends SituationType {
	
	@Role(label = "febrile")
	private Person febrile;

	public void setFebrile(Person febrile) {
		this.febrile = febrile;
	}

	public Person getFebrile() {
		return febrile;
	}
	@Override
	public void setActive() {
		super.setActive();
		System.out.println(febrile.getName() + ": Fever activated at " + this.getActivation().getTimestamp());		
	}
	@Override
	public void setInactive() {
		super.setInactive();
		System.out.println(febrile.getName() + ": Fever deactivated at " + this.getDeactivation().getTimestamp() + ". It lasts: " + (this.getDeactivation().getTimestamp() - this.getActivation().getTimestamp()) );		
	}	

}
