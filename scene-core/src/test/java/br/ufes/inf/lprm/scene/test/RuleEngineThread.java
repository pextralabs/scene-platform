package br.ufes.inf.lprm.scene.test;

import org.kie.api.runtime.KieSession;

public class RuleEngineThread extends Thread {
    private KieSession ksession;
    public RuleEngineThread(KieSession ksession) {
        this.ksession = ksession;
    }
    public void run() {
        this.ksession.fireUntilHalt();
    }
}