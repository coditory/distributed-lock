package com.coditory.sherlock;

import com.coditory.sherlock.connector.InitializationResult;
import com.coditory.sherlock.connector.ReleaseResult;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import static com.coditory.sherlock.Preconditions.expectNonEmpty;

/**
 * Manages distributed locks using Reactor API.
 */
public interface ReactorSherlock {
    /**
     * Initializes underlying infrastructure. If this method is not invoked explicitly then it can be
     * invoked implicitly when acquiring or releasing a lock for the first time.
     * <p>
     * Most often initialization is related with creating indexes and tables.
     *
     * @return {@link InitializationResult#SUCCESS} if initialization was successful, otherwise {@link
     * InitializationResult#FAILURE} is returned
     */
    @NotNull
    Mono<InitializationResult> initialize();

    /**
     * Creates a distributed lock. Created lock may be acquired only once by the same owner:
     *
     * <pre>{@code
     * assert lock.acquire() == true
     * assert lock.acquire() == false
     * }</pre>
     *
     * @return the lock builder
     */
    @NotNull
    DistributedLockBuilder<ReactorDistributedLock> createLock();

    /**
     * Creates a lock with default configuration.
     *
     * @param lockId lock identifier
     * @return the lock
     * @see ReactorSherlock#createLock()
     */
    @NotNull
    default ReactorDistributedLock createLock(@NotNull String lockId) {
        expectNonEmpty(lockId, "lockId");
        return createLock().withLockId(lockId).build();
    }

    /**
     * Creates a distributed reentrant lock. Reentrant lock may be acquired multiple times by the same
     * owner:
     *
     * <pre>{@code
     * assert reentrantLock.acquire() == true
     * assert reentrantLock.acquire() == true
     * }</pre>
     *
     * @return the reentrant lock builder
     */
    @NotNull
    DistributedLockBuilder<ReactorDistributedLock> createReentrantLock();

    /**
     * Creates a reentrant lock with default configuration.
     *
     * @param lockId lock identifier
     * @return the reentrant lock
     * @see ReactorSherlock#createReentrantLock()
     */
    @NotNull
    default ReactorDistributedLock createReentrantLock(@NotNull String lockId) {
        expectNonEmpty(lockId, "lockId");
        return createReentrantLock().withLockId(lockId).build();
    }

    /**
     * Create a distributed overriding lock. Returned lock may acquire or release any other lock
     * without checking its state:
     *
     * <pre>{@code
     * assert someLock.acquire() == true
     * assert overridingLock.acquire() == true
     * }</pre>
     * <p>
     * It could be used for administrative actions.
     *
     * @return the overriding lock builder
     */
    @NotNull
    DistributedLockBuilder<ReactorDistributedLock> createOverridingLock();

    /**
     * Creates an overriding lock with default configuration.
     *
     * @param lockId lock identifier
     * @return the overriding lock
     * @see ReactorSherlock#createOverridingLock()
     */
    @NotNull
    default ReactorDistributedLock createOverridingLock(@NotNull String lockId) {
        expectNonEmpty(lockId, "lockId");
        return createOverridingLock().withLockId(lockId).build();
    }

    /**
     * Force releases all acquired locks.
     * <p>
     * It could be used for administrative actions.
     *
     * @return {@link ReleaseResult#SUCCESS} if lock was released, otherwise {@link
     * ReleaseResult#FAILURE} is returned
     */
    @NotNull
    Mono<ReleaseResult> forceReleaseAllLocks();

    /**
     * Force releases a lock.
     * <p>
     * It could be used for administrative actions.
     *
     * @param lockId lock identifier
     * @return {@link ReleaseResult#SUCCESS} if lock was released, otherwise {@link
     * ReleaseResult#FAILURE} is returned
     */
    @NotNull
    default Mono<ReleaseResult> forceReleaseLock(@NotNull String lockId) {
        expectNonEmpty(lockId, "lockId");
        return createOverridingLock(lockId).release();
    }
}
