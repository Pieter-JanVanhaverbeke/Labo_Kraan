package be.kul.gantry.domain;

import java.io.File;
import java.io.IOException;

public class TestMain {
    public static void main(String[] args) throws IOException {
        File dataDir = new File("src/be/kul/gantry/domain/data");
        System.out.println(dataDir.getAbsolutePath());
        File[] toRun = dataDir.listFiles();

        File resultDir = new File("src/be/kul/gantry/domain/result");
        File resultFile;

        for (File input: toRun) {
            resultFile = new File(resultDir.getAbsolutePath(),
                    input.getName().replace("json", "csv"));
            Main.main(new String[]{input.getAbsolutePath(), resultFile.getAbsolutePath()});
            Runtime.getRuntime().exec(
                    new String[]{"cmd", "/c", "start", "cmd", "/k", String.format(
                            "java -jar \"validator-v7.jar\" %s %s",
                            input.getAbsolutePath(),
                            resultFile.getAbsolutePath())
                    }
            );
        }
    }
}
