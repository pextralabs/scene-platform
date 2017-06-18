package br.ufes.inf.lprm.scene.examples.fever;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import br.ufes.inf.lprm.scene.examples.fever.entities.Person;
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
			KieSession kSession = kContainer.newKieSession("br.ufes.inf.lprm.scene.examples.fever.session");
            kSession.addEventListener(new SCENESessionListener());

			SceneApplication app = new SceneApplication("Fever", kSession);

            final RuleEngineThread eng = new RuleEngineThread(kSession);
			eng.start();

			//FactType factType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.examples.fever.entities", "Person");

			Person p1 = new Person();

			p1.setId(1);
			p1.setName("john");
			p1.getTemperature().setValue(37);

			Person p2 = new Person();
			p2.setId(2);
			p2.setName("isaac");
			p2.getTemperature().setValue(37);

			/*Object p1 = factType.newInstance();
			Object p2 = factType.newInstance();

			FactField id = factType.getField("id");
			FactField name = factType.getField("name");
			FactField temperature = factType.getField("temperature");

			id.set(p1, 1);
			name.set(p1, "john");
			temperature.set(p1, 37);

			id.set(p2, 2);
			name.set(p2, "isaac");
			temperature.set(p2, 37); */

			FactHandle fh1 = kSession.insert(p1);
			FactHandle fh2 = kSession.insert(p2);

			while (true) {
				
				Thread.sleep(1000);
				p1.getTemperature().setValue(38);
				p2.getTemperature().setValue(38);
				kSession.update(fh1,  p1);
				kSession.update(fh2,  p2);

				Thread.sleep(3000);

				p1.getTemperature().setValue(39);
				p2.getTemperature().setValue(39);

				kSession.update(fh1,  p1);
				kSession.update(fh2,  p2);

				Thread.sleep(3000);

				p1.getTemperature().setValue(40);
				p2.getTemperature().setValue(40);

				kSession.update(fh1,  p1);
				kSession.update(fh2,  p2);

				Thread.sleep(3000);

				p1.getTemperature().setValue(39);
				p2.getTemperature().setValue(39);

				kSession.update(fh1,  p1);
				kSession.update(fh2,  p2);

				Thread.sleep(3000);

				p1.getTemperature().setValue(37);
				p2.getTemperature().setValue(37);

				kSession.update(fh1,  p1);
				kSession.update(fh2,  p2);

				Thread.sleep(3000);

				p1.getTemperature().setValue(32);
				p2.getTemperature().setValue(32);

				kSession.update(fh1,  p1);
				kSession.update(fh2,  p2);

				Thread.sleep(3000);

				p1.getTemperature().setValue(31);
				p2.getTemperature().setValue(31);

				kSession.update(fh1,  p1);
				kSession.update(fh2,  p2);

			}
						
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
