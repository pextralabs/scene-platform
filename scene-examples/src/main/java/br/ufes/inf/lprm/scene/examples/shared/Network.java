package br.ufes.inf.lprm.scene.examples.shared;

import java.io.Serializable;

public class Network implements Serializable {

	private String id;
	private NetworkType type;
	
	public Network(String id, NetworkType type) {
		this.id 	= id;
		this.type	= type;
	}

	public NetworkType getType() {
		return type;
	}

	public String getId() {
		return id;
	}

	
}
