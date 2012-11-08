package br.ufes.inf.lprm.scene.base;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.drools.definition.KnowledgePackage;
import org.drools.definitions.rule.impl.RuleImpl;
import org.drools.base.SalienceInteger;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.EvaluatorOption;
import org.drools.io.ResourceFactory;
import org.drools.rule.Rule;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.drools.spi.KnowledgeHelper;


import br.ufes.inf.lprm.scene.base.evaluators.AfterEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.BeforeEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.CoincidesEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.DuringEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.FinishedByEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.FinishesEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.IncludesEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.OverlappedByEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.OverlapsEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.StartedByEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.evaluators.StartsEvaluatorDefinition;
import br.ufes.inf.lprm.scene.base.events.ActivateSituationEvent;
import br.ufes.inf.lprm.scene.base.events.DeactivateSituationEvent;

//@SuppressWarnings("restriction")
public class SituationHelper {
	
	private static boolean toSnapshot(KnowledgeHelper khelper, Class<?> type) {
		
		if (getSPM(khelper).getConfigurationHash().containsKey(type.getSimpleName())) {
			return getSPM(khelper).getConfigurationHash().get(type.getSimpleName()).getSnapshot();
		}
		return false;
	}
	
	public static SituationProfileManager getSPM(KnowledgeHelper khelper) {
		return (SituationProfileManager) khelper.getKnowledgeRuntime().getGlobal("SPM");		
	}
	
	public static void SetupSituationProfileManager(KnowledgeHelper khelper) throws Exception {
		
		SituationProfileManager spm;
		SituationProfile conf;
		
		spm = new SituationProfileManager();
	
		for (KnowledgePackage pkg: khelper.getKnowledgeRuntime().getKnowledgeBase().getKnowledgePackages()) {
			
			for (Rule rule:  getRulesFromPackage(pkg)) {
				
				if (rule.getMetaData().containsKey("role")) {
					
					if (rule.getMetaData().get("role").equals("situation")) {
						
						String type = (String) rule.getMetaData().get("type");
						conf = spm.getConfigurationHash().get(type);
						
						if (conf == null) {
							conf = new SituationProfile();
							spm.getConfigurationHash().put(type, conf);
						}
						
						conf.setType(Class.forName(rule.getPackageName() +"." + type));
						
						if (rule.getMetaData().containsKey("snapshot")) {
							
							if (rule.getMetaData().get("snapshot").equals("on")) {
								conf.setSnapshot(true);
							}
							else {
								if (rule.getMetaData().get("snapshot").equals("off")) {
									conf.setSnapshot(false);
								}
								else {
									throw new Exception();
								}
							}
							
						} else {
							conf.setSnapshot(false);
						}
						
						if (rule.getMetaData().containsKey("restore")) {
							
							if (rule.getMetaData().get("restore").equals("first")) {
								conf.setRestoreType(CastRestoreType.FIRST);
							}
							else {
								if (rule.getMetaData().get("restore").equals("last")) {
									conf.setRestoreType(CastRestoreType.LAST);
								}
								else {
									if (rule.getMetaData().get("restore").equals("stable")) {
										conf.setRestoreType(CastRestoreType.STABLE);
									}
									else {
										throw new Exception();
									}
								}
							}
							
						} else {
							conf.setRestoreType(CastRestoreType.FIRST);
						}						
					}				
				}
			}
		}
		khelper.getKnowledgeRuntime().setGlobal("SPM", spm);
	}
	
	public static void refactorSaliences(KnowledgeBuilder kbuilder) {
		
		KnowledgePackage situationBasePkg = null;
		Integer maxSalience = null;
		
		for (KnowledgePackage pkg: kbuilder. getKnowledgePackages()) {
			
			if (pkg.getName().equals("br.ufes.inf.lprm.base")) situationBasePkg = pkg;
			
			for (Rule rule:  getRulesFromPackage(pkg)) {
				
        		if (maxSalience != null) {
            		if (rule.getSalience().getValue(null, null, null) > maxSalience) {
            			maxSalience = new Integer(rule.getSalience().getValue(null, null, null));
            		}        			
        		} else maxSalience = new Integer(rule.getSalience().getValue(null, null, null));
				
			}			
		}
		if (situationBasePkg != null) {
			getRuleFromPackage(situationBasePkg, "SituationActivation").setSalience(new SalienceInteger(maxSalience + 1));			
			getRuleFromPackage(situationBasePkg, "SituationDeactivation").setSalience(new SalienceInteger(maxSalience + 2));
		}
		//System.out.println(maxSalience);
	}
	
	private static Collection<Rule> getRulesFromPackage(KnowledgePackage pkg) {
		
		LinkedList<Rule> collection = new LinkedList<Rule>();
		
		for (Object obj: pkg.getRules()) {
			
    		RuleImpl ruleImpl = (RuleImpl) obj;
    		
    		Field ruleField;
			try {
				ruleField = ruleImpl.getClass().getDeclaredField("rule");
				
	    		ruleField.setAccessible(true);
	    		Rule rule = (Rule) ruleField.get(ruleImpl);
	    		ruleField.setAccessible(false);			
				collection.add(rule);
				
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		return collection;
	}
	
	private static Rule getRuleFromPackage(KnowledgePackage pkg, String rulename) {
		
		Field ruleField;
		
		for (Object obj: pkg.getRules()) {
			
    		RuleImpl ruleImpl = (RuleImpl) obj;
    		
    		if (ruleImpl.getName().equals(rulename)) {
    			try {
    				
					ruleField = ruleImpl.getClass().getDeclaredField("rule");
					
		    		ruleField.setAccessible(true);
		    		Rule rule = (Rule) ruleField.get(ruleImpl);
		    		ruleField.setAccessible(false);
		    		
		    		return rule;
	    		
	    			} catch (SecurityException e) {
	    				e.printStackTrace();
	    			} catch (NoSuchFieldException e) {
	    				e.printStackTrace();
	    			} catch (IllegalArgumentException e) {
	    				e.printStackTrace();
	    			} catch (IllegalAccessException e) {
	    				e.printStackTrace();
	    			}	    		
    		}			
		}
		return null;
	}
	
	public static void setBuilderConfSituationAwareness(KnowledgeBuilderConfiguration builderConf) {
	  builderConf.setOption(EvaluatorOption.get("after", new AfterEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("before", new BeforeEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("overlaps", new OverlapsEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("overlappedby", new OverlappedByEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("includes", new IncludesEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("during", new DuringEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("starts", new StartsEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("startedby", new StartedByEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("finishes", new FinishesEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("finishedby", new FinishedByEvaluatorDefinition()));
	  builderConf.setOption(EvaluatorOption.get("coincides", new CoincidesEvaluatorDefinition()));
	}
	
	public static void setKnowledgeBuilderSituationAwareness(KnowledgeBuilder kbuilder) {
	  kbuilder.add(ResourceFactory.newClassPathResource("br/ufes/inf/lprm/scene/base/SituationBaseRules.drl"), ResourceType.DRL);
	  kbuilder.add(ResourceFactory.newClassPathResource("br/ufes/inf/lprm/scene/base/SituationProfileRules.drl"), ResourceType.DRL);
	}
	
	public static void SetFieldsFromMatchedObjects(Object situation, SituationCast cast) {
		
		List<Field> targetObjFields = SituationUtils.getSituationRoleFields(situation.getClass());
		Object participant;
		
		
		for(Field field: targetObjFields) {
			Role role = field.getAnnotation(Role.class);
			if (role != null) {
				if (role.label() != "") {
					participant = cast.get(field.getName());
				}
				else {
					participant = cast.get(role.label());
				}
				if (participant != null) {
					try {
						field.setAccessible(true);
						field.set(situation, participant);
						field.setAccessible(false);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}				
			}
		}
	}

	public static void situationDetected(KnowledgeHelper khelper) throws Exception {	
		
		SituationProfile prof = getSPM(khelper).getProfile(khelper.getRule());
		
		CurrentSituation asf = new CurrentSituation(prof.getType());	
		asf.setTimestamp(khelper.getKnowledgeRuntime().getSessionClock().getCurrentTime());
    	asf.setCast(new SituationCast(khelper.getActivation(), prof.getType()));

    	if (prof.getSnapshot()) {
    	
	    	QueryResults results = khelper.getKnowledgeRuntime().getQueryResults("CurrentSituation", new Object[] {asf.getTypename(), asf.getHashcode()} );
	    	
	    	if (results.size() > 0) {
	    		
		    	for ( QueryResultsRow r : results ) {
		    		((SituationType) r.get("sit")).getSnapshots().add(new CastSnapshot(asf.getCast(), asf.getTimestamp()));
		    	}    	
	    	
	    	}
    	}
    	khelper.insertLogical(asf);
	}	
	
	@Deprecated
	public static void situationDetected(KnowledgeHelper khelper, Class<?> type) throws Exception {	
		
		CurrentSituation asf = new CurrentSituation(type);	
		asf.setTimestamp(khelper.getKnowledgeRuntime().getSessionClock().getCurrentTime());
    	asf.setCast(new SituationCast(khelper.getActivation(), type));

    	if (toSnapshot(khelper, type)) {
    	
	    	QueryResults results = khelper.getKnowledgeRuntime().getQueryResults("CurrentSituation", new Object[] {asf.getTypename(), asf.getHashcode()} );
	    	
	    	if (results.size() > 0) {
	    		
		    	for ( QueryResultsRow r : results ) {
		    		((SituationType) r.get("sit")).getSnapshots().add(new CastSnapshot(asf.getCast(), asf.getTimestamp()));
		    	}    	
	    	
	    	}
    	}

    	khelper.insertLogical(asf);

	}
	
	public static SituationType activateSituation(KnowledgeHelper khelper, SituationCast cast, Class<?> type, long timestamp) {

		long evn_timestamp = khelper.getKnowledgeRuntime().getSessionClock().getCurrentTime();
		ActivateSituationEvent ase = new ActivateSituationEvent(evn_timestamp);
		
		SituationType sit = null;
		
		try {
			
			sit = (SituationType) type.newInstance();
			
			//
			sit.getSnapshots().add(new CastSnapshot(cast, evn_timestamp));
			
			SetFieldsFromMatchedObjects(sit, cast);
			ase.setSituation(sit);
			sit.setActivation(ase);
			khelper.getKnowledgeRuntime().insert(ase);
			khelper.getKnowledgeRuntime().insert(sit);
			
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
				
		return sit;
	}
	
	public static void deactivateSituation(KnowledgeHelper khelper, Object sit) {
		long evn_timestamp = khelper.getKnowledgeRuntime().getSessionClock().getCurrentTime();
		DeactivateSituationEvent dse = new DeactivateSituationEvent(evn_timestamp);
		dse.setSituation((SituationType) sit);
		if (toSnapshot(khelper, sit.getClass())) {
			try {
				SetFieldsFromMatchedObjects(sit, ((SituationType) sit).getSnapshots().getFirst().getSituationCast());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}			
		}
		((SituationType) sit).setDeactivation(dse);	
		khelper.getKnowledgeRuntime().insert(dse);		
		khelper.getKnowledgeRuntime().update(khelper.getKnowledgeRuntime().getFactHandle(sit), sit);
	}
	
}
