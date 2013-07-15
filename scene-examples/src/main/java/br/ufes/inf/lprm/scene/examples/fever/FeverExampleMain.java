package br.ufes.inf.lprm.scene.examples.fever;

import java.util.Random;

import org.drools.KnowledgeBase;

import org.drools.builder.KnowledgeBuilder;

import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;

import org.drools.builder.ResourceType;

import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import br.ufes.inf.lprm.scene.SituationKnowledgeBaseFactory;
import br.ufes.inf.lprm.scene.SituationKnowledgeBuilderFactory;
import br.ufes.inf.lprm.scene.examples.shared.Person;

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

public class FeverExampleMain {

    public static final void main(String[] args) {
        try {
        	
            // load up the knowledge base
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            
            final RuleEngineThread eng = new RuleEngineThread(ksession);
			eng.start();			
			
			Person p1 = new Person("john", 1);
						
			p1.setTemperature(37);
			
			FactHandle fh1 = ksession.insert(p1);
			
			while (true) {
				
				Thread.sleep(1000);
	
				p1.setTemperature(38);
				ksession.update(fh1,  p1);
				
				Thread.sleep(3000);
	
				p1.setTemperature(39);			
				ksession.update(fh1,  p1);
	
				Thread.sleep(3000);			
				
				p1.setTemperature(40);			
				ksession.update(fh1,  p1);
				
				Thread.sleep(3000);			
				
				p1.setTemperature(38);			
				ksession.update(fh1,  p1);
	
				Thread.sleep(3000);
				
				p1.setTemperature(37);			
				ksession.update(fh1,  p1);			
	
				Thread.sleep(3000);				
				
				p1.setTemperature(32);			
				ksession.update(fh1,  p1);	
				
				Thread.sleep(3000);				
				
				p1.setTemperature(31);			
				ksession.update(fh1,  p1);
			
			}
						
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static KnowledgeBase readKnowledgeBase() throws Exception {
    	
    	KnowledgeBuilder kbuilder = SituationKnowledgeBuilderFactory.newKnowledgeBuilder();
    
        kbuilder.add(ResourceFactory.newClassPathResource("br/ufes/inf/lprm/scene/examples/fever/FeverSituation.drl"), ResourceType.DRL);
        
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
