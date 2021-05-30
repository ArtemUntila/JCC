package jcc;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class CompilerTest {

    public void compile(String path) throws IOException {

        List<String> lines = Files.readAllLines(Path.of(path));

        String pack = path.replaceAll("([A-Za-z_0-9]+)\\.java", ""); // путь к пакету
        System.out.println("pack = " + pack);

        String name = path.replace(pack, "").replace(".java", "") + "Runnable"; // имя нового Runnable-класса
        String namePath = pack + name + ".java"; // путь к новому Runnable-классу
        System.out.println("name = " + name);

        BufferedWriter bW = Files.newBufferedWriter(Path.of(namePath));

        List<String> methods = new ArrayList<>(); // список с методами класса, которые нужно запустить в run()
        int lastIndex = lines.size() - 1; // последняя строка - закрывающаяся фигурная скобка
        for (int i = 0; i < lastIndex; i++) {
            String line = lines.get(i);
            if (line.contains("class")) { // меняем имя класса и ставим реализацию Runnable
                line = line.replaceAll("class [A-Za-z0-9]+", "class " + name).
                        replace("{", "implements Runnable {");
            }
            if (line.contains("public void")) { // добавляем метод, необходимый для запуска
                methods.add(line.split("(\\s+public void\\s+)|(\\s)")[1]);
            }
            bW.write(line);
            bW.newLine();
        }

        bW.write(addRunMethod(methods)); // добавление метода run()
        bW.write(lines.get(lastIndex));
        bW.close();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, namePath);  // компилируем в тот же пакет
    }

    public static void main(String[] args) throws IOException {
        CompilerTest cT = new CompilerTest();
        cT.compile("src/main/java/jcc/Adder.java");
        cT.compile("src/main/java/tests/TestAdder.java");
    }

    private String addRunMethod(List<String> methods) { // добавление метода run()
        StringBuilder run = new StringBuilder("\n    @Override\n    public void run() {");
        for (String method : methods) {
            run.append("\n        ").append(method).append(";");
        }
        run.append("\n    }\n");
        return run.toString();
    }
}
