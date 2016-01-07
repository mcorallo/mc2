use `bs-auth`;
start transaction;

/*admin pwd 4tWLQR4SaMH506pY*/
insert into sys_users (username,password,email,default_access) values ('admin', '5b0b188718d91aad5c963cef516a42d453269c26', '', 'FULL');
insert into sys_profiles (name,description) values ('ADMIN', 'System administrator');
insert into sys_user_profiles_rel values ((select id from sys_users where username='admin'), (select id from sys_profiles where name='ADMIN'));
insert into sys_groups values(-1,'ALL');
insert into sys_user_groups_rel values((select id from sys_users where username = 'admin'),-1);

insert into sys_profiles (name,description) values ('USER', 'Generic user');
insert into sys_users (username,password,email,default_access) values ('user-1', '5b0b188718d91aad5c963cef516a42d453269c26', '', 'FULL');
insert into sys_user_profiles_rel values ((select id from sys_users where username='user-1'), (select id from sys_profiles where name='USER'));

delete from sys_rules;

/* TYPES: AREA, FUNCTION, PAGE, COMMAND */
/* ACCESSES: NONE, READONLY, FULL */
--INSERT INTO sys_rules (name,type,r_key,access) VALUES ('deny.area.a','AREA','a','NONE');
--INSERT INTO sys_rules (name,type,r_key,access) VALUES ('readonly.area.b','AREA','b','READONLY');

INSERT INTO sys_rules (name,type,value,access) VALUES ('full.access.xxx','URL_MATCH','/xxx/**','FULL');
INSERT INTO sys_profile_rules_rel(profile_id,rule_id) values((select id from sys_profiles where name='USER'),(select id from sys_rules where name='full.access.xxx'));

--INSERT INTO `sys_configuration` (`name`, `value`, `description`) VALUES ('job.a.enabled', 'true', NULL);

commit;









