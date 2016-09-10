package br.ufes.inf.lprm.scene.base.evaluators.definition;

import br.ufes.inf.lprm.scene.base.evaluators.implementation.SCENEBeforeEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.BeforeEvaluatorDefinition;
import org.drools.core.base.evaluators.Operator;
import org.drools.core.base.evaluators.TimeIntervalParser;
import org.drools.core.spi.Evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hborjaille on 9/7/16.
 */
public class SCENEBeforeEvaluatorDefinition extends BeforeEvaluatorDefinition {

    protected static final String afterOp = "after";

    public static Operator AFTER;
    public static Operator NOT_AFTER;

    private static String[] SUPPORTED_IDS;

    private Map<String, SCENEBeforeEvaluator> cache = Collections.emptyMap();

    static {
        if ( Operator.determineOperator( afterOp, false ) == null ) {
            AFTER = Operator.addOperatorToRegistry( afterOp, false );
            NOT_AFTER = Operator.addOperatorToRegistry( afterOp, true );
            SUPPORTED_IDS = new String[]{afterOp};
        }
    }

    @Override
    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText,
                                  final Target left,
                                  final Target right) {
        if ( this.cache == Collections.EMPTY_MAP ) {
            this.cache = new HashMap<String, SCENEBeforeEvaluator>();
        }
        String key = left + ":" + right + ":" + isNegated + ":" + parameterText;
        SCENEBeforeEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            long[] params = TimeIntervalParser.parse( parameterText );
            eval = new SCENEBeforeEvaluator( type,
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
}