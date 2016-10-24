package br.ufes.inf.lprm.scene.util;

import org.drools.core.definitions.rule.impl.RuleImpl;
import org.kie.api.definition.KiePackage;

import java.util.Collection;
import java.util.LinkedList;

public class SituationUtils {

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
