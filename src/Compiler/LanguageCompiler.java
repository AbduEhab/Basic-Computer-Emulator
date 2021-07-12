package Compiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import Debugger.Logger;
import Emulator.BasicComputer;
import Exceptions.FileNotParsed;
import Exceptions.InvalidSyntax;

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

    public static boolean loadMemoryFromBinaryFile(String path, BasicComputer computer)
            throws FileNotParsed, FileNotFoundException, IOException {

        Logger.Declare("Loading Memory From BIN File");

        BufferedReader lineBuffer = null;

        try {
            lineBuffer = new BufferedReader(new FileReader(path));

        } catch (FileNotFoundException e) {

            Logger.Error("File at path: {" + path + "} is not found");
            throw new FileNotFoundException("File at path: {" + path + "} is not found");
        }

        String line = "";

        short[] newMemory = new short[4096];
        int i = 0;

        while ((line = lineBuffer.readLine()) != null) {
            if (i > 4096) {
                Logger.Error("File not Parsed correctly (Max Memory reached)");
                throw new FileNotParsed("File not Parsed correctly (Max Memory reached)");
            }

            newMemory[i++] = (short) Integer.parseInt(line, 2);

            Logger.Log("Memory Line $" + (i - 1) + " Loaded with Value: " + newMemory[i - 1]);
        }

        lineBuffer.close();

        if (i < 4097) {
            Logger.Error("File not Parsed correctly (Max Memory NOT reached)");
            throw new FileNotParsed("File not Parsed correctly (Max Memory NOT reached)");
        }

        computer.setMemory(newMemory);

        Logger.Declare("Memory Loaded From BIN File");
        return true;
    }

    public static boolean loadMemoryFromHexFile(String path, BasicComputer computer)
            throws FileNotParsed, FileNotFoundException, IOException {

        Logger.Declare("Loading Memory From HEX File");

        BufferedReader lineBuffer = null;

        try {
            lineBuffer = new BufferedReader(new FileReader(path));

        } catch (FileNotFoundException e) {

            Logger.Error("File at path: {" + path + "} is not found");
            throw new FileNotFoundException("File at path: {" + path + "} is not found");
        }

        String line = "";

        short[] newMemory = new short[4096];
        int i = 0;

        while ((line = lineBuffer.readLine()) != null) {
            if (i > 4096 || line.charAt(0) != ':') {
                Logger.Error("File not Parsed correctly (Max Memory reached)");
                throw new FileNotParsed("File not Parsed correctly (Max Memory reached)");
            }

            line = line.substring(1);

            if (line.length() != 14 || line.substring(0, 2) != "04") {
                Logger.Error("File not Parsed correctly (Size Mismatch)");
                throw new FileNotParsed("File not Parsed correctly (Size Mismatch)");
            }

            newMemory[i++] = (short) Integer.parseInt(line.substring(10, 12) + line.substring(8, 10), 16);

            Logger.Log("Memory Line $" + (i - 1) + " Loaded with Value: " + newMemory[i - 1]);
        }

        lineBuffer.close();

        if (i < 4097) {
            Logger.Error("File not Parsed correctly (Max Memory NOT reached)");
            throw new FileNotParsed("File not Parsed correctly (Max Memory NOT reached)");
        }

        computer.setMemory(newMemory);

        Logger.Declare("Memory Loaded From HEX File");
        return true;
    }

    public static boolean checkSyntax(BufferedReader fileBuffer) throws IOException, InvalidSyntax {
        Logger.Declare("Checking Syntax");

        String[] stringFile = new String[4096];
        String line = "";

        HashMap<String, Integer> variables = new HashMap<String, Integer>();

        for (int i = 0; (line = fileBuffer.readLine()) != null; i++) {
            stringFile[i] = line;
        }

        if (stringFile.length > 4096) {
            Logger.Error("Syntax Check Found Errors (Code Too Big, Max 4096 Lines)");
            throw new InvalidSyntax("Code Too Big (Max 4096 Lines)", -1, -1);
        }

        if (stringFile[0].split(" ")[0] != "ORG") {
            Logger.Error("Syntax Check Found Errors (ORG Not Specified)");
            throw new InvalidSyntax("ORG Not Specified", 0, 0);
        }

        int hlt = 0;

        for (int i = 1; i < stringFile.length; i++) {
            if (stringFile[i] == "HLT") {
                hlt = i;
                break;
            }
        }

        if (hlt == 0) {
            Logger.Error("Syntax Check Found Errors (HLT Not Specified)");
            throw new InvalidSyntax("HLT Not Specified", 4095, 0);
        }

        for (int i = hlt + 1; i < stringFile.length; i++) {

            String[] lineArray = stringFile[i].split(" ");

            if (lineArray.length != 3) {
                Logger.Error("Syntax Check Found Errors (Incorrect Structure)");
                throw new InvalidSyntax("Variable Structure should be: [{NAME}, {TYPE} {Value}]", i, 0);
            }

            if (lineArray[0].charAt(lineArray.length - 1) != ',') {
                Logger.Error("Syntax Check Found Errors (Variable NAME Not Specified)");
                throw new InvalidSyntax("Variable NAME Not Specified", i, 0);
            }
            if (lineArray[1] != "HEX" || lineArray[1] != "BIN" || lineArray[1] != "DEC") {
                Logger.Error("Syntax Check Found Errors (Variable TYPE Not Specified)");
                throw new InvalidSyntax("Variable TYPE Not Specified", i, lineArray[0].length());
            }
            if (lineArray[2].length() == 0) {
                Logger.Error("Syntax Check Found Errors (Variable Value Not Specified)");
                throw new InvalidSyntax("Variable Value Not Specified", i,
                        lineArray[0].length() + lineArray[1].length() + 2);
            }

            if (variables.get(lineArray[0]) != null) {
                Logger.Error("Syntax Check Found Errors (Variable Name Duplicated)");
                throw new InvalidSyntax("Variable Name Duplicated", i, 0);
            }

            variables.put(lineArray[0], 0);

        }

        for (int i = 1; i < hlt; i++) {

            String[] lineArray = stringFile[i].split(" ");

            if (getOpCode(lineArray[0]) == null) {
                Logger.Error("Syntax Check Found Errors (Unknown Instruction Signature)");
                throw new InvalidSyntax("Unknown Instruction Signature", i, lineArray[0].length());
            }

            switch (getOpCode(lineArray[0])[1] + "") {
                case "M":
                    if (stringFile[i].length() > 3 || stringFile[i].length() < 3) {
                        Logger.Error("Syntax Check Found Errors (Incorrect Structure)");
                        throw new InvalidSyntax(
                                "Argument Structure For Memort operation should be: [{INSTRUCTION}, {TARGET} {INDIRECT or DIRECT }(optional)]",
                                i, 0);
                    }

                    if (variables.get(lineArray[1]) == null || lineArray[1].charAt(0) != '#'
                            || lineArray[1].charAt(0) != '$') {
                        Logger.Error("Syntax Check Found Errors (Unknown Variable Signature)");
                        throw new InvalidSyntax(
                                "Unknown Variable Signature. Only use valid variables, Hex or Dec Values", i,
                                lineArray[0].length() + 1);
                    }

                    if (lineArray.length == 3 && lineArray[2] != "I") {
                        Logger.Error("Syntax Check Found Errors (Unrecognized Symbol)");
                        throw new InvalidSyntax(
                                "Unrecognized Symbol. For Indirect use \"I\" and leave empty for direct", i,
                                lineArray[0].length() + lineArray[1].length() + 2);
                    }

                    break;

                default:
                    if (stringFile[i].length() > 1 || stringFile[i].length() < 1) {
                        Logger.Error("Syntax Check Found Errors (Incorrect Structure)");
                        throw new InvalidSyntax(
                                "Argument Structure for Non memory operation should be: [{INSTRUCTION}]", i, 0);
                    }

                    break;
            }

        }

        Logger.Declare("Syntax Check Complete");
        return true;
    }

    public static boolean compile(FileFormat fileType, String filePath) {

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
                    // variables.put(key, value);
                }
            }

            if (fileType == FileFormat.HEX) {

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

    public static Object[] decodeOpcode(String opcode, FileFormat filetype) {

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
