package br.ufes.inf.lprm.scene.base.evaluators;

import java.io.Serializable;

import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.ResourceType;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import br.ufes.inf.lprm.scene.SituationKnowledgeBaseFactory;
import br.ufes.inf.lprm.scene.SituationKnowledgeBuilderFactory;
import br.ufes.inf.lprm.situation.Role;
import br.ufes.inf.lprm.situation.SituationType;

import org.junit.Test;
import static org.junit.Assert.*;

class RuleEngineThread extends Thread {
	private StatefulKnowledgeSession ksession;	
	public RuleEngineThread(StatefulKnowledgeSession ksession) {
		this.ksession = ksession;
	}
    public void run() {	
    	this.ksession.fireUntilHalt();
    }
}

class Person implements Serializable {

	private static final long serialVersionUID = 1L;
	private int identifier;
	private String name;
	private int temperature;

	public Person(String name, int id) {
		this.setName(name);
		this.setIdentifier(id);
		this.setTemperature(37);
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public int getTemperature() {
		return temperature;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}	
}

class Fever extends SituationType {

	private static final long serialVersionUID = 1L;
	@Role
	private Person febrile;

	public void setFebrile(Person febrile) {
		this.febrile = febrile;
	}

	public Person getFebrile() {
		return febrile;
	}
}

class FeverAfterFever extends SituationType {

	private static final long serialVersionUID = 1L;
	@Role
	private Fever previous;
	@Role
	private Fever latter;

	public Fever getPrevious() {
		return previous;
	}
	public void setPrevious(Fever previous) {
		this.previous = previous;
	}		
	
	public Fever getLatter() {
		return latter;
	}
	public void setLatter(Fever latter) {
		this.latter = latter;
	}

}

public class AfterEvaluatorTest {
	
	private int detection_counter = 0;

	public class AfterEvaluatorListener extends DefaultAgendaEventListener {

		@Override
		public void afterActivationFired(final AfterActivationFiredEvent event) {
			
			if (event.getActivation().getRule().getName() == "FeverAfterFever") {
				detection_counter++;
				
				Fever f1 = (Fever) event.getActivation().getDeclarationValue("previous");
				Fever f2 = (Fever) event.getActivation().getDeclarationValue("latter");
								
				assertTrue("f2 after f1", f2.getActivation().getTimestamp() < f1.getDeactivation().getTimestamp());
				
			}
			
		}
	}	
	
	@Test
	public void Test() {
		
		try {
			KnowledgeBase kbase = readKnowledgeBase();
			StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            ksession.addEventListener(new AfterEvaluatorListener());
			final RuleEngineThread eng = new RuleEngineThread(ksession);
            eng.start();
            
            
            Person p = new Person("John", 1);
            FactHandle fh = ksession.insert(p);
            
            Thread.sleep(500);            

            //first fever starts
            p.setTemperature(38);
            ksession.update(fh, p);
            
            Thread.sleep(1000); 
			
          //first fever stops
            p.setTemperature(37);
            ksession.update(fh, p);			
			
            Thread.sleep(1000);
            
            //second fever starts
            p.setTemperature(38);
            ksession.update(fh, p);
            
            Thread.sleep(1000); 
			
          //second fever stops
            p.setTemperature(37);
            ksession.update(fh, p);
 
            Thread.sleep(1000);            
            
            eng.stop();
            
            assertEquals("single situation detected!", 1, detection_counter);
            
		}
		catch(Exception e) {
			
		}
        
	}
	
	private static KnowledgeBase readKnowledgeBase() throws Exception {
				
		KnowledgeBuilder kbuilder = SituationKnowledgeBuilderFactory.newKnowledgeBuilder();

	    kbuilder.add(ResourceFactory.newClassPathResource("br/ufes/inf/lprm/scene/base/evaluators/AfterEvaluatorTestRules.drl"), ResourceType.DRL);
	    
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
