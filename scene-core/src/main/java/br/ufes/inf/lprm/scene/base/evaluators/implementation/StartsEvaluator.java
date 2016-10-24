package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.model.Situation;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.StartsEvaluatorDefinition;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;

/**
 * Created by hborjaille on 9/8/16.
 */
public class StartsEvaluator extends StartsEvaluatorDefinition.StartsEvaluator {

    private long startDev;

    public StartsEvaluator(final ValueType type,
                           final boolean isNegated,
                           final long[] params,
                           final String paramText) {
        super( type,
                isNegated,
                params,
                paramText );
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value) {
        throw new RuntimeException( "The 'starts' operator can only be used to compare one event to another, and never to compare to literal constraints." );
    }

    @Override
    public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left) {

        if ( context.rightNull ) {
            return false;
        }

        long leftStartTS = -1;
        long leftEndTS = -1;
        long rightStartTS = -1;
        long rightEndTS = -1;

        DefaultFactHandle leftFH = (DefaultFactHandle) left;

        if (leftFH instanceof EventFactHandle) {
            leftStartTS = ((EventFactHandle) leftFH).getStartTimestamp();
            leftEndTS = ((EventFactHandle) leftFH).getEndTimestamp();
        }
        else {
            Object leftFact =  workingMemory.getObject(leftFH);
            if (leftFact instanceof Situation) {
                leftStartTS = ((Situation) leftFact).getActivation().getTimestamp();
                //'during' is not applicable when situationA not finished
                if (!((Situation) leftFact).isActive()) {
                    leftEndTS = ((Situation) leftFact).getDeactivation().getTimestamp();
                }
            }
        }

        DefaultFactHandle rightFH = (DefaultFactHandle) ((ObjectVariableContextEntry) context).right;

        if (rightFH instanceof EventFactHandle) {
            rightStartTS = ((EventFactHandle) rightFH).getStartTimestamp();
            rightEndTS = ((EventFactHandle) rightFH).getEndTimestamp();
        }
        else {
            Object rightFact =  workingMemory.getObject(rightFH);
            if (rightFact instanceof Situation) {
                rightStartTS = ((Situation) rightFact).getActivation().getTimestamp();
                if (!((Situation) rightFact).isActive()) {
                    rightEndTS = ((Situation) rightFact).getDeactivation().getTimestamp();
                } else return false;
            }
        }
        long distStart = Math.abs( rightStartTS - leftStartTS );
        if (leftEndTS==(-1)) {
            return this.getOperator().isNegated() ^ (distStart <= this.startDev);
        } else {
            long distEnd = leftEndTS - rightEndTS;
            return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd > 0 );
        }
    }

    @Override
    public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right) {
        if ( context.extractor.isNullValue( workingMemory,
                right ) ) {
            return false;
        }
        long leftStartTS = -1;
        long leftEndTS = -1;
        long rightStartTS = -1;
        long rightEndTS = -1;

        DefaultFactHandle leftFH = (DefaultFactHandle) ((ObjectVariableContextEntry) context).left;

        if (leftFH instanceof EventFactHandle) {
            leftStartTS = ((EventFactHandle) leftFH).getStartTimestamp();
            leftEndTS = ((EventFactHandle) leftFH).getEndTimestamp();
        }
        else {
            Object leftFact =  workingMemory.getObject(leftFH);
            if (leftFact instanceof Situation) {
                leftStartTS = ((Situation) leftFact).getActivation().getTimestamp();
                //'during' is not applicable when situationA not finished
                if (!((Situation) leftFact).isActive()) {
                    leftEndTS = ((Situation) leftFact).getDeactivation().getTimestamp();
                }
            }
        }

        DefaultFactHandle rightFH = (DefaultFactHandle) right;

        if (rightFH instanceof EventFactHandle) {
            rightStartTS = ((EventFactHandle) rightFH).getStartTimestamp();
            rightEndTS = ((EventFactHandle) rightFH).getEndTimestamp();
        }
        else {
            Object rightFact =  workingMemory.getObject(rightFH);
            if (rightFact instanceof Situation) {
                rightStartTS = ((Situation) rightFact).getActivation().getTimestamp();
                if (!((Situation) rightFact).isActive()) {
                    rightEndTS = ((Situation) rightFact).getDeactivation().getTimestamp();
                } else return false;
            }
        }
        long distStart = Math.abs( rightStartTS - leftStartTS );
        if (leftEndTS==(-1)) {
            return this.getOperator().isNegated() ^ (distStart <= this.startDev);
        } else {
            long distEnd = leftEndTS - rightEndTS;
            return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd > 0 );
        }
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor, InternalFactHandle right) {
        if ( leftExtractor.isNullValue( workingMemory,
                left ) ) {
            return false;
        }

        long obj1StartTS = -1;
        long obj1EndTS = -1;
        long obj2StartTS = -1;
        long obj2EndTS = -1;

        DefaultFactHandle obj1FH = (DefaultFactHandle) left;

        if (obj1FH instanceof EventFactHandle) {
            obj1StartTS = ((EventFactHandle) obj1FH).getStartTimestamp();
            obj1EndTS = ((EventFactHandle) obj1FH).getEndTimestamp();
        }
        else {
            Object obj1Fact =  workingMemory.getObject(obj1FH);
            if (obj1Fact instanceof Situation) {
                obj1StartTS = ((Situation) obj1Fact).getActivation().getTimestamp();
                if (!((Situation) obj1Fact).isActive()) {
                    obj1EndTS = ((Situation) obj1Fact).getDeactivation().getTimestamp();
                } else return false;
            }
        }

        DefaultFactHandle obj2FH = (DefaultFactHandle) right;

        if (obj2FH instanceof EventFactHandle) {
            obj2StartTS = ((EventFactHandle) obj2FH).getStartTimestamp();
            obj2EndTS = ((EventFactHandle) obj2FH).getEndTimestamp();
        }
        else {
            Object obj2Fact =  workingMemory.getObject(obj2FH);
            if (obj2Fact instanceof Situation) {
                obj2StartTS = ((Situation) obj2Fact).getActivation().getTimestamp();
                if (!((Situation) obj2Fact).isActive()) {
                    obj2EndTS = ((Situation) obj2Fact).getDeactivation().getTimestamp();
                }
            }
        }

        long distStart = Math.abs( obj1StartTS - obj2StartTS );
        if (obj2StartTS==(-1)) {
            return this.getOperator().isNegated() ^ (distStart <= this.startDev);
        } else {
            long distEnd = obj2EndTS - obj1EndTS;
            return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd > 0 );
        }

    }
}
