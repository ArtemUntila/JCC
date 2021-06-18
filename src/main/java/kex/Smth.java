package kex;

import java.net.URL;
import java.net.URLClassLoader;

public class Smth {

    public static void main(String[] args) throws Exception { //METHOD(klass=jcc/Adder, method=sum)
        String jarPath = "find.jar";
        System.setProperty("java.class.path",
                System.getProperty("java.class.path") + System.getProperty("path.separator") + jarPath);
        System.out.println(System.getProperty("java.class.path"));
        URLClassLoader classLoader = new URLClassLoader(new URL[]{ new URL("file:" + jarPath) });
        CoverageReporter coverageReporter = new CoverageReporter("scan", classLoader);
        //System.out.println(coverageReporter.execute("METHOD(klass=jcc1/Adder, method=sum)"));
        //System.out.println(coverageReporter.execute("CLASS(klass=jcc1/Adder)"));
        //System.out.println(coverageReporter.execute("CLASS(klass=jcc1/Multiplexer)"));
        System.out.println(coverageReporter.execute("nothing@Package"));
    }

}
