package br.ufes.inf.lprm.scene.test.equality;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import br.ufes.inf.lprm.scene.test.equality.model.Person;
import br.ufes.inf.lprm.scene.test.equality.utils.Pair;
import br.ufes.inf.lprm.situation.model.Situation;
import javassist.ClassPool;
import org.junit.Assert;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.time.SessionPseudoClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class EqualityTest {
    static final Logger LOG = LoggerFactory.getLogger(EqualityTest.class);

    private void KieBaseCheck(KieBase kieBase) {
        LOG.info("There should be rules: ");
        for ( KiePackage kp : kieBase.getKiePackages() ) {
            for (Rule rule : kp.getRules()) LOG.info("kp " + kp + " rule " + rule.getName());
        }
    }

    private KieBase buildKieBase(KieBaseConfiguration config, String name) {
        KieServices kieServices = KieServices.Factory.get();

        KieContainer kContainer = kieServices.getKieClasspathContainer();
        Results verifyResults = kContainer.verify();

        for (Message m : verifyResults.getMessages()) LOG.info("{}", m);

        LOG.info("Creating kieBase");

        KieBase kieBase = kContainer.newKieBase(name, config);

        KieBaseCheck(kieBase);

        return kieBase;
    }

    private void update(Collection<Pair<KieSession, FactHandle>> sessionHandles, Object object) {
        sessionHandles.forEach(
                sessionHandle ->
                    sessionHandle.getFirst().update(sessionHandle.getSecond(), object)
        );
    }

    private void fire(Collection<Pair<KieSession, FactHandle>> pairs)  {
        pairs.forEach(
                pair -> pair.getFirst().fireAllRules()
        );
    }

    private void advance(Collection<Pair<KieSession, FactHandle>> pairs, int time, TimeUnit unit){
        pairs.forEach(
                pair -> ((SessionPseudoClock) pair.getFirst().getSessionClock()).advanceTime(time, unit)
        );
    }

    @Test
    public void equality() {

        KieBaseConfiguration kbConfig1 = KieServices.Factory.get().newKieBaseConfiguration();
        kbConfig1.setOption(EventProcessingOption.STREAM);

        KieBaseConfiguration kbConfig2 = KieServices.Factory.get().newKieBaseConfiguration();
        kbConfig2.setOption(EventProcessingOption.STREAM);
        kbConfig2.setOption(EqualityBehaviorOption.EQUALITY);

        KieSessionConfiguration sessionConfig = KieServices.Factory.get().newKieSessionConfiguration();
        sessionConfig.setOption(ClockTypeOption.get("pseudo"));

        LOG.info("Creating kieSession");
        KieSession session1 = buildKieBase(kbConfig1, "equality-test")
                                .newKieSession(sessionConfig, null);

        Logger logger1 = LoggerFactory.getLogger("SESSION 1");
        //session1.addEventListener(new SCENESessionListener( LoggerFactory.getLogger("SESSION 1") ));
        ArrayList<Situation> situations1 = new ArrayList<>();
        session1.setGlobal("situations", situations1);
        session1.setGlobal("logger", logger1);
        new SceneApplication(ClassPool.getDefault(), session1, "app1");

        KieSession session2 = buildKieBase(kbConfig2, "equality-test")
                .newKieSession(sessionConfig, null);

        Logger logger2 = LoggerFactory.getLogger("SESSION 2");
        //session2.addEventListener(new SCENESessionListener( LoggerFactory.getLogger("SESSION 2") ));
        ArrayList<Situation> situations2 = new ArrayList<>();
        session2.setGlobal("situations", situations2);
        session2.setGlobal("logger", logger2);
        new SceneApplication(ClassPool.getDefault(), session2, "app2");

        Person person = new Person("John").setTemperature(36.5);
        FactHandle fh1 = session1.insert(person);
        FactHandle fh2 = session2.insert(person);

        ArrayList<Pair<KieSession, FactHandle>> pairs = new ArrayList<>();
        pairs.add(new Pair<>(session1, fh1));
        pairs.add(new Pair<>(session2, fh2));

        advance(pairs, 30, TimeUnit.MINUTES);
        update(pairs, person.setTemperature(37.6));
        fire(pairs);

        advance(pairs, 30, TimeUnit.MINUTES);
        update(pairs, person.setTemperature(37.4));
        fire(pairs);

        advance(pairs, 30, TimeUnit.MINUTES);
        update(pairs, person.setTemperature(37.6));
        fire(pairs);

        advance(pairs, 30, TimeUnit.MINUTES);
        update(pairs, person.setTemperature(37.4));
        fire(pairs);

        LOG.debug("size: {}", situations1.size());
        LOG.debug("size: {}", situations2.size());

        Assert.assertTrue(situations1.size() == situations2.size());

    }

}



