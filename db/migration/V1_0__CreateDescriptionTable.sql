--
-- Create the Description table
--
-- Table holds Vulnerability Description and the
-- Name Entity Recognition value of the description
--
-- Each entry is uniquely identified by it's Hash

CREATE TABLE IF NOT EXISTS description
(
    id int auto_increment
        primary key,
    hash int not null ,
    text varchar(4096) charset utf8 not null,
    ner varchar(16384) charset utf8 null,
    constraint UK_7fgqk7iqoy01saceqb5l1cpue
        unique (hash)
);

create index description_hash_index
    on description (hash);

