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