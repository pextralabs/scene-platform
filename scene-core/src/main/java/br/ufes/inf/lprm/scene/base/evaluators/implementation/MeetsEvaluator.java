package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.model.Situation;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.MetByEvaluatorDefinition;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.rule.VariableRestriction.LeftStartRightEndContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;

/**
 * Created by hborjaille on 10/12/16.
 */
public class MeetsEvaluator extends MetByEvaluatorDefinition.MetByEvaluator {
    private long              finalRange;

    public MeetsEvaluator(final ValueType type,
                          final boolean isNegated,
                          final long[] parameters,
                          final String paramText) {
        super( type,
                isNegated,
                parameters,
                paramText);
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

        long leftStartTS = 0;
        if(left instanceof EventFactHandle) {
            leftStartTS = ((EventFactHandle) left).getStartTimestamp();
        } else {
            Object leftFact =  workingMemory.getObject(left);
            if (leftFact instanceof Situation) {
                leftStartTS = ((Situation) leftFact).getActivation().getTimestamp();
            }
        }

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

        long leftStartTS =  ((LeftStartRightEndContextEntry) context).timestamp;
        long dist = 0;
        if (right instanceof EventFactHandle) {
            dist = Math.abs( leftStartTS - ((EventFactHandle) right).getEndTimestamp() );
        } else {
            Object rightFact =  workingMemory.getObject(right);
            if (rightFact instanceof Situation) {
                dist = Math.abs( leftStartTS - ((Situation) rightFact).getDeactivation().getTimestamp());
            }
        }

        return this.getOperator().isNegated() ^ (dist <= this.finalRange);
    }

    public boolean evaluate(InternalWorkingMemory workingMemory,
                            final InternalReadAccessor extractor1,
                            final InternalFactHandle handle1,
                            final InternalReadAccessor extractor2,
                            final InternalFactHandle handle2) {
        if ( extractor1.isNullValue( workingMemory, handle1.getObject() ) ||
                extractor2.isNullValue( workingMemory, handle2.getObject() ) ) {
            return false;
        }

        long obj2StartTS = -1;
        if(handle2 instanceof EventFactHandle) {
            obj2StartTS = ((EventFactHandle) handle2).getStartTimestamp();
        } else {
            Object obj2Fact =  workingMemory.getObject(handle2);
            if (obj2Fact instanceof Situation) {
                obj2StartTS = ((Situation) obj2Fact).getActivation().getTimestamp();
            }
        }

        long dist = 0;
        if (handle1 instanceof EventFactHandle) {
            dist = Math.abs( obj2StartTS - ((EventFactHandle) handle1).getEndTimestamp() );
        } else {
            Object obj1Fact = workingMemory.getObject(handle1);
            if (obj1Fact instanceof Situation) {
                dist = Math.abs( obj2StartTS - ((Situation) obj1Fact).getDeactivation().getTimestamp());
            }
        }

        return this.getOperator().isNegated() ^ (dist <= this.finalRange);
    }
}
