# Changing from relational to timeseries database

## Motivation: 
Relational databases like MySQL or PostgreSQL are good options for general purpose data storage and retrieval. They provide flexible schema design, strong ACID compliance for transactions, and powerful SQL querying via mature ecosystems of tools and support.

However, for workloads involving high volumes of timestamped data, like in the Permasense project, relational databases are not the best choice. Timeseries databases are optimized for storing and querying timeseries data, and are ideally suited to this purpose.

## Choice of timeseries database:

In a first round of evaluation, we looked at various timeseries databases.

- QuestDB: is a timeseries database that is optimized for timeseries data. It is written in Java and uses a columnar storage format. It is designed to be used as a timeseries database, but it also supports a wide range of use cases. https://questdb.io/. The problem with questdb is that it is not open source. There exists an open source free to use version, but this version is very limited in terms of usability and functionality. 

- OpenTSDB: is a distributed Time Series Database built on top of Apache HBase. It is used to collect, store and display metrics of various computer systems, and generate readable data graphs easily. The developer claims that OpenTSDB is the first open-source monitoring system built on an open-source distributed database. http://opentsdb.net/. The problem with opentsdb is that it is a a NoSQL database. Therefore to much development on the underlying gsn2.0 code base would be needed in order to change to this type of database. 

- TimescaleDB: is an open-source time series database invented to make SQL scalable for time-series data. TimescaleDB is packaged as a PostgreSQL extension. As a time-series database, it provides automatic partitioning across date and key values. https://github.com/timescale/timescaledb. 
Since it is a PostgreSQL extension, it can be easily integrated and the PostgresStorageManager, with some slight modifications, could be used for the data management of Permasense. 
Useful Links: 
- Blog Post, TimescaleDB vs Postgres: https://www.timescale.com/blog/timescaledb-vs-6a696248104e/

- Installation: https://docs.timescale.com/self-hosted/latest/install/installation-linux/

- Presentation from Mike Freedman: https://www.youtube.com/watch?v=Fc5NhhjTy_U&list=PLdfRcubzeD1Oq1LrE0G3Mi2pMsOVRfSea&ab_channel=Percona

- Doc HyperTables: https://docs.timescale.com/getting-started/latest/tables-hypertables/

