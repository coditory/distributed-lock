package com.coditory.sherlock.rxjava.migrator

import com.coditory.sherlock.BlockingRxSherlockWrapper
import com.coditory.sherlock.Sherlock
import com.coditory.sherlock.inmem.rxjava.InMemorySherlock
import com.coditory.sherlock.migrator.MigratorChangeSetsSpec
import com.coditory.sherlock.migrator.MigratorSpec
import com.coditory.sherlock.migrator.base.BlockingMigratorBuilder
import com.coditory.sherlock.migrator.base.MigratorCreator

import java.time.Clock
import java.time.Duration

class RxMigratorSpec extends MigratorSpec implements UsesRxInMemorySherlock {}

class RxMigratorChangeSetSpec extends MigratorChangeSetsSpec implements UsesRxInMemorySherlock {}

trait UsesRxInMemorySherlock implements MigratorCreator {
    @Override
    Sherlock createSherlock(String ownerId, Duration duration, Clock clock, String collectionName) {
        com.coditory.sherlock.rxjava.Sherlock rxLocks = InMemorySherlock.builder()
            .withOwnerId(ownerId)
            .withLockDuration(duration)
            .withClock(clock)
            .build()
        return new BlockingRxSherlockWrapper(rxLocks)
    }

    @Override
    BlockingMigratorBuilder createMigratorBuilder(Sherlock sherlock) {
        if (sherlock instanceof BlockingRxSherlockWrapper) {
            return new BlockingRxMigratorBuilder(sherlock.unwrap())
        }
        throw new IllegalArgumentException("Expected ${BlockingRxSherlockWrapper.simpleName}, got: " + sherlock.class.simpleName)
    }
}
