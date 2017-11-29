package br.ufes.inf.lprm.scene.base.listeners;

import br.ufes.inf.lprm.scene.util.OnGoingSituation;
import br.ufes.inf.lprm.scene.base.logging.SCENELogger;
import br.ufes.inf.lprm.situation.model.events.SituationEvent;
import org.kie.api.event.rule.DefaultRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.slf4j.Logger;

public class SCENESessionListener extends DefaultRuleRuntimeEventListener {

    Logger logger = SCENELogger.logger;

    public SCENESessionListener() {
        this.logger = SCENELogger.logger;
    }
    public SCENESessionListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void objectInserted(ObjectInsertedEvent event) {
        Object in = event.getObject();
        if (in instanceof OnGoingSituation) {
            logger.debug("SITUATION ACTIVATION - " + in.toString());
        } else if (in instanceof SituationEvent) {
            logger.debug(in.toString());
        } else {
            if (event.getRule() != null) {
                logger.debug("INSERT ("+ event.getRule().getName() +") - " + in.toString());
            } else {
                logger.debug("INSERT - " + in.toString());
            }
        }
    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {
        if (event.getRule() != null) {
            logger.debug("UPDATE ("+ event.getRule().getName() +") - " + event.getObject().toString());
        } else {
            logger.debug("UPDATE - " + event.getObject().toString());
        }
    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        Object in = event.getOldObject();
        if (in instanceof OnGoingSituation) {
            logger.debug("SITUATION DEACTIVATION - " + in.toString());
        } else logger.debug("RETRACT - " + event.getOldObject().toString());
    }
}
