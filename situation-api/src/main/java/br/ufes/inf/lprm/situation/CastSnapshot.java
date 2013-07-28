package br.ufes.inf.lprm.situation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CastSnapshot implements Serializable {
	
	private static final long serialVersionUID = 6510062738888443595L;
	private long 	timestamp;
	private byte[] 	serializedCast;
	
	public CastSnapshot(SituationCast cast, long timestamp) throws IOException {
		
		this.timestamp = timestamp;
		
		ByteArrayOutputStream baOutput = new ByteArrayOutputStream();
	    ObjectOutputStream out = new ObjectOutputStream(baOutput);
	    out.writeObject(cast);
	    serializedCast = baOutput.toByteArray();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public byte[] getSerializedCast() {
		return serializedCast;
	}
	
	public SituationCast getSituationCast() throws IOException, ClassNotFoundException {
		ByteArrayInputStream baInput = new ByteArrayInputStream(serializedCast);
		ObjectInputStream in = new ObjectInputStream(baInput);		
		return (SituationCast) in.readObject();
	}

}
