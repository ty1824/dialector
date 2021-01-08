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
    : FUN name=IDENTIFIER parameters=functionParameters COLON returnType=type EQ body
    ;

functionParameters
    : LPAREN RPAREN
    | LPAREN (parameterDeclaration (COMMA parameterDeclaration)*)? RPAREN
    ;

structDeclaration
    : STRUCT name=IDENTIFIER LPAREN NL* (fieldDeclaration NL* (COMMA NL* fieldDeclaration NL*)*)? COMMA? NL* RPAREN
    ;

fieldDeclaration
    : name=IDENTIFIER COLON type
    ;

parameterDeclaration
    : name=IDENTIFIER (COLON type)?
    ;

body
    : expression
    ;

expression
    : additiveExpression
    ;

additiveExpression
    : multiplicativeExpression (addOperator multiplicativeExpression)*
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

statement
    : valStatement
    | returnStatement
    ;

valStatement
    : VAL name=IDENTIFIER (COLON type)? EQ expression
    ;

returnStatement
    : RETURN expression
    ;

block
    : LCURL NL* (statement NL* (statement NL*)*)? RCURL
    ;

lambdaLiteral
    : LCURL NL* (lambdaParameters NL* ARROW NL*)? body NL* RCURL
    ;

lambdaParameters
    : parameterDeclaration (NL* COMMA NL* parameterDeclaration)*
    ;

argumentList
    : LPAREN RPAREN
    | LPAREN NL* expression? (NL* COMMA NL* expression)* NL* COMMA? NL* RPAREN
    ;

numberLiteral
    : value=NUMBER
    ;

integerLiteral
    : value=INTEGER
    ;

stringLiteral
    : value=STRING
    ;

simpleIdentifier
    : referent=IDENTIFIER
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
    | functionType
    | identifier
    ;

functionTypeParameterList
    : LPAREN RPAREN
    | LPAREN (functionTypeParameterDefinition (COMMA functionTypeParameterDefinition)*)? RPAREN
    ;

functionTypeParameterDefinition
    : (name=identifier COLON)? type
    ;

functionType
    : functionTypeParameterList ARROW returnType=type
    ;

typeConstructor
    : referent=identifier typeParameterList
    ;

typeParameterList
    : LANGLE RANGLE
    | LANGLE (typeParameterDeclaration (COMMA typeParameterDeclaration)*)? RANGLE
    ;

typeParameterDeclaration
    : name=identifier (COLON type)?
    ;
