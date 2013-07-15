package br.ufes.inf.lprm.scene.publishing.sinos.publisher;

import java.rmi.RemoteException;

import br.ufes.inf.lprm.sinos.publisher.SituationChannel;
import br.ufes.inf.lprm.situation.SituationType;

public class SinosActivePublisher implements SinosPublisher{

	private  SituationChannel channel;
	
	public SinosActivePublisher(SituationChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public void publishActivation(SituationType sit) {
		try {
			channel.publish(sit);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void publishDeactivation(SituationType sit) {
		try {
			channel.publish(sit);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}

}
