package br.ufes.inf.lprm.scene.base;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;

import org.drools.rule.Rule;
import org.drools.definition.KnowledgePackage;

import br.ufes.inf.lprm.scene.situation.publishing.Publish;
import br.ufes.inf.lprm.scene.situation.publishing.SituationPublisher;

public final class SituationProfileManager {
	
	private static final SituationProfileManager INSTANCE = new SituationProfileManager();
		
	private HashMap<String, SituationProfile> profiles;

	private SituationProfileManager() {

		this.profiles = new HashMap<String, SituationProfile>();
	}
	
	public static synchronized SituationProfileManager getInstance() {
        return INSTANCE;
	}	
	
	public void BuildProfileFromPackages(Collection<KnowledgePackage> packages) throws Exception {
		
		SituationProfileManager spm;
		SituationProfile conf;

		spm = SituationProfileManager.getInstance();
		
		for (KnowledgePackage pkg: packages) {
			 		
			for (Rule rule:  SituationUtils.getRulesFromPackage(pkg)) {
				
				if (rule.getMetaData().containsKey("role")) {
					
					if (rule.getMetaData().get("role").equals("situation")) {
						
						String type = (String) rule.getPackageName() + "." + rule.getMetaData().get("type");
						conf = spm.getConfigurationHash().get(type);
						
						if (conf == null) {
							conf = new SituationProfile();
							spm.getConfigurationHash().put(type, conf);
						}
											
						conf.setType(Class.forName(type));
						
						Publish pub = Class.forName(type).getAnnotation(Publish.class);
						if (pub!=null) {
							
							Constructor<SituationPublisher> ctor = (Constructor<SituationPublisher>) pub.publisher().getDeclaredConstructors()[0];
							ctor.setAccessible(true);
							conf.setPublisher((SituationPublisher) ctor.newInstance(conf.getType(), pub.host(), pub.port()));
							ctor.setAccessible(false);
						}
					
						if (rule.getMetaData().containsKey("snapshot")) {
							
							if (rule.getMetaData().get("snapshot").equals("on")) {
								conf.setSnapshot(true);
								
								if (rule.getMetaData().containsKey("restore")) {
									
									if (rule.getMetaData().get("restore").equals("first")) {
										conf.setRestoreType(CastRestoreType.FIRST);
									}
									else {
										if (rule.getMetaData().get("restore").equals("last")) {
											conf.setRestoreType(CastRestoreType.LAST);
										}
										else {
											if (rule.getMetaData().get("restore").equals("stable")) {
												conf.setRestoreType(CastRestoreType.STABLE);
											}
											else {
												throw new Exception();
											}
										}
									}
									
								} else {
									conf.setRestoreType(CastRestoreType.FIRST);
								}								
								
								
							}
							else {
								if (rule.getMetaData().get("snapshot").equals("off")) {
									conf.setSnapshot(false);
								}
								else {
									throw new Exception();
								}
							}
							
						} else {
							conf.setSnapshot(false);
						}						
					}				
				}
			}
		}
		System.out.print(spm.toString());	
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
