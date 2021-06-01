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

    public void execute(File file) throws Exception {

        String[] data = getProgramText(file);
        String program = data[1];
        String name = data[0];

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        JavaFileObject compilationUnit =
                new StringJavaFileObject(name, program);

        SimpleJavaFileManager fileManager =
                new SimpleJavaFileManager(compiler.getStandardFileManager(null, null, null));

        JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                null, fileManager, null, null, null, Arrays.asList(compilationUnit));

        compilationTask.call();

        CompiledClassLoader classLoader =
                new CompiledClassLoader(fileManager.getGeneratedOutputFiles());

        Class<?> klass = classLoader.loadClass(name);
        invokeStaticTests(klass);
    }

    public void executeAll(File directory) throws Exception {
        if (!directory.isDirectory()) throw new IllegalArgumentException();
        File[] files = directory.listFiles();
        for (File file : files) {
            execute(file);
        }
    }

    public static void main(String[] args) throws Exception {
        new Snatch().executeAll(new File("tests/jcc"));
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
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            Iterator<ClassJavaFileObject> itr = files.iterator();
            while (itr.hasNext()) {
                ClassJavaFileObject file = itr.next();
                if (file.getClassName().equals(name)) {
                    itr.remove();
                    byte[] bytes = file.getBytes();
                    return super.defineClass(name, bytes, 0, bytes.length);
                }
            }
            return super.findClass(name);
        }
    }

    public String[] getProgramText(File program) throws IOException {
        List<String> lines = Files.readAllLines(program.toPath());
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.contains("package")) continue;
            sb.append(line).append('\n');
        }
        return new String[]{program.getName().replaceAll("\\.java", ""), sb.toString()};
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
