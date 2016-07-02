CREATE KEYSPACE IF NOT EXISTS market_data WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': '2'
};

USE prices;

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

