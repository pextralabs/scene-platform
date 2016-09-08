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

import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.EvaluatorDefinition;
import org.drools.core.base.evaluators.Operator;
import org.drools.core.spi.Evaluator;

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
 * <p>The implementation of the <code>includes</code> evaluator definition.</p>
 * 
 * <p>The <b><code>includes</code></b> evaluator correlates two events and matches when the event 
 * being correlated happens during the current event. It is the symmetrical opposite of <code>during</code>
 * evaluator.</p> 
 * 
 * <p>Lets look at an example:</p>
 * 
 * <pre>$eventA : EventA( this includes $eventB )</pre>
 *
 * <p>The previous pattern will match if and only if the $eventB starts after $eventA starts and finishes
 * before $eventA finishes. In other words:</p>
 * 
 * <pre> $eventA.startTimestamp < $eventB.startTimestamp <= $eventB.endTimestamp < $eventA.endTimestamp </pre>
 * 
 * <p>The <b><code>includes</code></b> operator accepts 1, 2 or 4 optional parameters as follow:</p>
 * 
 * <ul><li>If one value is defined, this will be the maximum distance between the start timestamp of both
 * event and the maximum distance between the end timestamp of both events in order to operator match. Example:</li></lu>
 * 
 * <pre>$eventA : EventA( this includes[ 5s ] $eventB )</pre>
 * 
 * Will match if and only if:
 * 
 * <pre> 
 * 0 < $eventB.startTimestamp - $eventA.startTimestamp <= 5s &&
 * 0 < $eventA.endTimestamp - $eventB.endTimestamp <= 5s
 * </pre>
 * 
 * <ul><li>If two values are defined, the first value will be the minimum distance between the timestamps
 * of both events, while the second value will be the maximum distance between the timestamps of both events. 
 * Example:</li></lu>
 * 
 * <pre>$eventA : EventA( this includes[ 5s, 10s ] $eventB )</pre>
 * 
 * Will match if and only if:
 * 
 * <pre> 
 * 5s <= $eventB.startTimestamp - $eventA.startTimestamp <= 10s &&
 * 5s <= $eventA.endTimestamp - $eventB.endTimestamp <= 10s
 * </pre>
 * 
 * <ul><li>If four values are defined, the first two values will be the minimum and maximum distances between the 
 * start timestamp of both events, while the last two values will be the minimum and maximum distances between the 
 * end timestamp of both events. Example:</li></lu>
 * 
 * <pre>$eventA : EventA( this includes[ 2s, 6s, 4s, 10s ] $eventB )</pre>
 * 
 * Will match if and only if:
 * 
 * <pre> 
 * 2s <= $eventB.startTimestamp - $eventA.startTimestamp <= 6s &&
 * 4s <= $eventA.endTimestamp - $eventB.endTimestamp <= 10s
 * </pre>
 */
public class IncludesEvaluatorDefinition
    implements
    EvaluatorDefinition {

    public static final Operator           INCLUDES      = Operator.addOperatorToRegistry( "includes",
                                                                                           false );
    public static final Operator           INCLUDES_NOT  = Operator.addOperatorToRegistry( "includes",
                                                                                           true );

    private static final String[]          SUPPORTED_IDS = {INCLUDES.getOperatorString()};

    private Map<String, IncludesEvaluator> cache         = Collections.emptyMap();
    private volatile TimeIntervalParser    parser        = new TimeIntervalParser();

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        cache = (Map<String, IncludesEvaluator>) in.readObject();
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
            this.cache = new HashMap<String, IncludesEvaluator>();
        }
        String key = isNegated + ":" + parameterText;
        IncludesEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            Long[] params = parser.parse( parameterText );
            eval = new IncludesEvaluator( type,
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
     * Implements the 'includes' evaluator itself
     */
    public static class IncludesEvaluator extends BaseEvaluator {
        private static final long serialVersionUID = 510l;

        private long              startMinDev, startMaxDev;
        private long              endMinDev, endMaxDev;
        private String            paramText;

        public IncludesEvaluator() {
        }

        public IncludesEvaluator(final ValueType type,
                                 final boolean isNegated,
                                 final Long[] parameters,
                                 final String paramText) {
            super( type,
                   isNegated ? INCLUDES_NOT : INCLUDES );
            this.paramText = paramText;
            this.setParameters( parameters );
        }

        public void readExternal(ObjectInput in) throws IOException,
                                                ClassNotFoundException {
            super.readExternal( in );
            startMinDev = in.readLong();
            startMaxDev = in.readLong();
            endMinDev = in.readLong();
            endMaxDev = in.readLong();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal( out );
            out.writeLong( startMinDev );
            out.writeLong( startMaxDev );
            out.writeLong( endMinDev );
            out.writeLong( endMaxDev );
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
            throw new RuntimeDroolsException( "The 'includes' operator can only be used to compare one event to another, and never to compare to literal constraints." );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context,
                                           final Object left) {

            if ( context.rightNull ) {
                return false;
            }
            
            //A includes B
            //right -> Fact A
            //left	-> Fact B
            
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
            	if (leftFact instanceof SituationType) {
            		leftStartTS = ((SituationType) leftFact).getActivation().getTimestamp();
        			//includes is not applicable when situationB not finished
        			if (!((SituationType) leftFact).isActive()) {
        				leftEndTS = ((SituationType) leftFact).getDeactivation().getTimestamp();
        			} else return false;         		
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
            long distStart = leftStartTS - rightStartTS;       
            if (rightEndTS==(-1)) {
            	return this.getOperator().isNegated() ^ (distStart >= this.startMinDev && distStart <= this.startMaxDev);              
            } else {
                long distEnd = rightEndTS - leftEndTS;
                return this.getOperator().isNegated() ^ (distStart >= this.startMinDev && distStart <= this.startMaxDev && 
                										 distEnd >= this.endMinDev && distEnd <= this.endMaxDev);
            }
        }

        public boolean evaluateCachedLeft(InternalWorkingMemory workingMemory,
                                          final VariableContextEntry context,
                                          final Object right) {
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
            	if (leftFact instanceof SituationType) {
            		leftStartTS = ((SituationType) leftFact).getActivation().getTimestamp();
        			//includes is not applicable when situationB not finished
        			if (!((SituationType) leftFact).isActive()) {
        				leftEndTS = ((SituationType) leftFact).getDeactivation().getTimestamp();
        			} else return false;
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
            
            long distStart = leftStartTS - rightStartTS;
            
            if (rightEndTS==(-1)) {
                return this.getOperator().isNegated() ^ (distStart >= this.startMinDev && distStart <= this.startMaxDev);              
            } else {
                long distEnd = rightEndTS - leftEndTS;
            	return this.getOperator().isNegated() ^ (distStart >= this.startMinDev && distStart <= this.startMaxDev && 
                										 distEnd >= this.endMinDev && distEnd <= this.endMaxDev);

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
            		//includes is not applicable when situationB is not finished
        			if (!((SituationType) obj2Fact).isActive()) {
        				obj2EndTS = ((SituationType) obj2Fact).getDeactivation().getTimestamp();
        			} else return false;
            	}
            }
            
            long distStart = obj2StartTS - obj1StartTS;        
            if (obj1EndTS==(-1)) {
                return this.getOperator().isNegated() ^ (distStart >= this.startMinDev && distStart <= this.startMaxDev);              
            } else {
                long distEnd = obj1EndTS - obj2EndTS;          	
                return this.getOperator().isNegated() ^ (distStart >= this.startMinDev && distStart <= this.startMaxDev && 
                										 distEnd >= this.endMinDev && distEnd <= this.endMaxDev);
            }
        }

        public String toString() {
            return "includes[" + startMinDev + ", " + startMaxDev + ", " + endMinDev + ", " + endMaxDev + "]";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = super.hashCode();
            result = PRIME * result + (int) (endMaxDev ^ (endMaxDev >>> 32));
            result = PRIME * result + (int) (endMinDev ^ (endMinDev >>> 32));
            result = PRIME * result + (int) (startMaxDev ^ (startMaxDev >>> 32));
            result = PRIME * result + (int) (startMinDev ^ (startMinDev >>> 32));
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
            final IncludesEvaluator other = (IncludesEvaluator) obj;
            return endMaxDev == other.endMaxDev && endMinDev == other.endMinDev && startMaxDev == other.startMaxDev && startMinDev == other.startMinDev;
        }

        /**
         * This methods sets the parameters appropriately.
         *
         * @param parameters
         */
        private void setParameters(Long[] parameters) {
            if ( parameters == null || parameters.length == 0 ) {
                // open bounded range
                this.startMinDev = 1;
                this.startMaxDev = Long.MAX_VALUE;
                this.endMinDev = 1;
                this.endMaxDev = Long.MAX_VALUE;
            } else if ( parameters.length == 1 ) {
                // open bounded ranges
                this.startMinDev = 1;
                this.startMaxDev = parameters[0].longValue();
                this.endMinDev = 1;
                this.endMaxDev = parameters[0].longValue();
            } else if ( parameters.length == 2 ) {
                // open bounded ranges
                this.startMinDev = parameters[0].longValue();
                this.startMaxDev = parameters[1].longValue();
                this.endMinDev = parameters[0].longValue();
                this.endMaxDev = parameters[1].longValue();
            } else if ( parameters.length == 4 ) {
                // open bounded ranges
                this.startMinDev = parameters[0].longValue();
                this.startMaxDev = parameters[1].longValue();
                this.endMinDev = parameters[2].longValue();
                this.endMaxDev = parameters[3].longValue();
            } else {
                throw new RuntimeDroolsException( "[During Evaluator]: Not possible to use " + parameters.length + " parameters: '" + paramText + "'" );
            }
        }

    }

}
