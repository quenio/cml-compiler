grammar Expressions;

import Literals, Paths;

expression returns [Expression expr]
    : literalExpression
    | pathExpression
    | comprehensionExpression
    | operator=('+' | '-' | NOT) expression
    | <assoc=right> expression operator='^' expression
    | expression operator=('*' | '/' | '%') expression
    | expression operator=('+' | '-') expression
    | expression operator=('<' | '<=' | '>' | '>=') expression
    | expression operator=('==' | '!=') expression
    | expression operator=AND expression
    | expression operator=OR expression
    | expression operator=XOR expression
    | expression operator=IMPLIES expression
    | IF cond=expression
      THEN then=expression
      ELSE else_=expression
    | variable=NAME assignment='=' value=expression
    | invocationExpression
    | keyword=NAME ':' arg=expression
    | expression operator=',' expression
    | expression operator='|' expression
    | '(' inner=expression ')';

invocationExpression returns [Invocation invocation]:
    NAME '(' expression (',' expression)* ')';

comprehensionExpression returns [Comprehension comprehension]:
    FOR enumeratorDeclaration (',' enumeratorDeclaration)* '|' expression;

enumeratorDeclaration returns [Enumerator enumerator]:
    var=NAME IN pathExpression;

//transformDeclaration returns [Transform transform]:
//    (FROM var=NAME '=' init=expression)?
//    operation=
//        ( REJECT
//        | YIELD    | RECURSE
//        | INCLUDES | EXCLUDES
//        | EVERY    | EXISTS
//        | REDUCE
//        | TAKE     | DROP
//        | FIRST    | LAST
//        | COUNT    | SUM       | AVERAGE
//        | MAX      | MIN
//        | REVERSE)
//    suffix=(UNIQUE | WHILE)?
//    expr=expression?;

