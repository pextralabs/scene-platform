package br.ufes.inf.lprm.scene.examples.connectionswitch;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.ufes.inf.lprm.situation.Part;
import br.ufes.inf.lprm.situation.SituationType;

public class SituationSwitch extends SituationType {
	
	@Part(label = "wlan")
	private SituationConnected wlan;
	@Part(label = "bluetooth")
	private SituationConnected bluetooth;
	
	public SituationConnected getWlan() {
		return wlan;
	}
	public void setWlan(SituationConnected wlan) {
		this.wlan = wlan;
	}
	public SituationConnected getBluetooth() {
		return bluetooth;
	}
	public void setBluetooth(SituationConnected bluetooth) {
		this.bluetooth = bluetooth;
	}
	
	@Override
	public void setActive() {
		super.setActive();
		System.out.println(wlan.getDevice().getId() + " switched from " + wlan.getNetwork().getId() + " to " + bluetooth.getNetwork().getId() + " at " + new SimpleDateFormat("H:mm:ss").format(  new Date( this.getActivation().getTimestamp() ) ) );		
	}

}
