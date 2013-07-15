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
 * <p>The implementation of the 'coincides' evaluator definition.</p>
 * 
 * <p>The <b><code>coincides</code></b> evaluator correlates two events and matches when both
 * happen at the same time. Optionally, the evaluator accept thresholds for the distance between
 * events' start and finish timestamps.</p> 
 * 
 * <p>Lets look at an example:</p>
 * 
 * <pre>$eventA : EventA( this coincides $eventB )</pre>
 *
 * <p>The previous pattern will match if and only if the start timestamps of both $eventA
 * and $eventB are the same AND the end timestamp of both $eventA and $eventB also are
 * the same.</p>
 * 
 * <p>Optionally, this operator accepts one or two parameters. These parameters are the thresholds
 * for the distance between matching timestamps. If only one paratemer is given, it is used for 
 * both start and end timestamps. If two parameters are given, then the first is used as a threshold
 * for the start timestamp and the second one is used as a threshold for the end timestamp. In other
 * words:</p>
 * 
 * <pre> $eventA : EventA( this coincides[15s, 10s] $eventB ) </pre> 
 * 
 * Above pattern will match if and only if:
 * 
 * <pre>
 * abs( $eventA.startTimestamp - $eventB.startTimestamp ) <= 15s &&
 * abs( $eventA.endTimestamp - $eventB.endTimestamp ) <= 10s 
 * </pre>
 * 
 * <p><b>NOTE:</b> it makes no sense to use negative interval values for the parameters and the 
 * engine will raise an error if that happens.</p>
 */
public class CoincidesEvaluatorDefinition
    implements
    EvaluatorDefinition {

    public static final Operator            COINCIDES     = Operator.addOperatorToRegistry( "coincides",
                                                                                            false );
    public static final Operator            COINCIDES_NOT = Operator.addOperatorToRegistry( "coincides",
                                                                                            true );

    private static final String[]           SUPPORTED_IDS = {COINCIDES.getOperatorString()};

    private Map<String, CoincidesEvaluator> cache         = Collections.emptyMap();
    private volatile TimeIntervalParser     parser        = new TimeIntervalParser();

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        cache = (Map<String, CoincidesEvaluator>) in.readObject();
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
            this.cache = new HashMap<String, CoincidesEvaluator>();
        }
        String key = left + ":" + right + ":" + isNegated + ":" + parameterText;
        CoincidesEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            Long[] params = parser.parse( parameterText );
            eval = new CoincidesEvaluator( type,
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
    public static class CoincidesEvaluator extends BaseEvaluator {
        private static final long serialVersionUID = 510l;

        private long              startDev;
        private long              endDev;
        private String            paramText;
        private boolean           unwrapLeft;
        private boolean           unwrapRight;

        public CoincidesEvaluator() {
        }

        public CoincidesEvaluator(final ValueType type,
                                  final boolean isNegated,
                                  final Long[] parameters,
                                  final String paramText,
                                  final boolean unwrapLeft,
                                  final boolean unwrapRight) {
            super( type,
                   isNegated ? COINCIDES_NOT : COINCIDES );
            this.paramText = paramText;
            this.unwrapLeft = unwrapLeft;
            this.unwrapRight = unwrapRight;
            this.setParameters( parameters );
        }

        public void readExternal(ObjectInput in) throws IOException,
                                                ClassNotFoundException {
            super.readExternal( in );
            startDev = in.readLong();
            endDev = in.readLong();
            unwrapLeft = in.readBoolean();
            unwrapRight = in.readBoolean();
            paramText = (String) in.readObject();
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal( out );
            out.writeLong( startDev );
            out.writeLong( endDev );
            out.writeBoolean( unwrapLeft );
            out.writeBoolean( unwrapRight );
            out.writeObject( paramText );
        }

        @Override
        public Object prepareLeftObject(InternalFactHandle handle) {
            return unwrapLeft ? handle.getObject() : handle;
        }

        @Override
        public Object prepareRightObject(InternalFactHandle handle) {
            return unwrapRight ? handle.getObject() : handle;
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
            return new Interval( 0,
                                 0 );
        }

        public boolean evaluate(InternalWorkingMemory workingMemory,
                                final InternalReadAccessor extractor,
                                final Object object1,
                                final FieldValue object2) {
            throw new RuntimeDroolsException( "The 'coincides' operator can only be used to compare one event to another, and never to compare to literal constraints." );
        }

        public boolean evaluateCachedRight(InternalWorkingMemory workingMemory,
                                           final VariableContextEntry context,
                                           final Object left) {
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
            	if (leftFact instanceof SituationType) {
            		leftStartTS = ((SituationType) leftFact).getActivation().getTimestamp();
            		//'started' is not applicable when situationB not finished
        			if (!((SituationType) leftFact).isActive()) {
        				leftEndTS = ((SituationType) leftFact).getDeactivation().getTimestamp();
        			}  else return false;
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
        			} else return false;
            	}
            }

            long distStart = Math.abs( rightStartTS - leftStartTS );
            long distEnd = Math.abs( rightEndTS - leftEndTS );
            return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd <= this.endDev);
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
            		//'coincides' is not applicable when situationB not finished
        			if (!((SituationType) leftFact).isActive()) {
        				leftEndTS = ((SituationType) leftFact).getDeactivation().getTimestamp();
        			}  else return false;
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
        			} else return false;
            	}
            }            

            long distStart = Math.abs( rightStartTS - leftStartTS );
            long distEnd = Math.abs( rightEndTS - leftEndTS );
            return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd <= this.endDev);
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
        			} else return false;   		
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
        			} else return false;           		      			
            	}
            } 
            
            long distStart = Math.abs( obj1StartTS - obj2StartTS );
            long distEnd = Math.abs( obj1EndTS - obj2EndTS);
            return this.getOperator().isNegated() ^ (distStart <= this.startDev && distEnd <= this.endDev);
        }

        public String toString() {
            return "coincides[" + startDev + ", " + endDev + "]";
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = super.hashCode();
            result = PRIME * result + (int) (endDev ^ (endDev >>> 32));
            result = PRIME * result + (int) (startDev ^ (startDev >>> 32));
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
            final CoincidesEvaluator other = (CoincidesEvaluator) obj;
            return endDev == other.endDev && startDev == other.startDev;
        }

        /**
         * This methods sets the parameters appropriately.
         *
         * @param parameters
         */
        private void setParameters(Long[] parameters) {
            if ( parameters == null || parameters.length == 0 ) {
                // open bounded range
                this.startDev = 0;
                this.endDev = 0;
                return;
            } else {
                for ( Long param : parameters ) {
                    if ( param.longValue() < 0 ) {
                        throw new RuntimeDroolsException( "[Coincides Evaluator]: negative values not allowed for temporal distance thresholds: '" + paramText + "'" );
                    }
                }
                if ( parameters.length == 1 ) {
                    // same deviation for both
                    this.startDev = parameters[0].longValue();
                    this.endDev = parameters[0].longValue();
                } else if ( parameters.length == 2 ) {
                    // different deviation 
                    this.startDev = parameters[0].longValue();
                    this.endDev = parameters[1].longValue();
                } else {
                    throw new RuntimeDroolsException( "[Coincides Evaluator]: Not possible to have more than 2 parameters: '" + paramText + "'" );
                }
            }
        }

    }

}
