package br.ufes.inf.lprm.scene.examples.message;

import br.ufes.inf.lprm.situation.Role;
import br.ufes.inf.lprm.situation.SituationType;

public class MessageExisting extends SituationType{

	private static final long serialVersionUID = 1L;

	@Role(label = "message")
	private Message message;

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}	
}
