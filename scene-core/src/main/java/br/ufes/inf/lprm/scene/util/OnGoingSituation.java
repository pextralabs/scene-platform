package br.ufes.inf.lprm.scene.util;

import br.ufes.inf.lprm.scene.model.SituationType;
import br.ufes.inf.lprm.situation.model.Situation;

public class OnGoingSituation {
	
	private int currentId;
	private SituationCast cast;
	private br.ufes.inf.lprm.situation.model.SituationType type;
	private String typename;
	private Situation situation;
	private long timestamp;
	private int hashcode;
	
	public OnGoingSituation(br.ufes.inf.lprm.situation.model.SituationType type, long timestamp, SituationCast cast) {
		this.currentId = ((SituationType) type).getTypeClass().hashCode() + cast.hashCode();
		this.hashcode = cast.hashCode();
		this.cast = cast;
		this.type = type;
		this.typename = type.getName();
		this.timestamp = timestamp;
	}

	public SituationCast getCast() {
		return cast;
	}
	public br.ufes.inf.lprm.situation.model.SituationType getType() {
		return type;
	}

	public void setSituation(Situation situation) {
		this.situation = situation;
	}
	public Situation getSituation() {
		return situation;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null) {
			return false;
		}
		else {			
			if ( !(obj instanceof OnGoingSituation) ) {
				return false;
			}			
			else {
				return this.type.equals(((OnGoingSituation) obj).getType()) && this.cast.equals(((OnGoingSituation) obj).getCast());
				
			}
		}		
	}
	@Override	
	public int hashCode() {
		return this.currentId;
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

    public String toString() {

        StringBuilder str = new StringBuilder();
        str.append("TYPE: ");
        str.append(typename);
        str.append("\t");
        str.append("ID: ");
        str.append(currentId);
        str.append("\t");
        str.append("CAST: ");
        str.append(this.cast.toString());

        return str.toString();

    }

}

