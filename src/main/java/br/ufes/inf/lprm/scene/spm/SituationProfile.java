package br.ufes.inf.lprm.scene.spm;

import br.ufes.inf.lprm.scene.publishing.SituationPublisher;

public class SituationProfile {
	
	private Class<?> 				type;
	private Boolean 				snapshot;
	private CastRestoreType			restoretype;
	private SituationPublisher 		publisher;
	
	public Boolean getSnapshot() {
		return snapshot;
	}
	public void setSnapshot(Boolean snapshot) {
		this.snapshot = snapshot;
	}
	public CastRestoreType getRestoretype() {
		return restoretype;
	}
	public void setRestoreType(CastRestoreType restoretype) {
		this.restoretype = restoretype;
	}
	public Class<?> getType() {
		return type;
	}
	public void setType(Class<?> type) {
		this.type = type;
	}
	public SituationPublisher getPublisher() {
		return publisher;
	}
	public void setPublisher(SituationPublisher publisher) {
		this.publisher = publisher;
	}
	
}
