CREATE KEYSPACE IF NOT EXISTS market_data WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': '2'
};

CREATE KEYSPACE IF NOT EXISTS oanda_ticks WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': '2'
};

CREATE KEYSPACE IF NOT EXISTS hist_ticks WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': '2'
};

USE janusz_forex_rt_demo;

CREATE TABLE IF NOT EXISTS ticks (
   pair_day text, 
   issued_at timestamp, 
   bid float, 
   ask float, 
   PRIMARY KEY (pair_day, issued_at) 
) WITH compaction = {'class' : 'LeveledCompactionStrategy' };

CREATE TABLE IF NOT EXISTS ticks_avg_s (
   pair_day text, 
   issued_at timestamp, 
   bid float, 
   ask float, 
   PRIMARY KEY (pair_day, issued_at) 
) WITH compaction = {'class' : 'LeveledCompactionStrategy' };


USE oanda_ticks;

CREATE TABLE IF NOT EXISTS eurusd_ticks (
   day   date,
   nanos time, 
   bid   float, 
   ask   float, 
   PRIMARY KEY (day, nanos)
) WITH compaction = {'class' : 'LeveledCompactionStrategy' };

CREATE TABLE IF NOT EXISTS eurusd_ticks_tps (
   day  date,
   hour int,
   min  int,
   sec  int,
   tps  int,
   PRIMARY KEY (day, hour, min, sec)
) WITH compaction = {'class' : 'LeveledCompactionStrategy' };


USE hist_ticks;

CREATE TABLE IF NOT EXISTS eurusd_ticks (
   day   date,
   nanos time, 
   bid   float, 
   ask   float, 
   PRIMARY KEY (day, nanos)
) WITH compaction = {'class' : 'LeveledCompactionStrategy' };

CREATE TABLE IF NOT EXISTS eurusd_ticks_tps (
   day  date,
   hour int,
   min  int,
   sec  int,
   tps  int,
   PRIMARY KEY (day, hour, min, sec)
) WITH compaction = {'class' : 'LeveledCompactionStrategy' };
