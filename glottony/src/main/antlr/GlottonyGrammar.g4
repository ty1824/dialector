parser grammar GlottonyGrammar;

options { tokenVocab = GlottonyLexer; }

@header {
package dev.dialector.glottony.parser;
}

file
    : functionDeclaration
    | expression
    ;

functionDeclaration
    : FUN IDENTIFIER LPAREN (parameterDeclaration (COMMA parameterDeclaration)*)? RPAREN COLON type EQ body
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
    : literalExpression (multiplyOperator literalExpression)*
    ;

literalExpression
    : numberLiteral
    | integerLiteral
    | stringLiteral
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
    ;