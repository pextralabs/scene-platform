package br.ufes.inf.lprm.scene.examples.connectionswitch;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.ufes.inf.lprm.scene.model.impl.Situation;
import br.ufes.inf.lprm.situation.annotations.part;

public class SituationSwitch extends Situation {
	
	@part(label = "wlan")
	private SituationConnected wlan;
	@part(label = "bluetooth")
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


}
