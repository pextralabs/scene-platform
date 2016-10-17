package br.ufes.inf.lprm.scene.base.evaluators.definition;

import org.drools.core.base.ValueType;
import org.drools.core.base.evaluators.TimeIntervalParser;
import org.drools.core.spi.Evaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hborjaille on 10/12/16.
 */
public class MetByEvaluatorDefinition extends org.drools.core.base.evaluators.MetByEvaluatorDefinition {
    private Map<String, br.ufes.inf.lprm.scene.base.evaluators.implementation.MetByEvaluator> cache         = Collections.emptyMap();

    @Override
    public Evaluator getEvaluator(final ValueType type,
                                  final String operatorId,
                                  final boolean isNegated,
                                  final String parameterText,
                                  final Target left,
                                  final Target right ) {
        if ( this.cache == Collections.EMPTY_MAP ) {
            this.cache = new HashMap<String, br.ufes.inf.lprm.scene.base.evaluators.implementation.MetByEvaluator>();
        }
        String key = isNegated + ":" + parameterText;
        br.ufes.inf.lprm.scene.base.evaluators.implementation.MetByEvaluator eval = this.cache.get( key );
        if ( eval == null ) {
            long[] params = TimeIntervalParser.parse( parameterText );
            eval = new br.ufes.inf.lprm.scene.base.evaluators.implementation.MetByEvaluator( type,
                    isNegated,
                    params,
                    parameterText );
            this.cache.put( key,
                    eval );
        }
        return eval;
    }
}
