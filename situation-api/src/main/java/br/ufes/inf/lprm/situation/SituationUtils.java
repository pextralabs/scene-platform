package br.ufes.inf.lprm.situation;

import org.drools.core.definitions.rule.impl.RuleImpl;
import org.kie.api.definition.KiePackage;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SituationUtils {

	public static List<Field> getSituationRoleFields(Class<?> sit) {

		List<Field> situationRoleFields = new LinkedList<Field>();
		
		//recursively gets fields on superclass tree
		if (sit.getSuperclass() != null) {
			situationRoleFields.addAll(getSituationRoleFields(sit.getSuperclass()));
		}
		Field[] fields = sit.getDeclaredFields();
		for(Field field: fields) {						
			Role role = field.getAnnotation(Role.class);			
			if (role != null) situationRoleFields.add(field);
		}
		return situationRoleFields;
	}

	public static RuleImpl getRuleFromPackage(KiePackage pkg, String rulename) {
		for (Object obj: pkg.getRules()) {

			RuleImpl ruleImpl = (RuleImpl) obj;

			if (ruleImpl.getName().equals(rulename)) {
				return ruleImpl;
			}
		}
		return null;
	}
		
	public static Collection<RuleImpl> getRulesFromPackage(KiePackage pkg) {
		
		LinkedList<RuleImpl> collection = new LinkedList<RuleImpl>();
		
		for (Object obj: pkg.getRules()) {
    		RuleImpl ruleImpl = (RuleImpl) obj;
			collection.add(ruleImpl);
		}
		
		return collection;
	}
}
