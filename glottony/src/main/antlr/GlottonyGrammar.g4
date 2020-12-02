parser grammar GlottonyGrammar;

options { tokenVocab = GlottonyLexer; }

@header {
package dev.dialector.glottony.parser;
}

file
    : NL* (topLevelConstruct NL*)* EOF
    ;

topLevelConstruct
    : functionDeclaration
    | structDeclaration
    | expression
    ;

functionDeclaration
    : FUN IDENTIFIER functionParameters COLON type EQ body
    ;

functionParameters
    : LPAREN RPAREN
    | LPAREN (parameterDeclaration (COMMA parameterDeclaration)*)? RPAREN
    ;

structDeclaration
    : STRUCT IDENTIFIER LPAREN NL* (fieldDeclaration NL* (COMMA NL* fieldDeclaration NL*)*)? COMMA? NL* RPAREN
    ;

fieldDeclaration
    : IDENTIFIER COLON type
    ;

parameterDeclaration
    : IDENTIFIER COLON type
    ;

body
    : expression
    ;

expression
    : additiveExpression
    ;

additiveExpression
    : multiplicativeExpression (addOperator expression)*
    ;

multiplicativeExpression
    : callExpression (multiplyOperator callExpression)*
    ;

callExpression
    : memberAccessExpression
    | memberAccessExpression argumentList+
    ;

memberAccessExpression
    : primaryExpression
    | primaryExpression DOT identifier
    ;

primaryExpression
    : simpleIdentifier
    | numberLiteral
    | integerLiteral
    | stringLiteral
    | lambdaLiteral
    | block
    ;

block
    : LCURL NL* expression* NL* RCURL
    ;

lambdaLiteral
    : LCURL NL* lambdaParameters NL* ARROW NL* body NL* RCURL
    ;

lambdaParameters
    : parameterDeclaration? (NL* COMMA NL* parameterDeclaration)*
    ;

argumentList
    : LPAREN RPAREN
    | LPAREN NL* expression? (NL* COMMA NL* expression)* NL* COMMA? NL* RPAREN
    ;

numberLiteral
    : NUMBER
    ;

integerLiteral
    : INTEGER
    ;

stringLiteral
    : STRING
    ;

simpleIdentifier
    : IDENTIFIER
    ;

identifier
    : simpleIdentifier (DOT simpleIdentifier)*
    ;

addOperator
    : PLUS
    | MINUS
    ;

multiplyOperator
    : MUL
    | DIV
    ;

type
    : NUMBER_TYPE
    | INTEGER_TYPE
    | STRING_TYPE
    | identifier
    ;

typeConstructor
    : identifier typeParameterList
    ;

typeParameterList
    : LANGLE RANGLE
    | LANGLE (typeParameterDeclaration (COMMA typeParameterDeclaration)*)? RANGLE
    ;

typeParameterDeclaration
    : identifier (COLON type)?
    ;
