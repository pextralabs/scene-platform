package br.ufes.inf.lprm.scene.base;

import java.util.HashMap;

public class SituationProfileMap {
	
	private HashMap<String, SituationProfile> configurations;
	
	public SituationProfileMap() {
		this.configurations = new HashMap<String, SituationProfile>();
	}

	public HashMap<String, SituationProfile> getConfigurationHash() {
		return configurations;
	}

}
