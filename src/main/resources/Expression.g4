grammar Expression;

expression
    : additiveExpression EOF?
    ;

additiveExpression
    : multiplicativeExpression MINUS additiveExpression
    | multiplicativeExpression ((MINUS | PLUS) additiveExpression)?
    ;

multiplicativeExpression
    : powerExpression ((TIMES | DIVISION)? multiplicativeExpression)?
    ;

powerExpression
    : unaryExpression (POWER powerExpression)?
    ;

unaryExpression
    : (MINUS | SQRT)? primaryExpression
    ;

primaryExpression
    : NUMBER
    | VAR
    | PI
    | LPAREN additiveExpression RPAREN
    ;

DIVISION : '/';
LPAREN   : '(';
MINUS    : '-';
PI       : 'pi'   | '\u03C0';//Unicode symbol for pi
SQRT     : 'sqrt' | '\u221A';//Unicode symbol for square root
PLUS     : '+';
POWER    : '^';
RPAREN   : ')';
TIMES    : '*';
NUMBER   : [0-9]+ ('.' [0-9]+)?;
VAR      : 'EC' | 'EN' | 'MC' | [a-zA-Z];
WS       : [ \t\r\n]+  -> skip ;
