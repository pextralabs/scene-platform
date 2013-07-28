package br.ufes.inf.lprm.situation;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.drools.definition.KnowledgePackage;
import org.drools.definitions.rule.impl.RuleImpl;
import org.drools.rule.Rule;

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
	
	public static String getSituationMetaDataValue(Rule rule, String key) {
		return (String) rule.getMetaData().get(key);		
	}
		
	public static Collection<Rule> getRulesFromPackage(KnowledgePackage pkg) {
		
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
	
	
	//public static 
	
	
}
