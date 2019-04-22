/*
 *  The contents of this file are subject to the Initial
 *  Developer's Public License Version 1.0 (the "License");
 *  you may not use this file except in compliance with the
 *  License. You may obtain a copy of the License at
 *  http://www.ibphoenix.com/main.nfs?a=ibphoenix&page=ibp_idpl.
 *
 *  Software distributed under the License is distributed AS IS,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied.
 *  See the License for the specific language governing rights
 *  and limitations under the License.
 *
 *  The Original Code was created by Mark O'Donohue
 *  for the Firebird Open Source RDBMS project.
 *
 *  Copyright (c) 2002 Mark O'Donohue <skywalker@users.sourceforge.net>
 *  and all contributors signed below.
 *
 *  All Rights Reserved.
 *  Contributor(s): ______________________________________.
 *
 *  2005.05.19 Claudio Valderrama: signal tokens that aren't reserved in the
 *      engine thanks to special handling.
 */

#include "firebird.h"

#ifdef HAVE_STRING_H
#include <string.h>
#endif

#include "dsql.tab.h"
#include "keywords.h"

// CVC: The latest column indicates whether the token as special handling in
// the parser. If it does, KEYWORD_stringIsAToken will return false.
// I discovered isql was being fooled and put double quotes around those
// special cases unnecessarily.

static const TOK tokens[] =
{
	{NOT_LSS, "!<", 1, false},
	{NEQ, "!=", 1, false},
	{NOT_GTR, "!>", 1, false},
	{LPAREN, "(", 1, false},
	{RPAREN, ")", 1, false},
	{COMMA, ",", 1, false},
	{LSS, "<", 1, false},
	{LEQ, "<=", 1, false},
	{NEQ, "<>", 1, false},	// Alias of !=
	{EQL, "=", 1, false},
	{GTR, ">", 1, false},
	{GEQ, ">=", 1, false},
#ifdef FB_NEW_INTL_ALLOW_NOT_READY
	{ACCENT, "ACCENT", 2, true},
#endif
	{ACTION, "ACTION", 1, true},
	{ACTIVE, "ACTIVE", 1, false},
	{ADD, "ADD", 1, false},
	{ADMIN, "ADMIN", 1, false},
	{AFTER, "AFTER", 1, false},
	{ALL, "ALL", 1, false},
	{ALTER, "ALTER", 1, false},
	{AND, "AND", 1, false},
	{ANY, "ANY", 1, false},
	{AS, "AS", 1, false},
	{ASC, "ASC", 1, false},	// Alias of ASCENDING
	{ASC, "ASCENDING", 1, false},
	{AT, "AT", 1, false},
	{AUTO, "AUTO", 1, false},
	{AVG, "AVG", 1, false},
	{BACKUP, "BACKUP", 2, true},
	{BEFORE, "BEFORE", 1, false},
	{BEGIN, "BEGIN", 1, false},
	{BETWEEN, "BETWEEN", 1, false},
	{BIGINT, "BIGINT", 2, false},
	{BIT_LENGTH, "BIT_LENGTH", 2, false},
	{BLOB, "BLOB", 1, false},
	{BLOCK, "BLOCK", 2, true},
	{BOTH, "BOTH", 2, false},
	{KW_BREAK, "BREAK", 2, true},
	{BY, "BY", 1, false},
	{CASCADE, "CASCADE", 1, true},
	{CASE, "CASE", 2, false},
	{CAST, "CAST", 1, false},
	{KW_CHAR, "CHAR", 1, false},
	{CHAR_LENGTH, "CHAR_LENGTH", 2, false},
	{CHARACTER, "CHARACTER", 1, false},
	{CHARACTER_LENGTH, "CHARACTER_LENGTH", 2, false},
	{CHECK, "CHECK", 1, false},
	{CLOSE, "CLOSE", 2, false},
	{COALESCE, "COALESCE", 2, true},
	{COLLATE, "COLLATE", 1, false},
	{COLLATION, "COLLATION", 2, true},
	{COLUMN, "COLUMN", 2, false},
	{COMMENT, "COMMENT", 2, true},
	{COMMIT, "COMMIT", 1, false},
	{COMMITTED, "COMMITTED", 1, false},
	{COMPUTED, "COMPUTED", 1, false},
	{CONDITIONAL, "CONDITIONAL", 1, false},
	{CONSTRAINT, "CONSTRAINT", 1, false},
	{CONTAINING, "CONTAINING", 1, false},
	{COUNT, "COUNT", 1, false},
	{CREATE, "CREATE", 1, false},
	{CROSS, "CROSS", 2, false},
	{CSTRING, "CSTRING", 1, false},
	{CURRENT, "CURRENT", 1, false},
	{CURRENT_CONNECTION, "CURRENT_CONNECTION", 2, false},
	{CURRENT_DATE, "CURRENT_DATE", 2, false},
	{CURRENT_ROLE, "CURRENT_ROLE", 2, false},
	{CURRENT_TIME, "CURRENT_TIME", 2, false},
	{CURRENT_TIMESTAMP, "CURRENT_TIMESTAMP", 2, false},
	{CURRENT_TRANSACTION, "CURRENT_TRANSACTION", 2, false},
	{CURRENT_USER, "CURRENT_USER", 2, false},
	{CURSOR, "CURSOR", 1, false},
	{DATABASE, "DATABASE", 1, false},
	{DATE, "DATE", 1, false},
	{DAY, "DAY", 2, false},
	{KW_DEBUG, "DEBUG", 1, false},
	{KW_DEC, "DEC", 1, false},
	{DECIMAL, "DECIMAL", 1, false},
	{DECLARE, "DECLARE", 1, false},
	{DEFAULT, "DEFAULT", 1, false},
	{KW_DELETE, "DELETE", 1, false},
	{DELETING, "DELETING", 2, true},
	{DESC, "DESC", 1, false},	// Alias of DESCENDING
	{DESC, "DESCENDING", 1, false},
	{KW_DESCRIPTOR,	"DESCRIPTOR", 2, true},
	{KW_DIFFERENCE, "DIFFERENCE", 2, true},
	{DISTINCT, "DISTINCT", 1, false},
	{DO, "DO", 1, false},
	{DOMAIN, "DOMAIN", 1, false},
	{KW_DOUBLE, "DOUBLE", 1, false},
	{DROP, "DROP", 1, false},
	{ELSE, "ELSE", 1, false},
	{END, "END", 1, false},
	{ENTRY_POINT, "ENTRY_POINT", 1, false},
	{ESCAPE, "ESCAPE", 1, false},
	{EXCEPTION, "EXCEPTION", 1, false},
	{EXECUTE, "EXECUTE", 1, false},
	{EXISTS, "EXISTS", 1, false},
	{EXIT, "EXIT", 1, false},
	{EXTERNAL, "EXTERNAL", 1, false},
	{EXTRACT, "EXTRACT", 2, false},
	{FETCH, "FETCH", 2, false},
	{KW_FILE, "FILE", 1, false},
	{FILTER, "FILTER", 1, false},
	{FIRST, "FIRST", 2, true},
	{KW_FLOAT, "FLOAT", 1, false},
	{FOR, "FOR", 1, false},
	{FOREIGN, "FOREIGN", 1, false},
	{FREE_IT, "FREE_IT", 1, true},
	{FROM, "FROM", 1, false},
	{FULL, "FULL", 1, false},
	{FUNCTION, "FUNCTION", 1, false},
	{GDSCODE, "GDSCODE", 1, false},
	{GENERATOR, "GENERATOR", 1, false},
	{GEN_ID, "GEN_ID", 1, false},
	{GRANT, "GRANT", 1, false},
	{GROUP, "GROUP", 1, false},
	{HAVING, "HAVING", 1, false},
	{HOUR, "HOUR", 2, false},
	{IF, "IF", 1, false},
	{KW_IGNORE, "IGNORE", 2, true},
	{IIF, "IIF", 2, true},
	{KW_IN, "IN", 1, false},
	{INACTIVE, "INACTIVE", 1, false},
	{INDEX, "INDEX", 1, false},
	{INNER, "INNER", 1, false},
	{INPUT_TYPE, "INPUT_TYPE", 1, false},
#ifdef FB_NEW_INTL_ALLOW_NOT_READY
	{INSENSITIVE, "INSENSITIVE", 2, false},
#endif
	{INSERT, "INSERT", 1, false},
	{INSERTING, "INSERTING", 2, true},
	{KW_INT, "INT", 1, false},
	{INTEGER, "INTEGER", 1, false},
	{INTO, "INTO", 1, false},
	{IS, "IS", 1, false},
	{ISOLATION, "ISOLATION", 1, false},
	{JOIN, "JOIN", 1, false},
	{KEY, "KEY", 1, false},
	{LAST, "LAST", 2, true},
	{LEADING, "LEADING", 2, false},
	{LEAVE, "LEAVE", 2, true},
	{LEFT, "LEFT", 1, false},
	{LENGTH, "LENGTH", 1, false},
	{LEVEL, "LEVEL", 1, false},
	{LIKE, "LIKE", 1, false},
	{LIMBO, "LIMBO", 2, true},
	{LOCK, "LOCK", 2, true},
	{KW_LONG, "LONG", 1, false},
	{KW_LOWER, "LOWER", 2, false},
	{MANUAL, "MANUAL", 1, false},
	{MAXIMUM, "MAX", 1, false},
	{MAX_SEGMENT, "MAXIMUM_SEGMENT", 1, false},
	{MERGE, "MERGE", 1, false},
	{MESSAGE, "MESSAGE", 1, false},
	{MINIMUM, "MIN", 1, false},
	{MINUTE, "MINUTE", 2, false},
	{MODULE_NAME, "MODULE_NAME", 1, false},
	{MONTH, "MONTH", 2, false},
	{NAMES, "NAMES", 1, false},
	{NATIONAL, "NATIONAL", 1, false},
	{NATURAL, "NATURAL", 1, false},
	{NCHAR, "NCHAR", 1, false},
	{NEXT, "NEXT", 2, true},
	{NO, "NO", 1, false},
	{NOT, "NOT", 1, false},
	{NULLIF, "NULLIF", 2, true},
	{KW_NULL, "NULL", 1, false},
	{NULLS, "NULLS", 2, true},
	{KW_NUMERIC, "NUMERIC", 1, false},
	{OCTET_LENGTH, "OCTET_LENGTH", 2, false},
	{OF, "OF", 1, false},
	{ON, "ON", 1, false},
	{ONLY, "ONLY", 1, false},
	{OPEN, "OPEN", 2, false},
	{OPTION, "OPTION", 1, false},
	{OR, "OR", 1, false},
	{ORDER, "ORDER", 1, false},
	{OUTER, "OUTER", 1, false},
	{OUTPUT_TYPE, "OUTPUT_TYPE", 1, false},
	{OVERFLOW, "OVERFLOW", 1, false},
#ifdef FB_NEW_INTL_ALLOW_NOT_READY
	{PAD, "PAD", 2, true},
#endif
	{PAGE, "PAGE", 1, false},
	{PAGES, "PAGES", 1, false},
	{KW_PAGE_SIZE, "PAGE_SIZE", 1, false},
	{PARAMETER, "PARAMETER", 1, false},
	{PASSWORD, "PASSWORD", 1, false},
	{PLAN, "PLAN", 1, false},
	{POSITION, "POSITION", 1, false},
	{POST_EVENT, "POST_EVENT", 1, false},
	{PRECISION, "PRECISION", 1, false},
	{PRIMARY, "PRIMARY", 1, false},
	{PRIVILEGES, "PRIVILEGES", 1, false},
	{PROCEDURE, "PROCEDURE", 1, false},
	{PROTECTED, "PROTECTED", 1, false},
	{DB_KEY, "RDB$DB_KEY", 1, false},
	{READ, "READ", 1, false},
	{REAL, "REAL", 1, false},
	{VERSION, "RECORD_VERSION", 1, false},
	{RECREATE, "RECREATE", 2, false},
	{REFERENCES, "REFERENCES", 1, false},
	{RELEASE, "RELEASE", 2, false},
	{REQUESTS, "REQUESTS", 2, true},
	{RESERVING, "RESERV", 1, false},	// Alias of RESERVING
	{RESERVING, "RESERVING", 1, false},
	{RESTART, "RESTART", 2, true},
	{RESTRICT, "RESTRICT", 1, true},
	{RETAIN, "RETAIN", 1, false},
	{RETURNING, "RETURNING", 2, true},
	{RETURNING_VALUES, "RETURNING_VALUES", 1, false},
	{RETURNS, "RETURNS", 1, false},
	{REVOKE, "REVOKE", 1, false},
	{RIGHT, "RIGHT", 1, false},
	{ROLE, "ROLE", 1, true},
	{ROLLBACK, "ROLLBACK", 1, false},
	{ROW_COUNT, "ROW_COUNT", 2, false},
	{ROWS, "ROWS", 2, false},
	{SAVEPOINT, "SAVEPOINT", 2, false},
	{SCALAR_ARRAY, "SCALAR_ARRAY", 2, true},
	{DATABASE, "SCHEMA", 1, false},	// Alias of DATABASE
	{SECOND, "SECOND", 2, false},
	{SEGMENT, "SEGMENT", 1, false},
	{SELECT, "SELECT", 1, false},
#ifdef FB_NEW_INTL_ALLOW_NOT_READY
	{SENSITIVE, "SENSITIVE", 2, false},
#endif
	{SEQUENCE, "SEQUENCE", 2, true},
	{SET, "SET", 1, false},
	{SHADOW, "SHADOW", 1, false},
	{KW_SHARED, "SHARED", 1, false},
	{SINGULAR, "SINGULAR", 1, false},
	{KW_SIZE, "SIZE", 1, false},
	{SKIP, "SKIP", 2, true},
	{SMALLINT, "SMALLINT", 1, false},
	{SNAPSHOT, "SNAPSHOT", 1, false},
	{SOME, "SOME", 1, false},
	{SORT, "SORT", 1, false},
#ifdef FB_NEW_INTL_ALLOW_NOT_READY
	{SPACE, "SPACE", 2, true},
#endif
	{SQLCODE, "SQLCODE", 1, false},
	{STABILITY, "STABILITY", 1, false},
	{STARTING, "STARTING", 1, false},
	{STARTING, "STARTS", 1, false},	// Alias of STARTING
	{STATEMENT, "STATEMENT", 2, true},
	{STATISTICS, "STATISTICS", 1, false},
	{SUBSTRING,	"SUBSTRING", 2, true},
	{SUB_TYPE, "SUB_TYPE", 1, false},
	{SUM, "SUM", 1, false},
	{SUSPEND, "SUSPEND", 1, false},
	{TABLE, "TABLE", 1, false},
	{THEN, "THEN", 1, false},
	{TIME, "TIME", 2, false},
	{TIMESTAMP, "TIMESTAMP", 2, false},
	{TIMEOUT, "TIMEOUT", 2, true},
	{TO, "TO", 1, false},
	{TRAILING, "TRAILING", 2, false},
	{TRANSACTION, "TRANSACTION", 1, false},
	{TRIGGER, "TRIGGER", 1, false},
	{TRIM, "TRIM", 2, false},
	{TYPE, "TYPE", 2, true},
	{UNCOMMITTED, "UNCOMMITTED", 1, false},
	{UNDO, "UNDO", 2, true},
	{UNION, "UNION", 1, false},
	{UNIQUE, "UNIQUE", 1, false},
	{UPDATE, "UPDATE", 1, false},
	{UPDATING, "UPDATING", 2, true},
	{KW_UPPER, "UPPER", 1, false},
	{USER, "USER", 1, false},
	{USING, "USING", 2, false},
	{KW_VALUE, "VALUE", 1, false},
	{VALUES, "VALUES", 1, false},
	{VARCHAR, "VARCHAR", 1, false},
	{VARIABLE, "VARIABLE", 1, false},
	{VARYING, "VARYING", 1, false},
	{VIEW, "VIEW", 1, false},
	{WAIT, "WAIT", 1, false},
	{WEEKDAY, "WEEKDAY", 2, true},
	{WHEN, "WHEN", 1, false},
	{WHERE, "WHERE", 1, false},
	{WHILE, "WHILE", 1, false},
	{WITH, "WITH", 1, false},
	{WORK, "WORK", 1, false},
	{WRITE, "WRITE", 1, false},
	{YEAR, "YEAR", 2, false},
	{YEARDAY, "YEARDAY", 2, true},
	{NOT_LSS, "^<", 1, false},	// Alias of !<
	{NEQ, "^=", 1, false},				// Alias of !=
	{NOT_GTR, "^>", 1, false},			// Alias of !>
	{CONCATENATE, "||", 1, false},
	{NOT_LSS, "~<", 1, false},	// Alias of !<
	{NEQ, "~=", 1, false},				// Alias of !=
	{NOT_GTR, "~>", 1, false},			// Alias of !>
	{0, 0, 0, false}
};

/* This method is currently used in isql/isql.epp to check if a
   user field is a reserved word, and hence needs to be quoted.
   Obviously a hash table would make this a little quicker.

   MOD 29-June-2002
*/

extern "C" {

int KEYWORD_stringIsAToken(const char* in_str)
{
    const TOK* tok_ptr = tokens;
    while (tok_ptr->tok_string) {
        if (!tok_ptr->specialHandling && !strcmp(tok_ptr->tok_string, in_str)) {
            return true;
        }
        ++tok_ptr;
    }
    return false;
}

const TOK* KEYWORD_getTokens()
{
    return tokens;
}

}
