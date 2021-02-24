package Emulator.ComputerFiles;

import Debugger.BasicComputerListener;

// Where the computer will be implemented (Memory included)
public class BasicComputer {

    private short[] memory; // Computer Memory

    private byte SC = 0b00; // Sequence Counter
    private int AC = 0x00; // Accumulator (I have it set to int for simplicity but will only use 16 bits)
    private short PC = 0x00; // Program Counter
    private short DR = 0x00; // Data Register
    private short AR = 0x00; // Address Register
    private short IR = 0x00; // Information Register
    private short TR = 0x00; // Temp Register
    private byte OUTR = 0x00; // Output Register
    private byte INPR = 0x00; // Input Register

    private byte cycles = 0; // cycles taken (Refrenced as T in later comments)

    private byte flags = 0b00; // byte containing all flages
    // 0 -> Stop, 1 -> Interupt(IEN), 2 -> InputFlag (FGI), 3 -> OutputFlag (FGO),
    // 4 -> E, 5 -> CarryBit, 6 -> IndirectMemoryAccess (I), 7 -> RedFlag (R)

    private BasicComputerListener listener; // Listener for the StateTracker

    public BasicComputer() {
        memory = new short[4096];

        for (int i = 0; i < memory.length; i++) {
            memory[i] = 0x00;
        }
    }

    public BasicComputer(short[] memory) {
        this.memory = memory;
    }

    public int completeInstruction() {
        if (isStopped()) {
            return 0;
        }
        if (getIEN() && getFGI() || getFGO()) {
            setR(1);
            AR = 0;
            TR = PC;
            memory[AR] = TR;
            PC = 0;
            PC++;
            setIEN(0);
            setR(0);
        }

        cycles = 0; // resetting cycles from previous instruction

        // Load AR with PC at T0
        SC = 0x01;
        AR = PC;

        cycles++;

        // Fetch AR from memory and load it into IR, while incrementing PC at T1
        SC = 0x05;
        IR = memory[AR];

        PC++;
        cycles++;

        // Set AR with IR(0-11), Set I accordingly, extract oppcode at T2
        SC = 0x01;
        AR = (short) (IR & 0xFFF);
        setI(IR >> 15);
        short oppcode = (short) ((IR & 0x7000) >> 12);

        cycles++;

        // Check oppcode type at T3
        if (oppcode <= 0x06 || oppcode >= 0x08 && oppcode <= 0x0E) { // If oppcode is a memory reference

            if (getI()) {
                SC = 0x05;
                IR = memory[AR];

                cycles++;
            } else {
                cycles++;
            }

            switch (oppcode) {
                case 0: // AND
                    SC = 0x03;
                    DR = memory[AR];

                    cycles++;

                    AC &= DR;

                    cycles++;
                    break;

                case 1: // ADD
                    SC = 0x03;
                    DR = memory[AR];

                    cycles++;

                    AC += DR;
                    setCarryBit((AC & 0xFFFF) > 0 ? 1 : 0);
                    setE(getCarryBit() ? 1 : 0);

                    cycles++;
                    break;

                case 2: // LDA
                    SC = 0x03;
                    DR = memory[AR];
                    cycles++;

                    AC = DR;

                    cycles++;
                    break;

                case 3: // STA
                    SC = 0x07;
                    memory[AR] = (short) AC;

                    cycles++;
                    break;

                case 4: // BUN
                    PC = AR;

                    cycles++;
                    break;

                case 5: // BSA
                    SC = 0x07;
                    memory[AR] = PC;
                    AR++;

                    cycles++;

                    PC = AR;

                    cycles++;
                    break;

                case 6: // ISZ
                    SC = 0x03;
                    DR = memory[AR];

                    cycles++;

                    DR++;

                    cycles++;

                    memory[AR] = DR;
                    if (DR == 0)
                        PC++;

                    break;

                // Second set of oppcodes for Memory opperations
                case 0x08: // AND
                    SC = 0x03;
                    DR = memory[AR];

                    cycles++;

                    AC &= DR;

                    cycles++;
                    break;

                case 0x09: // ADD
                    SC = 0x03;
                    DR = memory[AR];
                    cycles++;

                    AC += DR;
                    setCarryBit((AC & 0xFFFF) > 0 ? 1 : 0);
                    setE(getCarryBit() ? 1 : 0);
                    AC &= 0xFFFF;

                    cycles++;
                    break;

                case 0x10: // LDA
                    SC = 0x03;
                    DR = memory[AR];

                    cycles++;

                    AC = DR;

                    cycles++;
                    break;

                case 0x11: // STA
                    SC = 0x07;
                    memory[AR] = (short) AC;

                    cycles++;
                    break;

                case 0x12: // BUN
                    PC = AR;

                    cycles++;
                    break;

                case 0x13: // BSA
                    SC = 0x07;
                    memory[AR] = PC;
                    AR++;

                    cycles++;

                    PC = AR;

                    cycles++;
                    break;

                case 0x14: // ISZ
                    SC = 0x03;
                    DR = memory[AR];

                    cycles++;

                    DR++;

                    cycles++;

                    memory[AR] = DR;
                    if (DR == 0)
                        PC++;

                    break;

                default:
                    System.out.println("invalid memory oppcode : " + getI() + " " + oppcode + " " + AR);
                    break;
            }

        } else { // If oppcode is a register or I/O opperation

            if (getI()) { // If I/O opperation
                switch (AR) {
                    case 0x800: // INP
                        SC = 0x04;
                        AC = (AC & 0xFF00) | INPR;
                        setFGI(0);

                        cycles++;
                        break;

                    case 0x400: // OUT
                        OUTR = (byte) AC;
                        setFGO(0);

                        cycles++;
                        break;

                    case 0x200: // SKI
                        if (getFGI())
                            PC++;

                        cycles++;
                        break;

                    case 0x100: // SKO
                        if (getFGO())
                            PC++;

                        cycles++;
                        break;

                    case 0x080: // ION
                        setIEN(1);

                        cycles++;
                        break;

                    case 0x040: // IOF
                        setIEN(0);

                        cycles++;
                        break;

                    default:
                        System.out.println("invalid I/O oppcode : " + getI() + " " + oppcode + " " + AR);
                        break;
                }
            } else { // If register opperation
                switch (AR) {
                    case 0x800: // CLA
                        AC = 0x00;

                        cycles++;
                        break;

                    case 0x400: // CLE
                        setE(0);

                        cycles++;
                        break;

                    case 0x200: // CMA
                        AC = ~AC;

                        cycles++;
                        break;

                    case 0x100: // CME
                        setE(getE() ? 0 : 1);

                        cycles++;
                        break;

                    case 0x080: // CIR
                        short temp = (short) AC;
                        AC >>= 1;
                        AC |= (getE() ? 1 : 0) << 15;
                        setE(temp >> 15);

                        cycles++;
                        break;

                    case 0x040: // CIL
                        short temp2 = (short) AC;
                        AC <<= 1;
                        AC |= (getE() ? 1 : 0);
                        setE(temp2 & 0x01);

                        cycles++;
                        break;

                    case 0x020: // INC
                        AC++;
                        setCarryBit(AC >> 15);
                        setE(getCarryBit() ? 1 : 0);
                        AC &= 0xFFFF;

                        cycles++;
                        break;

                    case 0x010: // SPA
                        if (AC >> 15 == 0)
                            PC++;

                        cycles++;
                        break;

                    case 0x08: // SNA
                        if (AC >> 15 == 1)
                            PC++;

                        cycles++;
                        break;

                    case 0x04: // SZA
                        if (AC == 0)
                            PC++;

                        cycles++;
                        break;

                    case 0x02: // SZE
                        if (!getE())
                            PC++;

                        cycles++;
                        break;

                    case 0x01: // HLT
                        setStop();
                        break;

                    default:
                        System.out.println("invalid Register oppcode : " + getI() + " " + oppcode + " " + AR);
                        break;
                }
            }
        }
        listener.onEveryThingChanging();

        return cycles;
    }

    private boolean setR(int value) {
        int R = flags & 0b10000000;
        if (R == value << 7) {
            return true;
        } else {
            flags ^= R;
        }
        return true;
    }

    public boolean getR() {
        int value = flags & 0b10000000;
        return value > 0 ? true : false;
    }

    private boolean setI(int value) {
        int I = flags & 0b1000000;
        if (I == value << 7) {
            return true;
        } else {
            flags ^= I;
        }
        return true;
    }

    public boolean getI() {
        int value = flags & 0b1000000;
        return value > 0 ? true : false;
    }

    private boolean setCarryBit(int value) {
        int CarryBit = flags & 0b100000;
        if (CarryBit == value << 5) {
            return true;
        } else {
            flags ^= CarryBit;
        }
        return true;
    }

    public boolean getCarryBit() {
        int value = flags & 0b100000;
        return value > 0 ? true : false;
    }

    private boolean setE(int value) {
        int E = flags & 0b10000;
        if (E == value << 4) {
            return true;
        } else {
            flags ^= E;
        }
        return true;
    }

    public boolean getE() {
        int value = flags & 0b10000;
        return value > 0 ? true : false;
    }

    public boolean setFGO(int value) {
        int FGO = flags & 0b1000;
        if (FGO == value << 3) {
            return true;
        } else {
            flags ^= FGO;
        }
        return true;
    }

    public boolean getFGO() {
        int value = flags & 0b1000;
        return value > 0 ? true : false;
    }

    public boolean setFGI(int value) {
        int FGI = flags & 0b100;
        if (FGI == value << 2) {
            return true;
        } else {
            flags ^= FGI;
        }
        return true;
    }

    public boolean getFGI() {
        int value = flags & 0b100;
        return value > 0 ? true : false;
    }

    public boolean setIEN(int value) {
        int IEN = flags & 0b10;
        if (IEN == value << 1) {
            return true;
        } else {
            flags ^= IEN;
        }
        return true;
    }

    public boolean getIEN() {
        int value = flags & 0b01;
        return value > 0 ? true : false;
    }

    private boolean setStop() {
        flags ^= 0b1;
        return true;
    }

    public boolean isStopped() {
        int stopFlag = flags & 0b1;
        return stopFlag == 1 ? true : false;
    }

    public boolean setINPR(byte INPR) {
        this.INPR = INPR;
        return true;
    }

    public byte getOUTR() {
        return OUTR;
    }

    public void setListener(BasicComputerListener listener) {
        this.listener = listener;
    }

}