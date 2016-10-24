package br.ufes.inf.lprm.scene.examples.feverjson;

import br.ufes.inf.lprm.scene.SceneApplication;

import java.io.File;
import java.util.Scanner;

/**
 * Created by hborjaille on 10/15/16.
 */
public class FeverSituation {
    public static final void main(String[] args) {

        ClassLoader classLoader = FeverSituation.class.getClassLoader();
        SceneApplication app1 = new SceneApplication();

        String content = null;
        try {
            content = new Scanner(new File(classLoader.getResource("mock/insertfeverapp.json").getFile())).useDelimiter("\\Z").next();
            app1.insertCode(content);

            content = new Scanner(new File(classLoader.getResource("mock/insertfeverdata.json").getFile())).useDelimiter("\\Z").next();
            app1.insertData(content);

            Thread.sleep(1000);

            content = new Scanner(new File(classLoader.getResource("mock/updatefeverdata1.json").getFile())).useDelimiter("\\Z").next();

            app1.updateData(content);

            Thread.sleep(1000);

            content = new Scanner(new File(classLoader.getResource("mock/updatefeverdata2.json").getFile())).useDelimiter("\\Z").next();
            app1.updateData(content);

            Thread.sleep(1000);

            content = new Scanner(new File(classLoader.getResource("mock/updatefeverdata3.json").getFile())).useDelimiter("\\Z").next();
            app1.updateData(content);

            Thread.sleep(1000);

            content = new Scanner(new File(classLoader.getResource("mock/updatefeverdata2.json").getFile())).useDelimiter("\\Z").next();
            app1.updateData(content);

            Thread.sleep(1000);

            content = new Scanner(new File(classLoader.getResource("mock/updatefeverdata1.json").getFile())).useDelimiter("\\Z").next();
            app1.updateData(content);

            Thread.sleep(1000);

            content = new Scanner(new File(classLoader.getResource("mock/updatefeverdata4.json").getFile())).useDelimiter("\\Z").next();
            app1.updateData(content);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
