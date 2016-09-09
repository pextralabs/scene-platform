package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.SituationType;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.AfterEvaluatorDefinition.AfterEvaluator;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction.LongVariableContextEntry;
import org.drools.core.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;
import org.kie.api.runtime.rule.FactHandle;

import java.util.Date;

/**
 * Created by hborjaille on 9/7/16.
 */

public class SCENEAfterEvaluator extends AfterEvaluator {

    protected long              initRange;
    protected long              finalRange;
    protected String            paramText;
    protected boolean           unwrapLeft;
    protected boolean           unwrapRight;

    public SCENEAfterEvaluator(ValueType type,
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
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value) {
        throw new RuntimeException("The 'after' operator can only be used to compare one event to another, and never to compare to literal constraints.");
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor, InternalFactHandle right) {
        if ( rightExtractor.isNullValue( workingMemory,
                right ) ) {
            return false;
        }
        long rightTS;

        if (this.unwrapRight) {
            rightTS = rightExtractor.getLongValue( workingMemory, right );
        }
        else {
            Object rightFact = workingMemory.getObject(right);

            if(rightFact instanceof SituationType )
            {
                rightTS = ((SituationType) rightFact).getActivation().getTimestamp();
            } else {
                rightTS = ((EventFactHandle) right).getStartTimestamp();
            }
        }

        long leftTS;

        if (this.unwrapLeft) {
            leftTS = leftExtractor.getLongValue( workingMemory, left );
        }
        else {

            Object leftFact = workingMemory.getObject(left);

            if(leftFact instanceof SituationType )
            {
                if ( ! ((SituationType) leftFact).isActive() ) {
                    leftTS = ((SituationType) leftFact).getDeactivation().getTimestamp();
                }
                else {
                    return false;
                }
            } else {
                leftTS = ((EventFactHandle) left).getEndTimestamp();
            }
        }

        long dist = rightTS - leftTS;

        return this.getOperator().isNegated() ^ (dist >= this.initRange && dist <= this.finalRange);
    }

    @Override
    public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right) {
        if ( context.extractor.isNullValue( workingMemory,
                right ) ) {
            return false;
        }

        long rightTS;

        if (this.unwrapRight) {
            rightTS = context.declaration.getExtractor().getLongValue( workingMemory, right );
        }
        else {
            Object rightFact = workingMemory.getObject((FactHandle) right);

            if(rightFact instanceof SituationType )
            {
                rightTS = ((SituationType) rightFact).getActivation().getTimestamp();
            } else {
                rightTS = ((EventFactHandle) right).getStartTimestamp();
            }
        }

        long leftTS;
        if( this.unwrapLeft ) {
            if( context instanceof ObjectVariableContextEntry ) {
                if( ((ObjectVariableContextEntry) context).left instanceof Date ) {
                    leftTS = ((Date)((ObjectVariableContextEntry) context).left).getTime();
                } else {
                    leftTS = ((Number)((ObjectVariableContextEntry) context).left).longValue();
                }
            } else {
                leftTS = ((LongVariableContextEntry) context).left;
            }
        } else {

            Object leftFact =  workingMemory.getObject((FactHandle) ((ObjectVariableContextEntry) context).left);

            if(leftFact instanceof SituationType )
            {
                if ( ! ((SituationType) leftFact).isActive() ) {
                    leftTS = ((SituationType) leftFact).getDeactivation().getTimestamp();
                }
                else {
                    return false;
                }
            } else {
                leftTS = ((EventFactHandle) ((ObjectVariableContextEntry) context).left).getEndTimestamp();
            }
        }
        long dist = rightTS - leftTS;

        return this.getOperator().isNegated() ^ (dist >= this.initRange && dist <= this.finalRange);
    }

    @Override
    public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left) {

        if ( context.rightNull ) {
            return false;
        }

        long rightTS;
        if( this.unwrapRight ) {
            if( context instanceof ObjectVariableContextEntry) {
                if( ((ObjectVariableContextEntry) context).right instanceof Date) {
                    rightTS = ((Date)((ObjectVariableContextEntry) context).right).getTime();
                } else {
                    rightTS = ((Number)((ObjectVariableContextEntry) context).right).longValue();
                }
            } else {
                rightTS = ((LongVariableContextEntry) context).right;
            }
        } else {

            Object rightFact =  workingMemory.getObject((FactHandle) ((ObjectVariableContextEntry) context).right);

            if(rightFact instanceof SituationType)
            {
                rightTS = ((SituationType) rightFact).getActivation().getTimestamp();
            } else {
                rightTS = ((EventFactHandle) ((ObjectVariableContextEntry) context).right).getStartTimestamp();
            }
        }

        long leftTS;

        if (this.unwrapLeft) {
            leftTS = context.declaration.getExtractor().getLongValue( workingMemory,left );
        }
        else {

            Object leftFact = workingMemory.getObject(left);

            if(leftFact instanceof SituationType )
            {
                if ( ! ((SituationType) leftFact).isActive() ) {
                    leftTS = ((SituationType) leftFact).getDeactivation().getTimestamp();
                }
                else {
                    return false;
                }
            } else {
                leftTS = ((EventFactHandle) left).getEndTimestamp();
            }
        }

        long dist = rightTS - leftTS;

        return this.getOperator().isNegated() ^ (dist >= this.initRange && dist <= this.finalRange);
    }

    @Override
    public boolean isTemporal() {
        return true;
    }

    @Override
    protected boolean evaluate( long rightTS, long leftTS ) {
        long dist = rightTS - leftTS;
        return this.getOperator().isNegated() ^ ( dist >= this.initRange && dist <= this.finalRange );
    }

    @Override
    protected long getLeftTimestamp( InternalFactHandle handle ) {
        return ( (EventFactHandle) handle ).getEndTimestamp();
    }

    @Override
    protected long getRightTimestamp( InternalFactHandle handle ) {
        return ( (EventFactHandle) handle ).getStartTimestamp();
    }
}
