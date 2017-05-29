package br.ufes.inf.lprm.scene.model;

import br.ufes.inf.lprm.situation.model.Situation;
import br.ufes.inf.lprm.situation.model.bindings.SnapshotPolicy;

import java.lang.reflect.Field;
import java.util.*;


public class Snapshot implements br.ufes.inf.lprm.situation.model.bindings.Snapshot {

    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();
    private static final Set<Class<?>> PRIMITIVE_TYPES = getPrimitiveTypes();

    public static boolean isWrapperType(Class<?> clazz)
    {
        return WRAPPER_TYPES.contains(clazz);
    }

    public static boolean isPrimitiveType(Class<?> clazz)
    {
        return PRIMITIVE_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getPrimitiveTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(boolean.class);
        ret.add(char.class);
        ret.add(byte.class);
        ret.add(short.class);
        ret.add(int.class);
        ret.add(long.class);
        ret.add(float.class);
        ret.add(double.class);
        ret.add(void.class);
        return ret;
    }

    private static Set<Class<?>> getWrapperTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }

    private String label;
    private Field field;
    private SnapshotPolicy policy;

    public Snapshot(String label, Field field, SnapshotPolicy policy) {
        this.label  = label;
        this.field  = field;
        this.policy = policy;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public Field getField() {
        return field;
    }

    @Override
    public SnapshotPolicy getPolicy() {
        return policy;
    }

    private List<Field> fieldsFromClass(Class clazz) {
        List<Field> fields = new ArrayList<Field>();
        Class superclazz = clazz.getSuperclass();
        if (superclazz != null) {
            fields.addAll(fieldsFromClass(superclazz));
        }
        for (Field field: clazz.getDeclaredFields()) fields.add(field);
        return fields;
    }

    public void set(Situation situation, Object src) throws IllegalAccessException, InstantiationException {

        //Situation field is primitive or wrapper
        if ((isPrimitiveType(field.getType()) || isWrapperType(field.getType())) && isWrapperType(src.getClass())) {
            field.setAccessible(true);
            field.set(situation, src);
            field.setAccessible(false);
        } else {
            Object copy = src.getClass().newInstance();
            List<Field> fields = fieldsFromClass(copy.getClass());
            if (policy == SnapshotPolicy.Shallow) {
                for (Field subfield: fields) {
                    subfield.setAccessible(true);
                    subfield.set(copy, field.get(src));
                    subfield.setAccessible(false);
                }
                field.setAccessible(true);
                field.set(situation, copy);
                field.setAccessible(false);
            }
        }
    }

    public String toString() {
        return getLabel() + ": " + field.getType().getSimpleName();
    }


}
