package jcc;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class CompilerTest {

    public void compileToRunnable(String path) throws IOException {

        List<String> lines = Files.readAllLines(Path.of(path));

        String pack = path.replaceAll("([A-Za-z0-9_]+)\\.java", ""); // путь к пакету
        System.out.println("pack = " + pack);

        String name = path.replace(pack, "").replace(".java", "") + "_Runnable"; // имя нового Runnable-класса
        String namePath = "src/main/java/tests/" + name + ".java"; // путь к новому Runnable-классу
        System.out.println("name = " + name);

        BufferedWriter bW = Files.newBufferedWriter(Path.of(namePath));

        List<String> methods = new ArrayList<>(); // список с методами класса, которые нужно запустить в run()
        int braceCounter = 0;
        boolean insideClass = false;
        for (int i = 0; i < lines.size() - 1; i++) {
            String line = lines.get(i);
            if (line.contains("package")) {
                line = "package tests;";
            }
            if (line.contains("class")) { // меняем имя класса и ставим реализацию Runnable
                line = line.replaceAll("class [A-Za-z0-9_]+", "class " + name).
                        replace("{", "implements Runnable {");
                insideClass = true;
            }
            if (line.contains("public  void") && lines.get(i - 1).contains("@Test")) { // добавляем метод, необходимый для запуска
                methods.add(line.split("(\\s+public  void\\s+)|(\\s)")[1]);
            }
            if (line.contains("{"))
                braceCounter++;
            if(line.contains("}"))
                braceCounter--;
            if (braceCounter == 0 && insideClass) break;
            bW.write(line);
            bW.newLine();
        }

        bW.write(addRunMethod(methods)); // добавление метода run()
        bW.close();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, namePath);  // компилируем в тот же пакет
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        File file = new File("tests/jcc/Adder_init_235991254.java");
        List<String> program = Files.readAllLines(file.toPath());
        StringBuilder sb = new StringBuilder();
        for (String line : program) {
            sb.append(line).append('\n');
        }
        System.out.println(sb);
    }

    public void invokeStaticTests(Class<?> klass) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = klass.getMethods();
        for (Method method : methods) {
            if (method.getAnnotation(org.junit.Test.class) != null) {
                System.out.println(method.getName());
                method.invoke(null);
            }
        }
    }

    private String addRunMethod(List<String> methods) { // добавление метода run()
        StringBuilder run = new StringBuilder("\n    @Override\n    public void run() {" +
                "\n        try {");
        for (String method : methods) {
            run.append("\n        ").append(method).append(";");
        }
        run.append("\n        } catch (Throwable throwable) {" +
                "\n            throwable.printStackTrace();" +
                "\n        }" +
                "\n    }" +
                "\n\n}");
        return run.toString();
    }
}
