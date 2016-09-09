package br.ufes.inf.lprm.scene.base.evaluators.definition;

import br.ufes.inf.lprm.scene.base.evaluators.implementation.SCENEAfterEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.AfterEvaluatorDefinition;
import org.drools.core.base.evaluators.TimeIntervalParser;
import org.drools.core.spi.Evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SCENEAfterEvaluatorDefinition extends AfterEvaluatorDefinition {

    private Map<String, SCENEAfterEvaluator> cache = Collections.emptyMap();

    @Override
    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText,
                                  final Target left,
                                  final Target right) {
        if ( this.cache == Collections.EMPTY_MAP ) {
            this.cache = new HashMap<>();
        }
        String key = left + ":" + right + ":" + isNegated + ":" + parameterText;
        SCENEAfterEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            long[] params = TimeIntervalParser.parse( parameterText );
            eval = new SCENEAfterEvaluator( type,
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
