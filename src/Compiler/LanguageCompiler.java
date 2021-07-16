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
    public static final Object[][] instructionSet = { { "AND", "M", 5, 0, 0x08 }, { "ADD", "M", 5, 1, 0x09 },
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

    // Load the memory from a HEX file
    public static boolean loadMemoryFromHexFile(String path, BasicComputer computer)
            throws FileNotParsed, FileNotFoundException, IOException {

        Logger.Declare("Loading Memory From HEX File");

        BufferedReader lineBuffer = null;

        // check if file is valid
        try {
            lineBuffer = new BufferedReader(new FileReader(path));

        } catch (FileNotFoundException e) {

            Logger.Error("File at path: {" + path + "} is not found");
            throw new FileNotFoundException("File at path: {" + path + "} is not found");
        }

        String line = "";

        // array to replace current empty memory
        short[] newMemory = new short[4096];
        int i = 0;

        while ((line = lineBuffer.readLine()) != null) {
            // if the hex file valid
            if (i > 4096 || line.charAt(0) != ':') {
                Logger.Error("File not Parsed correctly (Max Memory reached)");
                throw new FileNotParsed("File not Parsed correctly (Max Memory reached)");
            }

            line = line.substring(1);

            // check if data bytes are NOT 4 or if the line is generally parsed incorrectly
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

            switch (lineArray[1]) {
                case "BIN":
                    if (Integer.parseInt(lineArray[2], 2) >= 2048) {
                        Logger.Error("Syntax Check Found Errors (Variable Size Greater Than allowed)");
                        throw new InvalidSyntax("Max Variable size can Not exceed 2047 (2^11)", i,
                                lineArray[0].length() + lineArray[1].length() + 2);
                    }
                    break;

                default:
                    break;
            }

            variables.put(lineArray[0], 0);

        }

        for (int i = 1; i <= hlt; i++) {

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

    public static boolean compileToHEX(String filePath, String destination) throws FileNotFoundException, IOException {

        Logger.Declare("File Compile Started");

        if (filePath == null) {
            Logger.Error("Source File at path: {" + filePath + "} is not found");
            throw new FileNotFoundException("File at path: {" + filePath + "} is not found");
        }
        if (destination == null) {
            Logger.Error("Write File at path: {" + destination + "} is not found");
            throw new FileNotFoundException("File at path: {" + destination + "} is not found");
        }

        String[] stringFile = new String[4096];

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {

            Logger.Error("File at path: {" + filePath + "} is not found");
            throw new FileNotFoundException("File at path: {" + filePath + "} is not found");
        }

        String readLine = reader.readLine();

        // load the StringArray containing all the lines
        for (int i = 0; (reader.readLine()) != null; i++) {
            stringFile[i] = readLine;
        }

        reader.close();

        String outputString = "";

        // Write ORG command
        {
            int data = Integer.parseInt("4" + "000", 16) + Integer.parseInt(stringFile[0].split(" ")[1]);

            String dataString = Integer.toHexString(data);

            // correctly formating Data value
            switch (dataString.length()) {
                case 1:
                    dataString = "0" + dataString + "00";
                    break;
                case 2:
                    dataString = dataString + "00";
                    break;
                case 3:
                    dataString = dataString.substring(1) + dataString.charAt(0);
                    break;
                case 4:
                    dataString = dataString.substring(2) + dataString.substring(0, 2);
                    break;
                default:
                    break;
            }
            outputString += ':' + "04" + "0000" + "00" + dataString + "00" + "\n";
        }

        int hlt = 0;

        for (int i = 1; i < stringFile.length; i++) {
            if (stringFile[i] == "HLT") {
                hlt = i;
                break;
            }
        }

        // hashmap of variables defined in the code
        HashMap<String, Integer> variables = new HashMap<String, Integer>();

        for (int i = hlt + 1; i < stringFile.length; i++) {

            String[] lineArray = stringFile[i].split(" ");

            switch (lineArray[1]) {
                case "DEC":
                    variables.put(lineArray[0], Integer.parseInt(lineArray[2]));
                    break;

                case "HEX":
                    variables.put(lineArray[0], Integer.parseInt(lineArray[2], 16));
                    break;

                case "BIN":
                    variables.put(lineArray[0], Integer.parseInt(lineArray[2], 2));
                    break;

                default:
                    break;
            }

        }

        for (int i = 1; i <= hlt; i++) {

            String[] lineArray = stringFile[i].split(" ");

            Object[] opCode = getOpCode(lineArray[0].substring(0, lineArray[0].length() - 1));

            int data;

            if (opCode[1] == "M") {

                if (variables.containsKey(lineArray[2]))
                    data = Integer
                            .parseInt(Integer.toHexString(
                                    Integer.parseInt((lineArray.length == 2 ? opCode[3] : opCode[4]) + "")) + "000", 16)
                            + Integer.parseInt(stringFile[0].split(" ")[1]);
                else
                    data = Integer
                            .parseInt(Integer.toHexString(
                                    Integer.parseInt((lineArray.length == 2 ? opCode[3] : opCode[4]) + "")) + "000", 16)
                            + variables.get(lineArray[1]);

                String dataString = Integer.toHexString(data);

                // correctly formating Data value
                switch (dataString.length()) {
                    case 1:
                        dataString = "0" + dataString + "00";
                        break;
                    case 2:
                        dataString = dataString + "00";
                        break;
                    case 3:
                        dataString = dataString.substring(1) + "0" + dataString.charAt(0);
                        break;
                    case 4:
                        dataString = dataString.substring(2) + dataString.substring(0, 2);
                        break;
                    default:
                        break;
                }

                String address = Integer.toHexString(i);
                // correctly formating Address value
                switch (address.length()) {
                    case 1:
                        address = "0" + dataString + "00";
                        break;
                    case 2:
                        address = dataString + "00";
                        break;
                    case 3:
                        address = dataString.substring(1) + "0" + dataString.charAt(0);
                        break;
                    case 4:
                        address = dataString.substring(2) + dataString.substring(0, 2);
                        break;
                    default:
                        break;
                }
                outputString += ':' + "04" + address + "00" + dataString + "00" + "\n";

            } else {

                data = Integer.parseInt(opCode[4] + "");

                String dataString = Integer.toHexString(data);

                // correctly formating Data value
                switch (dataString.length()) {
                    case 1:
                        dataString = "0" + dataString + "00";
                        break;
                    case 2:
                        dataString = dataString + "00";
                        break;
                    case 3:
                        dataString = dataString.substring(1) + "0" + dataString.charAt(0);
                        break;
                    case 4:
                        dataString = dataString.substring(2) + dataString.substring(0, 2);
                        break;
                    default:
                        break;
                }

                String address = Integer.toHexString(i);
                // correctly formating Address value
                switch (address.length()) {
                    case 1:
                        address = "0" + dataString + "00";
                        break;
                    case 2:
                        address = dataString + "00";
                        break;
                    case 3:
                        address = dataString.substring(1) + "0" + dataString.charAt(0);
                        break;
                    case 4:
                        address = dataString.substring(2) + dataString.substring(0, 2);
                        break;
                    default:
                        break;
                }
                outputString += ':' + "04" + address + "00" + dataString + "00" + "\n";
            }
        }

        for (int i = hlt + 1; i < stringFile.length; i++) {

            String[] lineArray = stringFile[i].split(" ");

            String dataString = lineArray[2];

            if (lineArray[1] != "HEX")
                switch (lineArray[1]) {

                    case "DEC":
                        dataString = Integer.toHexString(Integer.parseInt(lineArray[2]));
                        break;

                    case "BIN":
                        dataString = Integer.toHexString(Integer.parseInt(lineArray[2], 2));
                        break;
                    default:
                        break;
                }

            // correctly formating Data value
            switch (dataString.length()) {
                case 1:
                    dataString = "0" + dataString + "00";
                    break;
                case 2:
                    dataString = dataString + "00";
                    break;
                case 3:
                    dataString = dataString.substring(1) + "0" + dataString.charAt(0);
                    break;
                case 4:
                    dataString = dataString.substring(2) + dataString.substring(0, 2);
                    break;
                default:
                    break;
            }

            String address = Integer.toHexString(i);
            // correctly formating Address value
            switch (address.length()) {
                case 1:
                    address = "0" + dataString + "00";
                    break;
                case 2:
                    address = dataString + "00";
                    break;
                case 3:
                    address = dataString.substring(1) + "0" + dataString.charAt(0);
                    break;
                case 4:
                    address = dataString.substring(2) + dataString.substring(0, 2);
                    break;
                default:
                    break;
            }
            outputString += ':' + "04" + address + "00" + dataString + "00" + "\n";
        }

        FileWriter writer = new FileWriter(destination);
        BufferedWriter buffer = new BufferedWriter(writer);

        buffer.write(outputString);

        buffer.close();

        Logger.Declare("File Compile Complete");
        return true;
    }

    public static boolean compileToBIN(String filePath, String destination) throws FileNotFoundException, IOException {

        Logger.Declare("File Compile Started");

        if (filePath == null) {
            Logger.Error("Source File at path: {" + filePath + "} is not found");
            throw new FileNotFoundException("File at path: {" + filePath + "} is not found");
        }
        if (destination == null) {
            Logger.Error("Write File at path: {" + destination + "} is not found");
            throw new FileNotFoundException("File at path: {" + destination + "} is not found");
        }

        String[] stringFile = new String[4096];

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {

            Logger.Error("File at path: {" + filePath + "} is not found");
            throw new FileNotFoundException("File at path: {" + filePath + "} is not found");
        }

        String readLine = reader.readLine();

        // load the StringArray containing all the lines
        for (int i = 0; (reader.readLine()) != null; i++) {
            stringFile[i] = readLine;
        }

        reader.close();

        String outputString = "";

        // Write ORG command
        {
            int data = Integer.parseInt("4" + "000", 16) + Integer.parseInt(stringFile[0].split(" ")[1]);

            String dataString = Integer.toBinaryString(data);

            int datalength = 16 - dataString.length();

            // correctly formating Data value
            for (int i = 0; i < datalength; i++) {
                dataString = "0" + dataString;
            }
            outputString += dataString + "\n";
        }

        int hlt = 0;

        for (int i = 1; i < stringFile.length; i++) {
            if (stringFile[i] == "HLT") {
                hlt = i;
                break;
            }
        }

        // hashmap of variables defined in the code
        HashMap<String, Integer> variables = new HashMap<String, Integer>();

        for (int i = hlt + 1; i < stringFile.length; i++) {

            String[] lineArray = stringFile[i].split(" ");

            switch (lineArray[1]) {
                case "DEC":
                    variables.put(lineArray[0], Integer.parseInt(lineArray[2]));
                    break;

                case "HEX":
                    variables.put(lineArray[0], Integer.parseInt(lineArray[2], 16));
                    break;

                case "BIN":
                    variables.put(lineArray[0], Integer.parseInt(lineArray[2], 2));
                    break;

                default:
                    break;
            }

        }

        for (int i = 1; i <= hlt; i++) {

            String[] lineArray = stringFile[i].split(" ");

            Object[] opCode = getOpCode(lineArray[0].substring(0, lineArray[0].length() - 1));

            int data;

            if (opCode[1] == "M") {

                if (variables.containsKey(lineArray[2]))
                    data = Integer
                            .parseInt(Integer.toHexString(
                                    Integer.parseInt((lineArray.length == 2 ? opCode[3] : opCode[4]) + "")) + "000", 16)
                            + Integer.parseInt(stringFile[0].split(" ")[1]);
                else
                    data = Integer
                            .parseInt(Integer.toHexString(
                                    Integer.parseInt((lineArray.length == 2 ? opCode[3] : opCode[4]) + "")) + "000", 16)
                            + variables.get(lineArray[1]);

                String dataString = Integer.toBinaryString(data);

                int datalength = 16 - dataString.length();

                // correctly formating Data value
                for (int j = 0; j < datalength; i++) {
                    dataString = "0" + dataString;
                }
                outputString += dataString + "\n";

            } else {

                data = Integer.parseInt(opCode[4] + "");

                String dataString = Integer.toBinaryString(data);

                int datalength = 16 - dataString.length();

                // correctly formating Data value
                for (int j = 0; j < datalength; i++) {
                    dataString = "0" + dataString;
                }

                outputString += dataString + "\n";
            }
        }

        for (int i = hlt + 1; i < stringFile.length; i++) {

            String[] lineArray = stringFile[i].split(" ");

            String dataString = lineArray[2];

            if (lineArray[1] != "HEX")
                switch (lineArray[1]) {

                    case "DEC":
                        dataString = Integer.toBinaryString(Integer.parseInt(lineArray[2]));
                        break;

                    case "BIN":
                        dataString = Integer.toBinaryString(Integer.parseInt(lineArray[2], 2));
                        break;
                    default:
                        break;
                }

            int datalength = 16 - dataString.length();

            // correctly formating Data value
            for (int j = 0; j < datalength; i++) {
                dataString = "0" + dataString;
            }

            outputString += dataString + "\n";
        }

        FileWriter writer = new FileWriter(destination);
        BufferedWriter buffer = new BufferedWriter(writer);

        buffer.write(outputString);

        buffer.close();

        Logger.Declare("File Compile Complete");
        return true;
    }

    public static Object[] getOpCode(String op) {

        for (Object[] opCode : instructionSet) {
            if (opCode[0] == op.toUpperCase())
                return opCode;
        }

        return null;
    }

    public static String displayOpCodes() {
        String s = "";

        for (Object[] opcode : instructionSet) {
            s += "\t- " + opcode[0] + ", type: " + opcode[1] + ", Code = "
                    + (!opcode[1].equals("M") ? opcode[4] : opcode[3] + ", Indirect code = " + opcode[4]) + "\n";
        }

        return s;
    }
}
