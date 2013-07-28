package br.ufes.inf.lprm.situation;

import java.io.Serializable;

import br.ufes.inf.lprm.situation.events.ActivateSituationEvent;
import br.ufes.inf.lprm.situation.events.DeactivateSituationEvent;

@SuppressWarnings("serial")
public class SituationType implements Serializable {

	
	private ActivateSituationEvent activation;
	private DeactivateSituationEvent deactivation;
	private CastSnapshotSet snapshots;
	private boolean local;
	private boolean active;
	//tests
	private long detectionTimestamp;
	
	public SituationType() {
		this.local = true;
		snapshots = new CastSnapshotSet();
	}
	
	public void setActivation(ActivateSituationEvent activation) {
		this.activation = activation;
		this.setActive();
	}
	
	public ActivateSituationEvent getActivation() {
		return activation;
	}
	public void setDeactivation(DeactivateSituationEvent deactivation) {
		this.deactivation = deactivation;
		this.setInactive();
	}
	public DeactivateSituationEvent getDeactivation() {
		return deactivation;
	}
	public void setActive() {
		this.active = true;
	}
		
	
	public void setInactive() {
		this.active = false;
	}	
	
	public boolean isActive() {
		return active;
	}
	public long getDuration() {
		if (isActive()) throw new SituationNotFinishedException();
		return this.getDeactivation().getTimestamp() - this.getActivation().getTimestamp();
	}
	
	//tests
	public void setDetectionTimestamp(long detectionTimestamp) {
		this.detectionTimestamp = detectionTimestamp;
	}
	public long getDetectionTimestamp() {
		return detectionTimestamp;
	}

	public CastSnapshotSet getSnapshots() {
		return snapshots;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

}
