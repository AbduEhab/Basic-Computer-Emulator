package Exceptions;

public class InvalidSyntax extends Exception {

    private int line;
    private int column;

    public InvalidSyntax() {
        super();
    }

    public InvalidSyntax(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

}
