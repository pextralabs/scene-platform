package br.ufes.inf.lprm.scene.examples.shared;

import java.io.Serializable;

public class Device implements Serializable {
	
	private String id;
	private Network connection;
	
	public Device(String name) {
		this.id = name;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String name) {
		this.id = name;
	}
	public Network getConnection() {
		return connection;
	}
	public void setConnection(Network connection) {
		this.connection = connection;
	}

}
