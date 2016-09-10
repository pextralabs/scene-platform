package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.SituationType;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.AfterEvaluatorDefinition;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;

public class AfterEvaluator extends AfterEvaluatorDefinition.AfterEvaluator {

    public AfterEvaluator( final ValueType type,
                           final boolean isNegated,
                           final long[] parameters,
                           final String paramText,
                           final boolean unwrapLeft,
                           final boolean unwrapRight ) {
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
            if (!((SituationType) obj).isActive()) {
                return ((SituationType) obj).getDeactivation().getTimestamp();
            }
            // the AFTER operation is only TRUE when A STARTS after the ENDING of B.
            // .If a situation B is NOT OVER yet,
            // it returns the highest possible long number to cause a FALSE evaluation.
            else return Long.MAX_VALUE;

        } else return ((EventFactHandle) handle ).getEndTimestamp();
    }

    @Override
    protected long getRightTimestamp( InternalFactHandle handle ) {
        Object obj = handle.getObject();
        if (obj instanceof SituationType) {
            return ((SituationType) obj).getActivation().getTimestamp();
        } else return ( (EventFactHandle) handle ).getStartTimestamp();
    }
}
