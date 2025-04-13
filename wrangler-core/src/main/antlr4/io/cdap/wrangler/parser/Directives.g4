/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

grammar Directives;

// Parser Rules

byteSize           : BYTE_SIZE;
timeDuration       : TIME_DURATION;
byteSizeList       : byteSize (',' byteSize)*;
timeDurationList   : timeDuration (',' timeDuration)*;
recipe
    : directive* EOF
    ;

directive
    : command arguments* SEMICOLON
    | pragmaVersion
    | pragmaLoadDirective
    ;

command
    : Identifier
    ;

arguments
    : Identifier
    | propertyList
    | numberRanges
    | column
    | colList
    | number
    | numberList
    | bool
    | boolList
    | text
    | stringList
    | ecommand
    | condition
    ;

propertyList
    : property (COMMA property)*
    ;

property
    : Identifier EQUAL (Number | Bool | text)
    ;

numberRanges
    : numberRange (COMMA numberRange)*
    ;

numberRange
    : Number COLON Number EQUAL value
    ;

value
    : String
    | Identifier
    ;

column
    : Column
    ;

colList
    : Column (COMMA Column)*
    ;

numberList
    : Number (COMMA Number)*
    ;

boolList
    : Bool (COMMA Bool)*
    ;

text
    : String
    ;

stringList
    : String (COMMA String)*
    ;

number
    : Number
    ;

bool
    : Bool
    ;

ecommand
    : BANG Identifier
    ;

condition
    : LPAREN expression RPAREN
    ;

expression
    : (Identifier | Number | Bool | Column | String)+
    ;

pragmaLoadDirective
    : HASH 'pragma' 'load-directives' identifierList SEMICOLON
    ;

identifierList
    : Identifier (COMMA Identifier)*
    ;

pragmaVersion
    : HASH 'pragma' 'version' Number SEMICOLON
    ;

// Lexer Rules

Identifier
    : [a-zA-Z_] [a-zA-Z_0-9]*
    ;

Number
    : [0-9]+ ('.' [0-9]+)?
    ;

Bool
    : 'true'
    | 'false'
    ;

String
    : '\'' (~('\'' | '\\') | '\\' .)* '\''
    | '"' (~('"' | '\\') | '\\' .)* '"'
    ;

Column
    : '$' Identifier
    ;

BANG
    : '!'
    ;

HASH
    : '#'
    ;

EQUAL
    : '='
    ;

COMMA
    : ','
    ;

COLON
    : ':'
    ;

SEMICOLON
    : ';'
    ;

LPAREN
    : '('
    ;

RPAREN
    : ')'
    ;

WS
    : [ \t\r\n]+ -> skip
    ;

BYTE_SIZE          : DIGITS ('.' DIGITS)? BYTE_UNIT;
TIME_DURATION      : DIGITS ('.' DIGITS)? TIME_UNIT;

fragment BYTE_UNIT : [KkMmGgTt]? [Bb];
fragment TIME_UNIT : ('ms' | 's' | 'sec' | 'm' | 'min' | 'h' | 'hr');

fragment DIGITS    : [0-9]+;