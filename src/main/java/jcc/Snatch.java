package jcc;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;

/**
 * Origin: https://gist.github.com/chrisvest/9873843
 */

public class Snatch {

    public static CompiledClassLoader compiledClassLoader;

    /*public void execute(File file) throws Exception {

        CompiledClassLoader classLoader =
                new CompiledClassLoader();

        Class<?> klass = classLoader.loadClass(name);
        invokeStaticTests(klass);
    }*/

    private List<ClassJavaFileObject> getGeneratedClasses(File file) throws Exception {

        String program = getProgramText(file);
        String name = getJavaFileName(file);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        JavaFileObject compilationUnit =
                new StringJavaFileObject(name, program);

        SimpleJavaFileManager fileManager =
                new SimpleJavaFileManager(compiler.getStandardFileManager(null, null, null));

        JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                null, fileManager, null, null, null, Collections.singletonList(compilationUnit));

        compilationTask.call();

        return fileManager.getGeneratedOutputFiles();
    }

    static List<ClassJavaFileObject> list = new ArrayList<>();

    public void generateAll(File directory) throws Exception {
        if (!directory.isDirectory()) throw new IllegalArgumentException();
        File[] files = directory.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                list.addAll(getGeneratedClasses(file));
            } else {
                generateAll(file);
            }
        }
    }

    public CompiledClassLoader getCompiledClassLoader() {
        return new CompiledClassLoader(list);
    }

    public static void main(String[] args) throws Exception {
        File file = new File("tests"); //Adder_init_235991254.java
        Snatch snatch = new Snatch();
        snatch.generateAll(file);
        List<ClassJavaFileObject> list = snatch.list;
        list.forEach(f -> System.out.println(f.getClassName()));
        CompiledClassLoader classLoader = snatch.getCompiledClassLoader();
        for (ClassJavaFileObject f : list) {
            Class<?> klass = classLoader.loadClass(f.getClassName());
            System.out.println(f.getName());
            snatch.invokeStaticTests(klass);
        }
    }

    public static class StringJavaFileObject extends SimpleJavaFileObject {
        private final String code;

        public StringJavaFileObject(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
                    Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return code;
        }
    }

    private static class ClassJavaFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream outputStream;
        private final String className;

        protected ClassJavaFileObject(String className, Kind kind) {
            super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
            this.className = className;
            outputStream = new ByteArrayOutputStream();
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return outputStream;
        }

        public byte[] getBytes() {
            return outputStream.toByteArray();
        }

        public String getClassName() {
            return className;
        }
    }

    private static class SimpleJavaFileManager extends ForwardingJavaFileManager {
        private final List<ClassJavaFileObject> outputFiles;

        protected SimpleJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
            outputFiles = new ArrayList<ClassJavaFileObject>();
        }

        @Override
        public JavaFileObject getJavaFileForOutput(
                Location location, String className, JavaFileObject.Kind kind, FileObject sibling
        ) throws IOException {
            ClassJavaFileObject file = new ClassJavaFileObject(className, kind);
            outputFiles.add(file);
            return file;
        }

        public List<ClassJavaFileObject> getGeneratedOutputFiles() {
            return outputFiles;
        }
    }

    private static class CompiledClassLoader extends ClassLoader {
        private final List<ClassJavaFileObject> files;

        private CompiledClassLoader(List<ClassJavaFileObject> files) {
            this.files = files;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            for (ClassJavaFileObject file : files) {
                if (file.getClassName().equals(name)) {
                    byte[] bytes = file.getBytes();
                    return super.defineClass(name, bytes, 0, bytes.length);
                }
            }
            return super.loadClass(name);
        }
    }

    public String getProgramText(File program) throws IOException {
        List<String> lines = Files.readAllLines(program.toPath());
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.contains("package")) continue;
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    public String getJavaFileName(File javaFile) {
        return javaFile.getName().replace(".java", "");
    }

    public void invokeStaticTests(Class<?> klass) throws InvocationTargetException, IllegalAccessException {
        System.out.println("\nInvoking " + klass.getName() + " static tests...");
        Method[] methods = klass.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(org.junit.Test.class) != null) {
                System.out.print(method.getName() + ": ");
                method.invoke(null);
                System.out.println();
            }
        }
    }

}