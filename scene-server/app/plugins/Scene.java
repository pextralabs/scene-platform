package plugins;

import br.ufes.inf.lprm.scene.SceneApplication;
import org.drools.compiler.kie.builder.impl.KieServicesImpl;
import org.kie.api.KieServices;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import play.Environment;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Scene {

    public final KieSession kieSession;

    @Inject
    public Scene(ApplicationLifecycle lifecycle, Environment environment) {
        KieServices kieServices = new KieServicesImpl();
        KieContainer kc = kieServices.getKieClasspathContainer();
        kieSession = kc.newKieSession("HelloWorldKS");

        SceneApplication app = new SceneApplication("awesome-app", kieSession);

        // uncomment these to enable debugging
        //kieSession.addEventListener(new DebugAgendaEventListener());
        //kieSession.addEventListener(new DebugRuleRuntimeEventListener());


        lifecycle.addStopHook(() -> {
            kieSession.destroy();
            return F.Promise.pure(null);
        });
    }

}
