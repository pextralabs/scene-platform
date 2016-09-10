package br.ufes.inf.lprm.scene.examples.fever;

import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import br.ufes.inf.lprm.scene.examples.shared.Person;
import org.kie.api.KieServices;
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
			
			Person p1 = new Person("john", 1);
						
			p1.setTemperature(37);
			
			FactHandle fh1 = kSession.insert(p1);
			
			while (true) {
				
				Thread.sleep(1000);
	
				p1.setTemperature(38);
				kSession.update(fh1,  p1);
				
				Thread.sleep(3000);
	
				p1.setTemperature(39);			
				kSession.update(fh1,  p1);
	
				Thread.sleep(3000);			
				
				p1.setTemperature(40);			
				kSession.update(fh1,  p1);
				
				Thread.sleep(3000);			
				
				p1.setTemperature(38);			
				kSession.update(fh1,  p1);
	
				Thread.sleep(3000);
				
				p1.setTemperature(37);			
				kSession.update(fh1,  p1);
	
				Thread.sleep(3000);				
				
				p1.setTemperature(32);			
				kSession.update(fh1,  p1);
				
				Thread.sleep(3000);				
				
				p1.setTemperature(31);			
				kSession.update(fh1,  p1);
			
			}
						
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
