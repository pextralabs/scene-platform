package br.ufes.inf.lprm.scene.examples.connectionswitch;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.ufes.inf.lprm.scene.examples.shared.Device;
import br.ufes.inf.lprm.scene.examples.shared.Network;
import br.ufes.inf.lprm.situation.Role;
import br.ufes.inf.lprm.situation.SituationType;

public class SituationConnected extends SituationType {
	
	@Role(label = "device")
	private Device device;
	@Role(label = "network")
	private Network network;
	
	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		this.device = device;
	}
	public Network getNetwork() {
		return network;
	}
	public void setNetwork(Network network) {
		this.network = network;
	}
	
	@Override
	public void setActive() {
		super.setActive();
		System.out.println(device.getId() + " connected to " + network.getId() + " at " + new SimpleDateFormat("H:mm:ss").format(  new Date( this.getActivation().getTimestamp() ) ));		
	}
	@Override
	public void setInactive() {
		super.setInactive();
		System.out.println(device.getId() + " disconnected from " + network.getId() + " at " + new SimpleDateFormat("H:mm:ss").format(  new Date( this.getDeactivation().getTimestamp() ) ));		
	}		

}
