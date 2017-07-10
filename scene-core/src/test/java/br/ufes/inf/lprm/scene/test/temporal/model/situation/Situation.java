package br.ufes.inf.lprm.scene.test.temporal.model.situation;

import br.ufes.inf.lprm.scene.test.temporal.model.TemporalEntity;
import br.ufes.inf.lprm.scene.test.temporal.model.event.Event;
import br.ufes.inf.lprm.situation.bindings.part;
import org.kie.api.definition.type.Key;


public class Situation extends br.ufes.inf.lprm.scene.model.Situation implements TemporalEntity {

    @part @Key
    private Event event;

    @Override
    public String getId() {
        return getEvent().getId();
    }

    @Override
    public boolean isFinished() {
        return !this.isActive();
    }

    @Override
    public long getStart() {
        return this.getActivation().getTimestamp();
    }

    @Override
    public long getEnd() {
        return isFinished() ? this.getDeactivation().getTimestamp() : -1;
    }

    @Override
    public TemporalEntity.Type getTemporalType() {
        return Type.SITUATION;
    }

    public Event getEvent() {
        return event;
    }

}
