package br.ufes.inf.lprm.scene.mqc;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import org.kie.api.KieServices;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

/**
 * This is a sample class to launch a rule.
 */

class RuleEngineThread extends Thread {
    private KieSession ksession;
    public RuleEngineThread(KieSession ksession) {
        this.ksession = ksession;
    }
    public void run() {
        this.ksession.fireUntilHalt();
    }
}

public class Main {

    public static final void main(String[] args) {
        try {

            // load up the knowledge base
            KieServices ks = KieServices.Factory.get();
            KieContainer kContainer = ks.getKieClasspathContainer();
            KieSession kSession = kContainer.newKieSession("br.ufes.inf.lprm.scene.mqc.session");
            kSession.addEventListener(new SCENESessionListener());

            SceneApplication app = new SceneApplication("Fever", kSession);

            final RuleEngineThread eng = new RuleEngineThread(kSession);
            eng.start();

            FactType sensorType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.mqc", "Sensor");
            FactType readingType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.mqc", "SensorReading");

            Object sensor = sensorType.newInstance();
            sensorType.set(sensor, "id", 1);
            sensorType.set(sensor, "label", "Sensor 1");
            kSession.insert(sensor);

            Object reading;

            int count = 1;
            while (true) {

                reading = readingType.newInstance();
                readingType.set(reading, "id", count++);
                readingType.set(reading, "source", sensor);
                readingType.set(reading, "timestamp", kSession.getSessionClock().getCurrentTime());
                readingType.set(reading, "value", 1);
                kSession.insert(reading);

                Thread.sleep(3000);

                reading = readingType.newInstance();
                readingType.set(reading, "id", count++);
                readingType.set(reading, "source", sensor);
                readingType.set(reading, "timestamp", kSession.getSessionClock().getCurrentTime());
                readingType.set(reading, "value", 3);
                kSession.insert(reading);;

                Thread.sleep(3000);

                reading = readingType.newInstance();
                readingType.set(reading, "id", count++);
                readingType.set(reading, "source", sensor);
                readingType.set(reading, "timestamp", kSession.getSessionClock().getCurrentTime());
                readingType.set(reading, "value", 5);
                kSession.insert(reading);

                Thread.sleep(3000);

                reading = readingType.newInstance();
                readingType.set(reading, "id", count++);
                readingType.set(reading, "source", sensor);
                readingType.set(reading, "timestamp", kSession.getSessionClock().getCurrentTime());
                readingType.set(reading, "value", 6);
                kSession.insert(reading);

                Thread.sleep(3000);

                reading = readingType.newInstance();
                readingType.set(reading, "id", count++);
                readingType.set(reading, "source", sensor);
                readingType.set(reading, "timestamp", kSession.getSessionClock().getCurrentTime());
                readingType.set(reading, "value", 5);
                kSession.insert(reading);

                Thread.sleep(3000);

                reading = readingType.newInstance();
                readingType.set(reading, "id", count++);
                readingType.set(reading, "source", sensor);
                readingType.set(reading, "timestamp", kSession.getSessionClock().getCurrentTime());
                readingType.set(reading, "value", 4);
                kSession.insert(reading);

                Thread.sleep(3000);

            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
