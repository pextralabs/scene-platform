package br.ufes.inf.lprm.scene;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by higorborjaille on 13/10/2016.
 */
public class CodeInsert {
    private static final String packagePath = "br.ufes.inf.lprm.scene";

    public static KieSession compileCodeJson(KieServices kServices, File file, String absolutePath) {
        KieContainer kContainer = null;
        try {
            Gson gson = new Gson();
            Type jsonType = new TypeToken<Map<String, Object>>(){}.getType();

            Map<String, Object> myMap = gson.fromJson(new FileReader(file), jsonType);

            kContainer = readCodeJson(kServices, myMap, absolutePath);
        } catch (IOException e) {
            System.out.println("Could not open " + file.getName());
        }
        return kContainer.newKieSession();
    }

    public static String refactorAppName(String name) {
        return name.toLowerCase().replace(" ", "-").replace(".", "-");
    }

    public static KieContainer readCodeJson(KieServices kServices, Map<String, Object> map, String absolutePath) {
        // Instantiating a KieFileSystem
        KieFileSystem kFileSystem = kServices.newKieFileSystem();

        String appname = refactorAppName((String) map.get("application"));
        List<Map<String, String>> files = (List<Map<String, String>>) map.get("files");

        ReleaseId releaseId = kServices.newReleaseId(absolutePath.replace("-", ".") + appname,
                appname, map.get("version") + "-SNAPSHOT");
        for (Map<String, String> f: files) {
            String name = f.get("name");
            String path = absolutePath + "/" + f.get("package").replace(".", "/") + "/" + name;

            kFileSystem.write(path, f.get("content"));
        }

        KieBuilder kieBuilder = kServices.newKieBuilder(kFileSystem);
        kieBuilder.buildAll();
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" +
                    kieBuilder.getResults().toString());
        }

        return kServices.newKieContainer(releaseId);

    }

    public static final void main(String[] args) {
        // load up the knowledge base
        KieServices ks = KieServices.Factory.get();
        KieSession kSession = compileCodeJson(ks, new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/insertdrl.json"), "/Users/hborjaille/Projects/scene-platform/scene-server/src/main/resources");

        File file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/insert.json");
        DataInsert.compileDataJson(kSession, file, JsonType.INSERT, packagePath);
        kSession.fireAllRules();

        file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/update.json");
        DataInsert.compileDataJson(kSession, file, JsonType.UPDATE, packagePath);
        kSession.fireAllRules();

        file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/delete.json");
        DataInsert.compileDataJson(kSession, file, JsonType.DELETE, packagePath);
        kSession.fireAllRules();

        file  = new File("/Users/hborjaille/Projects/scene-platform/scene-server/src/main/mock/insert2.json");
        DataInsert.compileDataJson(kSession, file, JsonType.INSERT, packagePath);
        kSession.fireAllRules();
    }
}
