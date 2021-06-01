package jcc;

import java.lang.Throwable;
import java.lang.IllegalStateException;
import org.junit.Test;
import jcc.Adder;

public class Adder_init_235991254 {

    public <T> T unknown() {
        throw new IllegalStateException();
    }

    @Test
    public static void testbb0() throws Throwable {
        Adder term496 = new Adder();
        System.out.println("Hello there");
    }

};