package br.ufes.inf.lprm.scene.model.impl;

import br.ufes.inf.lprm.scene.util.SituationCast;
import br.ufes.inf.lprm.situation.model.Part;
import br.ufes.inf.lprm.situation.model.Participation;
import br.ufes.inf.lprm.situation.model.Actor;
import br.ufes.inf.lprm.situation.model.events.Activation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SituationTypeImpl implements br.ufes.inf.lprm.situation.model.SituationType {

    private Class clazz;
    private List<Part> parts;
    private Map<String, Part> mappedParts;

    public SituationTypeImpl(Class<?> clazz, List<Part> parts) {
        this.clazz = clazz;
        this.parts = parts;
        mappedParts = new HashMap<String, Part>();
        for (Part part: this.parts) {
            mappedParts.put(part.getLabel(), part);
        }
    }

    @Override
    public String getName() {
        return clazz.getName();
    }

    public Class getTypeClass() {
        return clazz;
    }

    @Override
    public List<Part> getParts() {
        return parts;
    }

    public Part getPart(String label) {
        return mappedParts.get(label);
    }

    public Situation newInstance(Activation activation, SituationCast cast) {
        try {
            Field fActivation       = Situation.class.getDeclaredField("activation");
            Field fType             = Situation.class.getDeclaredField("type");
            Field fParticipations   = Situation.class.getDeclaredField("participations");
            Field fActive           = Situation.class.getDeclaredField("active");

            fActivation.setAccessible(true);
            fType.setAccessible(true);
            fParticipations.setAccessible(true);
            fActive.setAccessible(true);

            Situation situation = (Situation) clazz.newInstance();

            fType.set(situation, this);
            fActivation.set(situation, activation);
            fActive.set(situation, true);

            List<Participation> participations = new ArrayList<Participation>();

            for(Part part: getParts()) {
                Actor participant = (Actor) cast.get(part.getLabel());
                if (participant != null) {
                    Participation participation = new ParticipationImpl(situation, part, participant);
                    participations.add(participation);
                }
            }

            fParticipations.set(situation, participations);

            fActivation.setAccessible(false);
            fType.setAccessible(false);
            fParticipations.setAccessible(false);
            fActive.setAccessible(false);

            return situation;

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String toString() {
        return clazz.getSimpleName();
    }
}
