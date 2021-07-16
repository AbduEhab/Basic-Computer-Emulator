package Main;

import Compiler.LanguageCompiler;

public class Main {
    public static void main(String[] args) {

        for (String arg : args) {
            switch (arg) {
                case "":

                    break;

                default:
                    System.out.println("List of possible instructions:\n" + LanguageCompiler.displayOpCodes() + null);
                    break;
            }
        }

        System.out.println(0x20 + "");

        System.out.println(Integer.toBinaryString(2) + " " + Integer.toBinaryString(2).length());

        System.out.println(Integer.toHexString(0b1010));

    }
}
