package br.ufes.inf.lprm.scene.base;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;

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

import br.ufes.inf.lprm.situation.CastSnapshot;
import br.ufes.inf.lprm.situation.Role;
import br.ufes.inf.lprm.situation.SituationCast;
import br.ufes.inf.lprm.situation.SituationType;
import br.ufes.inf.lprm.situation.SituationUtils;
import br.ufes.inf.lprm.situation.events.ActivateSituationEvent;
import br.ufes.inf.lprm.situation.events.DeactivateSituationEvent;
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
import br.ufes.inf.lprm.scene.publishing.SituationPublisher;
import br.ufes.inf.lprm.scene.spm.CastRestoreType;
import br.ufes.inf.lprm.scene.spm.SituationProfile;
import br.ufes.inf.lprm.scene.spm.SituationProfileManager;

//@SuppressWarnings("restriction")
public class SituationHelper {
	
	private static boolean toSnapshot(KnowledgeHelper khelper, Class<?> type) {
		
		if (SituationProfileManager.getInstance().getConfigurationHash().containsKey(type.getSimpleName())) {
			return SituationProfileManager.getInstance().getConfigurationHash().get(type.getSimpleName()).getSnapshot();
		}
		return false;
	}

	public static void SetupSituationProfileManager(KnowledgeHelper khelper) throws Exception {
		
		SituationProfileManager spm;
		SituationProfile conf;
		
		spm = SituationProfileManager.getInstance();
		
		for (KnowledgePackage pkg: khelper.getKnowledgeRuntime().getKnowledgeBase().getKnowledgePackages()) {
			
			for (Rule rule:  SituationUtils.getRulesFromPackage(pkg)) {
				
				if (rule.getMetaData().containsKey("role")) {
					
					if (rule.getMetaData().get("role").equals("situation")) {
						
						String type = (String) rule.getPackageName() + "." + rule.getMetaData().get("type");
						conf = spm.getConfigurationHash().get(type);
						
						if (conf == null) {
							conf = new SituationProfile();
							spm.getConfigurationHash().put(type, conf);
						}
											
						conf.setType(Class.forName(type));
												
						if (rule.getMetaData().containsKey("snapshot")) {
							
							if (rule.getMetaData().get("snapshot").equals("on")) {
								conf.setSnapshot(true);
								
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
					}				
				}
			}
		}
		//khelper.getKnowledgeRuntime().setGlobal("SPM", spm);
		//System.out.print(spm.toString());
	}
	
	public static void refactorSaliences(KnowledgeBuilder kbuilder) {
		
		KnowledgePackage situationBasePkg = null;
		Integer maxSalience = null;
		
		for (KnowledgePackage pkg: kbuilder.getKnowledgePackages()) {
			
			if (pkg.getName().equals("br.ufes.inf.lprm.base")) situationBasePkg = pkg;
			
			for (Rule rule:  SituationUtils.getRulesFromPackage(pkg)) {
				
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
	}
	
	public static void SetFieldsFromMatchedObjects(Object situation, SituationCast cast) {
		
		List<Field> targetObjFields = SituationUtils.getSituationRoleFields(situation.getClass());
		Object participant;
		
		for(Field field: targetObjFields) {
			Role role = field.getAnnotation(Role.class);
			if (role != null) {
				if (role.label() != "") {
					participant = cast.get(role.label());

				}
				else {
					participant = cast.get(field.getName());					
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
		
		SituationProfile prof = SituationProfileManager.getInstance().getProfile(khelper.getRule());
		
		CurrentSituation asf = new CurrentSituation(prof.getType());	
		asf.setTimestamp(khelper.getKnowledgeRuntime().getSessionClock().getCurrentTime());
    	asf.setCast(new SituationCast(khelper.getActivation(), prof.getType()));

    	if (prof.getSnapshot()) {
    		
    		//SituationProfileManager.LogOn
    		
    		//LOG
    		//System.out.
    	
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

		SituationProfile prof = SituationProfileManager.getInstance().getProfile(type.getName());		
		
		long evn_timestamp = khelper.getKnowledgeRuntime().getSessionClock().getCurrentTime();
		ActivateSituationEvent ase = new ActivateSituationEvent(evn_timestamp);
		
		SituationType sit = null;

		try {
			
			sit = (SituationType) type.newInstance();
			
			if (prof.getSnapshot()) {
				sit.getSnapshots().add(new CastSnapshot(cast, evn_timestamp));
			}
			
			SetFieldsFromMatchedObjects(sit, cast);
			ase.setSituation(sit);
			
			sit.setActivation(ase);	
			
			SituationPublisher pub = SituationProfileManager.getInstance().getConfigurationHash().get(type.getName()).getPublisher();
			
			if (pub!=null) pub.publishActivation(sit);

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
		
		SituationProfile prof = SituationProfileManager.getInstance().getProfile(sit.getClass().getName());
			
		if (prof.getSnapshot()) {
			try {
				CastSnapshot snap = null;
				try {
					if (prof.getRestoretype() == CastRestoreType.FIRST) snap 	= ((SituationType) sit).getSnapshots().getFirst(); 
					if (prof.getRestoretype() == CastRestoreType.STABLE) snap 	= ((SituationType) sit).getSnapshots().getStable(); 
					if (prof.getRestoretype() == CastRestoreType.LAST) snap 	= ((SituationType) sit).getSnapshots().getLast();	
				} catch(NoSuchElementException nsee) {
					snap = null;
				}
				if (snap != null) SetFieldsFromMatchedObjects(sit, snap.getSituationCast());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}			
		}
		((SituationType) sit).setDeactivation(dse);
		
		SituationPublisher pub = SituationProfileManager.getInstance().getConfigurationHash().get(sit.getClass().getName()).getPublisher();
		
		if (pub!=null) pub.publishDeactivation((SituationType) sit);		
		
		khelper.getKnowledgeRuntime().insert(dse);		
		khelper.getKnowledgeRuntime().update(khelper.getKnowledgeRuntime().getFactHandle(sit), sit);
	}
	
}
