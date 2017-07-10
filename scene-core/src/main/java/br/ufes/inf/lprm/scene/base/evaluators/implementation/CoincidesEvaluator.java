package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.model.Situation;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.CoincidesEvaluatorDefinition;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;

/**
 * Created by hborjaille on 9/8/16.
 */
public class CoincidesEvaluator extends CoincidesEvaluatorDefinition.CoincidesEvaluator {

    private long              startDev;
    private long              endDev;

    public CoincidesEvaluator(final ValueType type,
                              final boolean isNegated,
                              final long[] parameters,
                              final String paramText,
                              final boolean unwrapLeft,
                              final boolean unwrapRight) {
        super( type,
                isNegated,
                parameters,
                paramText,
                unwrapLeft,
                unwrapRight);
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value) {
        throw new RuntimeException( "The 'coincides' operator can only be used to compare one event to another, and never to compare to literal constraints." );
    }

    @Override
    public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left) {
        if ( context.rightNull ||
                context.declaration.getExtractor().isNullValue( workingMemory, left.getObject() )) {
            return false;
        }

        long rightStartTS, rightEndTS;
        long leftStartTS, leftEndTS;

        rightStartTS = ((VariableRestriction.TemporalVariableContextEntry) context).startTS;
        rightEndTS = ((VariableRestriction.TemporalVariableContextEntry) context).endTS;

        if ( context.declaration.getExtractor().isSelfReference() ) {
            if (left.getObject() instanceof Situation) {
                Situation sit = (Situation) left.getObject();
                if (sit.isActive()) return false;
                leftStartTS = sit.getActivation().getTimestamp();
                leftEndTS   = sit.getDeactivation().getTimestamp();
            } else {
                leftStartTS = ((EventFactHandle) left).getStartTimestamp();
                leftEndTS = ((EventFactHandle) left).getEndTimestamp();
            }
        } else {
            leftStartTS = context.declaration.getExtractor().getLongValue( workingMemory, left.getObject() );
            leftEndTS = leftStartTS;
        }

        long distStart = Math.abs( rightStartTS - leftStartTS );
        long distEnd = Math.abs( rightEndTS - leftEndTS );
        return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd <= this.endDev);

    }

    @Override
    public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right) {
        if ( context.leftNull ||
                context.extractor.isNullValue( workingMemory, right.getObject() ) ) {
            return false;
        }

        long rightStartTS, rightEndTS;
        long leftStartTS, leftEndTS;

        if ( context.extractor.isSelfReference() ) {
            if (right.getObject() instanceof Situation) {
                Situation sit = (Situation) right.getObject();
                if (sit.isActive()) return false;
                rightStartTS  = sit.getActivation().getTimestamp();
                rightEndTS = sit.getDeactivation().getTimestamp();
            }
            else {
                rightStartTS = ((EventFactHandle) right).getStartTimestamp();
                rightEndTS = ((EventFactHandle) right).getEndTimestamp();
            }
        } else {
            rightStartTS = context.extractor.getLongValue( workingMemory, right.getObject() );
            rightEndTS = rightStartTS;
        }

        leftStartTS = ((VariableRestriction.TemporalVariableContextEntry) context).startTS;
        leftEndTS = ((VariableRestriction.TemporalVariableContextEntry) context).endTS;

        long distStart = Math.abs( rightStartTS - leftStartTS );
        long distEnd = Math.abs( rightEndTS - leftEndTS );
        return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd <= this.endDev);
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory,
                            final InternalReadAccessor leftExtractor,
                            final InternalFactHandle left,
                            final InternalReadAccessor rightExtractor,
                            final InternalFactHandle right) {

        if ( leftExtractor.isNullValue( workingMemory, left.getObject() ) ||
                rightExtractor.isNullValue( workingMemory, right.getObject() ) ) {
            return false;
        }

        long rightStartTS, rightEndTS;
        long leftStartTS, leftEndTS;

        if ( leftExtractor.isSelfReference() ) {
            rightStartTS = ((EventFactHandle) left).getStartTimestamp();
            rightEndTS = ((EventFactHandle) left).getEndTimestamp();
        } else {
            rightStartTS = leftExtractor.getLongValue( workingMemory, left.getObject() );
            rightEndTS = rightStartTS;
        }

        if ( rightExtractor.isSelfReference() ) {
            leftStartTS = ((EventFactHandle) right).getStartTimestamp();
            leftEndTS = ((EventFactHandle) right).getEndTimestamp();
        } else {
            leftStartTS = rightExtractor.getLongValue( workingMemory, right.getObject() );
            leftEndTS = leftStartTS;
        }

        long distStart = Math.abs( rightStartTS - leftStartTS );
        long distEnd = Math.abs( rightEndTS - leftEndTS );
        return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd <= this.endDev);

    }

}
