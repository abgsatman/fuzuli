/*
 * fuzuli : A general purpose interpreter
 * Copyright (C) 2016 Mehmet Hakan Satman <mhsatman@yahoo.com>
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
package org.expr.fuzuli.interpreter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;


public class FunctionInternalExpression extends FunctionExpression {

    private Expression internalExpression;
    private Class ExprClass;
    private Constructor[] constructors;
    
    public FunctionInternalExpression(ArrayList<Expression> expr){
        super(expr);
    }
    
    public FunctionInternalExpression(ArrayList<Expression> expr, String fname, Class Clz){
        super(expr);
        this.fname = fname;
        this.ExprClass = Clz;
        this.exprs = expr;
        
        constructors = Clz.getConstructors();
        try{
            this.internalExpression = (Expression)constructors[0].newInstance(this.exprs);
        }catch (Exception insErr){
            throw new FuzuliException(insErr, "Internal function "+fname +" call error for class "+Clz.toString());
        }
    }
    
    @Override
    public Object eval(Environment e) {
        this.internalExpression.exprs = this.exprs;
        return(this.internalExpression.eval(e));
    }

}
