package br.ufes.inf.lprm.scene.base.evaluators.definition;

import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.TimeIntervalParser;
import org.drools.core.spi.Evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hborjaille on 9/8/16.
 */
public class StartsEvaluatorDefinition extends org.drools.core.base.evaluators.StartsEvaluatorDefinition {

    private Map<String, br.ufes.inf.lprm.scene.base.evaluators.implementation.StartsEvaluator> cache = Collections.emptyMap();

    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText,
                                  final Target left,
                                  final Target right ) {
        if ( this.cache == Collections.EMPTY_MAP ) {
            this.cache = new HashMap<String, br.ufes.inf.lprm.scene.base.evaluators.implementation.StartsEvaluator>();
        }
        String key = isNegated + ":" + parameterText;
        br.ufes.inf.lprm.scene.base.evaluators.implementation.StartsEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            long[] params = TimeIntervalParser.parse( parameterText );
            eval = new br.ufes.inf.lprm.scene.base.evaluators.implementation.StartsEvaluator( type,
                    isNegated,
                    params,
                    parameterText );
            this.cache.put( key,
                    eval );
        }
        return eval;
    }
}
