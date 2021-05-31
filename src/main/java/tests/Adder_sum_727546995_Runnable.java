package tests;

import java.lang.Throwable;
import java.lang.IllegalStateException;
import org.junit.Test;
import jcc.Adder;

public class Adder_sum_727546995_Runnable implements Runnable {

    public <T> T unknown() {
        throw new IllegalStateException();
    }

    @Test
    public  void testbb0() throws Throwable {
        Adder term506 = new Adder();
        int[] term505 = new int[0];
        term506.sum((int[])term505);
    }

    @Test
    public  void testlabelderoll0() throws Throwable {
        Adder term517 = new Adder();
        int[] term516 = new int[0];
        term517.sum((int[])term516);
    }

    @Test
    public  void testlabelderoll2() throws Throwable {
        Adder term528 = new Adder();
        int[] term527 = new int[2];
        term528.sum((int[])term527);
    }

    @Test
    public  void testlabelderoll3() throws Throwable {
        Adder term539 = new Adder();
        int[] term538 = new int[2];
        term539.sum((int[])term538);
    }


    @Override
    public void run() {
        try {
        testbb0();
        testlabelderoll0();
        testlabelderoll2();
        testlabelderoll3();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}