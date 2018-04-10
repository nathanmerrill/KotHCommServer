# --- First database schema

# --- !Ups

create table challenge (
  id                        bigint not null auto_increment,
  owner_id                  bigint not null,
  name                      varchar(255),
  source                    varchar(255),
  ref_id                    varchar(255),
  created_at                timestamp not null,
  constraint pk_challenge primary key (id))
;

create index on challenge (created_at);
create index on challenge (ref_id);


create table entry (
  id                        bigint not null auto_increment,
  owner_id                  bigint,
  challenge_id              bigint not null,
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
  created_at                timestamp not null,
  constraint pk_entry_version primary key (id))
;

create index on entry_version (created_at);

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
  version                   varchar(255) not null unique,
  git_hash                  varchar(255) not null,
  matchmaker                varchar(255) not null,
  scorer                    varchar(255) not null,
  scoring_parameters        text not null,
  created_at                timestamp not null,
  constraint pk_tournament primary key (id))
;

create index on tournament (version);
create index on tournament (created_at);


create table tournament_entry (
  id                        bigint not null auto_increment,
  version_id          bigint not null,
  tournament_id             bigint not null,
  rank                      bigint not null,
  created_at                timestamp not null,
  constraint pk_tournament_entry primary key (id))
;

create index on tournament_entry (rank);
create index on tournament_entry (created_at);


create table `user` (
  id                        bigint not null auto_increment,
  username                  varchar(255) not null unique,
  name                      varchar(255) not null,
  role                      varchar(255) not null,
  authentication            text not null,
  created_at                timestamp not null,
  constraint pk_user primary key (id))
;

create index on `user` (username);
create index on `user` (created_at);


alter table challenge add foreign key (owner_id) references `user` (id) on delete set null on update cascade;

alter table entry add foreign key (owner_id) references `user` (id) on delete set null on update cascade;
alter table entry add foreign key (challenge_id) references challenge (id) on delete cascade on update cascade;

alter table entry_version add foreign key (entry_id) references entry (id) on delete cascade on update cascade;

alter table game add foreign key (tournament_id) references tournament (id) on delete cascade on update cascade;

alter table score add foreign key (game_id) references game (id) on delete cascade on update cascade;
alter table score add foreign key (tournament_entry_id) references tournament_entry (id) on delete cascade on update cascade;

alter table tournament add foreign key (challenge_id) references challenge (id) on delete cascade on update cascade;

alter table tournament_entry add foreign key (version_id) references entry_version (id) on delete cascade on update cascade;
alter table tournament_entry add foreign key (tournament_id) references tournament (id) on delete cascade on update cascade;


# --- !Downs

drop table if exists score;
drop table if exists tournament_entry;
drop table if exists entry_version;
drop table if exists entry;
drop table if exists game;
drop table if exists tournament;
drop table if exists challenge;
drop table if exists `user`;

