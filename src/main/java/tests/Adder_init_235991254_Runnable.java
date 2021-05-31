package tests;

import java.lang.Throwable;
import java.lang.IllegalStateException;
import org.junit.Test;
import jcc.Adder;

public class Adder_init_235991254_Runnable implements Runnable {

    public <T> T unknown() {
        throw new IllegalStateException();
    }

    @Test
    public  void testbb0() throws Throwable {
        Adder term496 = new Adder();
    }


    @Override
    public void run() {
        try {
        testbb0();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}