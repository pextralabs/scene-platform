package br.ufes.inf.lprm.scene.situation.publishing.sinos;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import br.ufes.inf.lprm.scene.base.SituationType;
import br.ufes.inf.lprm.scene.situation.publishing.SituationPublisher;
import br.ufes.inf.lprm.sinos.consumer.callback.ConsumerCallback.Operation;
import br.ufes.inf.lprm.sinos.provider.channel.EventChannel;
import br.ufes.inf.lprm.sinos.provider.exceptions.ChannelAlreadyExists;
import br.ufes.inf.lprm.sinos.provider.exceptions.ChannelNotOpen;
import br.ufes.inf.lprm.sinos.provider.exceptions.InvalidEventChannel;
import br.ufes.inf.lprm.sinos.provider.exceptions.InvalidSituationType;

public class SinosSituationPublisher extends SituationPublisher {

	EventChannel channel;
	
	public SinosSituationPublisher(Class<?> type, String host, int port) {
		super(type, host, port);
		try {
			channel = new EventChannel(host, port, (Class<? extends SituationType>) type) {

				@Override
				public void onDisconnection() {
	
				}
			};
		} catch (RemoteException | NotBoundException | InvalidEventChannel | ChannelAlreadyExists e) {
			e.printStackTrace();
		}
	}

	@Override
	public void publishActivation(SituationType sit) {
		try {
			channel.publish(Operation.ACTIVATION, sit);
		} catch (RemoteException | ChannelNotOpen | InvalidSituationType e) {
			e.printStackTrace();
		}
	}

	@Override
	public void publishDeactivation(SituationType sit) {
		try {
			channel.publish(Operation.DEACTIVATION, sit);
		} catch (RemoteException | ChannelNotOpen | InvalidSituationType e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void publishSnapshot(SituationType sit) {
		try {
			channel.publish(Operation.SNAPSHOT, sit);
		} catch (RemoteException | ChannelNotOpen | InvalidSituationType e) {
			e.printStackTrace();
		}
	}

}
