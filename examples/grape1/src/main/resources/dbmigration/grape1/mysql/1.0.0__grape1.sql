-- apply changes
create table clazz_domain (
  id                            varchar(255) not null,
  name                          varchar(255),
  remark                        varchar(255),
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_clazz_domain primary key (id)
);

create table student_domain (
  id                            varchar(255) not null,
  name                          varchar(255),
  remark                        varchar(255),
  age                           integer not null,
  friend                        varchar(255),
  class_id_id                   varchar(255),
  when_created                  datetime(6) not null,
  when_updated                  datetime(6) not null,
  constraint pk_student_domain primary key (id)
);

create index ix_student_domain_class_id_id on student_domain (class_id_id);
alter table student_domain add constraint fk_student_domain_class_id_id foreign key (class_id_id) references clazz_domain (id) on delete restrict on update restrict;

