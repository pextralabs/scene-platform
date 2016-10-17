package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.SituationType;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.MetByEvaluatorDefinition;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction.LeftEndRightStartContextEntry;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;

/**
 * Created by hborjaille on 10/12/16.
 */
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

        long rightStartTS = ((LeftEndRightStartContextEntry)context).timestamp;
        long dist = 0;
        if(left instanceof EventFactHandle) {
            dist = Math.abs( rightStartTS - ((EventFactHandle) left).getEndTimestamp() );
        } else {
            Object leftFact =  workingMemory.getObject(left);
            if (leftFact instanceof SituationType) {
                dist = Math.abs( rightStartTS - ((SituationType) leftFact).getDeactivation().getTimestamp());
            }
        }
        return this.getOperator().isNegated() ^ (dist <= this.finalRange);
    }

    @Override
    public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                      final VariableContextEntry context,
                                      final InternalFactHandle right) {
        if ( context.leftNull ||
                context.extractor.isNullValue( workingMemory, right.getObject() ) ) {
            return false;
        }

        long rightStartTS = 0;
        if(right instanceof EventFactHandle) {
            rightStartTS = ((EventFactHandle) right).getStartTimestamp();
        } else {
            Object leftFact =  workingMemory.getObject(right);
            if (leftFact instanceof SituationType) {
                rightStartTS = ((SituationType) leftFact).getActivation().getTimestamp();
            }
        }
        long dist = Math.abs( rightStartTS - ((LeftEndRightStartContextEntry)context).timestamp );

        return this.getOperator().isNegated() ^ ( dist <= this.finalRange );
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory,
                            final InternalReadAccessor extractor1,
                            final InternalFactHandle handle1,
                            final InternalReadAccessor extractor2,
                            final InternalFactHandle handle2) {
        if ( extractor1.isNullValue( workingMemory, handle1.getObject() ) ||
                extractor2.isNullValue( workingMemory, handle2.getObject() ) ) {
            return false;
        }

        long obj1StartTS = -1;
        if(handle2 instanceof EventFactHandle) {
            obj1StartTS = ((EventFactHandle) handle1).getStartTimestamp();
        } else {
            Object obj2Fact =  workingMemory.getObject(handle1);
            if (obj2Fact instanceof SituationType) {
                obj1StartTS = ((SituationType) obj2Fact).getActivation().getTimestamp();
            }
        }

        long dist = 0;
        if (handle1 instanceof EventFactHandle) {
            dist = Math.abs( obj1StartTS - ((EventFactHandle) handle2).getEndTimestamp() );
        } else {
            Object obj2Fact = workingMemory.getObject(handle2);
            if (obj2Fact instanceof SituationType) {
                dist = Math.abs( obj1StartTS - ((SituationType) obj2Fact).getDeactivation().getTimestamp());
            }
        }

        return this.getOperator().isNegated() ^ ( dist <= this.finalRange );
    }
}
