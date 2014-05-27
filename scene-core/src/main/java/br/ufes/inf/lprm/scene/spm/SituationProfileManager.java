package br.ufes.inf.lprm.scene.spm;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;

import br.ufes.inf.lprm.scene.exceptions.AlreadyInstantiatedException;
import org.drools.definition.KnowledgePackage;
import org.drools.rule.Rule;

import br.ufes.inf.lprm.scene.publishing.Publish;
import br.ufes.inf.lprm.scene.publishing.SituationPublisher;
import br.ufes.inf.lprm.situation.SituationUtils;

/*
    The Situation Profile Manager (SPM) is a Singleton which keeps
    the particular configurations (situation's name, snapshot policy, etc)
    for each situation type published in runtime.
 */

public final class SituationProfileManager {
	
	private static SituationProfileManager INSTANCE;

    private ClassLoader classLoader;
	private HashMap<String, SituationProfile> profiles;

    private SituationProfileManager(ClassLoader classLoader) {

        this.classLoader = classLoader;
        this.profiles = new HashMap<String, SituationProfile>();
    }

    public static synchronized SituationProfileManager initInstance() throws AlreadyInstantiatedException {
        if (INSTANCE!=null) throw new AlreadyInstantiatedException("SPM already instatiated");
        INSTANCE = new SituationProfileManager(Thread.currentThread().getContextClassLoader());
        return INSTANCE;
    }

    public static synchronized SituationProfileManager initInstance(ClassLoader classLoader) throws AlreadyInstantiatedException {
        if (INSTANCE!=null) throw new AlreadyInstantiatedException("SPM already instatiated");
        INSTANCE = new SituationProfileManager(classLoader);
        return INSTANCE;
    }

	public static synchronized SituationProfileManager getInstance() {
        if (INSTANCE==null) try {
            return initInstance();
        } catch (AlreadyInstantiatedException e) {
            e.printStackTrace();
        }
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

                        Publish pub;
                        conf.setType(Class.forName(type, true, this.classLoader));
                        pub = Class.forName(type, true, this.classLoader).getAnnotation(Publish.class);

						if (pub!=null) {
							
							Constructor<SituationPublisher> ctor = (Constructor<SituationPublisher>) pub.publisher().getDeclaredConstructors()[0];
							ctor.setAccessible(true);
							conf.setPublisher((SituationPublisher) ctor.newInstance(conf.getType(), pub.host(), pub.port(), pub.delay(), pub.attempts(), pub.timeout()));
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
		//System.out.print(spm.toString());	
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

    public ClassLoader getClassLoader() {
        return classLoader;
    }

}
