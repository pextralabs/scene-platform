package br.ufes.inf.lprm.scene.base;

import br.ufes.inf.lprm.scene.publishing.SituationPublisher;
import br.ufes.inf.lprm.scene.spm.CastRestoreType;
import br.ufes.inf.lprm.scene.spm.SituationProfile;
import br.ufes.inf.lprm.scene.spm.SituationProfileManager;
import br.ufes.inf.lprm.situation.*;
import br.ufes.inf.lprm.situation.events.ActivateSituationEvent;
import br.ufes.inf.lprm.situation.events.DeactivateSituationEvent;
import org.drools.core.base.SalienceInteger;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.KnowledgeHelper;
import org.kie.api.KieBase;
import org.kie.api.definition.KiePackage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;

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
		RuleImpl rule = khelper.getRule();
		String type = rule.getPackageName() + "." + rule.getMetaData().get("type");
		Class clazz = Class.forName(type);
		CurrentSituation asf = new CurrentSituation(clazz);
		asf.setTimestamp(khelper.getKieRuntime().getSessionClock().getCurrentTime());
    	asf.setCast(new SituationCast(khelper.getMatch(), clazz));

    	khelper.insertLogical(asf);
	}
	
	public static SituationType activateSituation(KnowledgeHelper khelper, SituationCast cast, Class<?> type, long timestamp) {

		SituationProfile prof = SituationProfileManager.getInstance().getProfile(type.getName());		
		
		long evn_timestamp = khelper.getKieRuntime().getSessionClock().getCurrentTime();
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

			khelper.getKieRuntime().insert(ase);
			khelper.getKieRuntime().insert(sit);
			
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
		long evn_timestamp = khelper.getKieRuntime().getSessionClock().getCurrentTime();
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
				if (snap != null) SetFieldsFromMatchedObjects(sit, snap.getSituationCast(SituationProfileManager.getInstance().getClassLoader()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}			
		}
		((SituationType) sit).setDeactivation(dse);
		
		SituationPublisher pub = SituationProfileManager.getInstance().getConfigurationHash().get(sit.getClass().getName()).getPublisher();
		
		if (pub!=null) pub.publishDeactivation((SituationType) sit);		
		
		khelper.getKieRuntime().insert(dse);
		khelper.getKieRuntime().update(khelper.getKieRuntime().getFactHandle(sit), sit);
	}
	
}
