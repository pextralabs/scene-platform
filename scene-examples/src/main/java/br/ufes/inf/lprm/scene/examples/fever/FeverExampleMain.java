package br.ufes.inf.lprm.scene.examples.fever;

import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import org.kie.api.KieServices;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

/**
 * This is a sample class to launch a rule.
 */

class RuleEngineThread extends Thread {	
	private KieSession ksession;
	public RuleEngineThread(KieSession ksession) {
		this.ksession = ksession;
	}
    public void run() {  	
    	this.ksession.fireUntilHalt(); 	
    }
}

public class FeverExampleMain {

    public static final void main(String[] args) {
        try {
        	
            // load up the knowledge base
			KieServices ks = KieServices.Factory.get();
			KieContainer kContainer = ks.getKieClasspathContainer();
			KieSession kSession = kContainer.newKieSession("br.ufes.inf.lprm.scene.examples.fever.session");//SituationKieBase.newKieSession(kContainer, "br.ufes.inf.lprm.scene.examples.fever.session");
            kSession.addEventListener(new SCENESessionListener());
            
            final RuleEngineThread eng = new RuleEngineThread(kSession);
			eng.start();

			FactType factType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.examples.fever", "Person");

			Object p1 = factType.newInstance();

			factType.getField("id").set(p1, 1);
			factType.getField("name").set(p1, "john");
			FactField temperature = factType.getField("temperature");

			temperature.set(p1, 37);

			
			FactHandle fh1 = kSession.insert(p1);
			
			while (true) {
				
				Thread.sleep(1000);
				temperature.set(p1, 38);
				kSession.update(fh1,  p1);
				
				Thread.sleep(3000);

				temperature.set(p1, 39);
				kSession.update(fh1,  p1);
	
				Thread.sleep(3000);

				temperature.set(p1, 40);
				kSession.update(fh1,  p1);
				
				Thread.sleep(3000);

				temperature.set(p1, 39);
				kSession.update(fh1,  p1);
	
				Thread.sleep(3000);

				temperature.set(p1, 37);
				kSession.update(fh1,  p1);
	
				Thread.sleep(3000);

				temperature.set(p1, 32);
				kSession.update(fh1,  p1);
				
				Thread.sleep(3000);

				temperature.set(p1, 31);
				kSession.update(fh1,  p1);
			
			}
						
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
