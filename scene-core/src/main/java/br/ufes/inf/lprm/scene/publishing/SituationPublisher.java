package br.ufes.inf.lprm.scene.publishing;

import br.ufes.inf.lprm.situation.SituationType;

public abstract class SituationPublisher {

	public SituationPublisher(Class<?> type, String host, int port, long delay, long attempts, long timeout) {

	}
	
	public 		abstract void publishActivation(SituationType sit);
	public 		abstract void publishDeactivation(SituationType sit);

}
