package br.ufes.inf.lprm.situation.events;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DeactivateSituationEvent extends SituationEvent {

	private static final long serialVersionUID = -8350966238810288420L;

	public DeactivateSituationEvent(long timestamp) {
		super(timestamp);
	}

    public String toString() {

        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy MM dd HH:mm:ss");

        DateTime activation = new DateTime(this.getSituation().getActivation().getTimestamp());
        DateTime deactivation = new DateTime(this.getTimestamp());

        Period period = new Period(activation, deactivation);

        StringBuilder str = new StringBuilder();
        str.append("\tDeactivation timestamp: ");
        str.append(deactivation.toString(fmt));
        str.append(". Duration: ");
        str.append(period.getDays());
        str.append(" days ");
        str.append(period.getHours());
        str.append(" hours ");
        str.append(period.getMinutes());
        str.append(" minutes ");
        str.append(period.getSeconds());
        str.append(" seconds.");

        return str.toString();
    }

}
