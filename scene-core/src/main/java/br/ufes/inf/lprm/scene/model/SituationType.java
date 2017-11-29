package br.ufes.inf.lprm.scene.model;

import br.ufes.inf.lprm.scene.model.events.Activation;
import br.ufes.inf.lprm.scene.util.SituationCast;
import br.ufes.inf.lprm.situation.model.bindings.Part;
import br.ufes.inf.lprm.situation.model.bindings.Snapshot;

import java.lang.reflect.Field;
import java.util.*;

public class SituationType implements br.ufes.inf.lprm.situation.model.SituationType {

    private Class clazz;
    private List<Part> parts;
    private List<Snapshot> snapshots;
    private Map<String, Part> mappedParts;
    private Map<String, Snapshot> mappedSnapshots;

    public SituationType(Class<?> clazz, List<Part> parts, List<Snapshot> snapshots) {
        this.clazz = clazz;
        this.parts = parts;
        this.snapshots = snapshots;
        mappedParts = new HashMap<String, Part>();
        for (Part part: this.parts) {
            mappedParts.put(part.getLabel(), part);
        }
        mappedSnapshots = new HashMap<String, Snapshot>();
        for (Snapshot snapshot: this.snapshots) {
            mappedSnapshots.put(snapshot.getLabel(), snapshot);
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

    @Override
    public List<Snapshot> getSnapshots() {
        return snapshots;
    }

    public Part getPart(String label) {
        return mappedParts.get(label);
    }

    public Situation newInstance(Activation activation, SituationCast cast) {
        try {
            //Field fInternalKey      = Situation.class.getDeclaredField("internalKey");
            Field fRunningId        = Situation.class.getDeclaredField("runningId");
            Field fActivation       = Situation.class.getDeclaredField("activation");
            Field fType             = Situation.class.getDeclaredField("type");
            Field fParticipations   = Situation.class.getDeclaredField("participations");
            Field fActive           = Situation.class.getDeclaredField("active");

            //fInternalKey.setAccessible(true);
            fRunningId.setAccessible(true);
            fActivation.setAccessible(true);
            fType.setAccessible(true);
            fParticipations.setAccessible(true);
            fActive.setAccessible(true);

            Situation situation = (Situation) clazz.newInstance();

            //fInternalKey.set(situation, UUID.randomUUID().toString());
            fRunningId.setInt(situation, this.getTypeClass().hashCode() + cast.hashCode());
            fType.set(situation, this);
            fActivation.set(situation, activation);
            fActive.set(situation, true);

            List<br.ufes.inf.lprm.situation.model.Participation> participations = new ArrayList<br.ufes.inf.lprm.situation.model.Participation>();

            for(Part part: getParts()) {
                Object participant = cast.get(part.getLabel());
                if (participant != null) {
                    br.ufes.inf.lprm.situation.model.Participation participation = new Participation(situation, part, participant);
                    participations.add(participation);
                }
            }
            fParticipations.set(situation, participations);

            for(Snapshot snapshot: getSnapshots()) {
                Object obj = cast.get(snapshot.getLabel());
                ((br.ufes.inf.lprm.scene.model.Snapshot) snapshot).set(situation, obj);
            }

            //fInternalKey.setAccessible(false);
            fRunningId.setAccessible(false);
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
