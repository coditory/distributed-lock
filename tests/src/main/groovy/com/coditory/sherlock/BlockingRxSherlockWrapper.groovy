package com.coditory.sherlock

import groovy.transform.CompileStatic
import org.jetbrains.annotations.NotNull

import java.time.Duration

// @CompileStatic - groovy compiler throws StackOverflow when uncommented
// it's probably related with implementing an interface with default methods
class BlockingRxSherlockWrapper implements Sherlock {
    static Sherlock blockingRxSherlock(RxSherlock locks) {
        return new BlockingRxSherlockWrapper(locks)
    }

    private final RxSherlock locks

    private BlockingRxSherlockWrapper(RxSherlock locks) {
        this.locks = locks
    }

    @Override
    void initialize() {
        locks.initialize()
                .blockingGet()
    }

    @Override
    @NotNull
    DistributedLockBuilder createLock() {
        return blockingLockBuilder(locks.createLock())
    }

    @Override
    @NotNull
    DistributedLockBuilder createReentrantLock() {
        return blockingLockBuilder(locks.createReentrantLock())
    }

    @Override
    @NotNull
    DistributedLockBuilder createOverridingLock() {
        return blockingLockBuilder(locks.createOverridingLock())
    }

    @Override
    @NotNull
    boolean forceReleaseAllLocks() {
        return locks.forceReleaseAllLocks()
                .blockingGet().released
    }

    @Override
    boolean forceReleaseLock(@NotNull String lockId) {
        return createOverridingLock(lockId)
                .release()
    }

    private DistributedLockBuilder blockingLockBuilder(DistributedLockBuilder<RxDistributedLock> reactiveBuilder) {
        return reactiveBuilder.withMappedLock({ lock -> new BlockingRxDistributedLock(lock) })
    }
}


@CompileStatic
class BlockingRxDistributedLock implements DistributedLock {
    private final RxDistributedLock lock

    BlockingRxDistributedLock(RxDistributedLock lock) {
        this.lock = lock
    }

    @Override
    @NotNull
    String getId() {
        return lock.id
    }

    @Override
    boolean acquire() {
        return lock.acquire()
                .blockingGet().acquired
    }

    @Override
    boolean acquire(@NotNull Duration duration) {
        return lock.acquire(duration)
                .blockingGet().acquired
    }

    @Override
    boolean acquireForever() {
        return lock.acquireForever()
                .blockingGet().acquired
    }

    @Override
    boolean release() {
        return lock.release()
                .blockingGet().released
    }
}
