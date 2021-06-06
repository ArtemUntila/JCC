package jcc;

import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


public class CoverageReporter {

    private final URLClassLoader baseClassLoader;

    private final ClassLoader testsClassLoader;

    private final MemoryClassLoader instrAndTestsClassLoader;

    private final List<String> tests;


    public CoverageReporter(String classesPath, String testsPath) throws IOException {

        Snatch snatch = new Snatch();
        snatch.generateAll(new File(testsPath));

        this.baseClassLoader = new URLClassLoader(new URL[]{ new URL("file:" + classesPath) });
        this.testsClassLoader = snatch.getCompiledClassLoader();
        this.instrAndTestsClassLoader = new MemoryClassLoader();
        this.tests = snatch.getTestsNames();
    }

    public void execute() throws Exception {

        final IRuntime runtime = new LoggerRuntime();

        InputStream original;

        List<String> classes = new ArrayList<>();

        String fullyQualifiedName1 = "jcc.Adder";
        System.out.println("jcc/Adder.class" + " - " + fullyQualifiedName1);
        original = baseClassLoader.getResourceAsStream("jcc/Adder.class");
        final Instrumenter instr = new Instrumenter(runtime);
        final byte[] instrumented = instr.instrument(original, fullyQualifiedName1);
        original.close();
        instrAndTestsClassLoader.addDefinition(fullyQualifiedName1, instrumented);
        classes.add("jcc/Adder.class");

        final RuntimeData data = new RuntimeData();
        runtime.startup(data);

        System.out.println("\nRunning tests...\n");

        for (String testName : tests) {
            original = testsClassLoader.getResourceAsStream(testName);
            instrAndTestsClassLoader.addDefinition(testName, original.readAllBytes());
            final Class<?> testClass = instrAndTestsClassLoader.loadClass(testName);
            invokeStaticTests(testClass);
        }

        System.out.println("\nAnalyzing Coverage...\n");

        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        data.collect(executionData, sessionInfos, false);
        runtime.shutdown();

        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

        for (String className : classes) {
            original = baseClassLoader.getResourceAsStream(className);
            analyzer.analyzeClass(original, getFullyQualifiedName(className));
            original.close();
        }

        for (final IClassCoverage cc : coverageBuilder.getClasses()) {
            String className = cc.getName();
            System.out.printf("Coverage of class %s:%n", className);

            printCounter("instructions", cc.getInstructionCounter());
            printCounter("branches", cc.getBranchCounter());
            printCounter("lines", cc.getLineCounter());
            printCounter("methods", cc.getMethodCounter());
            printCounter("complexity", cc.getComplexityCounter());
        }

    }

    private String getFullyQualifiedName(String name) {
        return name.substring(0, name.length() - 6).replace('/', '.');
    }

    private void printCounter(final String unit, final ICounter counter) {
        final Integer covered = counter.getCoveredCount();
        final Integer total = counter.getTotalCount();
        System.out.printf("%s of %s %s covered%n", covered, total, unit);
    }

    public void invokeStaticTests(Class<?> klass) throws InvocationTargetException, IllegalAccessException {
        System.out.println("Invoking " + klass.getName() + " static tests...");
        Method[] methods = klass.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(org.junit.Test.class) != null) {
                System.out.print(method.getName() + ": ");
                method.invoke(null);
                System.out.println();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new CoverageReporter("src/main/java/jcc", "tests").execute();
    }

    public static class MemoryClassLoader extends ClassLoader {

        private final Map<String, byte[]> definitions = new HashMap<>();

        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.loadClass(name);
        }
    }

}
