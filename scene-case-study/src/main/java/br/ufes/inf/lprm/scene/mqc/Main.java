package br.ufes.inf.lprm.scene.mqc;

import br.ufes.inf.lprm.scene.SceneApplication;
import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import org.kie.api.KieServices;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.time.SessionClock;

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


            FactType personType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.mqc", "Person");
            FactType freezerType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.mqc", "Freezer");
            FactType sensorType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.mqc", "Sensor");
            FactType temperatureType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.mqc", "Temperature");
            FactType locationType = kSession.getKieBase().getFactType("br.ufes.inf.lprm.scene.mqc", "Location");

            Object doctor = personType.newInstance();
            personType.set(doctor, "id", 1);
            personType.set(doctor, "name", "Doctor");
            FactHandle doctorFH = kSession.insert(doctor);

            Object freezer = freezerType.newInstance();
            freezerType.set(freezer, "id", 1);
            freezerType.set(freezer, "label", "Doctor_Freezer");
            freezerType.set(freezer, "owner", doctor);
            FactHandle freezerFH = kSession.insert(freezer);

            Object freezerThermometer = sensorType.newInstance();
            sensorType.set(freezerThermometer, "id", 1);
            sensorType.set(freezerThermometer, "label", "Freezer_Thermometer_01");
            sensorType.set(freezerThermometer, "type", "temperature");
            sensorType.set(freezerThermometer, "bearer", freezer);
            kSession.insert(freezerThermometer);

            Object freezerGps = sensorType.newInstance();
            sensorType.set(freezerGps, "id", 2);
            sensorType.set(freezerGps, "label", "Freezer_GPS_01");
            sensorType.set(freezerGps, "type", "location");
            sensorType.set(freezerGps, "bearer", freezer);
            kSession.insert(freezerGps);

            Object doctorGps = sensorType.newInstance();
            sensorType.set(doctorGps, "id", 3);
            sensorType.set(doctorGps, "label", "Doctor_GPS_01");
            sensorType.set(doctorGps, "type", "location");
            sensorType.set(doctorGps, "bearer", doctor);
            kSession.insert(doctorGps);
            
            Object reading;
            int count = 1;

            SessionClock clock = kSession.getSessionClock();
            
            reading = locationType.newInstance();
            locationType.set(reading, "id", count++);
            locationType.set(reading, "source", freezerGps);
            locationType.set(reading, "timestamp", clock.getCurrentTime());
            locationType.set(reading, "latitude", 38.898556);
            locationType.set(reading, "longitude", -77.037852);

            kSession.insert(reading);

            freezerType.set(freezer, "location", reading);
            kSession.update(freezerFH, freezer);
            
            while (true) {

                reading = locationType.newInstance();
                locationType.set(reading, "id", count++);
                locationType.set(reading, "source", doctorGps);
                locationType.set(reading, "timestamp", clock.getCurrentTime());
                locationType.set(reading, "latitude", 38.898556);
                locationType.set(reading, "longitude", -77.037852);

                kSession.insert(reading);
                personType.set(doctor, "location", reading);
                //kSession.update(doctorFH, doctor);

                Thread.sleep(5000);

                reading = locationType.newInstance();
                locationType.set(reading, "id", count++);
                locationType.set(reading, "source", doctorGps);
                locationType.set(reading, "timestamp", clock.getCurrentTime());
                locationType.set(reading, "latitude", 38.897147);
                locationType.set(reading, "longitude", -77.043934);
                kSession.insert(reading);

                kSession.insert(reading);
                personType.set(doctor, "location", reading);
                //kSession.update(doctorFH, doctor);

                Thread.sleep(3000);

            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
