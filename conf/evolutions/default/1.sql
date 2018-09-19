# --- First database schema

# --- !Ups

create table challenge (
  id                        bigint not null auto_increment,
  owner_id                  bigint not null,
  active_tournament_id      bigint null,
  name                      varchar(255) not null,
  repo_url                  varchar(255) not null,
  ref_id                    varchar(255) not null,
  status                    varchar(255) not null,
  language                  varchar(255) not null,
  build_parameters          text,
  created_at                timestamp not null,
  constraint pk_challenge primary key (id))
;

create index on challenge (created_at);
create index on challenge (ref_id);


create table entry (
  id                        bigint not null auto_increment,
  owner_id                  bigint,
  challenge_id              bigint not null,
  latest_version_id         bigint,
  current_name              varchar(255),
  ref_id                    varchar(255),
  created_at                timestamp not null,
  constraint pk_entry primary key (id))
;

create index on entry (created_at);
create index on entry (ref_id);

create table entry_version (
  id                        bigint not null auto_increment,
  entry_id                  bigint not null,
  name                      varchar(255),
  code                      text,
  language                  varchar(255),
  valid                     boolean not null,
  created_at                timestamp not null,
  constraint pk_entry_version primary key (id))
;

create index on entry_version (created_at);
create index on entry_version (valid);

create table game (
  id                        bigint not null auto_increment,
  tournament_id             bigint not null,
  start_time                timestamp not null,
  end_time                  timestamp,
  created_at                timestamp not null,
  constraint pk_game primary key (id))
;

create index on game (created_at);
create index on game (start_time);
create index on game (end_time);

create table score (
  id                        bigint not null auto_increment,
  game_id                   bigint not null,
  tournament_entry_id       bigint not null,
  score                     double not null,
  created_at                timestamp not null,
  constraint pk_score primary key (id))
;

create index on score (created_at);
create index on score (tournament_entry_id);
create index on score (score);

create table tournament (
  id                        bigint not null auto_increment,
  challenge_id              bigint not null,
  primary_group_id          bigint null,
  version                   varchar(255) not null unique,
  git_hash                  varchar(255) null,
  configuration             bigint not null,
  iteration_goal            bigint not null,
  created_at                timestamp not null,
  constraint pk_tournament primary key (id))
;

create index on tournament (version);
create index on tournament (created_at);


create table groups (
  id                        bigint not null auto_increment,
  tournament_id             bigint not null,
  name                      varchar(255) null,
  size                      bigint not null,
  matchmaker                varchar(255) not null,
  matchmaker_parameters     text null,
  scorer                    varchar(255) not null,
  scorer_parameters         text null,
  rank_descending           bool not null,
  created_at                timestamp not null,
  constraint pk_group primary key (id))
;

create index on groups (created_at);


create table tournament_entry (
  id                        bigint not null auto_increment,
  version_id                bigint not null,
  tournament_id             bigint not null,
  group_id                  bigint not null,
  rank                      bigint,
  created_at                timestamp not null,
  constraint pk_tournament_entry primary key (id))
;

create index on tournament_entry (rank);
create index on tournament_entry (created_at);


create table users (
  id                        bigint not null auto_increment,
  username                  varchar(255) not null unique,
  name                      varchar(255) not null,
  role                      varchar(255) not null,
  authentication            text,
  created_at                timestamp not null,
  constraint pk_user primary key (id))
;

create index on users (username);
create index on users (created_at);


alter table challenge add foreign key (owner_id) references users (id) on delete set null on update cascade;
alter table challenge add foreign key (active_tournament_id) references tournament (id) on delete set null on update cascade;

alter table entry add foreign key (owner_id) references users (id) on delete set null on update cascade;
alter table entry add foreign key (challenge_id) references challenge (id) on delete cascade on update cascade;
alter table entry add foreign key (latest_version_id) references entry_version (id) on delete cascade on update cascade;

alter table entry_version add foreign key (entry_id) references entry (id) on delete cascade on update cascade;

alter table game add foreign key (tournament_id) references tournament (id) on delete cascade on update cascade;

alter table score add foreign key (game_id) references game (id) on delete cascade on update cascade;
alter table score add foreign key (tournament_entry_id) references tournament_entry (id) on delete cascade on update cascade;

alter table tournament add foreign key (challenge_id) references challenge (id) on delete cascade on update cascade;
alter table tournament add foreign key (primary_group_id) references groups (id) on delete set null on update cascade;

alter table groups add foreign key (tournament_id) references tournament (id) on delete cascade on update cascade;

alter table tournament_entry add foreign key (version_id) references entry_version (id) on delete cascade on update cascade;
alter table tournament_entry add foreign key (tournament_id) references tournament (id) on delete cascade on update cascade;
alter table tournament_entry add foreign key (group_id) references groups (id) on delete cascade on update cascade;

insert into users (username, name, role, authentication, created_at)
VALUES('20198', 'Nathan Merrill', 'Admin', '', CURRENT_TIMESTAMP())

# --- !Downs

drop table if exists score;
drop table if exists tournament_entry;
drop table if exists entry_version;
drop table if exists entry;
drop table if exists game;
drop table if exists groups;
drop table if exists tournament;
drop table if exists challenge;
drop table if exists users;

