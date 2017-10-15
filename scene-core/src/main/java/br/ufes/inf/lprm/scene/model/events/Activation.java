package br.ufes.inf.lprm.scene.model.events;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Activation extends SituationEvent {

	private static final long serialVersionUID = -8574595668587040347L;

	public Activation(long timestamp) {
		super(timestamp);
	}

    public String toString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy MM dd HH:mm:ss");
        DateTime activation = new DateTime(this.getTimestamp());
        StringBuilder str = new StringBuilder();
        str.append("\tActivation timestamp: ");
        str.append(activation.toString(fmt));
        str.append(".");
        return str.toString();
    }

}
