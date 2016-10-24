package br.ufes.inf.lprm.scene.examples.connectionswitch;

import br.ufes.inf.lprm.scene.examples.shared.Device;
import br.ufes.inf.lprm.scene.examples.shared.Network;
import br.ufes.inf.lprm.scene.examples.shared.NetworkType;
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

public class SituationOfSituationMain {

    public static final void main(String[] args) {
        try {
            // load up the knowledge base
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            KieSession ksession = kContainer.newKieSession("br.ufes.inf.lprm.scene.examples.connectionswitch.session");

            //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
            // go !

            final RuleEngineThread eng = new RuleEngineThread(ksession);
			eng.start();			
			
			Network wlan = new Network("Enschede", NetworkType.WLAN);			
			ksession.insert(wlan);			
			Network bt = new Network("Nemo", NetworkType.BLUETOOTH);		
			ksession.insert(bt);
			
			Device dev = new Device("Computer01");
			
			FactHandle fh = ksession.insert(dev);
			
			Thread.sleep(1000);
			
			dev.setConnection(wlan);
			ksession.update(fh, dev);
			
			Thread.sleep(3000);

			dev.setConnection(null);
			ksession.update(fh, dev);			

			Thread.sleep(1000);
			
			dev.setConnection(bt);
			ksession.update(fh, dev);				
			
			Thread.sleep(3000);
						
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
