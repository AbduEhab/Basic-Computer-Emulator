package Debugger;

import Emulator.BasicComputer;
import Emulator.Register;

// This class will track the state of the Computer's Registers and Ram
public class StateTracker implements BasicComputerListener {

    public StateTracker(BasicComputer computer) {
        computer.setListener(this);
    }

    @Override
    public boolean onMemoryChange(int address, short value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onRegisterChange(Register register, short value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onFlagChange() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onEveryThingChanging() {
        // TODO Auto-generated method stub
        return false;
    }

}
