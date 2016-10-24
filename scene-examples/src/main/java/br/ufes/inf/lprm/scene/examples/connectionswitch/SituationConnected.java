package br.ufes.inf.lprm.scene.examples.connectionswitch;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.ufes.inf.lprm.scene.examples.shared.Device;
import br.ufes.inf.lprm.scene.examples.shared.Network;
import br.ufes.inf.lprm.scene.model.impl.Situation;
import br.ufes.inf.lprm.situation.annotations.part;

public class SituationConnected extends Situation {
	
	@part(label = "device")
	private Device device;
	@part(label = "network")
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


}
