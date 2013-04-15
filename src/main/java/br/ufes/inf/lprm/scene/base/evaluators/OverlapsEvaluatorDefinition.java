/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.ufes.inf.lprm.scene.base.evaluators;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.drools.RuntimeDroolsException;
import org.drools.base.BaseEvaluator;
import org.drools.base.ValueType;
import org.drools.base.evaluators.EvaluatorDefinition;
import org.drools.base.evaluators.Operator;
import org.drools.base.evaluators.TimeIntervalParser;
import org.drools.common.DefaultFactHandle;
import org.drools.common.EventFactHandle;
import org.drools.common.InternalFactHandle;
import org.drools.common.InternalWorkingMemory;
import org.drools.rule.VariableRestriction.ObjectVariableContextEntry;
import org.drools.rule.VariableRestriction.VariableContextEntry;
import org.drools.spi.Evaluator;
import org.drools.spi.FieldValue;
import org.drools.spi.InternalReadAccessor;
import org.drools.time.Interval;

import br.ufes.inf.lprm.situation.SituationType;

/**
 * <p>The implementation of the <code>overlaps</code> evaluator definition.</p>
 * 
 * <p>The <b><code>overlaps</code></b> evaluator correlates two events and matches when the current event 
 * starts before the correlated event starts and finishes after the correlated event starts, but before
 * the correlated event finishes. In other words, both events have an overlapping period.</p> 
 * 
 * <p>Lets look at an example:</p>
 * 
 * <pre>$eventA : EventA( this overlaps $eventB )</pre>
 *
 * <p>The previous pattern will match if and only if:</p>
 * 
 * <pre> $eventA.startTimestamp < $eventB.startTimestamp < $eventA.endTimestamp < $eventB.endTimestamp </pre>
 * 
 * <p>The <b><code>overlaps</code></b> operator accepts 1 or 2 optional parameters as follow:</p>
 * 
 * <ul><li>If one parameter is defined, this will be the maximum distance between the start timestamp of the
 * correlated event and the end timestamp of the current event. Example:</li></lu>
 * 
 * <pre>$eventA : EventA( this overlaps[ 5s ] $eventB )</pre>
 * 
 * Will match if and only if:
 * 
 * <pre> 
 * $eventA.startTimestamp < $eventB.startTimestamp < $eventA.endTimestamp < $eventB.endTimestamp &&
 * 0 <= $eventA.endTimestamp - $eventB.startTimestamp <= 5s 
 * </pre>
 * 
 * <ul><li>If two values are defined, the first value will be the minimum distance and the second value will be 
 * the maximum distance between the start timestamp of the correlated event and the end timestamp of the current 
 * event. Example:</li></lu>
 * 
 * <pre>$eventA : EventA( this overlaps[ 5s, 10s ] $eventB )</pre>
 * 
 * Will match if and only if:
 * 
 * <pre> 
 * $eventA.startTimestamp < $eventB.startTimestamp < $eventA.endTimestamp < $eventB.endTimestamp &&
 * 5s <= $eventA.endTimestamp - $eventB.startTimestamp <= 10s 
 * </pre>
 */
public class OverlapsEvaluatorDefinition
    implements
    EvaluatorDefinition {

    public static final Operator           OVERLAPS      = Operator.addOperatorToRegistry( "overlaps",
                                                                                           false );
    public static final Operator           OVERLAPS_NOT  = Operator.addOperatorToRegistry( "overlaps",
                                                                                           true );

    private static final String[]          SUPPORTED_IDS = {OVERLAPS.getOperatorString()};

    private Map<String, OverlapsEvaluator> cache         = Collections.emptyMap();
    private volatile TimeIntervalParser    parser        = new TimeIntervalParser();

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        cache = (Map<String, OverlapsEvaluator>) in.readObject();
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
                                  final Target right ) {
        if ( this.cache == Collections.EMPTY_MAP ) {
            this.cache = new HashMap<String, OverlapsEvaluator>();
        }
        String key = isNegated + ":" + parameterText;
        OverlapsEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            Long[] params = parser.parse( parameterText );
            eval = new OverlapsEvaluator( type,
                                          isNegated,
                                          params,
                                          parameterText );
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
        return Target.HANDLE;
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
     * Implements the 'overlaps' evaluator itself
     */
    public static class OverlapsEvaluator extends BaseEvaluator {
        private static final long serialVersionUID = 510l;

        private long              minDev, maxDev;
        private String            paramText;

        public OverlapsEvaluator() {
        }

        public OverlapsEvaluator(final ValueType type,
                                 final boolean isNegated,
                                 final Long[] parameters, 
                                 final String paramText) {
            super( type,
                   isNegated ? OVERLAPS_NOT : OVERLAPS );
            this.paramText = paramText;
            this.setParameters( parameters );
        }

        public void readExternal(ObjectInput in) throws IOException,
                                                ClassNotFoundException {
            super.readExternal( in );
            minDev = in.readLong();
            maxDev = in.readLong();
            paramText = (String) in.readObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal( out );
            out.writeLong( minDev );
            out.writeLong( maxDev );
            out.writeObject( paramText );
        }

        @Override
        public Object prepareLeftObject(InternalFactHandle handle) {
            return handle;
        }

        @Override
        public boolean isTemporal() {
            return true;
        }

        @Override
        public Interval getInterval() {
            if ( this.getOperator().isNegated() ) {
                return new Interval( Interval.MIN,
                                     Interval.MAX );
            }
            return new Interval( Interval.MIN,
                                 0 );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final InternalReadAccessor extractor,
                                final Object object1,
                                final FieldValue object2) {
            throw new RuntimeDroolsException( "The 'overlaps' operator can only be used to compare one event to another, and never to compare to literal constraints." );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context,
                                           final Object left) {

            if ( context.rightNull ) {
                return false;
            }
            
            long leftStartTS = 0;
            long leftEndTS = 0;
            long rightStartTS = 0;
            long rightEndTS = 0;
            
            DefaultFactHandle leftFH = (DefaultFactHandle) left;
            
            if (leftFH instanceof EventFactHandle) {
                leftStartTS = ((EventFactHandle) leftFH).getStartTimestamp();
                leftEndTS = ((EventFactHandle) leftFH).getEndTimestamp();
            }
            else {
            	Object leftFact =  workingMemory.getObject(leftFH);
            	if (leftFact instanceof SituationType) {
            		leftStartTS = ((SituationType) leftFact).getActivation().getTimestamp();
        			
            		if (!((SituationType) leftFact).isActive()) {
            			leftEndTS = ((SituationType) leftFact).getDeactivation().getTimestamp();
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
            	if (rightFact instanceof SituationType) {
        			rightStartTS = ((SituationType) rightFact).getActivation().getTimestamp();
        			
        			if (!((SituationType) rightFact).isActive()) {
        				rightEndTS = ((SituationType) rightFact).getDeactivation().getTimestamp();
        			}

            	}
            }
                        
            if (rightEndTS==0) {
            	return this.getOperator().isNegated() ^ (rightStartTS < leftStartTS);         	
            } else {
                long dist = rightEndTS - leftStartTS;
            	return this.getOperator().isNegated() ^ (rightStartTS < leftStartTS &&
            											 rightEndTS < leftEndTS &&
            											 dist >= this.minDev && dist <= this.maxDev);   	
            }  
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context,
                                          final Object right) {
            if ( context.extractor.isNullValue( workingMemory,
                                                right ) ) {
                return false;
            }
            
            long leftStartTS = 0;
            long leftEndTS = 0;
            long rightStartTS = 0;
            long rightEndTS = 0;
            
            DefaultFactHandle leftFH = (DefaultFactHandle) ((ObjectVariableContextEntry) context).left;
            
            if (leftFH instanceof EventFactHandle) {
                leftStartTS = ((EventFactHandle) leftFH).getStartTimestamp();
                leftEndTS = ((EventFactHandle) leftFH).getEndTimestamp();
            }
            else {
            	Object leftFact =  workingMemory.getObject(leftFH);
            	if (leftFact instanceof SituationType) {
            		leftStartTS = ((SituationType) leftFact).getActivation().getTimestamp();
            		if (!((SituationType) leftFact).isActive()) {
            			leftEndTS = ((SituationType) leftFact).getDeactivation().getTimestamp();
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
            	if (rightFact instanceof SituationType) {
        			rightStartTS = ((SituationType) rightFact).getActivation().getTimestamp();
        			if (!((SituationType) rightFact).isActive()) {
        				rightEndTS = ((SituationType) rightFact).getDeactivation().getTimestamp();
        			}

            	}
            }            
            
            if (rightEndTS==0) {
            	return this.getOperator().isNegated() ^ (rightStartTS < leftStartTS);         	
            } else {
                long dist = rightEndTS - leftStartTS;
            	return this.getOperator().isNegated() ^ (rightStartTS < leftStartTS &&
            											 rightEndTS < leftEndTS &&
            											 dist >= this.minDev && dist <= this.maxDev);   	
            }
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final InternalReadAccessor extractor1,
                                final Object object1,
                                final InternalReadAccessor extractor2,
                                final Object object2) {
            if ( extractor1.isNullValue( workingMemory,
                                         object1 ) ) {
                return false;
            }
            
            long obj1StartTS = -1;
            long obj1EndTS = -1;
            long obj2StartTS = -1;
            long obj2EndTS = -1;
                        
            DefaultFactHandle obj1FH = (DefaultFactHandle) object1;
            
            if (obj1FH instanceof EventFactHandle) {
            	obj1StartTS = ((EventFactHandle) obj1FH).getStartTimestamp();
            	obj1EndTS = ((EventFactHandle) obj1FH).getEndTimestamp();
            }
            else {
            	Object obj1Fact =  workingMemory.getObject(obj1FH);
            	if (obj1Fact instanceof SituationType) {
            		obj1StartTS = ((SituationType) obj1Fact).getActivation().getTimestamp();
        			if (!((SituationType) obj1Fact).isActive()) {
        				obj1EndTS = ((SituationType) obj1Fact).getDeactivation().getTimestamp();
        			}            		
            	}
            }

            DefaultFactHandle obj2FH = (DefaultFactHandle) object2;
            
            if (obj2FH instanceof EventFactHandle) {
            	obj2StartTS = ((EventFactHandle) obj2FH).getStartTimestamp();
            	obj2EndTS = ((EventFactHandle) obj2FH).getEndTimestamp();
            }
            else {
            	Object obj2Fact =  workingMemory.getObject(obj2FH);
            	if (obj2Fact instanceof SituationType) {
            		obj2StartTS = ((SituationType) obj2Fact).getActivation().getTimestamp();
        			if (!((SituationType) obj2Fact).isActive()) {
        				obj2EndTS = ((SituationType) obj2Fact).getDeactivation().getTimestamp();
        			}
            	}
            }
            
            if (obj1EndTS==-1) {
            	return this.getOperator().isNegated() ^ (obj1StartTS < obj2StartTS);         	
            } else {
            	long dist = obj1EndTS - obj2StartTS;
                return this.getOperator().isNegated() ^ ( obj1StartTS < obj2StartTS &&
                										  obj1EndTS < obj2EndTS &&
                										  dist >= this.minDev && dist <= this.maxDev );   	
            }

        }

        public String toString() {
            return "overlaps[" + ( ( paramText != null ) ? paramText : "" ) + "]";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = super.hashCode();
            result = PRIME * result + (int) (maxDev ^ (maxDev >>> 32));
            result = PRIME * result + (int) (minDev ^ (minDev >>> 32));
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
            final OverlapsEvaluator other = (OverlapsEvaluator) obj;
            return maxDev == other.maxDev && minDev == other.minDev ;
        }

        /**
         * This methods sets the parameters appropriately.
         *
         * @param parameters
         */
        private void setParameters(Long[] parameters) {
            if ( parameters == null || parameters.length == 0 ) {
                // open bounded range
                this.minDev = 1;
                this.maxDev = Long.MAX_VALUE;
            } else if ( parameters.length == 1 ) {
                // open bounded ranges
                this.minDev = 1;
                this.maxDev = parameters[0].longValue();
            } else if ( parameters.length == 2 ) {
                // open bounded ranges
                this.minDev = parameters[0].longValue();
                this.maxDev = parameters[1].longValue();
            } else {
                throw new RuntimeDroolsException( "[Overlaps Evaluator]: Not possible to use " + parameters.length + " parameters: '" + paramText + "'" );
            }
        }


    }

}
