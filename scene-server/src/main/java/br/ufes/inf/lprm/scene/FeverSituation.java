package br.ufes.inf.lprm.scene;

import java.io.File;

/**
 * Created by hborjaille on 10/15/16.
 */
public class FeverSituation {
    public static final void main(String[] args) {

        ClassLoader classLoader = FeverSituation.class.getClassLoader();

        File file = new File(classLoader.getResource("mock/insertfeverapp.json").getFile());
        CodeInsert code = new CodeInsert(file);

        try {
            file  = new File(classLoader.getResource("mock/insertfeverdata.json").getFile());
            code.insertData(file);

            Thread.sleep(1000);

            file  = new File(classLoader.getResource("mock/updatefeverdata1.json").getFile());

            code.updateData(file);

            Thread.sleep(1000);

            file  = new File(classLoader.getResource("mock/updatefeverdata2.json").getFile());
            code.updateData(file);

            Thread.sleep(1000);
            file  = new File(classLoader.getResource("mock/updatefeverdata3.json").getFile());
            code.updateData(file);

            Thread.sleep(1000);

            file  = new File(classLoader.getResource("mock/updatefeverdata2.json").getFile());
            code.updateData(file);

            Thread.sleep(1000);
            file  = new File(classLoader.getResource("mock/updatefeverdata1.json").getFile());
            code.updateData(file);

            Thread.sleep(1000);
            file  = new File(classLoader.getResource("mock/updatefeverdata4.json").getFile());
            code.updateData(file);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        file = new File(classLoader.getResource("mock/insertfeverapp2.json").getFile());
        CodeInsert code2 = new CodeInsert(file);

        try {
            file  = new File(classLoader.getResource("mock/insertfeverdata.json").getFile());
            code2.insertData(file);

            Thread.sleep(1000);

            file  = new File(classLoader.getResource("mock/updatefeverdata1.json").getFile());
            code2.updateData(file);

            Thread.sleep(1000);

            file  = new File(classLoader.getResource("mock/updatefeverdata2.json").getFile());
            code2.updateData(file);

            Thread.sleep(1000);

            file  = new File(classLoader.getResource("mock/updatefeverdata3.json").getFile());
            code2.updateData(file);

            Thread.sleep(1000);

            file  = new File(classLoader.getResource("mock/updatefeverdata2.json").getFile());
            code2.updateData(file);

            Thread.sleep(1000);

            file  = new File(classLoader.getResource("mock/updatefeverdata1.json").getFile());
            code2.updateData(file);

            Thread.sleep(1000);

            file  = new File(classLoader.getResource("mock/updatefeverdata4.json").getFile());
            code2.updateData(file);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
