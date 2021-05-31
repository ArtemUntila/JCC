package jcc;

import java.lang.Throwable;
import java.lang.IllegalStateException;
import org.junit.Test;
import jcc.Adder;

public class Adder_print_306551973 {

    public <T> T unknown() {
        throw new IllegalStateException();
    }

    @Test
    public  void testbb0() throws Throwable {
        Adder term552 = new Adder();
        term552.print();
    }

};