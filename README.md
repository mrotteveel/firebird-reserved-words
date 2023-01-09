Firebird reserved words
=======================

Tool to collect reserved and non-reserved keyword information for Firebird 
and SQL standard into a Firebird database.

Usage
-----

Example:

Options to load keywords based on data in `keywords` folder:

For SQL:2003

```
--version 2003
--reserved keywords\sql\2003\reserved.txt
--non-reserved keywords\sql\2003\non-reserved.txt
```

For Firebird 5:

```
--version 5.0 
--keywords-source-file keywords\firebird\5.0\keywords.cpp 
--override-non-reserved keywords\firebird\5.0\non-reserved-override.txt 
--override-reserved keywords\firebird\5.0\reserved-override.txt 
--delete-keywords keywords\firebird\fb_general_keywords_to_remove.txt 
--delete-keywords keywords\firebird\5.0\delete-keywords.txt 
```

For Firebird 4:

```
--version 4.0 
--keywords-source-file keywords\firebird\4.0\keywords.cpp 
--override-non-reserved keywords\firebird\4.0\non-reserved-override.txt 
--override-reserved keywords\firebird\4.0\reserved-override.txt 
--delete-keywords keywords\firebird\fb_general_keywords_to_remove.txt 
--delete-keywords keywords\firebird\4.0\delete-keywords.txt 
```

For Firebird 3:

```
--version 3.0 
--keywords-source-file keywords\firebird\3.0\keywords.cpp 
--override-non-reserved keywords\firebird\3.0\non-reserved-override.txt 
--override-reserved keywords\firebird\3.0\reserved-override.txt 
--delete-keywords keywords\firebird\fb_general_keywords_to_remove.txt 
--delete-keywords keywords\firebird\3.0\delete-keywords.txt 
```

For Firebird 2.5:

```
--version 2.5 
--keywords-source-file keywords\firebird\2.5\keywords.cpp 
--override-non-reserved keywords\firebird\2.5\non-reserved-override.txt 
--override-reserved keywords\firebird\2.5\reserved-override.txt 
--delete-keywords keywords\firebird\fb_general_keywords_to_remove.txt 
--delete-keywords keywords\firebird\2.5\delete-keywords.txt 
```

For Firebird 2.1:

```
--version 2.1 
--keywords-source-file keywords\firebird\2.1\keywords.cpp 
--override-non-reserved keywords\firebird\2.1\non-reserved-override.txt 
--override-reserved keywords\firebird\2.1\reserved-override.txt 
--delete-keywords keywords\firebird\fb_general_keywords_to_remove.txt 
--delete-keywords keywords\firebird\2.1\delete-keywords.txt 
```

For Firebird 2.0:

```
--version 2.0 
--keywords-source-file keywords\firebird\2.0\keywords.cpp 
--override-non-reserved keywords\firebird\2.0\non-reserved-override.txt 
--override-reserved keywords\firebird\2.0\reserved-override.txt 
--delete-keywords keywords\firebird\fb_general_keywords_to_remove.txt 
--delete-keywords keywords\firebird\2.0\delete-keywords.txt 
```

Testing
=======

Verifying keyword reserved/non-reserved status

```sql
execute block
  returns (WORD varchar(50), FB_VERSION NUMERIC(2,1), RESERVED_T CHAR(1), REASON varchar(100))
as
declare RESERVED boolean;
declare DUMMY integer;
declare VERSION_TO_CHECK NUMERIC(2,1) = 2.0;
declare REMOTE_DB VARCHAR(500) = 'localhost/30520:somefirebird20.fdb';
begin
  for select WORD, FB_VERSION, RESERVED 
    from FB_KEYWORD 
    where FB_VERSION = :VERSION_TO_CHECK
    order by WORD into :WORD, :FB_VERSION, :RESERVED
  do
  begin
    reason = '';
    RESERVED_T = case when RESERVED then 'T' else 'F' end;
    begin
      execute statement 'select 1 as ' || WORD || ' from rdb$database' 
        as user 'sysdba' password 'masterkey'
        on external REMOTE_DB
        into :dummy;
      when any do
      begin
        if (not reserved) then
        begin
          REASON = 'Non-reserved word ' || WORD || ' yielded an error';
          suspend;
        end
        else continue;
      end
    end
    if (reserved) then
    begin
      REASON = 'Reserved word ' || WORD || ' did not yield error';
      suspend;
    end
  end
  REASON = null;
end
```

Producing list of non-standard reserved words
---------------------------------------------

```sql
select list(WORD)
from (
  select fb.WORD
  from FB_KEYWORD fb
  left join SQL_KEYWORD sql
    ON fb.WORD = sql.WORD and sql.SQL_VERSION = 2003
  where fb.FB_VERSION = 4.0
  and fb.RESERVED
  and (sql.WORD is null or not sql.RESERVED)
  order by fb.WORD
) reserved
```
