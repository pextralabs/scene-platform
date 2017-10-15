package br.ufes.inf.lprm.situation.model.events;

import java.io.Serializable;

import br.ufes.inf.lprm.situation.model.Situation;

public interface SituationEvent {

	public long getTimestamp();
	public Situation getSituation();

}
