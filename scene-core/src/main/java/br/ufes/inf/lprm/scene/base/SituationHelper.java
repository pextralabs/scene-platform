package br.ufes.inf.lprm.scene.base;

import br.ufes.inf.lprm.situation.Part;
import br.ufes.inf.lprm.situation.SituationCast;
import br.ufes.inf.lprm.situation.SituationType;
import br.ufes.inf.lprm.situation.SituationUtils;
import br.ufes.inf.lprm.situation.events.ActivateSituationEvent;
import br.ufes.inf.lprm.situation.events.DeactivateSituationEvent;
import org.drools.core.base.SalienceInteger;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.KnowledgeHelper;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.type.FactType;

import java.lang.reflect.Field;
import java.util.List;

public class SituationHelper {
	
	public static void refactorSaliences(KieBase kbase) {
		
		KiePackage situationBasePkg = null;
		Integer maxSalience = null;
		
		for (KiePackage pkg: kbase.getKiePackages()) {
			
			if (pkg.getName().equals("br.ufes.inf.lprm.scene.base")) { situationBasePkg = pkg; }
			
			for (RuleImpl rule:  SituationUtils.getRulesFromPackage(pkg)) {
				
        		if (maxSalience != null) {
            		if (rule.getSalience().getValue() > maxSalience) {
            			maxSalience = rule.getSalience().getValue();
            		}        			
        		} else maxSalience = rule.getSalience().getValue();
			}
		}

		if (situationBasePkg != null) {
			SituationUtils.getRuleFromPackage(situationBasePkg, "SituationActivation").setSalience(new SalienceInteger(maxSalience + 1));
			SituationUtils.getRuleFromPackage(situationBasePkg, "SituationDeactivation").setSalience(new SalienceInteger(maxSalience + 2));
		}
	}
	
	public static void SetFieldsFromMatchedObjects(Object situation, SituationCast cast) {
		
		List<Field> targetObjFields = SituationUtils.getSituationRoleFields(situation.getClass());
		Object participant;
		
		for(Field field: targetObjFields) {
			Part part = field.getAnnotation(Part.class);
			if (part != null) {
				if (part.label() != "") {
					participant = cast.get(part.label());

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

	public static Class<? extends FactType> getDroolsClass(KnowledgeHelper khelper, String classname, String packagePath) {
		FactType type = khelper.getKieRuntime().getKieBase().getFactType(packagePath, classname);
		return type.getClass();
	}

	public static void situationDetected(KnowledgeHelper khelper) throws Exception {
		RuleImpl rule = khelper.getRule();

		String packageName = rule.getPackageName();
		String className = (String) rule.getMetaData().get("type");

		Class clazz = null;

		//find
		FactType type = khelper.getKieRuntime().getKieBase().getFactType(packageName, className);

		if (type == null) {
			clazz = Class.forName(packageName + "." + className);
		} else {
			clazz = type.getFactClass();
		}

		CurrentSituation asf = new CurrentSituation(clazz);
		asf.setTimestamp(khelper.getKieRuntime().getSessionClock().getCurrentTime());
    	asf.setCast(new SituationCast(khelper.getMatch(), clazz));

    	khelper.insertLogical(asf);
	}
	
	public static SituationType activateSituation(KnowledgeHelper khelper, SituationCast cast, Class<?> type, long timestamp) {
		
		long evn_timestamp = khelper.getKieRuntime().getSessionClock().getCurrentTime();
		ActivateSituationEvent ase = new ActivateSituationEvent(evn_timestamp);
		
		SituationType sit = null;

		try {
			
			sit = (SituationType) type.newInstance();
			
			SetFieldsFromMatchedObjects(sit, cast);
			ase.setSituation(sit);
			
			sit.setActivation(ase);

			khelper.getKieRuntime().insert(ase);
			khelper.getKieRuntime().insert(sit);
			
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return sit;
	}
	
	public static void deactivateSituation(KnowledgeHelper khelper, Object sit) {
		long evn_timestamp = khelper.getKieRuntime().getSessionClock().getCurrentTime();
		DeactivateSituationEvent dse = new DeactivateSituationEvent(evn_timestamp);
		dse.setSituation((SituationType) sit);

		((SituationType) sit).setDeactivation(dse);
		
		khelper.getKieRuntime().insert(dse);
		khelper.getKieRuntime().update(khelper.getKieRuntime().getFactHandle(sit), sit);
	}
	
}
