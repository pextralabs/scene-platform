package br.ufes.inf.lprm.scene.base.listeners;

import br.ufes.inf.lprm.scene.util.OnGoingSituation;
import br.ufes.inf.lprm.scene.util.SituationHelper;
import org.kie.api.event.rule.ObjectDeletedEvent;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.event.rule.ObjectUpdatedEvent;
import org.kie.api.event.rule.RuleRuntimeEventListener;


public class DeactivationListener implements RuleRuntimeEventListener {

    @Override
    public void objectInserted(ObjectInsertedEvent event) {

    }

    @Override
    public void objectUpdated(ObjectUpdatedEvent event) {

    }

    @Override
    public void objectDeleted(ObjectDeletedEvent event) {
        Object ongoing = event.getOldObject();
        if (ongoing instanceof OnGoingSituation) {
            SituationHelper.deactivateSituation(event.getKieRuntime(), ((OnGoingSituation) ongoing).getSituation() );
        }
    }
}
