package Debugger;

import Emulator.ComputerFiles.Register;

public interface BasicComputerListener {

    boolean onMemoryChange(int address, short value);

    boolean onRegisterChange(Register register, short value);

    boolean onFlagChange();

    boolean onEveryThingChanging();
}
