package br.ufes.inf.lprm.scene.examples.connectionswitch;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
//import org.drools.definition.KnowledgePackage;

import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import br.ufes.inf.lprm.scene.SituationKnowledgeBaseFactory;
import br.ufes.inf.lprm.scene.SituationKnowledgeBuilderFactory;
import br.ufes.inf.lprm.scene.base.SituationHelper;
//import org.drools.situation.example.model.Person;
import br.ufes.inf.lprm.scene.examples.shared.Device;
import br.ufes.inf.lprm.scene.examples.shared.Network;
import br.ufes.inf.lprm.scene.examples.shared.NetworkType;

/**
 * This is a sample class to launch a rule.
 */

class RuleEngineThread extends Thread {
	
	private StatefulKnowledgeSession ksession;
	
	public RuleEngineThread(StatefulKnowledgeSession ksession) {
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
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

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

    private static KnowledgeBase readKnowledgeBase() throws Exception {
 
    	KnowledgeBuilder kbuilder = SituationKnowledgeBuilderFactory.newKnowledgeBuilder();
    	
        kbuilder.add(ResourceFactory.newClassPathResource("br/ufes/inf/lprm/scene/examples/connectionswitch/SituationRules.drl"), ResourceType.DRL);
        
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (errors.size() > 0) {
            for (KnowledgeBuilderError error: errors) {
                System.err.println(error);
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }
        
        KnowledgeBase kbase = SituationKnowledgeBaseFactory.newKnowledgeBase(kbuilder);
        return kbase;
    }

}
