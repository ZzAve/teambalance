CREATE USER teambalance WITH PASSWORD 'teambalance';

CREATE DATABASE teambalance WITH OWNER teambalance;

SELECT pg_reload_conf();
