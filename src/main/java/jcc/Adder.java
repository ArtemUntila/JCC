package jcc;

public class Adder {

    public int sum(int[] a) {
        int sum = 0;
        for (int i : a) sum += i;
        return sum;
    }

    public void go() {
        System.out.println("go");
    }

    public void went() {
        System.out.println("went");
    }

    public void gone() {
        System.out.println("gone");
    }

    public void print() {
        System.out.println("Did you cover me?");
    }

}
