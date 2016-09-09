package br.ufes.inf.lprm.scene;

import br.ufes.inf.lprm.scene.base.SituationHelper;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class SituationKieBase {
	
    public static KieSession newKieSession(KieContainer kContainer, String name) throws Exception {
    	SituationHelper.refactorSaliences(kContainer.getKieBase());
    	KieSession kSession = kContainer.newKieSession(name);
    	return kSession;
    }

}
