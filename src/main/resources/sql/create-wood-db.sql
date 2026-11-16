-- nuke the database if it exists
-- create a new database called 'wood_db' with the proper charset for 8-bit ASCII
drop database if exists wood_db;
create database wood_db default charset=latin1;
