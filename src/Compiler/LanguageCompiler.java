package Compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import Emulator.BasicComputer;

// This class will compile some psudo code to Basic Assembly Code
public class LanguageCompiler {
    static final Object[][] instructionSet = { { "AND", "M", 5, 0, 0x08 }, { "ADD", "M", 5, 1, 0x09 },
            { "LDA", "M", 5, 2, 0x0A }, { "STA", "M", 5, 3, 0x0B }, { "BUN", "M", 5, 4, 0x0C },
            { "BSA", "M", 5, 5, 0x0D }, { "ISZ", "M", 6, 6, 0x0E }, { "CLA", "R", 3, -1, 0x7800 },
            { "CLE", "R", 3, -1, 0x7400 }, { "CMA", "R", 3, -1, 0x7200 }, { "CME", "R", 3, -1, 0x7100 },
            { "CIR", "R", 3, -1, 0x7080 }, { "CIL", "R", 3, -1, 0x7040 }, { "INC", "R", 3, -1, 0x7020 },
            { "SPA", "R", 3, -1, 0x7010 }, { "SNA", "R", 3, -1, 0x7008 }, { "SZA", "R", 3, -1, 0x7004 },
            { "SZE", "R", 3, -1, 0x7002 }, { "HLT", "R", 3, -1, 0x7001 }, { "INP", "I/O", 3, -1, 0xF800 },
            { "OUT", "I/O", 3, -1, 0xF400 }, { "SKI", "I/O", 3, -1, 0xF200 }, { "SKO", "I/O", 3, -1, 0xF100 },
            { "ION", "I/O", 3, -1, 0xF080 }, { "IOF", "I/O", 3, -1, 0xF040 } };

    public static boolean loadMemory(String path, MachineCode fileType, BasicComputer computer) {
        if (fileType != null)
            return false;

        try {
            BufferedReader lineBuffer = new BufferedReader(new FileReader(path));

            String line = "";

            short[] newMemory = new short[4096];
            int i = 0;

            switch (fileType) {

                case BIN:
                    while ((line = lineBuffer.readLine()) != null)
                        newMemory[i++] = (short) Integer.parseInt(line, 2);
                    break;
                case HEX:

                    break;

                default:
                    break;
            }

            lineBuffer.close();
        } catch (FileNotFoundException e) {
            System.out.println("File at path: {" + path + "} is not found");
            return false;
        } catch (IOException e) {
            System.out.println("File end reached unexpectidly");
            return false;
        }

        return true;
    }

    public static boolean compile(MachineCode fileType, String filePath) {

        try {

            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            FileWriter writer;
            BufferedWriter buffer;

            String outputString = "";
            String readLine = reader.readLine();

            String[] stringFile = new String[4096];

            for (int i = 0; (reader.readLine()) != null; i++) {
                stringFile[i] = readLine;
            }

            // hashmap of variables defined in the code
            HashMap<String, Object[]> variables = new HashMap<String, Object[]>();

            int stopPoint = 0;

            for (int i = 4095; i > 0; i--) {
                if (stringFile[i] == "HLT") {
                    stopPoint = i;
                    break;
                } else {
                    //variables.put(key, value);
                }
            }

            if (fileType == MachineCode.HEX) {

                writer = new FileWriter("../Examples/COMPILEDCODE.hex");
                buffer = new BufferedWriter(writer);

                for (int i = 0; i < stopPoint; i++) {

                    String[] lineArray = stringFile[i].split(" ");

                    Object[] opCode;

                    if ((opCode = getOpCode(stringFile[i])) == null)
                        return false;

                    if (opCode[1] == "M") {

                        if (!variables.containsKey(lineArray[1])) {
                            if (lineArray[1].charAt(0) == '#')
                                lineArray[1] = Integer.decode(lineArray[1]) + "";
                        } else {
                            lineArray[1] = (String) variables.get(lineArray[0])[2];
                        }

                        if (opCode[0] == "STA") {
                            if (true)
                                outputString += ':' + "02" + "0064" + "00" + "0" + Integer.toHexString((int) opCode[4])
                                        + variables.get(lineArray[1])[1];
                        }

                        if (opCode[0] == "LDAI") {
                            outputString += ':' + "02" + "0064" + "00" + "1";
                        } else {
                            outputString += ':' + "02" + "0064" + "00" + "0";
                        }

                    } else if (opCode[1] == "R") {

                        outputString += ':' + "02" + "0064" + "00";

                    } else if (opCode[1] == "I/O") {

                        outputString += ':' + "02" + "0064" + "00";

                    }

                }

            } else {

                writer = new FileWriter("../Examples/COMPILEDCODE.bin");
                buffer = new BufferedWriter(writer);
            }

            buffer.close();
            reader.close();

        } catch (FileNotFoundException e) {
            System.out.println("File at path: {" + filePath + "} is not found");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static Object[] getOpCode(String op) {

        for (Object[] opCode : instructionSet) {
            if (opCode[0] == op.toUpperCase())
                return opCode;
        }

        return null;
    }

    public static Object[] decodeOpcode(String opcode, MachineCode filetype) {

        switch (filetype) {
            case BIN:
                if (opcode.substring(0, 3) == "1111") {
                    for (Object[] objects : instructionSet) {
                        if ((objects[4] + "") == opcode) {
                            return new Object[] { "I/O", Integer.toBinaryString((int) objects[4]) };
                        }
                    }
                    return new Object[] { "null", -1 };
                }

                if (opcode.substring(0, 3) == "0111") {
                    for (Object[] objects : instructionSet) {
                        if ((objects[4] + "") == opcode) {
                            return new Object[] { "R", "0" + Integer.toBinaryString((int) objects[4]) };
                        }
                    }
                    return new Object[] { "null", -1 };
                }

                if (opcode.charAt(0) == '0') {
                    for (Object[] objects : instructionSet) {
                        if ((objects[4] + "") == opcode) {
                            return new Object[] { "M", "0" + Integer.toBinaryString((int) objects[3]) };
                        }
                    }
                } else {
                    for (Object[] objects : instructionSet) {
                        if ((objects[4] + "") == opcode) {
                            return new Object[] { "M", Integer.toBinaryString((int) objects[4]) };
                        }
                    }
                }
                break;
            case HEX:

                break;
            default:
                break;
        }

        return new Object[] { "null", -1 };
    }
}
