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
		String situation =  rule.getPackageName() + "." + SituationUtils.getSituationMetaDataValue(rule, "type");
		return profiles.get(situation);
	}
	
	@Override
	public String toString() {
		SituationProfile profile;
		String result = new String();
		for (String situation: this.profiles.keySet()) {
			profile = profiles.get(situation);
			
			if (profile.getSnapshot()) {
				result = result.concat(situation + " - " + "type: " + profile.getType().getName() + "\t" + "snapshot: "  + profile.getSnapshot() + "\t" + "restore:" + profile.getRestoretype().name()+"\n");
			} else {
				result = result.concat(situation + " - " + "type: " + profile.getType().getName() + "\t" + "snapshot: "  + profile.getSnapshot()+"\n");				
			}
		}
		return result;
	}
}
