package Emulator;

import Debugger.BasicComputerListener;

// Where the computer will be implemented (Memory included)
public class BasicComputer {

    private int[] memory;

    private BasicComputerListener listener;

    public BasicComputer() {
        memory = new int[4096];
    }

    public void setListener(BasicComputerListener listener) {
        this.listener = listener;
    }

}