package br.ufes.inf.lprm.scene.mqc.evaluators;


import br.ufes.inf.lprm.scene.mqc.model.Location;
import org.drools.core.base.BaseEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.*;
import org.drools.core.common.EventFactHandle;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.rule.VariableRestriction.TemporalVariableContextEntry;
import org.drools.core.rule.VariableRestriction.VariableContextEntry;
import org.drools.core.spi.Evaluator;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;
import org.drools.core.time.Interval;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NearEvaluatorDefinition
        implements
        EvaluatorDefinition {

    protected static final String nearOp = "near";

    public static Operator NEAR;
    public static Operator NEAR_NOT;

    private static String[] SUPPORTED_IDS;

    private volatile DistanceParser parser = new DistanceParser();

    private Map<String, NearEvaluator> cache     = Collections.emptyMap();

    { init(); }

    static void init() {
        if ( Operator.determineOperator(nearOp, false ) == null ) {
            NEAR = Operator.addOperatorToRegistry(nearOp, false );
            NEAR_NOT = Operator.addOperatorToRegistry(nearOp, true );
            SUPPORTED_IDS = new String[] {nearOp};
        }
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        cache = (Map<String, NearEvaluator>) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject( cache );
    }

    /**
     * @inheridDoc
     */
    public Evaluator getEvaluator(ValueType type,
                                  Operator operator) {
        return this.getEvaluator( type,
                operator.getOperatorString(),
                operator.isNegated(),
                null );
    }

    /**
     * @inheridDoc
     */
    public Evaluator getEvaluator(ValueType type,
                                  Operator operator,
                                  String parameterText) {




        return this.getEvaluator( type,
                operator.getOperatorString(),
                operator.isNegated(),
                parameterText );
    }

    /**
     * @inheritDoc
     */
    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText) {
        return this.getEvaluator( type,
                operatorId,
                isNegated,
                parameterText,
                Target.HANDLE,
                Target.HANDLE );

    }

    /**
     * @inheritDoc
     */
    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText,
                                  final Target left,
                                  final Target right) {

        if ( this.cache == Collections.EMPTY_MAP ) {
            this.cache = new HashMap<String, NearEvaluator>();
        }

        String key = left + ":" + right + ":" + isNegated + ":" + parameterText;
        NearEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            Double[] params = (Double[]) parser.parse(parameterText);
            eval = new NearEvaluator( type,
                    isNegated,
                    params,
                    parameterText,
                    left == Target.FACT,
                    right == Target.FACT );
            this.cache.put( key,
                    eval );
        }
        return eval;
    }

    /**
     * @inheritDoc
     */
    public String[] getEvaluatorIds() {
        return SUPPORTED_IDS;
    }

    /**
     * @inheritDoc
     */
    public boolean isNegatable() {
        return true;
    }

    /**
     * @inheritDoc
     */
    public Target getTarget() {
        return Target.BOTH;
    }

    /**
     * @inheritDoc
     */
    public boolean supportsType(ValueType type) {
        // supports all types, since it operates over fact handles
        // Note: should we change this interface to allow checking of event classes only?
        return true;
    }

    /**
     * Implements the 'coincides' evaluator itself
     */
    public static class NearEvaluator extends BaseEvaluator {
        private static final long serialVersionUID = 510l;

        private Double            distance;
        private String            paramText;
        private boolean           unwrapLeft;
        private boolean           unwrapRight;

        {
            NearEvaluatorDefinition.init();
        }

        public NearEvaluator() {
        }

        public NearEvaluator(final ValueType type,
                             final boolean isNegated,
                             final Double[] parameters,
                             final String paramText,
                             final boolean unwrapLeft,
                             final boolean unwrapRight) {
            super( type,
                    isNegated ? NEAR_NOT : NEAR);
            this.paramText = paramText;
            this.unwrapLeft = unwrapLeft;
            this.unwrapRight = unwrapRight;
            this.setParameters( parameters );
        }

        public void readExternal(ObjectInput in) throws IOException,
                ClassNotFoundException {
            super.readExternal( in );
            distance = in.readDouble();
            unwrapLeft = in.readBoolean();
            unwrapRight = in.readBoolean();
            paramText = (String) in.readObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal( out );
            out.writeDouble( this.distance );
            out.writeBoolean( unwrapLeft );
            out.writeBoolean( unwrapRight );
            out.writeObject( paramText );
        }

        @Override
        public boolean isTemporal() {
            return false;
        }

        @Override
        public Interval getInterval() {
            if ( this.getOperator().isNegated() ) {
                return new Interval( Interval.MIN,
                        Interval.MAX );
            }
            return new Interval( 0,
                    0 );
        }

        public Double getDistance() {
            return distance;
        }

        public NearEvaluator setDistance(Double distance) {
            this.distance = distance;
            return this;
        }


        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final InternalReadAccessor extractor,
                                final InternalFactHandle object1,
                                final FieldValue object2) {
            throw new RuntimeException( "The 'near' operator can only be used to compare one location to another, and never to compare to literal constraints." );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context,
                                           final InternalFactHandle left) {
            if ( context.rightNull ||
                    context.declaration.getExtractor().isNullValue( workingMemory, left.getObject() )) {
                return false;
            }

            long rightStartTS, rightEndTS;
            long leftStartTS, leftEndTS;

            rightStartTS = ((TemporalVariableContextEntry) context).startTS;
            rightEndTS = ((TemporalVariableContextEntry) context).endTS;

            if ( context.declaration.getExtractor().isSelfReference() ) {
                leftStartTS = ((EventFactHandle) left).getStartTimestamp();
                leftEndTS = ((EventFactHandle) left).getEndTimestamp();
            } else {
                leftStartTS = context.declaration.getExtractor().getLongValue( workingMemory, left.getObject() );
                leftEndTS = leftStartTS;
            }

            long distStart = Math.abs( rightStartTS - leftStartTS );
            long distEnd = Math.abs( rightEndTS - leftEndTS );
            return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd <= this.endDev);
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context,
                                          final InternalFactHandle right) {
            final Location rightLocation = (Location) right;

            if (context.object == left)
                return false;

            if (rightLocation == null)
                return false;
            return this.getOperator().isNegated()^leftLocation.Near(rightLocation, this.distance);;
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

                final Object value1 = extractor1.getValue(workingMemory, handle1);
                final Object value2 = extractor2.getValue(workingMemory, handle2);
                Location leftLocation 	= (Location) value1;
                Location rightLocation 	= (Location) value2;

                return this.getOperator().isNegated()^leftLocation.Near(rightLocation, this.distance);
        }

        public String toString() {
            return "near[" + distance + "]";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = super.hashCode();
            result = PRIME * result + (distance.intValue() ^ (distance.intValue() >>> 32));
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) return true;
            if ( !super.equals( obj ) ) return false;
            if ( getClass() != obj.getClass() ) return false;
            final NearEvaluator other = (NearEvaluator) obj;
            return distance == other.distance;
        }

        /**
         * This methods sets the parameters appropriately.
         *
         * @param parameters
         */
        private void setParameters(Double[] parameters) {
            if ( parameters == null || parameters.length == 0 ) {
                // open bounded range
                this.distance = 0.0;
                return;
            } else {
                for ( Double param : parameters ) {
                    if ( param.doubleValue() < 0 ) {
                        throw new RuntimeException( "[Near Evaluator]: negative values not allowed for temporal distance thresholds: '" + paramText + "'" );
                    }
                }
                if ( parameters.length == 1 ) {
                    // same deviation for both
                    this.distance = parameters[0];
                } else {
                    throw new RuntimeException( "[Near Evaluator]: Not possible to have more than 2 parameters: '" + paramText + "'" );
                }
            }
        }

    }


    private class DistanceParser {

        public Double[] parse(String paramText) {


            if ( paramText == null || paramText.trim().length() == 0 ) {
                Double[] def = new Double[] {(double) 100};
                return def;
            }

            paramText = new String(paramText.replaceAll("\"", ""));

            String trimmed = paramText.trim();
            Double[] result = new Double[1];
            if ( trimmed.length() > 0 ) {
                result[0] = this.parseDistanceString(paramText);
            } else {
                throw new RuntimeDroolsException( "Empty parameters not allowed in: [" + paramText + "]" );
            }
            return result;
        }
        private Double parseDistanceString(String paramText) {

            Pattern DoublePattern = Pattern.compile("\\d+");
            Matcher matcher = DoublePattern.matcher(paramText);
            matcher.find();
            Double result = new Double(matcher.group());

            paramText = new String(paramText.replaceAll("\\d+", ""));

            Pattern pattern = Pattern.compile("\\D+");
            matcher = pattern.matcher(paramText);
            matcher.find();
            String unity = matcher.group();

            if (unity.equals("m")) {
                result = result*Math.pow(10, 0);
            }

            if (unity.equals("dam")) {
                result = result*Math.pow(10, 1);
            }

            if (unity.equals("hm")) {
                result = result*Math.pow(10, 2);
            }

            if (unity.equals("km")) {
                result = result*Math.pow(10, 3);
            }

            return result;
        }

    }


}