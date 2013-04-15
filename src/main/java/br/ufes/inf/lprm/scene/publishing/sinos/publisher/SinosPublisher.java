package br.ufes.inf.lprm.scene.publishing.sinos.publisher;

import br.ufes.inf.lprm.situation.SituationType;

public interface SinosPublisher {

	public abstract void publishActivation (SituationType sit);
	
	public abstract void publishDeactivation (SituationType sit);
	
}
