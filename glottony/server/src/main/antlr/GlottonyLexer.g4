lexer grammar GlottonyLexer;

@header {
package dev.dialector.glottony.parser;
}

FUN: 'fun';
STRUCT: 'struct';
LPAREN: '(';
RPAREN: ')';
LCURL: '{';
RCURL: '}';
LBRACKET: '[';
RBRACKET: ']';
VAL: 'val';
RETURN: 'return';
ARROW: '->';
LANGLE: '<';
RANGLE: '>';
COLON: ':';
COMMA: ',';
EQ: '=';
PLUS: '+';
MINUS: '-';
MUL: '*';
DIV: '/';
DOT: '.';
QUOTE: '"';
NUMBER_TYPE: 'num';
INTEGER_TYPE: 'int';
STRING_TYPE: 'string';

WS
    : [\u0020\u0009\u000C]
      -> channel(HIDDEN)
    ;

NL: '\u000A' | '\u000D' '\u000A' ;

NUMBER
    : ('0' .. '9')* (DOT ('0' .. '9')+)
    ;

INTEGER
    : ('0' .. '9')+
    ;

STRING
    : QUOTE VALID_STRING QUOTE
    ;

fragment VALID_STRING
    : ~('\n' | '\r' | '"')*
    ;

IDENTIFIER
    : VALID_ID_START VALID_ID_CHAR*
    ;

fragment VALID_ID_START
   : ('a' .. 'z')
   | ('A' .. 'Z')
   | '_'
   ;

fragment VALID_ID_CHAR
   : VALID_ID_START
   | ('0' .. '9')
   ;