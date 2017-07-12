package br.ufes.inf.lprm.scene.examples.fever;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import br.ufes.inf.lprm.scene.examples.fever.entities.Person;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
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



public class FeverExampleMain {

	/*
			CtMethod cm = tvce.getDeclaredMethod("updateFromTuple");
			cm.setBody(
							"{\n" +
							"            this.tuple = $2;\n" +
							"            this.workingMemory = $1;\n" +
							"            if ( this.declaration.getExtractor().isSelfReference() ) {\n" +
							"            \torg.drools.core.common.InternalFactHandle fh = $2.get( this.declaration );\n" +

							"              if (fh instanceof org.drools.core.common.EventFactHandle) {\n" +
							"                org.drools.core.common.EventFactHandle efh = ((org.drools.core.common.EventFactHandle) fh);\n" +
							"                this.startTS = efh.getStartTimestamp();\n" +
							"                this.endTS = efh.getEndTimestamp();               \n" +
							"              } else {\n" +
							"                Object obj = fh.getObject();\n" +
							"                if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
							"                \tbr.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;  \n" +
							"				\t\t\t\t\tthis.startTS = sit.getActivation().getTimestamp();\n" +
							"                    this.endTS = (!sit.isActive()) ? sit.getDeactivation().getTimestamp() : 0;\n" +
							"                }  \n" +
							"                \n" +
							"              }  \n" +
							"            } else {\n" +
							"              this.leftNull = this.declaration.getExtractor().isNullValue( $1,\n" +
							"                                                                           $2.getObject( this.declaration ) );\n" +
							"              if ( !leftNull ) { // avoid a NullPointerException\n" +
							"                  this.startTS = this.declaration.getExtractor().getLongValue( $1,\n" +
							"                                                                             $2.getObject( this.declaration ) );\n" +
							"                } else {\n" +
							"                    this.startTS = 0;\n" +
							"                }    \n" +
							"              endTS = startTS;\n" +
							"            }\n" +
							"}"

			);*/


	private static final String __LSRECE_GET_TIMESTAMP_FROM_TUPLE =
			"{" +
			"	org.drools.core.common.InternalFactHandle fh = $1.get( this.declaration );\n" +
			"	if (fh instanceof org.drools.core.common.EventFactHandle) {\n" +
			"		return ((org.drools.core.common.EventFactHandle) fh).getStartTimestamp();\n" +
			"	} else {\n" +
			"		Object obj = fh.getObject();\n" +
			"   	if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
			"			br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;  \n" +
			"			return sit.getActivation().getTimestamp();\n" +
			"		} else {\n" +
			"			return 0L;\n" +
			"		}\n" +
			"	}\n" +
			"}";
	private static final String __LSRECE_GET_TIMESTAMP_FROM_FACT_HANDLE =
			"{" +
			"	org.drools.core.common.InternalFactHandle fh = $1;\n" +
			"	if (fh instanceof org.drools.core.common.EventFactHandle) {\n" +
			"		return ((org.drools.core.common.EventFactHandle) fh).getEndTimestamp();\n" +
			"	} else {\n" +
			"		Object obj = fh.getObject();\n" +
			"   	if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
			"			br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;  \n" +
			"			return (!sit.isActive()) ? sit.getDeactivation().getTimestamp() : 0L;\n" +
			"		} else {\n" +
			"			return 0L;\n" +
			"		}\n" +
			"	}\n" +
			"}";

	private static final String __LERSCE_GET_TIMESTAMP_FROM_TUPLE =
					"{" +
					"	org.drools.core.common.InternalFactHandle fh = $1.get( this.declaration );\n" +
					"	if (fh instanceof org.drools.core.common.EventFactHandle) {\n" +
					"		return ((org.drools.core.common.EventFactHandle) fh).getEndTimestamp();\n" +
					"	} else {\n" +
					"		Object obj = fh.getObject();\n" +
					"   	if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
					"			br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;  \n" +
					"			System.out.println((!sit.isActive()) ? sit.getDeactivation().getTimestamp() : 0L ); " +
					"			return (!sit.isActive()) ? sit.getDeactivation().getTimestamp() : 0L;" +
					"		} else {\n" +
					"			return 0L;\n" +
					"		}\n" +
					"	}\n" +
					"}";
	private static final String __LERSCE_GET_TIMESTAMP_FROM_FACT_HANDLE =
					"{\n" +
					"	org.drools.core.common.InternalFactHandle fh = $1;\n" +
					"	if (fh instanceof org.drools.core.common.EventFactHandle) {\n" +
					"		return ((org.drools.core.common.EventFactHandle) fh).getStartTimestamp();\n" +
					"	} else {\n" +
					"		Object obj = fh.getObject();\n" +
					"   	if (obj instanceof br.ufes.inf.lprm.situation.model.Situation) {\n" +
					"			br.ufes.inf.lprm.situation.model.Situation sit = (br.ufes.inf.lprm.situation.model.Situation) obj;  \n" +
					"			System.out.println(); System.out.println(sit.getActivation().getTimestamp());" +
					"			return sit.getActivation().getTimestamp();\n" +
					"		} else {\n" +
					"			return 0L;\n" +
					"		}\n" +
					"	}\n" +
					"}";

    public static final void main(String[] args) {
        try {
        	
            // load up the knowledge base
			KieServices ks = KieServices.Factory.get();
			KieContainer kContainer = ks.getKieClasspathContainer();
			KieSession kSession = kContainer.newKieSession("br.ufes.inf.lprm.scene.examples.fever.session");
            kSession.addEventListener(new SCENESessionListener());




			//CtClass tvce = pool.get("org.drools.core.rule.VariableRestriction$TemporalVariableContextEntry");
			ClassPool pool = ClassPool.getDefault();

			CtMethod getTimestampFromTuple, getTimestampFromFactHandle;

			CtClass lersce = pool.get("org.drools.core.rule.VariableRestriction$LeftEndRightStartContextEntry");
			getTimestampFromTuple 		= lersce.getDeclaredMethod("getTimestampFromTuple");
			getTimestampFromFactHandle = lersce.getDeclaredMethod("getTimestampFromFactHandle");
			getTimestampFromTuple.setBody(__LERSCE_GET_TIMESTAMP_FROM_TUPLE);
			getTimestampFromFactHandle.setBody(__LERSCE_GET_TIMESTAMP_FROM_FACT_HANDLE);
			lersce.toClass();

			CtClass lsrece = pool.get("org.drools.core.rule.VariableRestriction$LeftStartRightEndContextEntry");
			getTimestampFromTuple 		= lsrece.getDeclaredMethod("getTimestampFromTuple");
			getTimestampFromFactHandle = lsrece.getDeclaredMethod("getTimestampFromFactHandle");
			getTimestampFromTuple.setBody(__LSRECE_GET_TIMESTAMP_FROM_TUPLE);
			getTimestampFromFactHandle.setBody(__LSRECE_GET_TIMESTAMP_FROM_FACT_HANDLE);
			lsrece.toClass();

			SceneApplication app = new SceneApplication("Fever", kSession);

            final RuleEngineThread eng = new RuleEngineThread(kSession);
			eng.start();

			//FactType factType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.examples.fever.entities", "Person");

			Person p1 = new Person();

			p1.setId(1);
			p1.setName("john");
			p1.getTemperature().setValue(37);
			FactHandle fh1 = kSession.insert(p1);


			Thread.sleep(8000);

			p1.getTemperature().setValue(38);
			kSession.update(fh1, p1);

			Thread.sleep(3000);

			p1.getTemperature().setValue(37);
			kSession.update(fh1, p1);


			while (true);

			/*while (true) {
				
				Thread.sleep(1000);
				p1.getTemperature().setValue(38);
				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(39);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(40);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(39);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(37);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(32);

				kSession.update(fh1,  p1);

				Thread.sleep(3000);

				p1.getTemperature().setValue(31);

				kSession.update(fh1,  p1);

			}*/
						
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
