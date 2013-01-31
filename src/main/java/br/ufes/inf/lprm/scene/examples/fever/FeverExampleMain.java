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

class FeverSimulationThread extends Thread {
	
	private String threadName;	
	private StatefulKnowledgeSession ksession;
	private int sampleSize;
	
	public FeverSimulationThread(StatefulKnowledgeSession ksession, String threadName, int size) {
		this.ksession = ksession;
		this.sampleSize = size;
		this.threadName = threadName;
	}

    public void run() {
    	
    	//setup sample

    	/*
        Person p = new Person("1");
        p.setTemperature(35);
        
        ksession.insert(p);
        
        
        p.setTemperature(38);
        
        ksession.update(ksession.getFactHandle(p), p);    	    	
        
        System.out.println("insertion time: " + ksession.getSessionClock().getCurrentTime());        ksession.insert(p);
        
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}            
        
        p.setTemperature(36);
        
        ksession.update(ksession.getFactHandle(p), p);    	    	
        System.out.println("update time: " + ksession.getSessionClock().getCurrentTime());
    	
    	*/
    	int index;
    	
    	FactHandle[] factArray = new FactHandle[sampleSize];
    	Person[] personArray = new Person[sampleSize];
    	
    	Person p;
    	
    	for (index = 0; index < sampleSize; index++) {
    		
    		p = new Person(threadName + "-" + String.valueOf(index), index);
    		p.setTemperature(36);
    		personArray[index] = p;
    		factArray[index] = ksession.insert(p);
    		
    		System.out.println(p.getName() + ": inserted");
    		
    	}

    	Random rand = new Random();
    	
    	while (true) {
    		
    		try {
				Thread.sleep(rand.nextInt(5) * 100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		
    		index = rand.nextInt(sampleSize);
    		
    		p = personArray[index];
    		int delta = (rand.nextInt(3) - 1);
    		p.setTemperature(p.getTemperature() + delta);

    		switch(delta) {
    		
				case -1: 
					System.out.println(p.getName() + ": " + "temperature decreasing to " + p.getTemperature());
					break;
				case  1: 
					System.out.println(p.getName() + ": " + "temperature increasing to " + p.getTemperature());
		
    		}    		    	
    		ksession.update(factArray[index], p);    		
    	}    	
    }		
}

public class FeverExampleMain {

    public static final void main(String[] args) {
        try {
        	
            // load up the knowledge base
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            //KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
            // go !
            
            
            
            final RuleEngineThread eng = new RuleEngineThread(ksession);
			eng.start();			
			
			Person p1 = new Person("john", 1);
			//Person p2 = new Person("mary", 2);
			
			p1.setTemperature(37);
			//p2.setTemperature(37);
			
			//FactHandle fh2 = ksession.insert(p2);		
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
    	System.out.println("aaaaa");
 	
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
