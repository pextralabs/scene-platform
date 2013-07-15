package br.ufes.inf.lprm.scene.base;

import br.ufes.inf.lprm.situation.SituationCast;
import br.ufes.inf.lprm.situation.SituationType;

public class CurrentSituation {
	
	private int currentId;
	private SituationCast cast;
	private Class<?> type;
	private String typename;
	private SituationType situation;
	private long timestamp;
	private int hashcode;
	
	public CurrentSituation(Class<?> type) {
		this.currentId = type.hashCode();
		this.type = type;
		this.typename = type.getName();
	}
	
	public void setCast(SituationCast cast) {
		this.currentId = this.currentId + cast.hashCode();
		this.cast = cast;
		this.hashcode = this.cast.hashCode();
	}
	public SituationCast getCast() {
		return cast;
	}
	public void setType(Class<?> type) {
		this.type = type;
	}
	public Class<?> getType() {
		return type;
	}
	public void setSituation(SituationType situation) {
		this.situation = situation;
	}
	public SituationType getSituation() {
		return situation;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null) {
			return false;
		}
		else {			
			if ( !(obj instanceof CurrentSituation) ) {			
				return false;
			}			
			else {
				//System.out.println("equals called: " + this.castset.equals(((CurrentSituation) obj).getCastset()));
				return this.type.equals(((CurrentSituation) obj).getType()) && this.cast.equals(((CurrentSituation) obj).getCast());
				
			}
		}		
	}
	@Override	
	public int hashCode() {
		//System.out.println("hashCode called: " + this.currentId);
		return this.currentId;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public long getTimestamp() {
		return timestamp;
	}

	public int getHashcode() {
		return hashcode;
	}

	public void setHashcode(int hashcode) {
		this.hashcode = hashcode;
	}

	public String getTypename() {
		return typename;
	}

	public void setTypename(String typename) {
		this.typename = typename;
	}
}
