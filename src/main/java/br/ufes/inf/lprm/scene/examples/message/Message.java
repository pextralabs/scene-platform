package br.ufes.inf.lprm.scene.examples.message;

import java.io.Serializable;

public class Message implements Serializable {

    private String message;

    public Message(String message) {
		this.message = message;
	}
    
    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
