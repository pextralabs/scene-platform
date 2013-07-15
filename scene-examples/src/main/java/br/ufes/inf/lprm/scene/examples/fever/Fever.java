package br.ufes.inf.lprm.scene.examples.fever;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import br.ufes.inf.lprm.scene.examples.shared.Person;
import br.ufes.inf.lprm.scene.publishing.Publish;
import br.ufes.inf.lprm.situation.Role;
import br.ufes.inf.lprm.situation.SituationType;

//@Publish(host="host", port=4040)
public class Fever extends SituationType {
	
	@Role(label = "f1")
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
		System.out.println(febrile.getName() + ": Fever activated at " + new SimpleDateFormat("H:mm:ss").format(  new Date( this.getActivation().getTimestamp() ) ) );		
	}
	@Override
	public void setInactive() {
		super.setInactive();
		System.out.println(febrile.getName() + ": Fever deactivated at " + new SimpleDateFormat("H:mm:ss").format(  new Date( this.getDeactivation().getTimestamp() ) )  + ". It lasted: " + TimeUnit.MILLISECONDS.toSeconds( (this.getDeactivation().getTimestamp() - this.getActivation().getTimestamp()) ) + " seconds" );		
	}	
}
