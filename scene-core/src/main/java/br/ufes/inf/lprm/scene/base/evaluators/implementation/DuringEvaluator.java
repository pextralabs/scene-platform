package br.ufes.inf.lprm.scene.base.evaluators.implementation;

import br.ufes.inf.lprm.situation.model.Situation;
import org.drools.core.base.ValueType;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction;
import org.drools.core.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;

import java.lang.reflect.Field;

/**
 * Created by hborjaille on 9/8/16.
 */
public class DuringEvaluator extends org.drools.core.base.evaluators.DuringEvaluatorDefinition.DuringEvaluator {

    private long              startMinDev, startMaxDev;
    private long              endMinDev, endMaxDev;

    public DuringEvaluator(final ValueType type,
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
            Field startMinDev = this.getClass().getSuperclass().getDeclaredField("startMinDev");
            Field startMaxDev = this.getClass().getSuperclass().getDeclaredField("startMaxDev");
            Field endMinDev   = this.getClass().getSuperclass().getDeclaredField("endMinDev");
            Field endMaxDev   = this.getClass().getSuperclass().getDeclaredField("endMaxDev");

            startMinDev.setAccessible(true);
            startMaxDev.setAccessible(true);
            endMinDev.setAccessible(true);
            endMaxDev.setAccessible(true);

            this.startMinDev = startMinDev.getLong(this);
            this.startMaxDev = startMaxDev.getLong(this);
            this.endMinDev = endMinDev.getLong(this);
            this.endMaxDev = endMaxDev.getLong(this);

        } catch (Exception e) {

            throw new RuntimeException( e.getMessage() );

        }
    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor extractor, InternalFactHandle factHandle, FieldValue value) {
        throw new RuntimeException( "The 'during' operator can only be used to compare one event to another, and never to compare to literal constraints." );
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
        } else {
            leftStartTS = ((EventFactHandle) left).getStartTimestamp();
            leftEndTS = ((EventFactHandle) left).getEndTimestamp();
        }

        rightStartTS = ((VariableRestriction.TemporalVariableContextEntry) context).startTS;
        rightEndTS = ((VariableRestriction.TemporalVariableContextEntry) context).endTS;

        long distStart = rightStartTS - leftStartTS;
        long distEnd = leftEndTS - rightEndTS;
        return this.getOperator().isNegated() ^ (distStart >= this.startMinDev && distStart <= this.startMaxDev && distEnd >= this.endMinDev && distEnd <= this.endMaxDev);

    }

    @Override
    public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory, VariableContextEntry context, InternalFactHandle right) {
        if ( context.leftNull ||
                context.extractor.isNullValue( workingMemory, right.getObject() ) ) {
            return false;
        }

        long rightStartTS, rightEndTS;

        if (right.getObject() instanceof Situation) {
            Situation sit = (Situation) right.getObject();
            rightStartTS = sit.getActivation().getTimestamp();
            rightEndTS   = !sit.isActive() ? sit.getDeactivation().getTimestamp() : Long.MAX_VALUE;
        } else {
            rightStartTS = ((EventFactHandle) right).getStartTimestamp();
            rightEndTS = ((EventFactHandle) right).getEndTimestamp();
        }

        long distStart = rightStartTS - ((VariableRestriction.TemporalVariableContextEntry) context).startTS;
        long distEnd = ((VariableRestriction.TemporalVariableContextEntry) context).endTS - rightEndTS;
        return this.getOperator().isNegated() ^ (distStart >= this.startMinDev && distStart <= this.startMaxDev && distEnd >= this.endMinDev && distEnd <= this.endMaxDev);

    }

    @Override
    public boolean evaluate(InternalWorkingMemory workingMemory, InternalReadAccessor leftExtractor, InternalFactHandle left, InternalReadAccessor rightExtractor, InternalFactHandle right) {

        if ( leftExtractor.isNullValue( workingMemory, left.getObject() ) ||
                rightExtractor.isNullValue( workingMemory, right.getObject() ) ) {
            return false;
        }

        long leftStartTS, leftEndTS, rightStartTS, rightEndTS;

        if (left.getObject() instanceof Situation) {
            Situation sit = (Situation) left.getObject();
            if (sit.isActive()) return false;
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

        long distStart = leftStartTS - rightStartTS;
        long distEnd = rightEndTS - leftEndTS;
        return this.getOperator().isNegated() ^ (distStart >= this.startMinDev && distStart <= this.startMaxDev && distEnd >= this.endMinDev && distEnd <= this.endMaxDev);

    }
}