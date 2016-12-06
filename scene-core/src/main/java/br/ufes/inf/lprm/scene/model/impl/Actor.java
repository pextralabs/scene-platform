package br.ufes.inf.lprm.scene.model.impl;

import br.ufes.inf.lprm.situation.annotations.id;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Actor {

    private Field actorId = null;

    List<Field> getFieldsAnnotatedWith(Class clazz, Class annotationClazz) {
        List<Field> fields = new ArrayList<Field>();
        Class zuper = clazz.getSuperclass();
        if (zuper != null) {
            fields.addAll(getFieldsAnnotatedWith(zuper, annotationClazz));
        }
        for (Field field: clazz.getDeclaredFields()) {
                Annotation ann = field.getDeclaredAnnotation(annotationClazz);
                if (ann != null) fields.add(field);
        }
        return fields;
    }

    public long getId() {
        try {
            if (actorId == null) {
                for (Field f: getFieldsAnnotatedWith(this.getClass(), id.class)) {

                    if (f.getType() == long.class || f.getType() == Long.class) {
                        actorId = f;
                        break;
                    }
                }
                if (actorId == null) throw new Exception("no suitable Id field found");
            }
            Boolean access = actorId.isAccessible();
            actorId.setAccessible(true);
            long result = actorId.getLong(this);
            actorId.setAccessible(access);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
