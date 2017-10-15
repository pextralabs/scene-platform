package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.model.Situation;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.StartedByEvaluatorDefinition;
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
public class StartedByEvaluator extends StartedByEvaluatorDefinition.StartedByEvaluator {

    private long startDev;

    public StartedByEvaluator(final ValueType type,
                              final boolean isNegated,
                              final long[] params,
                              final String paramText) {
        super( type,
                isNegated,
                params,
                paramText);
        extractParams();
    }

    private void extractParams() {
        try {
            Field startDev   = this.getClass().getSuperclass().getDeclaredField("startDev");
            startDev.setAccessible(true);
            this.startDev = startDev.getLong(this);
        } catch (Exception e) {
            throw new RuntimeException( e.getMessage() );
        }
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value) {
        throw new RuntimeException( "The 'startedby' operator can only be used to compare one event to another, and never to compare to literal constraints." );
    }

    @Override
    public boolean evaluateCachedRight(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle left) {

        if ( context.rightNull ||
                context.declaration.getExtractor().isNullValue( workingMemory, left.getObject() )) {
            return false;
        }

        long leftStartTS, leftEndTS, rightStartTS, rightEndTS;

        rightStartTS = ((VariableRestriction.TemporalVariableContextEntry) context).startTS;
        rightEndTS   = ((VariableRestriction.TemporalVariableContextEntry) context).endTS;

        if (left.getObject() instanceof Situation) {
            Situation sit = (Situation) left.getObject();
            leftStartTS = sit.getActivation().getTimestamp();
            leftEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        } else {
            leftStartTS = ((EventFactHandle) left).getStartTimestamp();
            leftEndTS = ((EventFactHandle) left).getEndTimestamp();
        }

        long distStart  = Math.abs(rightStartTS - leftStartTS);
        long distEnd    = rightEndTS - leftEndTS;
        return this.getOperator().isNegated() ^ ( distStart <= this.startDev && distEnd > 0 );

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
            rightStartTS = sit.getActivation().getTimestamp();
            rightEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        } else {
            rightStartTS = ((EventFactHandle) right).getStartTimestamp();
            rightEndTS = ((EventFactHandle) right).getEndTimestamp();
        }

        long distStart = Math.abs(rightStartTS - leftStartTS);
        long distEnd = rightEndTS - leftEndTS;
        return this.getOperator().isNegated() ^ ( distStart <= this.startDev && distEnd > 0 );
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor, InternalFactHandle right) {

        if (    leftExtractor.isNullValue( workingMemory, left.getObject() ) ||
                rightExtractor.isNullValue( workingMemory, right.getObject() )) {
            return false;
        }

        long leftStartTS, leftEndTS, rightStartTS, rightEndTS;

        if (left.getObject() instanceof Situation) {
            Situation sit = (Situation) left.getObject();
            leftStartTS = sit.getActivation().getTimestamp();
            leftEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        } else {
            leftStartTS = ((EventFactHandle) left).getStartTimestamp();
            leftEndTS = ((EventFactHandle) left).getEndTimestamp();
        }

        if (right.getObject() instanceof Situation) {
            Situation sit = (Situation) right.getObject();
            rightStartTS = sit.getActivation().getTimestamp();
            rightEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        } else {
            rightStartTS = ((EventFactHandle) right).getStartTimestamp();
            rightEndTS = ((EventFactHandle) right).getEndTimestamp();
        }

        long distStart = Math.abs(leftStartTS - rightStartTS);
        long distEnd = leftEndTS - rightEndTS;
        return this.getOperator().isNegated() ^ ( distStart <= this.startDev && distEnd > 0 );

    }
}
