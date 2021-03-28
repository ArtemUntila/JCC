import classes.Adder;
import org.junit.Assert;
import org.junit.Test;

public class Test1 {

    @Test
    public void class1Test() {
        int[] a = new int[] {1, 2, 3, 4};
        Adder adder = new Adder();
        int sum = adder.sum(a);
        Assert.assertEquals(10, sum);
        System.out.println("Expected sum = 10");
        System.out.println("Actual sum = " + sum);
    }

}
