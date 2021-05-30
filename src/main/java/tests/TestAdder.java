package tests;

import jcc.Adder;
import org.junit.Assert;
import org.junit.Test;

public class TestAdder {

    @Test
    public void sumTest() {
        int[] a = new int[] {1, 2, 3, 4};
        Adder adder = new Adder();
        int sum = adder.sum(a);
        Assert.assertEquals(10, sum);
        System.out.println("Expected sum = 10");
        System.out.println("Adder.sum() = " + sum);
    }

}
