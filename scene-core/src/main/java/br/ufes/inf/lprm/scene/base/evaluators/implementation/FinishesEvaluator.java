package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.model.Situation;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.FinishesEvaluatorDefinition;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;

import java.lang.reflect.Field;

/**
 * Created by hborjaille on 9/8/16.
 */
public class FinishesEvaluator extends FinishesEvaluatorDefinition.FinishesEvaluator {

    private long endDev;

    public FinishesEvaluator(final ValueType type,
                             final boolean isNegated,
                             final long[] parameters,
                             final String paramText) {
        super( type,
                isNegated,
                parameters,
                paramText );
        extractParams();
    }

    private void extractParams() {
        try {
            Field endDev   = this.getClass().getSuperclass().getDeclaredField("endDev");
            endDev.setAccessible(true);
            this.endDev = endDev.getLong(this);
        } catch (Exception e) {
            throw new RuntimeException( e.getMessage() );
        }
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value) {
        throw new RuntimeException( "The 'finishes' operator can only be used to compare one event to another, and never to compare to literal constraints." );
    }

    @Override
    public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left) {

        if ( context.rightNull ||
                context.declaration.getExtractor().isNullValue( workingMemory, left.getObject() )) {
            return false;
        }

        long leftStartTS, leftEndTS, rightStartTS, rightEndTS;

        if (left.getObject() instanceof Situation) {
            Situation sit = (Situation) left.getObject();
            if (sit.isActive()) return false;
            leftStartTS = sit.getActivation().getTimestamp();
            leftEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        }
        else {
            leftStartTS = ((EventFactHandle) left).getStartTimestamp();
            leftEndTS = ((EventFactHandle) left).getEndTimestamp();
        }

        rightStartTS = ((VariableRestriction.TemporalVariableContextEntry) context).startTS;
        rightEndTS = ((VariableRestriction.TemporalVariableContextEntry) context).endTS;

        long distStart = rightStartTS - leftStartTS;
        long distEnd = Math.abs( leftEndTS - rightEndTS );
        return this.getOperator().isNegated() ^ (distStart > 0 && distEnd <= this.endDev);

    }

    @Override
    public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right) {
        if ( context.leftNull ||
                context.extractor.isNullValue( workingMemory, right.getObject() ) ) {
            return false;
        }

        long leftStartTS, leftEndTS, rightStartTS, rightEndTS;

        leftStartTS = ((VariableRestriction.TemporalVariableContextEntry) context).startTS;
        leftEndTS = ((VariableRestriction.TemporalVariableContextEntry) context).endTS;

        if (right.getObject() instanceof Situation) {
            Situation sit = (Situation) right.getObject();
            if (sit.isActive()) return false;
            rightStartTS = sit.getActivation().getTimestamp();
            rightEndTS   = sit.getDeactivation().getTimestamp();
        } else {
            rightStartTS = ((EventFactHandle) right).getStartTimestamp();
            rightEndTS = ((EventFactHandle) right).getEndTimestamp();
        }

        long distStart = rightStartTS - leftStartTS;
        long distEnd = Math.abs(leftEndTS - rightEndTS );
        return this.getOperator().isNegated() ^ (distStart > 0 && distEnd <= this.endDev);
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor, InternalFactHandle right) {
        if ( leftExtractor.isNullValue( workingMemory,
                left ) ) {
            return false;
        }

        long leftStartTS, leftEndTS, rightStartTS, rightEndTS;

        if (left.getObject() instanceof Situation) {
            Situation sit = (Situation) left.getObject();
            if (sit.isActive()) return false;
            leftStartTS = sit.getActivation().getTimestamp();
            leftEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        }
        else {
            leftStartTS = ((EventFactHandle) left).getStartTimestamp();
            leftEndTS = ((EventFactHandle) left).getEndTimestamp();
        }

        if (right.getObject() instanceof Situation) {
            Situation sit = (Situation) right.getObject();
            if (sit.isActive()) return false;
            rightStartTS = sit.getActivation().getTimestamp();
            rightEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        } else {
            rightStartTS = ((EventFactHandle) right).getStartTimestamp();
            rightEndTS = ((EventFactHandle) right).getEndTimestamp();
        }

        long distStart = leftStartTS - rightStartTS;
        long distEnd = Math.abs( rightEndTS - leftEndTS );
        return this.getOperator().isNegated() ^ (distStart > 0 && distEnd <= this. endDev);
    }
}
