package br.ufes.inf.lprm.scene.base.evaluators.definition;

import br.ufes.inf.lprm.scene.base.evaluators.implementation.SCENEStartedByEvaluator;
import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.StartedByEvaluatorDefinition;
import org.drools.core.base.evaluators.TimeIntervalParser;
import org.drools.core.spi.Evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hborjaille on 9/8/16.
 */
public class SCENEStartedByEvaluatorDefinition extends StartedByEvaluatorDefinition {

    private Map<String, SCENEStartedByEvaluator> cache = Collections.emptyMap();

    @Override
    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText,
                                  final Target left,
                                  final Target right ) {
        if ( this.cache == Collections.EMPTY_MAP ) {
            this.cache = new HashMap<String, SCENEStartedByEvaluator>();
        }
        String key = isNegated + ":" + parameterText;
        SCENEStartedByEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            long[] params = TimeIntervalParser.parse( parameterText );
            eval = new SCENEStartedByEvaluator( type,
                    isNegated,
                    params,
                    parameterText );
            this.cache.put( key,
                    eval );
        }
        return eval;
    }
}
