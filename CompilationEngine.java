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

    public CompilationEngine(String input) throws IOException{
        this.tokenizer =new JackTokenizer(new File(input));
        String outputName = input.split("\\.")[0] + ".xml";
        this.output = new File(outputName);
        this.writer = new FileWriter(output);
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
            System.out.println("missing '('");
            return;
        }

        // match the closing ) for the paramater list
        if (tokenizer.getToken().equals(")")) {
            writeTag(")", "symbol");
        } else {
            System.out.println("missing ')'");
            return;
        }

        // checks after the declaration has a openning "{"
        tokenizer.advance();
        if (tokenizer.getToken().equals("{")) {
            compileSubroutineBody();
        } else {
            System.out.println("missing '{'");
            return;
        }
        
        decreaseTab();
        writer.write(tab + "</subroutineDec>");
    }

    // compiles a (possible empty) parameter list.
    // does not handle the enclosing parenthesis tokens (and).
    private void compileParameterList() throws IOException {
        writer.write(tab + "<parameterList>");
        increaseTab();

        // write var type
        tokenizer.advance();
        if (checkValType()) {
            // checks if has varName
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal identifier in parameter list");
                return;
            }

            // checks if has more vars 
            tokenizer.advance();
            while (tokenizer.getToken().equals(",")) {
                writeTag(",", "symbol");
                tokenizer.advance();
                if (!checkValType()) {
                    System.out.println("illegal type name");
                    return;
                }
                tokenizer.advance();
                if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                    writeTag(tokenizer.identifier(), "identifier");
                } else {
                    System.out.println("illegal identifier name");
                    return;
                }
                tokenizer.advance();
            }
        }

        decreaseTab();
        writer.write(tab + "</parameterList>");
    }

    // compiles a subroutine body
    private void compileSubroutineBody() throws IOException{
        writer.write(tab + "<subroutineBody>");
        increaseTab();

        writeTag("{", "symbol");

        tokenizer.advance();
        while ( tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                tokenizer.getToken().equals("var")) {
            compileVarDec();
            tokenizer.advance();
        }
        
        compileStatements();

        // checks if has a closing }
        if (!checkSymbol("}")) {
            System.out.println("missing } to close subroutine");
            System.out.printf("current token is : %s\n", tokenizer.getToken());
        }

        decreaseTab();
        writer.write(tab + "</subroutineBody>");
    }

    // compile a var declaration
    public void compileVarDec() throws IOException {
        writer.write(tab + "<varDec>");
        increaseTab();

        // write the var
        writeTag("var", "keyword");

        // checks the type of the var
        tokenizer.advance();
        if (!checkValType()) {
            System.out.println("illegal type for var");
            return;
        }

        // check the name of the variable
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
        } else {
            System.out.println("illegal identifier for var");
            return;
        }

        tokenizer.advance();
        while (tokenizer.getToken().equals(",")) {
            writeTag(",", "symbol");

            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal identifier for var");
                return;
            }
            tokenizer.advance();
        }

        if (tokenizer.getToken().equals(";")) {
            writeTag(";", "symbol");
        } else {
            System.out.println("varDec doesn't end with ;");
            return;
        }

        decreaseTab();
        writer.write(tab + "</varDec>");
    }
  
    // compiles statments 
    public void compileStatements() throws IOException{
        writer.write(tab + "<statements>");
        increaseTab();

        while (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD) {
            if (tokenizer.keyWord() == JackTokenizer.KeyWord.LET){
                compileLet(); 
                tokenizer.advance();
            }
            else if (tokenizer.keyWord() == JackTokenizer.KeyWord.IF){
                compileIf(); 
                tokenizer.advance();
            }
             else if (tokenizer.keyWord() == JackTokenizer.KeyWord.WHILE){
                compileWhile(); 
                tokenizer.advance();
            }
            else if (tokenizer.keyWord() == JackTokenizer.KeyWord.DO){
                compileDo(); 
                tokenizer.advance();
            }
            else if (tokenizer.keyWord() == JackTokenizer.KeyWord.RETURN){
                compileReturn(); 
                tokenizer.advance();
            }
            else {
                System.out.println("illegal statement");
            }
        }

        decreaseTab();
        writer.write(tab + "</statements>");
    }

    // compiles a let statement
    public void compileLet() throws IOException {
        writer.write(tab + "<LetStatement>");
        increaseTab();

        writeTag("let", "keyword");

        tokenizer.advance();
        if(!checkIdentifier()) {
            System.out.println("Illegal identifier");
            return;
        }

        tokenizer.advance();
        if (checkSymbol("[")) {
            tokenizer.advance();
            compileExpression();

            if(!checkSymbol("]")) {
                System.out.printf("No closing ], current: %s\n", tokenizer.getToken());
                return;
            }
            // if has [], advance and next should be =
            tokenizer.advance();
        }

        if (!checkSymbol("=")) {
            System.out.println("No = found");
            return;
        }

        tokenizer.advance();
        compileExpression();

        // No need to advance because compileExpression does one token look ahead
        if (!checkSymbol(";")) {
            System.out.println("No ; found at the end of statement");
            return;
        }

        decreaseTab();
        writer.write(tab + "</letStatement>");
    }
  
    // compiles an if statement
    public void compileIf() throws IOException{
       writer.write(tab + "<ifStatement>"); 
       increaseTab();

       writeTag("if", "keyword");

       tokenizer.advance();
       // checks if the statment has an openning "("
        if (!checkSymbol("(")) {
            System.out.println("missing an openning '(' in the if statement");
            return;
        }

        tokenizer.advance();
        compileExpression(); //checks if the if expression is valid 

         // checks if the statment has an closing ")"
        if (!checkSymbol(")")) {
            System.out.println("missing closing ) in the if statement");
            return;
        }

        tokenizer.advance();
        // checks if after the if expression there is an openning '{'
        if (!checkSymbol("{")) {
            System.out.println("missing { in the if statement");
            return;
        }
        
        tokenizer.advance();
        compileStatements();
        
        // checks if after the if expression there is a closing '}'
        if (!checkSymbol("}")) {
            System.out.println("missing } in the if statement");
            System.out.printf("the current symbol is %s\n", tokenizer.getToken());
            return;
        }
        
        tokenizer.advance();
        // checks if contains an else statement
        if (checkKeyword("else")) {
            tokenizer.advance();
            // checks if after the else expression there is an openning '{'
            if (!checkSymbol("{")) {
                System.out.println("missing { in the else statement");
                return;
            }

            tokenizer.advance();
            compileStatements();

            // checks if after the else expression there is a closing '}'
            if (!checkSymbol("}")) {
                System.out.println("missing } in the else statement");
                return;
            }
            tokenizer.advance();
        }

        decreaseTab();
        writer.write(tab + "</ifStatement>");
    }
    
    // compiles a while statement
    public void compileWhile() throws IOException{
        writer.write(tab + "<whileStatement>"); 
        increaseTab();
       
        writeTag("while", "keyword");

        tokenizer.advance();
        // checks whether the while contains an openning '('
        if (!checkSymbol("(")) {
            System.out.println("missing '(' in the while statement");
            return;
        }
        
        tokenizer.advance();
        compileExpression();
        
        // checks whether the while contains a closing ')'
        if (!checkSymbol(")")) {
            System.out.println("No ) in while statement");
            return;
        }
        
        tokenizer.advance();
        // checks if contains an openning '{'
        if (!checkSymbol("{")) {
            System.out.println("missing '{' in the while statement");
            return;
        }
        
        // checks for statements
        tokenizer.advance();
        compileStatements();
        
        // checks if contains a closing '}'
        if (!checkSymbol("}")) {
            System.out.println("missing '}' in the while statement");
            return;
        }

        decreaseTab();
        writer.write(tab + "</whileStatement>");
    }
   
    // compiles do
    public void compileDo() throws IOException{
        writer.write(tab + "<doStatement>"); 
        increaseTab();

        writeTag("do", "keyword");

        tokenizer.advance();
        // checks if the token is a valid identifier
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");

            tokenizer.advance();
            // checks whether it has a '(' or '.'
            if (checkSymbol(".") || checkSymbol("(")) {
                compileSubroutineAdvnc();
            } else {
                System.out.println("Not valid subroutine call");
                return;
            }
        } else{
            System.out.println("Not a valid identifier for do statement");
            return;
        }

        tokenizer.advance();
        // checks the ends with ';'
        if (!checkSymbol(";")) {
            System.out.println("missing closing ;");
            return;
        }

        decreaseTab();
        writer.write(tab + "</doStatement>");
    }

    // compiles a return statement
    public void compileReturn() throws IOException {
        writer.write(tab + "<returnStatement>");
        increaseTab();

        writeTag("return", "keyword");

        tokenizer.advance();
        // checks whether the symbol is ";" if true try to compile token
        if (!checkSymbol(";")) {
            compileExpression();

            // checks if at the end of the expression there is ";"
            if (!checkSymbol(";")) {
                System.out.println("invalid return statement - does not contain a ;");
            }
        }
        decreaseTab();
        writer.write(tab + "</returnStatement>");
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

    // check whether its an identifier or not
    private boolean checkIdentifier() throws IOException {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
            return true;
        } else {
            return false;
        }
    }

    // compiles an experession
    public void compileExpression() throws IOException {
        writer.write(tab + "<expression>");
        increaseTab();
        // checks whether the experation has a token after
        compileTerm();

        // checks for an operation symbol
        while (checkSymbol("+") || checkSymbol("-") || checkSymbol("*") || checkSymbol("/") ||
                checkSymbol("&") || checkSymbol("|") || checkSymbol("<") || checkSymbol(">") ||
                checkSymbol("=")) {
            tokenizer.advance();
            compileTerm();
        }

        decreaseTab();
        writer.write(tab + "</expression>");
    }
    
    // compiles a list. note that wanted a return int  
    public void compileExpressionList() throws IOException {
        writer.write(tab + "<expressionList>");
        increaseTab();
        // checks if has a closing ')'
        if (!tokenizer.getToken().equals(")")) {
            compileExpression();

            // checks elements terms in the list 
            while (checkSymbol(",")) {
                tokenizer.advance();
                compileExpression();
            }
        }
        decreaseTab();
        writer.write(tab + "</expressionList>");
    }
    
    //
    public void compileSubroutineAdvnc() throws IOException {
        // the case where we have a list 
        if (tokenizer.getToken().equals("(")) {
            tokenizer.advance();
            compileExpressionList();

            if (!checkSymbol(")")) {
                System.out.println("No closing ) for the expressionlist");
                return;
            }
        // the case of an identifier
        }else{
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal identifier for subroutine call");
                return;
            }

            tokenizer.advance();
            if (!checkSymbol("(")) {
                System.out.println("Expecting a open bracket in subroutine call");
                return;
            }

            tokenizer.advance();
            compileExpressionList();

            if (!checkSymbol(")")) {
                System.out.println("No closing ) for the expressionlist");
                return;
            }
        }
    }
    
    // compiles the term
    public void compileTerm() throws IOException {
        writer.write(tab + "<term>");
        increaseTab();

        if(tokenizer.tokenType() == JackTokenizer.TokenType.INT_CONST) {
            writeTag(Integer.toString(tokenizer.intVal()), "integerConstant");
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.STRING_CONST) {
            writeTag(tokenizer.stringVal(), "stringConstant");
            tokenizer.advance();
        } else if (checkKeyword("ture") || checkKeyword("false") || checkKeyword("null") || checkKeyword("this")) {
            tokenizer.advance();
        } else if (checkKeyword("-") || checkKeyword("~")) {
            // checks the next token 
            tokenizer.advance();
            compileTerm();
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");

            tokenizer.advance();
            // checks if the term is an array by containing '['
            if(checkSymbol("[")) {
                compileArray();
                tokenizer.advance();
            }
            // checks if the term is a list or an '.'
            else if (checkSymbol("(") || checkSymbol(".")) {
                compileSubroutineAdvnc(); 
                tokenizer.advance();
            }

        // otherwise it is an identifier
        } else if (tokenizer.tokenType() == JackTokenizer.TokenType.SYMBOL) {
            if (checkSymbol("(")) {
                tokenizer.advance();
                compileExpression();
                if (checkSymbol(")")) {
                    tokenizer.advance();
                } else {
                    System.out.println("missing ')' for term");
                }
            }
        } else {
            System.out.printf("illegal varName: %s\n", tokenizer.getToken());
            return;
        }

        decreaseTab();
        writer.write(tab + "</term>");
    }
    
    // compiles an array
    public void compileArray() throws IOException {
        tokenizer.advance();
        compileExpression();

        if (!checkSymbol("]")) {
            System.out.println("No closing ] for the array expression");
        }
    }
    
    // checks the exact keyword 
    private boolean checkKeyword(String k) throws IOException {
        if (tokenizer.tokenType() == JackTokenizer.TokenType.KEYWORD &&
                tokenizer.getToken().equals(k)) {
            writeTag(k, "keyword");
            return true;
        } else {
            return false;
        }
    }

}
