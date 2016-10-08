package br.ufes.inf.lprm.scene;

import org.kie.api.runtime.rule.FactHandle;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hborjaille on 10/8/16.
 */
public class ServerContext {
    private static ServerContext uniqueInstance;
    private Map<String, Map<Integer, FactHandle>> factHandleMap;

    private ServerContext() {
        factHandleMap = new HashMap<String, Map<Integer, FactHandle>>();
    }

    public static synchronized ServerContext getInstance() {
        if (uniqueInstance == null)
            uniqueInstance = new ServerContext();

        return uniqueInstance;
    }

    public void put(String clazz, int id, FactHandle fact) {
        Map<Integer, FactHandle> item = factHandleMap.get(clazz);
        if(item != null) {
            item.put(id, fact);
        } else {
            item = new HashMap<Integer, FactHandle>();
            item.put(id, fact);
            factHandleMap.put(clazz, item);
        }
    }

    public FactHandle get(String clazz, int id) {
        return factHandleMap.get(clazz).get(id);
    }

    public void remove(String clazz, FactHandle fact) {
        factHandleMap.get(clazz).remove(fact);
    }
}
