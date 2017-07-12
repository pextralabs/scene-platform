package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.model.Situation;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.MetByEvaluatorDefinition;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction.LeftEndRightStartContextEntry;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;

import java.lang.reflect.Field;

public class MetByEvaluator extends MetByEvaluatorDefinition.MetByEvaluator {

    private long              finalRange;

    public MetByEvaluator(final ValueType type,
                          final boolean isNegated,
                          final long[] parameters,
                          final String paramText) {
        super( type,
                isNegated,
                parameters,
                paramText);
        extractParams();
    }


    private void extractParams() {
        try {
            Field finalRange   = this.getClass().getSuperclass().getDeclaredField("finalRange");
            finalRange.setAccessible(true);
            this.finalRange = finalRange.getLong(this);
        } catch (Exception e) {
            throw new RuntimeException( e.getMessage() );
        }
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory,
                            final InternalReadAccessor extractor,
                            final InternalFactHandle object1,
                            final FieldValue object2) {
        throw new RuntimeException( "The 'metby' operator can only be used to compare one event or situation event to another, and never to compare to literal constraints." );
    }

    @Override
    public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                       final VariableContextEntry context,
                                       final InternalFactHandle left) {
        if ( context.rightNull ||
                context.declaration.getExtractor().isNullValue( workingMemory, left.getObject() )) {
            return false;
        }

        long leftEndTS, rightStartTS;

        rightStartTS = ((LeftEndRightStartContextEntry)context).timestamp;

        if (left.getObject() instanceof Situation) {
            Situation sit = (Situation) left.getObject();
            //if (sit.isActive()) return false;
            leftEndTS = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        }
        else {
            leftEndTS = ((EventFactHandle) left).getEndTimestamp();
        }

        long dist = Math.abs( rightStartTS - leftEndTS );
        return this.getOperator().isNegated() ^ ( dist <= this.finalRange );
    }

    @Override
    public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                      final VariableContextEntry context,
                                      final InternalFactHandle right) {
        if ( context.leftNull ||
                context.extractor.isNullValue( workingMemory, right.getObject() ) ) {
            return false;
        }

        long rightStartTS;

        if (right.getObject() instanceof Situation) {
            Situation sit = (Situation) right.getObject();
            rightStartTS = sit.getActivation().getTimestamp();
        }
        else {
            rightStartTS = ((EventFactHandle) right).getStartTimestamp();
        }

        long dist = Math.abs( rightStartTS - ((LeftEndRightStartContextEntry)context).timestamp );
        return this.getOperator().isNegated() ^ ( dist <= this.finalRange );
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

        long leftStartTS, rightEndTS;

        if (left.getObject() instanceof Situation) {
            Situation sit = (Situation) left.getObject();
            if (sit.isActive()) return false;
            leftStartTS = sit.getActivation().getTimestamp();
        } else {
            leftStartTS = ((EventFactHandle) left).getStartTimestamp();
        }

        if (right.getObject() instanceof Situation) {
            Situation sit = (Situation) right.getObject();
            rightEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        } else {
            rightEndTS = ((EventFactHandle) right).getEndTimestamp();
        }

        long dist = Math.abs( leftStartTS - rightEndTS );
        return this.getOperator().isNegated() ^ ( dist <= this.finalRange );
    }
}
