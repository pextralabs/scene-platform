package br.ufes.inf.lprm.scene.examples.message;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import br.ufes.inf.lprm.scene.base.SituationHelper;

/**
 * This is a sample class to launch a rule.
 */
public class DroolsTest {

    public static final void main(String[] args) {
        try {
            KnowledgeBase kbase = readKnowledgeBase();
            StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
            
            Engine engine = new Engine(ksession);
            engine.start();
            System.out.println("Server is ready.");
            ksession.insert(new Message("ola"));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    private static KnowledgeBase readKnowledgeBase() throws Exception {
    	
    	KnowledgeBuilderConfiguration builderConf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        SituationHelper.setBuilderConfSituationAwareness(builderConf);
    	KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(builderConf);
    	SituationHelper.setKnowledgeBuilderSituationAwareness(kbuilder);
        kbuilder.add(ResourceFactory.newClassPathResource("br/ufes/inf/lprm/scene/examples/message/Situation.drl"), ResourceType.DRL);
        SituationHelper.refactorSaliences(kbuilder);
        KnowledgeBuilderErrors errors = kbuilder.getErrors();
        if (errors.size() > 0) {
            for (KnowledgeBuilderError error: errors) {
                System.err.println(error);
            }
            throw new IllegalArgumentException("Could not parse knowledge.");
        }        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());        
        return kbase;
    }    
    
}
class Engine extends Thread {
	
	private StatefulKnowledgeSession ksession;
	
	public Engine(StatefulKnowledgeSession ksession) {
		this.ksession = ksession;
	}
	
	public void run () {
		ksession.fireUntilHalt();
	}
	
	public void stopEngine () {
		ksession.halt();
	}
}
	
