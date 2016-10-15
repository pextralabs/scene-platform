package br.ufes.inf.lprm.scene;

import br.ufes.inf.lprm.scene.base.listeners.SCENESessionListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.drools.core.ClockType;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by higorborjaille on 13/10/2016.
 */
public class CodeInsert {
    private KieServices kServices;
    private KieSession kSession;
    private ArrayList<String> packages;
    private DataInsert dataInsert;

    public CodeInsert(File file) {
        packages = new ArrayList<String>();
        kServices = KieServices.Factory.get();
        compileCodeJson(file);
        kSession.addEventListener(new SCENESessionListener());
        dataInsert = new DataInsert(kSession, packages);
    }

    public void compileCodeJson(File file) {
        try {
            Gson gson = new Gson();
            Type jsonType = new TypeToken<Map<String, Object>>(){}.getType();

            Map<String, Object> myMap = gson.fromJson(new FileReader(file), jsonType);

            if(myMap.get("application") == null) {
                System.out.println(file.getName() + " not compatible.");
            } else {
                readCodeJson(myMap);
            }
        } catch (IOException e) {
            System.out.println("Could not open " + file.getName());
        }
    }

    public String refactorAppName(String name) {
        return name.toLowerCase().replace(" ", "-").replace(".", "-");
    }

    public void readCodeJson(Map<String, Object> map) {
        // Instantiating a KieFileSystem and KieModuleModel
        KieFileSystem kFileSystem = kServices.newKieFileSystem();
        KieModuleModel kieModuleModel = kServices.newKieModuleModel();

        String appname = refactorAppName((String) map.get("application"));

        ReleaseId releaseId = kServices.newReleaseId("co.pextra", appname, (String) map.get("version"));
        kFileSystem.generateAndWritePomXML(releaseId);
        KieBaseModel sceneBase = kieModuleModel.newKieBaseModel("sceneKieBase");
        sceneBase =  kServices.getKieClasspathContainer().getKieBaseModel("sceneKieBase");
        KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel(appname);
        kieBaseModel.addInclude("sceneKieBase");

        List<Map<String, String>> files = (List<Map<String, String>>) map.get("files");

        List<String> packList = new ArrayList<String>();

        for (Map<String, String> f: files) {
            String name = f.get("name");
            String pakage = f.get("package");

            packages.add(pakage);
            kieBaseModel.addPackage(pakage);

            String path = "src/main/resources/" + pakage.replace(".", "/") + "/" + name;

            Resource resource = kServices.getResources().newByteArrayResource(f.get("content").getBytes()).setResourceType(ResourceType.DRL);
            kFileSystem.write(path, resource);
        }

        KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel(appname + ".session");
        kieSessionModel.setClockType(ClockTypeOption.get(ClockType.REALTIME_CLOCK.getId()))
                .setType(KieSessionModel.KieSessionType.STATEFUL);

        kFileSystem.writeKModuleXML(kieModuleModel.toXML());

        KieBuilder kbuilder = kServices.newKieBuilder(kFileSystem).buildAll();
        if (kbuilder.getResults().hasMessages()) {
            throw new IllegalArgumentException("Coudln't build knowledge module" + kbuilder.getResults());
        }

        KieModule kModule = kbuilder.getKieModule();
        KieContainer kContainer = kServices.newKieContainer(kModule.getReleaseId());

        kSession = kContainer.newKieSession(appname + ".session");
    }

    public void insertData(File file) {
        dataInsert.compileDataJson(file, JsonType.INSERT);
    }

    public void updateData(File file) {
        dataInsert.compileDataJson(file, JsonType.UPDATE);
    }

    public void deleteData(File file) {
        dataInsert.compileDataJson(file, JsonType.DELETE);
    }

    public KieSession getkSession() {
        return kSession;
    }

    public void setkSession(KieSession kSession) {
        this.kSession = kSession;
    }

    public static final void main(String[] args) {

        File file = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/insertfeverapp.json");
        CodeInsert code = new CodeInsert(file);

        try {
            file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/insertfeverdata.json");
            code.insertData(file);

            Thread.sleep(3000);

            file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/updatefeverdata1.json");
            code.updateData(file);

            Thread.sleep(3000);

            file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/updatefeverdata2.json");
            code.updateData(file);

            Thread.sleep(3000);

            file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/updatefeverdata3.json");
            code.updateData(file);

            Thread.sleep(3000);

            file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/updatefeverdata2.json");
            code.updateData(file);

            Thread.sleep(3000);

            file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/updatefeverdata1.json");
            code.updateData(file);

            Thread.sleep(3000);

            file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/updatefeverdata4.json");
            code.updateData(file);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
