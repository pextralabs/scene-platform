package br.ufes.inf.lprm.situation.model;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

public interface SituationType extends Type {
    public String getName();
    public List<Part> getParts();

}
