package br.ufes.inf.lprm.scene.base.evaluators.definition;

import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.TimeIntervalParser;
import org.drools.core.spi.Evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import br.ufes.inf.lprm.scene.base.evaluators.implementation.AfterEvaluator;

public class AfterEvaluatorDefinition extends org.drools.core.base.evaluators.AfterEvaluatorDefinition {

    private Map<String, br.ufes.inf.lprm.scene.base.evaluators.implementation.AfterEvaluator> cache = Collections.emptyMap();

    @Override
    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText,
                                  final Target left,
                                  final Target right) {
        if ( this.cache == Collections.EMPTY_MAP ) {
            this.cache = new HashMap<String, br.ufes.inf.lprm.scene.base.evaluators.implementation.AfterEvaluator>();
        }
        String key = left + ":" + right + ":" + isNegated + ":" + parameterText;
        br.ufes.inf.lprm.scene.base.evaluators.implementation.AfterEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            long[] params = TimeIntervalParser.parse( parameterText );
            eval = new br.ufes.inf.lprm.scene.base.evaluators.implementation.AfterEvaluator( type,
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
