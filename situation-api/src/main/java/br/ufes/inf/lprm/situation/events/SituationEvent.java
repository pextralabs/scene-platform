package br.ufes.inf.lprm.situation.events;

import java.io.Serializable;

import br.ufes.inf.lprm.situation.SituationType;

public class SituationEvent implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6112684277674462302L;
	private long timestamp;
	private SituationType situation;
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

	public void setSituation(SituationType situation) {
		this.situation = situation;
	}

	public SituationType getSituation() {
		return situation;
	}

	public void setSystemTimestamp(long systemTimestamp) {
		this.systemTimestamp = systemTimestamp;
	}

	public long getSystemTimestamp() {
		return systemTimestamp;
	}

}
