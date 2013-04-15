# SCENE

* **Developing with Eclipse**

        $ mvn eclipse:eclipse

* **Building JAR File**

        $ mvn package

* **Using the API**

	A Situation specification is comprised by two artifats: a *SituationType* child class definition and a *Situation Rule* declaration. The 		*SituationType* definition structures the situation in terms of participant entity types for which individuals will be casted in a 			situation occurrence. The Situation API provides an annotation (@SituationRole) in order to tag the class fields representing entity 			types which play a well-defined role on the situation.

	A *situation type* declaration:

        public MySituation extends SituationType {
         
                @Role(label = "label1")
                private Entity role1;
                
                ...
                
                @Role(label = "labelN")
                private Entity roleN;
         
                ... 
                
                //GETTERS AND SETTERS
        }

* *Situation Rule*

	A *situation rule* defines which conditions must be satisfied in order to establish a situation type occurrence. In its LHS the condition 		patterns must be related to identifiers which represents situation roles as declared on the respective *SituationType* class. The framework 	binds *SituationRole*-annotated fields with Situation Rule's LHS identifiers (label binding) in order to automate situation 	instatiation.

	A *situation rule* declaration:

        rule "MySituationRule"
			@role(situation)
			@type(MySituation)
                when
                        label1: Entity(<constraint 1>, ... ,<constraint N>)
                        ...
						labelN: Entity(<constraint 1'>, ... ,<constraint N'>)
                then
                        SituationHelper.situationDetected(drools);
        end

	In the Situation Rule's consequence (RHS) the *situationDetected* API method must be called passing as argument the correct Situation Type 		class. In addition, the *drools* argument also passed into is a exclusive RHS object which comprises the rule activation context, 			containing, by example, the set of objects (facts) satisfying the rule instance.

* *Situation-Aware KnowledgeSession*

        //create new SituationKnowledgeBuilder
        KnowledgeBuilder kbuilder = SituationKnowledgeBuilderFactory.newKnowledgeBuilder();
    
        // ADD YOUR DRL RESOURCES
		
		//create new Situation-Aware Knowledge Base
        KnowledgeBase kbase = SituationKnowledgeBaseFactory.newKnowledgeBase(kbuilder);		
