package Compiler;

// This class will compile some psudo code to Basic Assembly Code
public class LanguageCompiler {
    final Object[][] instructionSet = { { "AND", "M", 5, 0, 0x08 }, { "ADD", "M", 5, 1, 0x09 },
            { "LDA", "M", 5, 2, 0x0A }, { "STA", "M", 5, 3, 0x0B }, { "BUN", "M", 5, 4, 0x0C },
            { "BSA", "M", 5, 5, 0x0D }, { "ISZ", "M", 6, 6, 0x0E }, { "CLA", "R", 3, 7800 }, { "CLE", "R", 3, 7400 },
            { "CMA", "R", 3, 7200 }, { "CME", "R", 3, 7100 }, { "CIR", "R", 3, 7080 }, { "CIL", "R", 3, 7040 },
            { "INC", "R", 3, 7020 }, { "SPA", "R", 3, 0x7010 }, { "SNA", "R", 3, 7008 }, { "SZA", "R", 3, 7004 },
            { "SZE", "R", 3, 7002 }, { "HLT", "R", 3, 7001 }, { "INP", "I/O", 3, 0xF800 }, { "OUT", "I/O", 3, 0xf400 },
            { "SKI", "I/O", 3, 0xF200 }, { "SKO", "I/O", 3, 0xF100 }, { "ION", "I/O", 3, 0xF080 },
            { "IOF", "I/O", 3, 0xF040 } };
}
