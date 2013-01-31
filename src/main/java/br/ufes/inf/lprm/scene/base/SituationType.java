package br.ufes.inf.lprm.scene.base;

import java.io.Serializable;

import br.ufes.inf.lprm.scene.base.events.ActivateSituationEvent;
import br.ufes.inf.lprm.scene.base.events.DeactivateSituationEvent;
import br.ufes.inf.lprm.scene.situation.publishing.SituationPublisher;

@SuppressWarnings("serial")
public class SituationType implements Serializable {

	private ActivateSituationEvent activation;
	private DeactivateSituationEvent deactivation;
	private CastSnapshotSet snapshots;
	private boolean active;
	//tests
	private long detectionTimestamp;
	
	public SituationType() {
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
	
	public void setActive(SituationPublisher pub) {
		this.active = true;
		pub.publishActivation(this);
	}	
	
	public void setInactive() {
		this.active = false;
	}
	
	public void setInactive(SituationPublisher pub) {
		this.active = false;
		pub.publishDeactivation(this);
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

}
