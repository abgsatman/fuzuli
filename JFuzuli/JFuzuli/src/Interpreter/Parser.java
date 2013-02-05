/*
 * fuzuli : A general purpose interpreter
 * Copyright (C) 2013 Mehmet Hakan Satman <mhsatman@yahoo.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author hako
 */
public class Parser {

    String sourcecode;
    int charIndex;
    int tokenIndex;
    ArrayList<Token> tokens;

    public Parser(String code) {
        this.sourcecode = code;
        resetParser();
    }

    private void read(BufferedReader reader) throws Exception {
        StringBuffer code = new StringBuffer();
         
            char[] chars = new char[1024];
            int result;
            while (true) {
                result = reader.read(chars);
                if (result == -1) {
                    break;
                }
                code.append(chars);
                for (int i = 0; i < chars.length; i++) {
                    chars[i] = '\0';
                }
            }
            this.sourcecode = code.toString();
        }
    

    public Parser(File file) {
        StringBuffer code = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            this.read(reader);
        } catch (Exception e) {
            System.out.println("Error reading file:" + file.toString());
            e.printStackTrace();
        }
        resetParser();
    }
    
    public Parser(InputStream is){
        StringBuffer code = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            this.read(reader);
        } catch (Exception e) {
            System.out.println("Error reading stream:" + is.toString());
            e.printStackTrace();
        }
        resetParser();
    }

    public void resetParser() {
        charIndex = 0;
        tokenIndex = 0;
        tokens = new ArrayList<Token>();
        readAllTokens();
    }

    public char consume() {
        char current = this.sourcecode.charAt(charIndex);
        charIndex++;
        if (charIndex == sourcecode.length()) {
            return ('\0');
        }
        return (current);
    }

    public void putBackChar() {
        charIndex--;
    }

    public char looknext() {
        if ((charIndex + 1) != sourcecode.length()) {
            return (this.sourcecode.charAt(charIndex + 1));
        } else {
            return ('\0');
        }
    }
    

    public void readAllTokens(){
        while(true){
            Token tok = parseNextToken();
            this.tokens.add(tok);
            if(tok.type == Token.TokenType.EOP){
                break;
            }
        }
    }
    
    public Token parseNextToken() {
        StringBuffer buf = new StringBuffer();
        Token tok = new Token();
        char current;
        current = consume();
        if (current == '\0') {
            tok.type = Token.TokenType.EOP;
            tok.content = "End of Program";
            return (tok);
        } else if (current == ' ' || current == '\t' || current == '\n' || current == '\r' || Character.isSpaceChar(current)) {
            return (parseNextToken());
        } else if (current == '#'){
            while(true){
                current = consume();
                if (current == '\n') break;
            }
          return (parseNextToken());
        }else if (current == '(') {
            tok.content = "(";
            tok.type = Token.TokenType.LPARAN;
            return (tok);
        } else if (current == ')') {
            tok.content = ")";
            tok.type = Token.TokenType.RPARAN;
            return (tok);
        } else if (Character.isDigit(current)) {
            buf.append(current);
            while (true) {
                current = consume();
                if (Character.isDigit(current) || current == '.' || current == 'E') {
                    if (current == '.') {
                        tok.type = Token.TokenType.DOUBLE;
                    }
                    buf.append(current);
                } else {
                    putBackChar();
                    break;
                }
            }
            tok.content = buf.toString();
            tok.type = Token.TokenType.DOUBLE;
            return (tok);
        } else if (current == '+') {
            char next = consume();
            if (next == '+'){
                tok.content ="++";
                tok.type = Token.TokenType.PLUSPLUS;
                return(tok);
            }
            putBackChar();
            tok.content = "+";
            tok.type = Token.TokenType.PLUS;
            return (tok);
        } else if (current == '-') {
            char next = consume();
            if(Character.isDigit(next)){
                putBackChar();
                Token t = this.parseNextToken();
                buf.append("-");
                buf.append(t.content);
                tok.content = buf.toString();
                tok.type = Token.TokenType.DOUBLE;
                return(tok);
            }else if (next == '-'){
                tok.content ="--";
                tok.type = Token.TokenType.MINUSMINUS;
                return(tok);
            }
            putBackChar();
            tok.content = "-";
            tok.type = Token.TokenType.MINUS;
            return (tok);
        } else if (current == '/'){
            tok.content = "/";
            tok.type = Token.TokenType.DIVISION;
            return(tok);
        } else if (current == '*'){
            tok.content = "*";
            tok.type = Token.TokenType.ASTERIX;
            return(tok);
        }else if (current == '='){
            tok.content = "=";
            tok.type = Token.TokenType.EQUALS;
            return(tok);
        }else if (current == '<'){
            char next = consume();
            if(next=='<'){
                tok.content = "<<";
                tok.type = Token.TokenType.BITSHIFTLEFT;
                return(tok);
            }else if(next=='='){
                tok.content = "<=";
                tok.type = Token.TokenType.LESSOREQUAL;
                return(tok);
            }else {
                putBackChar();
            }
            tok.content = "<";
            tok.type = Token.TokenType.LESS;
            return(tok);
        }else if (current == '>'){
            char next = consume();
            if(next=='>'){
                tok.content = ">>";
                tok.type = Token.TokenType.BITSHIFTRIGHT;
                return(tok);
            }else if(next=='='){
                tok.content = ">=";
                tok.type = Token.TokenType.BIGGEROREQUAL;
                return(tok);
            }else {
                putBackChar();
            }
            tok.content = ">";
            tok.type = Token.TokenType.BIGGER;
            return(tok);
        }else if (current == '&'){
            tok.content = "&";
            tok.type = Token.TokenType.BITAND;
            return(tok);
        }else if (current == '|'){
            tok.content = "|";
            tok.type = Token.TokenType.BITOR;
            return(tok);
        }else if (current == '~'){
            tok.content = "~";
            tok.type = Token.TokenType.BITNOT;
            return(tok);
        }else if (current == '^'){
            tok.content = "^";
            tok.type = Token.TokenType.BITXOR;
            return(tok);
        }else if (current == ':'){
            tok.content = ":";
            tok.type = Token.TokenType.COLON;
            return(tok);
        }else if (current == '!'){
            buf.append(current);
            current = consume();
            if(current == '=') {
                buf.append(current);
                tok.content = buf.toString();
                tok.type = Token.TokenType.NOTEQUAL;
                return(tok);
            }else{
                putBackChar();
            }
           tok.content = "!";
           tok.type = Token.TokenType.EXCLAMATION;
           return(tok);
        }else if (Character.isLetter(current)) {
            buf.append(current);
            while (true) {
                current = consume();
                if (!Character.isLetter(current) && !Character.isDigit(current) && current!='_') {
                    putBackChar();
                    break;
                }
                buf.append(current);
            }
            tok.content = buf.toString();
            tok.type = Token.TokenType.IDENTIFIER;
            return (tok);
        } else if (current == '"') {
            while (true) {
                current = consume();
                if (current == '\\'){
                    current = consume();
                    if (current == 'n'){
                        buf.append("\n");
                    }
                    current = consume();
                }
                if (current == '"') {
                    break;
                }
                buf.append(current);
            }
            tok.type = Token.TokenType.STRING;
            tok.content = buf.toString();
            return(tok);
        }

        throw new RuntimeException("Unknow character: '"+current+"'");
    }

    public ArrayList<Expression> getExpressionList() {
        ArrayList<Expression> exprs = new ArrayList<Expression>();
        while (true) {
            Expression e = getNextExpression();
            if (e == null) {
                break;
            }
            exprs.add(e);
        }
        return (exprs);
    }
    
    public Token getNextToken(){
        Token tok = this.tokens.get(tokenIndex);
        tokenIndex++;
        return(tok);
    }
    
    public Token getPreviousToken(){
        if (tokenIndex >1){
            return(this.tokens.get(tokenIndex-2));
        }else{
            return(null);
        }
    }

    public Expression getNextExpression() {
        Token tok;
        ArrayList<Expression> exprs = null;
        tok = getNextToken();
        if (tok.type == Token.TokenType.EOP){
            return (null);
        }else if (tok.type == Token.TokenType.LPARAN) {
            return getNextExpression();
        } else if (tok.type == Token.TokenType.RPARAN) {
            return (null);
        } else if (tok.type == Token.TokenType.DOUBLE) {
            ConstantNumberExpression dexpr = new ConstantNumberExpression(Double.parseDouble(tok.content));
            return (dexpr);
        } else if (tok.type == Token.TokenType.PLUS) {
            exprs = getExpressionList();
            return (new PlusExpression(exprs));
        } else if (tok.type == Token.TokenType.MINUS) {
            exprs = getExpressionList();
            return (new MinusExpression(exprs));
        } else if (tok.type == Token.TokenType.DIVISION) {
            exprs = getExpressionList();
            return (new DivisionExpression(exprs));
        } else if (tok.type == Token.TokenType.ASTERIX) {
            exprs = getExpressionList();
            return (new AsterixExpression(exprs));
        }  else if (tok.type == Token.TokenType.EQUALS) {
            exprs = getExpressionList();
            return (new EqualsExpression(exprs));
        } else if (tok.type == Token.TokenType.NOTEQUAL) {
            exprs = getExpressionList();
            return (new NotEqualsExpression(exprs));
        } else if (tok.type == Token.TokenType.LESS) {
            exprs = getExpressionList();
            return (new LessExpression(exprs));
        }  else if (tok.type == Token.TokenType.BIGGER) {
            exprs = getExpressionList();
            return (new BiggerExpression(exprs));
        }  else if (tok.type == Token.TokenType.BIGGEROREQUAL) {
            exprs = getExpressionList();
            return (new BiggerOrEqualExpression(exprs));
        }  else if (tok.type == Token.TokenType.LESSOREQUAL) {
            exprs = getExpressionList();
            return (new LessOrEqualExpression(exprs));
        }else if (tok.type == Token.TokenType.EXCLAMATION) {
            exprs = getExpressionList();
            return (new BitNotExpression(exprs));
        }else if (tok.type == Token.TokenType.BITAND) {
            exprs = getExpressionList();
            return (new BitAndExpression(exprs));
        }else if (tok.type == Token.TokenType.BITOR) {
            exprs = getExpressionList();
            return (new BitOrExpression(exprs));
        }else if (tok.type == Token.TokenType.BITNOT) {
            exprs = getExpressionList();
            return (new BitNotExpression(exprs));
        }else if (tok.type == Token.TokenType.BITXOR) {
            exprs = getExpressionList();
            return (new BitXorExpression(exprs));
        }else if (tok.type == Token.TokenType.BITSHIFTLEFT) {
            exprs = getExpressionList();
            return (new BitShiftLeftExpression(exprs));
        }else if (tok.type == Token.TokenType.BITSHIFTRIGHT) {
            exprs = getExpressionList();
            return (new BitShiftRightExpression(exprs));
        }else if (tok.type == Token.TokenType.COLON) {
            exprs = getExpressionList();
            return (new ColonExpression(exprs));
        }else if (tok.content.equals("++")){
                exprs = getExpressionList();
                return (new IncExpression(exprs));
        }else if (tok.content.equals("--")){
                exprs = getExpressionList();
                return (new DecExpression(exprs));
        }else if (tok.type == Token.TokenType.STRING){
            return (new StringExpression(tok.content));
        }else if (tok.type == Token.TokenType.IDENTIFIER) {
            if (tok.content.equals("print")) {
                exprs = getExpressionList();
                return (new PrintExpression(exprs));
            } else if (tok.content.equals("println")) {
                exprs = getExpressionList();
                return (new PrintlnExpression(exprs));
            } else if (tok.content.equals("and")) {
                exprs = getExpressionList();
                return (new AndExpression(exprs));
            }else if (tok.content.equals("let")) {
                exprs = getExpressionList();
                return (new LetExpression(exprs));
            }else if (tok.content.equals("if")){
                exprs = getExpressionList();
                return (new IfExpression(exprs));
            }else if (tok.content.equals("switch")){
                exprs = getExpressionList();
                return (new SwitchExpression(exprs));
            }else if (tok.content.equals("case")){
                exprs = getExpressionList();
                return (new CaseExpression(exprs));
            }else if (tok.content.equals("block")){
                exprs = getExpressionList();
                return (new BlockExpression(exprs));
            }else if (tok.content.equals("setepsilon")){
                exprs = getExpressionList();
                return (new SetEpsilonExpression(exprs));
            }else if (tok.content.equals("typeof")){
                exprs = getExpressionList();
                return (new TypeOfExpression(exprs));
            }else if (tok.content.equals("for")){
                exprs = getExpressionList();
                return (new ForExpression(exprs));
            }else if (tok.content.equals("foreach")){
                exprs = getExpressionList();
                return (new ForEachExpression(exprs));
            }else if (tok.content.equals("while")){
                exprs = getExpressionList();
                return (new WhileExpression(exprs));
            }else if (tok.content.equals("break")){
                exprs = getExpressionList();
                return (new BreakExpression(exprs));
            }else if (tok.content.equals("def")){
                exprs = getExpressionList();
                return (new DefExpression(exprs));
            }else if (tok.content.equals("inc")){
                exprs = getExpressionList();
                return (new IncExpression(exprs));
            }else if (tok.content.equals("dec")){
                exprs = getExpressionList();
                return (new DecExpression(exprs));
            }else if (tok.content.equals("list")){
                exprs = getExpressionList();
                return (new ListExpression(exprs));
            }else if (tok.content.equals("nth")){
                exprs = getExpressionList();
                return (new NthExpression(exprs));
            }else if (tok.content.equals("length")){
                exprs = getExpressionList();
                return (new LengthExpression(exprs));
            }else if (tok.content.equals("set")){
                exprs = getExpressionList();
                return (new SetExpression(exprs));
            }else if (tok.content.equals("remove")){
                exprs = getExpressionList();
                return (new RemoveExpression(exprs));
            }else if (tok.content.equals("find")){
                exprs = getExpressionList();
                return (new FindExpression(exprs));
            }else if (tok.content.equals("append")){
                exprs = getExpressionList();
                return (new AppendExpression(exprs));
            }else if (tok.content.equals("prepend")){
                exprs = getExpressionList();
                return (new PrependExpression(exprs));
            }else if (tok.content.equals("fill")){
                exprs = getExpressionList();
                return (new FillExpression(exprs));
            }else if (tok.content.equals("first")){
                exprs = getExpressionList();
                return (new FirstExpression(exprs));
            }else if (tok.content.equals("last")){
                exprs = getExpressionList();
                return (new LastExpression(exprs));
            }else if (tok.content.equals("setprecision")){
                exprs = getExpressionList();
                return (new SetPrecisionExpression(exprs));
            }else if (tok.content.equals("function")){
                exprs = getExpressionList();
                return (new FunctionExpression(exprs));
            }else if (tok.content.equals("params")){
                exprs = getExpressionList();
                return (new ParamsExpression(exprs));
            }else if (tok.content.equals("return")){
                exprs = getExpressionList();
                return (new ReturnExpression(exprs));
            }else if (tok.content.equals("timing")){
                exprs = getExpressionList();
                return (new TimingExpression(exprs));
            }else if (tok.content.equals("require")){
                exprs = getExpressionList();
                return (new RequireExpression(exprs));
            }else if (tok.content.equals("dynload")){
                exprs = getExpressionList();
                return (new DynLoadExpression(exprs));
            }else if (tok.content.equals("javastatic")){
                exprs = getExpressionList();
                return (new JavaStaticExpression(exprs));
            }else{
                String fname = tok.content;
                if(this.getPreviousToken().type == Token.TokenType.LPARAN){                    
                    exprs = getExpressionList();
                    //System.out.println("Function call to "+fname+" with "+exprs.toString());
                    FunctionCallExpression fce = new FunctionCallExpression(exprs);
                    fce.fname = fname;
                    return(fce);
                }else{
                    return(new IdentifierExpression(tok.content));
                }
            }
        }
        throw new RuntimeException("Can not understand '" + tok.content+"'");
    }
}
