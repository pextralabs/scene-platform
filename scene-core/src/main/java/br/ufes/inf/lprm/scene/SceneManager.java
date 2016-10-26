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

    public void putApp(SceneApplication app) {
        registry.put(app.getName().hashCode(), app);
    }

    public void removeApp(int key) {
        registry.remove(key);
    }

    public SceneApplication getApp(int key) {
        return registry.get(key);
    }

}
