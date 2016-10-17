package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.SituationType;
import org.drools.core.base.ValueType;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;

/**
 * Created by hborjaille on 9/7/16.
 */

public class BeforeEvaluator extends org.drools.core.base.evaluators.BeforeEvaluatorDefinition.BeforeEvaluator {

    public BeforeEvaluator(ValueType type,
                           boolean isNegated,
                           long[] parameters,
                           String paramText,
                           boolean unwrapLeft,
                           boolean unwrapRight) {
        super( type,
                isNegated,
                parameters,
                paramText,
                unwrapLeft,
                unwrapRight );
    }

    @Override
    protected long getLeftTimestamp( InternalFactHandle handle ) {
        Object obj = handle.getObject();
        if (obj instanceof SituationType) {
            if(((SituationType) obj).isActive()) {
                return Long.MAX_VALUE;
            }
            return ((SituationType) obj).getDeactivation().getTimestamp();
        } else return ( (EventFactHandle) handle ).getEndTimestamp();
    }

    @Override
    protected long getRightTimestamp( InternalFactHandle handle ) {
        Object obj = handle.getObject();
        if (obj instanceof SituationType) {
            return ((SituationType) obj).getActivation().getTimestamp();
        } else return ( (EventFactHandle) handle ).getStartTimestamp();
    }
}