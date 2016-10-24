package br.ufes.inf.lprm.situation.model.events;

import java.io.Serializable;

import br.ufes.inf.lprm.situation.model.Situation;

public class SituationEvent implements Serializable {

	private static final long serialVersionUID = -6112684277674462302L;
	private long timestamp;
	private Situation situation;
	private long systemTimestamp;
	
	public SituationEvent(long timestamp) {
		this.setTimestamp(timestamp);
		this.systemTimestamp = System.nanoTime();
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setSituation(Situation situation) {
		this.situation = situation;
	}

	public Situation getSituation() {
		return situation;
	}

	public void setSystemTimestamp(long systemTimestamp) {
		this.systemTimestamp = systemTimestamp;
	}

	public long getSystemTimestamp() {
		return systemTimestamp;
	}

}
