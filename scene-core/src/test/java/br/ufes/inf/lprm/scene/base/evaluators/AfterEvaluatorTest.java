package br.ufes.inf.lprm.scene.base.evaluators;

import br.ufes.inf.lprm.scene.SituationKieBase;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import br.ufes.inf.lprm.situation.Part;
import br.ufes.inf.lprm.situation.SituationType;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;

class RuleEngineThread extends Thread {
	private KieSession ksession;
	public RuleEngineThread(KieSession ksession) {
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
	@Part
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
	@Part
	private Fever previous;
	@Part
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
	
	@org.junit.Test
	public void Test() {
		
		try {
			KieServices ks = KieServices.Factory.get();
			KieContainer kContainer = ks.getKieClasspathContainer();
			KieSession ksession = SituationKieBase.newKieSession(kContainer, "br.ufes.inf.lprm.scene.examples.fever.session");
			ksession.addEventListener(new SCENESessionListener());
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

}
