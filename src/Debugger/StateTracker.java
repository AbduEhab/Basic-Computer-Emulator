package Debugger;

import Emulator.BasicComputer;

// This class will track the state of the Computer's Registers and Ram
public class StateTracker implements BasicComputerListener {

    public StateTracker(BasicComputer computer) {
        computer.setListener(this);
    }

    public void onMemoryChange(int add, int value) {

    }

}
