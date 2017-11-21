package br.ufes.inf.lprm.scene.util;

import br.ufes.inf.lprm.situation.model.bindings.Part;
import br.ufes.inf.lprm.situation.model.bindings.Snapshot;
import br.ufes.inf.lprm.situation.model.SituationType;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.Activation;

import java.util.*;

@SuppressWarnings("serial")
public class SituationCast extends HashMap<String, Object> {
	
	private int hash = 17;

	public SituationCast(Activation activation, SituationType type) throws Exception {
		
		int 	counter;
		String 	roleLabel;
		Object 	obj;
		RuleImpl rule = activation.getRule();
		//List<Part> parts = type.getParts();
		
		for(Part p: type.getParts()) {
			put(p.getLabel(), activation.getDeclarationValue(p.getLabel()), p.isKey());
		}

		for(Snapshot s: type.getSnapshots()) {
			put(s.getLabel(), activation.getDeclarationValue(s.getLabel()), false);

		}
	}	


	/*public SituationCast(Situation sit) {
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
	}*/

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		else {
			if ( !(obj instanceof SituationCast) ) {
				return false;
			}
			else {
				return this.hashCode() == obj.hashCode();

			}
		}
	}

	public Object put(String key, Object value, boolean hash) {

		if (hash) {
			this.hash = this.hash + (key.hashCode() + value.getClass().hashCode() + value.hashCode());
			this.hash = 31*this.hash + key.hashCode();
			this.hash = 31*this.hash + value.getClass().hashCode();
			this.hash = 31*this.hash + value.hashCode();
		}
		return super.put(key, value);
	}

	@Override
	public int hashCode() {
		return hash;
	}

}
