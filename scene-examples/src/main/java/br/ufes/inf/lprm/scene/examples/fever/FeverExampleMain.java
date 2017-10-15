package br.ufes.inf.lprm.scene.examples.fever;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import br.ufes.inf.lprm.scene.examples.fever.entities.Person;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.kie.api.KieServices;
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
			KieSession kSession = kContainer.newKieSession("br.ufes.inf.lprm.scene.examples.fever.session");
            kSession.addEventListener(new SCENESessionListener());

			SceneApplication app = new SceneApplication(ClassPool.getDefault(), kSession, "Fever");

            final RuleEngineThread eng = new RuleEngineThread(kSession);
			eng.start();

			//FactType factType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.examples.fever.entities", "Person");

			Person p1 = new Person();

			p1.setId(1);
			p1.setName("john");
			p1.getTemperature().setValue(37);
			FactHandle fh1 = kSession.insert(p1);


			Thread.sleep(8000);

			p1.getTemperature().setValue(38);
			kSession.update(fh1, p1);

			Thread.sleep(3000);

			p1.getTemperature().setValue(37);
			kSession.update(fh1, p1);


			while (true);

			/*while (true) {
				
				Thread.sleep(1000);
				p1.getTemperature().setValue(38);
				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(39);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(40);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(39);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(37);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(32);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(31);

				kSession.update(fh1,  p1);

			}*/
						
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
