package br.ufes.inf.lprm.scene.base;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

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
		
	
	//public static 
	
	
}
