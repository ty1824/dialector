lexer grammar GlottonyLexer;

FUN: 'fun';
STRUCT: 'struct';
LPAREN: '(';
RPAREN: ')';
LCURL: '{';
RCURL: '}';
LBRACKET: '[';
RBRACKET: ']';
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
      -> skip
    ;

NL: '\u000A' | '\u000D' '\u000A' ;

NUMBER
    : ('0' .. '9')* (DOT ('0' .. '9')+)
    ;

INTEGER
    : ('0' .. '9')+
    ;

STRING
    : QUOTE ('a' .. 'z')* QUOTE
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