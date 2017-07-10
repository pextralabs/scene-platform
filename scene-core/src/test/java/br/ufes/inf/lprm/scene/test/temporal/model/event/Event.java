package br.ufes.inf.lprm.scene.test.temporal.model.event;

import br.ufes.inf.lprm.scene.test.temporal.model.TemporalEntity;
import org.kie.api.definition.type.Duration;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.io.Serializable;

@Role(Role.Type.EVENT)
@Timestamp("start")
@Duration("duration")
public class Event implements Serializable, TemporalEntity {

    private static final long serialVersionUID = 1L;
    private long start;
    private long duration;
    private boolean finished;
    private String id;

    public Event(String id, long start, long duration) {
        this.id = id;
        this.start = start;
        this.duration = duration;
    }

    public long getStart() {
        return start;
    }

    public long getDuration() {
        return duration;
    }

    public String getId() {
        return id;
    }

    public boolean isFinished() {
        return finished;
    }

    public Event setFinished(boolean finished) {
        this.finished = finished;
        return this;
    }

    public long getEnd() {
        return start + duration;
    }

    @Override
    public TemporalEntity.Type getTemporalType() {
        return Type.EVENT;
    }
}
