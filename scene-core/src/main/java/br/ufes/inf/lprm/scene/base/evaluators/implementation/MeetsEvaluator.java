package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.model.Situation;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.MeetsEvaluatorDefinition;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.rule.VariableRestriction.LeftStartRightEndContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;

import java.lang.reflect.Field;

public class MeetsEvaluator extends MeetsEvaluatorDefinition.MeetsEvaluator {

    private long              finalRange;

    public MeetsEvaluator(final ValueType type,
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

    public boolean evaluate(InternalWorkingMemory workingMemory,
                            final InternalReadAccessor extractor,
                            final InternalFactHandle object1,
                            final FieldValue object2) {
        throw new RuntimeException( "The 'meets' operator can only be used to compare one event or situation event to another, and never to compare to literal constraints." );
    }

    public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                       final VariableContextEntry context,
                                       final InternalFactHandle left) {
        if ( context.rightNull ||
                context.declaration.getExtractor().isNullValue( workingMemory, left.getObject() )) {
            return false;
        }

        long leftStartTS;

        if (left.getObject() instanceof Situation) {
            Situation sit = (Situation) left.getObject();
            leftStartTS = sit.getActivation().getTimestamp();
        }
        else leftStartTS = ((EventFactHandle) left).getStartTimestamp();

        long dist = Math.abs( leftStartTS - ((LeftStartRightEndContextEntry) context).timestamp );
        return this.getOperator().isNegated() ^ (dist <= this.finalRange);
    }

    public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                      final VariableContextEntry context,
                                      final InternalFactHandle right) {
        if ( context.leftNull ||
                context.extractor.isNullValue( workingMemory, right.getObject() ) ) {
            return false;
        }

        long leftStartTS, rightEndTS;

        leftStartTS =  ((LeftStartRightEndContextEntry) context).timestamp;

        if (right.getObject() instanceof Situation) {
            Situation sit = (Situation) right.getObject();
            rightEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        } else {
            rightEndTS = ((EventFactHandle) right).getEndTimestamp();
        }

        long dist = Math.abs( leftStartTS - rightEndTS );
        return this.getOperator().isNegated() ^ (dist <= this.finalRange);
    }

    public boolean evaluate(InternalWorkingMemory workingMemory,
                            final InternalReadAccessor leftExtractor,
                            final InternalFactHandle left,
                            final InternalReadAccessor rightExtractor,
                            final InternalFactHandle right) {

        if ( leftExtractor.isNullValue( workingMemory, left.getObject() ) ||
                rightExtractor.isNullValue( workingMemory, right.getObject() ) ) {
            return false;
        }

        long leftEndTS, rightStartTS;

        if (left.getObject() instanceof Situation) {
            Situation sit = (Situation) left.getObject();
            leftEndTS = sit.getDeactivation().getTimestamp();
        }
        else leftEndTS = ((EventFactHandle) left).getEndTimestamp();

        if (right.getObject() instanceof Situation) {
            Situation sit = (Situation) right.getObject();
            rightStartTS = sit.getActivation().getTimestamp();
        } else {
            rightStartTS = ((EventFactHandle) right).getStartTimestamp();
        }

        long dist = Math.abs( rightStartTS - leftEndTS );
        return this.getOperator().isNegated() ^ (dist <= this.finalRange);
    }
}
