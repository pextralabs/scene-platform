package br.ufes.inf.lprm.scene;

import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static SceneManager uniqueInstance;
    private Map<Integer, SceneApplication> registry;


    private SceneManager() {
        registry = new HashMap<>();
    }

    public static synchronized SceneManager getInstance() {
        if (uniqueInstance == null)
            uniqueInstance = new SceneManager();
        return uniqueInstance;
    }

    public int putApp(SceneApplication app, int hashCode) {
        if(registry.get(hashCode) != null) {
            return putApp(app, hashCode + 7);
        }
        registry.put(hashCode, app);
        return hashCode;
    }

    public Map<Integer, SceneApplication> getApps() {
        return registry;
    }

    public void removeApp(int key) {
        registry.remove(key);
    }

    public SceneApplication getApp(int key) {
        return registry.get(key);
    }

}
