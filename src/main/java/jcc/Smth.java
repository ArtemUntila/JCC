package jcc;

import java.net.URL;
import java.net.URLClassLoader;

public class Smth {
    public static void main(String[] args) throws Exception {
        String jarPath = "jcc-test.jar";
        System.setProperty("java.class.path",
                System.getProperty("java.class.path") + System.getProperty("path.separator") + jarPath);
        System.out.println(System.getProperty("java.class.path"));
        URLClassLoader classLoader = new URLClassLoader(new URL[]{ new URL("file:" + jarPath) });
        Snatch snatch = new Snatch(classLoader);
        snatch.main();
        CoverageReporter coverageReporter = new CoverageReporter(classLoader, "tests");
        coverageReporter.execute();
    }
}
