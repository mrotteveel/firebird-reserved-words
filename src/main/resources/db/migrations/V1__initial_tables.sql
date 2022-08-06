create table SQL_KEYWORD (
    WORD varchar(50) not null,
    SQL_VERSION smallint not null,
    RESERVED boolean not null,
    constraint PK_SQL_KEYWORD primary key (WORD, SQL_VERSION)
);
comment on column SQL_KEYWORD.RESERVED is 'true: reserved keyword, false: non-reserved keyword';
create index IDX_SQL_KEYWORD_VERSION_WORD on SQL_KEYWORD (SQL_VERSION, WORD);

create table FB_KEYWORD (
    WORD varchar(50) not null,
    FB_VERSION NUMERIC(2,1) not null,
    RESERVED boolean,
    constraint PK_FB_KEYWORD primary key (WORD, FB_VERSION)
);
comment on column FB_KEYWORD.RESERVED is 'true: reserved keyword, false: non-reserved keyword';
create index IDX_FB_KEYWORD_VERSION_WORD on FB_KEYWORD (FB_VERSION, WORD);
