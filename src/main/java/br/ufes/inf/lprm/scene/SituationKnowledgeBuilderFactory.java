package br.ufes.inf.lprm.scene;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;

import br.ufes.inf.lprm.scene.base.SituationHelper;

public class SituationKnowledgeBuilderFactory {
	
    public static KnowledgeBuilder newKnowledgeBuilder() {

    	KnowledgeBuilderConfiguration builderConf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        SituationHelper.setBuilderConfSituationAwareness(builderConf);     
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(builderConf);
        SituationHelper.setKnowledgeBuilderSituationAwareness(kbuilder);
        return kbuilder;
        
    }	

}
