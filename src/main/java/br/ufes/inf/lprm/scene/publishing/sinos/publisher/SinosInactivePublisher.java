package br.ufes.inf.lprm.scene.publishing.sinos.publisher;

import br.ufes.inf.lprm.situation.SituationType;

public class SinosInactivePublisher implements SinosPublisher{

	@Override
	public void publishActivation(SituationType sit) {}
	
	@Override
	public void publishDeactivation(SituationType sit) {}

}
