package br.ufes.inf.lprm.scene;

import java.io.File;

/**
 * Created by hborjaille on 10/15/16.
 */
public class FeverSituation {
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
