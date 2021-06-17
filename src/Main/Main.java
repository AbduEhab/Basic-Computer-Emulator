package Main;

public class Main {
    public static void main(String[] args) {
        System.out.println(Integer.toBinaryString(2) + " " + Integer.toBinaryString(2).length());

        System.out.println(Integer.toHexString(0b1010));


        short[] newMemory = new short[4096];
        System.out.println(newMemory[0]);
    }
}
