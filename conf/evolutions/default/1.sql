# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table message (
  id                        bigint not null,
  uuid                      integer,
  message                   varchar(255),
  sender_username           varchar(255),
  sent                      timestamp,
  constraint pk_message primary key (id))
;

create table user (
  username                  varchar(255) not null,
  password                  varchar(255),
  constraint pk_user primary key (username))
;

create sequence message_seq;

create sequence user_seq;

alter table message add constraint fk_message_sender_1 foreign key (sender_username) references user (username) on delete restrict on update restrict;
create index ix_message_sender_1 on message (sender_username);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists message;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists message_seq;

drop sequence if exists user_seq;

