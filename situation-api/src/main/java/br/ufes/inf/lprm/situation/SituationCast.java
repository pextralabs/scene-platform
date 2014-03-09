package br.ufes.inf.lprm.situation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drools.rule.Rule;
import org.drools.runtime.rule.Activation;

import br.ufes.inf.lprm.situation.SituationUtils;

@SuppressWarnings("serial")
public class SituationCast extends HashMap<String, Object> {
	
	private int hash = 0;

	public SituationCast(Activation activation, Class<?> type) throws Exception {
		
		int 	counter;
		String 	roleLabel;
		Object 	obj;
		
		Rule 	rule 					= (Rule) activation.getRule();
		List<String> LHSIdentifiers 	= new ArrayList<String>(rule.getDeclarations().keySet());
		List<Field> situationRoleFields = SituationUtils.getSituationRoleFields(type);
		
		for(Field field: situationRoleFields) {
			Role role = field.getAnnotation(Role.class);
			if (role != null) {
				
				if (role.label() != "") {
					roleLabel = new String(role.label());
				} else {
					roleLabel = new String(field.getName());
				}
			
				if (field.getType().equals(java.util.Set.class)) {
					Set<Object> set = new HashSet<Object>();
					counter = 1;				
					do {
						obj = null;
						roleLabel = new String(roleLabel + "$" + counter);
						if (LHSIdentifiers.contains(roleLabel)) {
							obj = activation.getDeclarationValue(roleLabel);
							set.add(obj);
						}
						counter++;
					} while (obj != null);
					if (set.size() < 2) throw new Exception();				
					this.put(field.getName(), set);
				}
				else {
					this.put(roleLabel, activation.getDeclarationValue(roleLabel));
				}			
			}
		}
	}	
	
	public SituationCast(SituationType sit) {
		List<Field> fields = SituationUtils.getSituationRoleFields(sit.getClass());
		for(Field field: fields) {
			try {
				this.put(field.getName(), field.get(sit));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Object put(String key, Object value) {
		//this.hash = this.hash + (key.hashCode() + value.getClass().hashCode() + ((Entity) value).getEID());
		this.hash = this.hash + (key.hashCode() + value.getClass().hashCode() + value.hashCode());
		return super.put(key, value);
	}

	@Override
	public int hashCode() {
		return hash;
	}

    /*public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("{");
        ArrayList<String> keys = (ArrayList) this.keySet();
        for (String k: keys) {
            if (str.length() > 1) str.append(", ");
            str.append(k);
            str.append(": [");
            str.append(this.get(k).toString());
            str.append("]");
        }
        str.append("}");
        return str.toString();
    } */
	
	//public int getHash() {
	//	return hash;
	//}

}
