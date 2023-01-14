import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
    private File output;
    private FileWriter writer;
    private JackTokenizer tokenizer;  // input
    private String tab = "";
    //private String tabBack = "";

    public CompilationEngine(File input, File output) throws IOException{
        this.tokenizer =new JackTokenizer(input);
        this.writer = new FileWriter(output);
        //String outName = output.getPath().split("\\.")[0] + ".xml";
        this.output = output;
    }

    // compiles a complete class
    public void compileClass() throws IOException{
        tokenizer.advance();
        // write the class
        if(tokenizer.keyWord() == JackTokenizer.KeyWord.CLASS){
            this.writer.write("<class>");
            // increase tab
            increaseTab();
            writeTag(tokenizer.getToken(), "keyword");

            // read next token
            tokenizer.advance();
            // the class name 
            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                // System.out.println("illegal class name identifier");
                // return;
            }
            // checks if contains "{" 
            tokenizer.advance();
            if (!checkSymbol("{")) {
                System.out.println("no openning { for class");
                return;
            }
            
            tokenizer.advance();
            // parse classVarDec 
            while ((tokenizer.keyWord() == JackTokenizer.KeyWord.FIELD) || (tokenizer.keyWord() == JackTokenizer.KeyWord.STATIC)){
                compileClassVarDec();
                tokenizer.advance();
            }

            // parse subroutine - checks if counstructor or method or function
            while ((tokenizer.keyWord() == JackTokenizer.KeyWord.CONSTRUCTOR) || (tokenizer.keyWord() == JackTokenizer.KeyWord.METHOD) || (tokenizer.keyWord() == JackTokenizer.KeyWord.FUNCTION)){
                compileSubroutine();
                tokenizer.advance();
            }
        }
    }

    // compiles a static var and a field declarations
    public void compileClassVarDec() throws IOException{
        writer.write(tab + "<classVarDec>");
        increaseTab();

        writeTag(tokenizer.getToken(), "keyword");

        // checks the type of the var
        tokenizer.advance();
        if (!checkValType()) {
            System.out.println("illegal type for class var dec");
            return;
        }
        
        // checks the name of the var
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
        } else {
            System.out.println("illegal classVar identifier");
            return;
        }

        // check more vars exists of the same type ", var_name"
        tokenizer.advance();
        while (tokenizer.getToken().equals(",")) {
            writeTag(",", "symbol");
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal classVar identifier");
                return;
            }
            tokenizer.advance();
        }

        // checks if reached the end with ";"
        if (tokenizer.getToken().equals(";")) {
            writeTag(";", "symbol");
        } else {
            System.out.println("no ending ;");
            return;
        }

        // close tag
        decreaseTab();
        writer.write(tab + "</classVarDec>");
    }

    // compiles constructor or method or function
    public void compileSubroutine() throws IOException{
        // write tag of subroutine
        writer.write(tab + "<subroutineDec>");
        increaseTab();

        // write a keyword tag of a constructor or method or function
        writeTag(tokenizer.getToken(), "keyword");

        // checks what type it returns
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD && tokenizer.getToken().equals("void")) {
            writeTag("void", "keyword");
        } else if (!checkValType()) {
            System.out.println("Illegal type name for subroutine");
            return;
        }

        // checks if has a name/identifier
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
        } else {
            System.out.println("illegal subroutine name");
            return;
        }

        // checks if has a openning "("
        tokenizer.advance();
        if (tokenizer.getToken().equals("(")) {
            writeTag("(", "symbol");
            // checks parameters arguments
            compileParameterList();
        } else {
            System.out.println("no ( after function name");
            return;
        }

    }

    private void compileParameterList(){
        
    }
    // increase the line a tab foward
    private void increaseTab() {
        tab += "\t";
    }
    
    // decrease the line a tab backward
    private void decreaseTab() {
        tab = tab.substring(1);
    }
    
    // tags the token with <open> and </close>
    private void writeTag(String word, String type) throws IOException {
        this.writer.write(tab + "<" + type + "> " + word + " </" + type + ">");
    }

    // checks the symbol 
    private boolean checkSymbol(String s) throws IOException {
        if (s.equals("<")) { s = "&lt;"; }
        else if (s.equals(">")) { s = "&gt;"; }
        else if (s.equals("&")) { s = "&amp;"; }

        if (tokenizer.getToken() == (s)) {
            writeTag(s, "symbol");
            return true;
        } else {
            return false;
        }
    }

    // if a valid var type then returns true and writes a corresponding tag
    // if a valid identifier then returns true and writes a corresponding tag
    // else false
    private boolean checkValType() throws IOException{
        if (tokenizer.keyWord() == JackTokenizer.KeyWord.INT ||
            tokenizer.keyWord() == JackTokenizer.KeyWord.BOOLEAN ||
            tokenizer.keyWord() == JackTokenizer.KeyWord.CHAR) {
            writeTag(tokenizer.getToken(), "keyword");
            return true;
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
            return true;
        } else {
            return false;
        }
    }
}
