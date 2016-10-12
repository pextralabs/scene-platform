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
    private Map<String, Map<Integer, Object>> objectMap;

    private ServerContext() {
        factHandleMap = new HashMap<String, Map<Integer, FactHandle>>();
        objectMap = new HashMap<String, Map<Integer, Object>>();
    }

    public static synchronized ServerContext getInstance() {
        if (uniqueInstance == null)
            uniqueInstance = new ServerContext();

        return uniqueInstance;
    }

    public void putObj(String clazz, int id, Object obj) {
        Map<Integer, Object> item = objectMap.get(clazz);
        if(item != null) {
            item.put(id, obj);
        } else {
            item = new HashMap<Integer, Object>();
            item.put(id, obj);
            objectMap.put(clazz, item);
        }
    }

    public Object getObj(String clazz, int id) {
        return objectMap.get(clazz).get(id);
    }

    public void removeObj(String clazz, Object obj) {
        objectMap.get(clazz).remove(obj);
    }

    public void putFact(String clazz, int id, FactHandle fact) {
        Map<Integer, FactHandle> item = factHandleMap.get(clazz);
        if(item != null) {
            item.put(id, fact);
        } else {
            item = new HashMap<Integer, FactHandle>();
            item.put(id, fact);
            factHandleMap.put(clazz, item);
        }
    }

    public FactHandle getFact(String clazz, int id) {
        return factHandleMap.get(clazz).get(id);
    }

    public void removeFact(String clazz, FactHandle fact) {
        factHandleMap.get(clazz).remove(fact);
    }
}
