package br.ufes.inf.lprm.scene.base;

class SituationProfile {
	
	private Class<?>		type;
	private Boolean 		snapshot;
	private CastRestoreType	restoretype;
	
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
	
}
