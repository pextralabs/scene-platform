package br.ufes.inf.lprm.scene.test.complementary;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import br.ufes.inf.lprm.scene.test.RuleEngineThread;
import br.ufes.inf.lprm.scene.test.model.Person;
import br.ufes.inf.lprm.situation.model.Situation;
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
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.toIntExact;

public class ComplementaryTest {
    static final Logger LOG = LoggerFactory.getLogger(ComplementaryTest.class);


    private void KieBaseCheck(KieBase kieBase) {
        LOG.info("There should be rules: ");
        for ( KiePackage kp : kieBase.getKiePackages() ) {
            for (Rule rule : kp.getRules()) LOG.info("kp " + kp + " rule " + rule.getName());
        }
    }

    @Test
    public void test() throws InterruptedException {
        KieServices kieServices = KieServices.Factory.get();

        KieContainer kContainer = kieServices.getKieClasspathContainer();
        Results verifyResults = kContainer.verify();
        for (Message m : verifyResults.getMessages()) LOG.info("{}", m);

        LOG.info("Creating kieBase");

        KieBaseConfiguration config = KieServices.Factory.get().newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);
        KieBase kieBase = kContainer.newKieBase("complementary-test", config);

        KieBaseCheck(kieBase);

        LOG.info("Creating kieSession");
        KieSession session = kieBase.newKieSession();

        new SceneApplication("fraud-scenario", session);

        session.addEventListener(new SCENESessionListener());

        final RuleEngineThread ruleEngineThread = new RuleEngineThread(session);
        ruleEngineThread.start();

        LOG.info("Now running data");

        Person person = new Person(1, "John");
        FactHandle handle = session.insert(person);

        int loops = 5;
        int interval = 200;

        for (int i = 0; i < loops; i++) {

            //dead
            Thread.sleep(interval);
            person.setHeartrate(0);
            session.update(handle, person);

            //alive
            Thread.sleep(interval);
            person.setHeartrate(80);
            session.update(handle, person);

        }

        FactType aliveType  = kieBase.getFactType("br.ufes.inf.lprm.scene.test.complementary", "Alive");
        FactType deadType   = kieBase.getFactType("br.ufes.inf.lprm.scene.test.complementary", "Dead");


        ArrayList<Situation> allSituations      =  new ArrayList<Situation>((Collection<Situation>) session.getObjects(new ClassObjectFilter(Situation.class)));
        ArrayList<Situation> aliveSituations    =  new ArrayList<Situation>((Collection<Situation>) session.getObjects(new ClassObjectFilter(aliveType.getFactClass())));
        ArrayList<Situation> deadSituations     =  new ArrayList<Situation>((Collection<Situation>) session.getObjects(new ClassObjectFilter(deadType.getFactClass())));

        Thread.sleep(3000);

        Assert.assertEquals(loops + 1, aliveSituations.size());
        Assert.assertEquals(loops, deadSituations.size());

        LOG.info("alive: " + aliveSituations.size());
        LOG.info("dead: " + deadSituations.size());

        allSituations.sort(
                new Comparator<Situation>() {
                    @Override
                    public int compare(Situation sit1, Situation sit2) {
                        return toIntExact(sit1.getActivation().getTimestamp() - sit2.getActivation().getTimestamp());
                    }
                }
        );

        Situation last = null;

        for (Situation sit : allSituations) {

            /*if (sit.isActive()) {
                LOG.info(sit.getType().getName() + "\t(start: " + sit.getActivation().getTimestamp() + " end: ???)");
            } else {
                LOG.info(sit.getType().getName() + "\t(start: " + sit.getActivation().getTimestamp() + " end: " + sit.getDeactivation().getTimestamp() + ")");
            }*/

            if (last != null) {
                Assert.assertTrue( last.getDeactivation().getTimestamp() <= sit.getActivation().getTimestamp());
            }
            last = sit;

        }

        LOG.info("Final checks");
    }
}
