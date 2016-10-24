package br.ufes.inf.lprm.scene.util;

import br.ufes.inf.lprm.scene.model.impl.SituationTypeImpl;
import br.ufes.inf.lprm.situation.model.Participation;
import br.ufes.inf.lprm.situation.model.Situation;
import br.ufes.inf.lprm.situation.model.SituationType;
import br.ufes.inf.lprm.situation.model.events.Activation;
import br.ufes.inf.lprm.situation.model.events.Deactivation;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.KnowledgeHelper;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;

public class SituationHelper {

	public static SituationType getSituationType(KieRuntime runtime, String typeName) {
		QueryResults results = runtime.getQueryResults("SituationType", new Object[] {typeName} );
		for (QueryResultsRow row: results ) {
			return (SituationType) row.get( "type" );
		}
		return null;
	}

	public static void situationDetected(KnowledgeHelper khelper) throws Exception {
		RuleImpl rule = khelper.getRule();

		String packageName = rule.getPackageName();
		String className = (String) rule.getMetaData().get("type");

        SituationType type = getSituationType(khelper.getKieRuntime(), packageName + '.' + className);

		OnGoingSituation ongoing = new OnGoingSituation(type,
                                                        khelper.getKieRuntime().getSessionClock().getCurrentTime(),
                                                        new SituationCast(khelper.getMatch(), type));

    	khelper.insertLogical(ongoing);
	}
	
	public static Situation activateSituation(KnowledgeHelper khelper, SituationCast cast, SituationType type, long timestamp) {

		KieRuntime runtime = khelper.getKieRuntime();

		long evn_timestamp = runtime.getSessionClock().getCurrentTime();
		Activation activation = new Activation(evn_timestamp);
		
		Situation situation = ((SituationTypeImpl) type).newInstance(activation, cast);
        activation.setSituation(situation);
		runtime.insert(activation);
		for (Participation participation: situation.getParticipations()) {
			runtime.insert(participation);
		}
		runtime.insert(situation);
		return situation;
	}
	
	public static void deactivateSituation(KnowledgeHelper khelper, Object sit) {
		long evn_timestamp = khelper.getKieRuntime().getSessionClock().getCurrentTime();
		Deactivation deactivation = new Deactivation(evn_timestamp);
		deactivation.setSituation((Situation) sit);

		((Situation) sit).setDeactivation(deactivation);
		
		khelper.getKieRuntime().insert(deactivation);
		khelper.getKieRuntime().update(khelper.getKieRuntime().getFactHandle(sit), sit);
	}
	
}
