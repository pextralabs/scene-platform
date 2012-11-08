package br.ufes.inf.lprm.scene.base;

import java.util.HashMap;

import org.drools.rule.Rule;

public class SituationProfileManager {
	
	private HashMap<String, SituationProfile> profiles;
	
	public SituationProfileManager() {
		this.profiles = new HashMap<String, SituationProfile>();
	}

	public HashMap<String, SituationProfile> getConfigurationHash() {
		return profiles;
	}
	
	public SituationProfile getProfile(String situation) {
		return profiles.get(situation);
	}

	public SituationProfile getProfile(Rule rule) {
		String situation = SituationUtils.getSituationMetaDataValue(rule, "type");
		return profiles.get(situation);
	}	
}
