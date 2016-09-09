package br.ufes.inf.lprm.scene.base.listeners;

import br.ufes.inf.lprm.scene.base.CurrentSituation;
import br.ufes.inf.lprm.scene.base.logging.SCENELogger;
import br.ufes.inf.lprm.situation.events.SituationEvent;
import org.kie.api.event.rule.DefaultRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;

public class SCENESessionListener extends DefaultRuleRuntimeEventListener {

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        Object in = event.getObject();
        if (in instanceof CurrentSituation) {
            SCENELogger.logger.debug("SITUATION ACTIVATION - " + in.toString());
        } else if (in instanceof SituationEvent) {
            SCENELogger.logger.debug(in.toString());
        } else SCENELogger.logger.debug("INSERT - " + in.toString());
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        SCENELogger.logger.debug("UPDATE - " + event.getObject().toString());
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        Object in = event.getOldObject();
        if (in instanceof CurrentSituation) {
            SCENELogger.logger.debug("SITUATION DEACTIVATION - " + in.toString());
        } else SCENELogger.logger.debug("RETRACT - " + event.getOldObject().toString());
    }
}
