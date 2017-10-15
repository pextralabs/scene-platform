package br.ufes.inf.lprm.scene.test.temporal;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.test.temporal.model.TemporalEntity;
import br.ufes.inf.lprm.scene.test.temporal.model.TemporalRelation;
import br.ufes.inf.lprm.scene.test.temporal.model.event.Event;
import javassist.ClassPool;
import org.drools.core.time.SessionPseudoClock;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TemporalOperatorsTest {
    static final Logger LOG = LoggerFactory.getLogger(TemporalOperatorsTest.class);

    static final long MINUTE = 60000;
    static final long HOUR = 3600000;

    private int sumAcc(int n, int total) {
        if (n <= 0) return total;
        return sumAcc(n-1, total + n);
    }

    private int sum(int n) {
        return sumAcc(n, 0);
    }

    private void KieBaseCheck(KieBase kieBase) {
        LOG.info("There should be rules: ");
        for ( KiePackage kp : kieBase.getKiePackages() ) {
            for (Rule rule : kp.getRules()) LOG.info("kp " + kp + " rule " + rule.getName());
        }
    }

    private Map<TemporalRelation.Type, List<TemporalRelation>> initTemporalRelationLists () {
        Map<TemporalRelation.Type, List<TemporalRelation>> map = new HashMap<TemporalRelation.Type, List<TemporalRelation>> ();
        for (TemporalRelation.Type type: TemporalRelation.Type.values()) {
            map.put(type, new ArrayList<TemporalRelation>());
        }
        return map;
    }

    private void logRelations(Map<TemporalRelation.Type, List<TemporalRelation>> temporalRelations) {
        for( List<TemporalRelation> list : temporalRelations.values()) {
            for( TemporalRelation relation: list ) {

                TemporalEntity a = relation.getA();
                TemporalEntity b = relation.getB();

                LOG.info( a.getTemporalType() + " " + a.getId() + "("+ a.getStart() + " - " + (a.getEnd()) +") " + relation.getType().toString().toLowerCase() + " " +
                          b.getTemporalType() + " " + b.getId() + "("+b.getStart() + " - " + (b.getEnd()) +")"
                );
            }
        }
    }


    private KieSession newSession() {
        KieServices kieServices = KieServices.Factory.get();

        KieContainer kContainer = kieServices.getKieClasspathContainer();
        Results verifyResults = kContainer.verify();
        for (Message m : verifyResults.getMessages()) LOG.info("{}", m);

        LOG.info("Creating kieBase");

        KieBaseConfiguration config = KieServices.Factory.get().newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);
        KieBase kieBase = kContainer.newKieBase("temporal-operators", config);

        KieBaseCheck(kieBase);

        KieSessionConfiguration pseudoConfig = KieServices.Factory.get().newKieSessionConfiguration();
        pseudoConfig.setOption(ClockTypeOption.get("pseudo"));

        LOG.info("Creating kieSession");
        KieSession session = kieBase.newKieSession(pseudoConfig, null);

        new SceneApplication(ClassPool.getDefault(), session, "test");

        return session;
    }


    @Test
    public void before() {

        KieSession session = newSession();

        SessionPseudoClock clock = session.getSessionClock();
        Map<TemporalRelation.Type, List<TemporalRelation>> temporalRelations = initTemporalRelationLists();
        session.setGlobal("relations", temporalRelations);

        LOG.info("Now running data");

        long start = clock.getCurrentTime();

        Event a = new Event("A", start,  1*HOUR );
        Event b = new Event("B", a.getEnd() + 10*MINUTE, 1*HOUR );

        FactHandle fha = session.insert(a);
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.HOURS);
        session.update(fha, a.setFinished(true));
        session.fireAllRules();
        clock.advanceTime(10, TimeUnit.MINUTES);
        FactHandle fhb = session.insert(b);
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.HOURS);
        session.update(fhb, b.setFinished(true));
        session.fireAllRules();

        logRelations(temporalRelations);

        List<TemporalRelation> before = temporalRelations.get(TemporalRelation.Type.BEFORE);
        List<TemporalRelation> after  = temporalRelations.get(TemporalRelation.Type.AFTER);

        Assert.assertTrue(before.size() == 2);
        Assert.assertTrue(after.size() == 2);

    }

    @Test
    public void meets() {
        KieSession session = newSession();
        SessionPseudoClock clock = session.getSessionClock();
        Map<TemporalRelation.Type, List<TemporalRelation>> temporalRelations = initTemporalRelationLists();
        session.setGlobal("relations", temporalRelations);

        LOG.info("Now running data");

        long start = clock.getCurrentTime();

        Event a = new Event("A", start,  1*HOUR );
        Event b = new Event("B", a.getEnd(), 1*HOUR );

        FactHandle fha = session.insert(a);
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.HOURS);
        session.update(fha, a.setFinished(true));
        session.fireAllRules();
        FactHandle fhb = session.insert(b);
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.HOURS);
        session.update(fhb, b.setFinished(true));
        session.fireAllRules();

        logRelations(temporalRelations);

        List<TemporalRelation> meets = temporalRelations.get(TemporalRelation.Type.MEETS);
        List<TemporalRelation> metBy  = temporalRelations.get(TemporalRelation.Type.MET_BY);

        Assert.assertTrue(meets.size() == 2);
        Assert.assertTrue(metBy.size() == 2);
    }

    @Test
    public void overlaps() {
        KieSession session = newSession();
        SessionPseudoClock clock = session.getSessionClock();
        Map<TemporalRelation.Type, List<TemporalRelation>> temporalRelations = initTemporalRelationLists();
        session.setGlobal("relations", temporalRelations);

        LOG.info("Now running data");

        long start = clock.getCurrentTime();

        Event a = new Event("A", start,  1*HOUR );
        Event b = new Event("B", a.getEnd() - 1*MINUTE, 1*HOUR );

        FactHandle fha = session.insert(a);
        session.fireAllRules();
        clock.advanceTime(59, TimeUnit.MINUTES);
        FactHandle fhb = session.insert(b);
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.MINUTES);
        //session.update(fha, a.setFinished(true));
        //session.fireAllRules();
        clock.advanceTime(59, TimeUnit.MINUTES);
        //session.update(fhb, b.setFinished(true));
        //session.fireAllRules();

        logRelations(temporalRelations);

        List<TemporalRelation> overlaps = temporalRelations.get(TemporalRelation.Type.OVERLAPS);
        List<TemporalRelation> overlappedBy  = temporalRelations.get(TemporalRelation.Type.OVERLAPPED_BY);

        Assert.assertTrue(overlaps.size() == 2);
        Assert.assertTrue(overlappedBy.size() == 2);

    }

    @Test
    public void starts() {
        KieSession session = newSession();
        SessionPseudoClock clock = session.getSessionClock();
        Map<TemporalRelation.Type, List<TemporalRelation>> temporalRelations = initTemporalRelationLists();
        session.setGlobal("relations", temporalRelations);

        LOG.info("Now running data");

        long start = clock.getCurrentTime();

        Event a = new Event("A", start,  1*HOUR );
        Event b = new Event("B", start, 2*HOUR );

        FactHandle fha = session.insert(a);
        session.fireAllRules();
        FactHandle fhb = session.insert(b);
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.HOURS);
        session.update(fha, a.setFinished(true));
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.HOURS);
        session.update(fhb, b.setFinished(true));
        session.fireAllRules();

        logRelations(temporalRelations);

        List<TemporalRelation> starts = temporalRelations.get(TemporalRelation.Type.STARTS);
        List<TemporalRelation> startedBy  = temporalRelations.get(TemporalRelation.Type.STARTED_BY);

        Assert.assertTrue(starts.size() == 2);
        Assert.assertTrue(startedBy.size() == 2);

    }

    @Test
    public void during() {
        KieSession session = newSession();
        SessionPseudoClock clock = session.getSessionClock();
        Map<TemporalRelation.Type, List<TemporalRelation>> temporalRelations = initTemporalRelationLists();
        session.setGlobal("relations", temporalRelations);

        LOG.info("Now running data");


        long start = clock.getCurrentTime();

        Event a = new Event("A", start,  1*HOUR );
        Event b = new Event("B", a.getStart() + 1*MINUTE, 58*MINUTE );

        FactHandle fha = session.insert(a);
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.MINUTES);
        FactHandle fhb = session.insert(b);
        session.fireAllRules();
        clock.advanceTime(58, TimeUnit.MINUTES);
        session.update(fhb, b.setFinished(true));
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.MINUTES);
        session.update(fha, a.setFinished(true));
        session.fireAllRules();

        logRelations(temporalRelations);

        List<TemporalRelation> during = temporalRelations.get(TemporalRelation.Type.DURING);
        List<TemporalRelation> includes  = temporalRelations.get(TemporalRelation.Type.INCLUDES);

        Assert.assertTrue(during.size() == 2);
        Assert.assertTrue(includes.size() == 2);

    }

    @Test
    public void finishes() {
        KieSession session = newSession();
        SessionPseudoClock clock = session.getSessionClock();
        Map<TemporalRelation.Type, List<TemporalRelation>> temporalRelations = initTemporalRelationLists();
        session.setGlobal("relations", temporalRelations);

        LOG.info("Now running data");

        long start = clock.getCurrentTime();

        Event a = new Event("A", start,  1*HOUR );
        Event b = new Event("B", start + 1*MINUTE, 59*MINUTE );

        FactHandle fha = session.insert(a);
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.MINUTES);
        FactHandle fhb = session.insert(b);
        session.fireAllRules();
        clock.advanceTime(59, TimeUnit.MINUTES);
        session.update(fha, a.setFinished(true));
        session.update(fhb, b.setFinished(true));
        session.fireAllRules();

        logRelations(temporalRelations);

        List<TemporalRelation> finishes = temporalRelations.get(TemporalRelation.Type.FINISHES);
        List<TemporalRelation> finishedby  = temporalRelations.get(TemporalRelation.Type.FINISHED_BY);

        Assert.assertTrue(finishes.size() == 2);
        Assert.assertTrue(finishedby.size() == 2);
    }

    @Test
    public void coincides() {
        KieSession session = newSession();
        SessionPseudoClock clock = session.getSessionClock();
        Map<TemporalRelation.Type, List<TemporalRelation>> temporalRelations = initTemporalRelationLists();
        session.setGlobal("relations", temporalRelations);

        LOG.info("Now running data");

        long start = clock.getCurrentTime();

        Event a = new Event("A", start,1*HOUR );
        Event b = new Event("B", start,1*HOUR );

        FactHandle fha = session.insert(a);
        FactHandle fhb = session.insert(b);
        session.fireAllRules();
        clock.advanceTime(1, TimeUnit.HOURS);
        session.update(fha, a.setFinished(true));
        session.update(fhb, b.setFinished(true));
        session.fireAllRules();

        logRelations(temporalRelations);

        List<TemporalRelation> coincides = temporalRelations.get(TemporalRelation.Type.COINCIDES);

        Assert.assertTrue(coincides.size() == 4);
    }

}



