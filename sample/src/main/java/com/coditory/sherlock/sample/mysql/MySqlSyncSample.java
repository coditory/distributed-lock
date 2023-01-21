package com.coditory.sherlock.sample.mysql;

import com.coditory.sherlock.DistributedLock;
import com.coditory.sherlock.Sherlock;
import com.coditory.sherlock.SherlockMigrator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Duration;

import static com.coditory.sherlock.SqlSherlockBuilder.sqlSherlock;

public class MySqlSyncSample {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Sherlock sherlock = sqlSherlock()
            .withClock(Clock.systemDefaultZone())
            .withLockDuration(Duration.ofMinutes(5))
            .withUniqueOwnerId()
            .withConnectionPool(connectionPool())
            .withLocksTable("LOCKS")
            .build();


    private static DataSource connectionPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test");
        config.setUsername("mysql");
        config.setPassword("mysql");
        return new HikariDataSource(config);
    }

    void sampleMySqlLockUsage() throws Exception {
        logger.info(">>> SAMPLE: Lock usage");
        DistributedLock lock = sherlock.createLock("sample-lock");
        lock.acquireAndExecute(() -> logger.info("Lock acquired!"));
    }

    private void sampleMySqlMigration() {
        // first commit - all migrations are executed
        new SherlockMigrator("db-migration", sherlock)
                .addChangeSet("change set 1", () -> logger.info(">>> Change set 1"))
                .addChangeSet("change set 2", () -> logger.info(">>> Change set 2"))
                .migrate();
        // second commit - only new change set is executed
        new SherlockMigrator("db-migration", sherlock)
                .addChangeSet("change set 1", () -> logger.info(">>> Change set 1"))
                .addChangeSet("change set 2", () -> logger.info(">>> Change set 2"))
                .addChangeSet("change set 3", () -> logger.info(">>> Change set 3"))
                .migrate();
    }

    void runSamples() throws Exception {
        sampleMySqlLockUsage();
        sampleMySqlMigration();
    }

    public static void main(String[] args) throws Exception {
        new MySqlSyncSample().runSamples();
    }
}
