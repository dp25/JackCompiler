import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class  JackTokenizer {
    private File codeFile;
    private Scanner scanner;
    private String currentToken;

    public enum TokenType{
        KEYWORD,
        SYMBOL,
        IDENTIFIER,
        INT_CONST,
        STRING_CONST;
    }
    
    public enum KeyWord{
        CLASS,
        METHOD,
        FUNCTION,
        CONSTRUCTOR,
        INT,
        BOOLEAN,
        CHAR,
        VOID,
        VAR,
        STATIC,
        FIELD,
        LET,
        DO,
        IF,
        ELSE,
        WHILE,
        RETURN,
        TRUE,
        FALSE,
        NULL,
        THIS;
    }

    // opens the input .jack file and gets readt to tokenize it 
    public JackTokenizer (File file) throws FileNotFoundException {
        this.codeFile = file;
        this.scanner = new Scanner(file);
        scanner.useDelimiter("//.*|\\s+");
        this.currentToken = "";
    }

    // checks if there are more tokens 
    public boolean hasMoreTokens (){
        return (this.scanner.hasNext());       
    }

    // gets the next token from the input and makes it the current token. 
    public void advance(){
        if (hasMoreTokens()){
            this.currentToken = scanner.next(); 
        }
    }

    public TokenType tokenType(){
        ArrayList<String> keywords = new ArrayList<>();
        keywords.add("class"); 
        keywords.add("method");
        keywords.add("function");
        keywords.add("constructor");
        keywords.add("int");
        keywords.add("boolean");
        keywords.add("char");
        keywords.add("void");
        keywords.add("var");
        keywords.add("static");
        keywords.add("field");
        keywords.add("let");
        keywords.add("do");
        keywords.add("if");
        keywords.add("else");
        keywords.add("while");
        keywords.add("return");
        keywords.add("true");
        keywords.add("false");
        keywords.add("null");
        keywords.add("this");

        ArrayList<String> symbols = new ArrayList<>();
        symbols.add("{"); 
        symbols.add("}");
        symbols.add("(");
        symbols.add(")");
        symbols.add("[");
        symbols.add("]");
        symbols.add(".");
        symbols.add(",");
        symbols.add(";");
        symbols.add("+");
        symbols.add("-");
        symbols.add("*");
        symbols.add("/");
        symbols.add("&");
        symbols.add("|");
        symbols.add("<");
        symbols.add(">");
        symbols.add("=");
        symbols.add("~");


        // checks KEYWORD token
        if (keywords.contains(currentToken)) {
            return TokenType.KEYWORD;
        }
        // checks SYMBOL token
        if (symbols.contains(currentToken)) {
            return TokenType.SYMBOL;
        }

        // checks INT_CONST token
        int check = Integer.parseInt(currentToken);
        if(( check <= 32767) && (check >= 0)){
            return TokenType.INT_CONST;
        }
        
        // checks STRING_CONST token
        if(currentToken.startsWith("\"") && currentToken.endsWith("\"")){
            return TokenType.STRING_CONST;
        }
        
        // checks IDENTIFIER token
        if (currentToken.matches("^[a-zA-Z_][a-zA-Z_0-9]*")){
            return TokenType.IDENTIFIER;
        }

        return null;
    }

    public KeyWord keyWord(){
        // checks if class
        if(currentToken.equals("class")){
            return KeyWord.CLASS;
        }
        //checks if method
        if(currentToken.equals("method")){
            return KeyWord.METHOD;
        }
        //checks if function
        if(currentToken.equals("function")){
            return KeyWord.FUNCTION;
        }
        //checks if constructor
        if(currentToken.equals("constructor")){
            return KeyWord.CONSTRUCTOR;
        }
        //checks if int
        if(currentToken.equals("int")){
            return KeyWord.INT;
        }
        //checks if boolean
        if(currentToken.equals("boolean")){
            return KeyWord.BOOLEAN;
        }
        //checks if char
        if(currentToken.equals("char")){
            return KeyWord.CHAR;
        }
        //checks if void
        if(currentToken.equals("void")){
            return KeyWord.VOID;
        }
        //checks if var
        if(currentToken.equals("var")){
            return KeyWord.VAR;
        }
        //checks if static
        if(currentToken.equals("static")){
            return KeyWord.STATIC;
        }
        //checks if field
        if(currentToken.equals("field")){
            return KeyWord.FIELD;
        }
        //checks if let
        if(currentToken.equals("let")){
            return KeyWord.LET;
        }
        //checks if do
        if(currentToken.equals("do")){
            return KeyWord.DO;
        }
        //checks if if
        if(currentToken.equals("if")){
            return KeyWord.IF;
        }
        //checks if else
        if(currentToken.equals("else")){
            return KeyWord.ELSE;
        }
        //checks if while
        if(currentToken.equals("while")){
            return KeyWord.WHILE;
        }
        //checks if return
        if(currentToken.equals("return")){
            return KeyWord.RETURN;
        }
        //checks if true
        if(currentToken.equals("true")){
            return KeyWord.TRUE;
        }
        //checks if false
        if(currentToken.equals("false")){
            return KeyWord.FALSE;
        }
        //checks if null
        if(currentToken.equals("null")){
            return KeyWord.NULL;
        }
        //checks if this
        if(currentToken.equals("this")){
            return KeyWord.THIS;
        }

        return null;
    }
   
    public char symbol() {
        char ch = this.currentToken.charAt(0);
        return ch;
    }

    public String identifier() {
        return this.currentToken;
    }

    public int intVal() {
        return Integer.parseInt(this.currentToken);
    }

    public String stringVal() {
        return currentToken.substring(1, currentToken.length() - 1);
    }

}


