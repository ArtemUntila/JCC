package classes;

import java.util.Objects;

public class Adder {

    public int sum(int[] a) {
        Objects.requireNonNull(a);
        int sum = 0;
        for (int i : a) sum += i;
        return sum;
    }

}
