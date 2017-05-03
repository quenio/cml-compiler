grammar Expressions;

import Literals;

expression returns [Expression expr]
    : literalExpression
    | pathExpression
    | operator=('+' | '-') expression
    | <assoc=right> expression operator='^' expression
    | expression operator=('*' | '/' | '%') expression
    | expression operator=('+' | '-') expression
    | expression operator=('==' | '!=' | '<' | '<=' | '>' | '>=') expression;


pathExpression returns [Path path]:
    NAME ('.' NAME)*;