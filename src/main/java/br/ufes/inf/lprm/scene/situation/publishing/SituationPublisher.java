package br.ufes.inf.lprm.scene.situation.publishing;

import br.ufes.inf.lprm.scene.base.SituationType;

public abstract class SituationPublisher {

	public SituationPublisher(Class<?> type, String host, int port) {

	}
	
	public 		abstract void publishActivation(SituationType sit);
	public 		abstract void publishSnapshot(SituationType sit);	
	public 		abstract void publishDeactivation(SituationType sit);

}
