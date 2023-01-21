package com.coditory.sherlock;

import com.coditory.sherlock.connector.AcquireResult;
import com.coditory.sherlock.connector.ReleaseResult;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.coditory.sherlock.Preconditions.expectNonEmpty;
import static com.coditory.sherlock.Preconditions.expectNonNull;
import static com.coditory.sherlock.UuidGenerator.uuid;

public final class ReactorDistributedLockMock implements ReactorDistributedLock {
    @NotNull
    public static ReactorDistributedLockMock releasedInMemoryLock() {
        return releasedInMemoryLock(uuid());
    }

    @NotNull
    public static ReactorDistributedLockMock acquiredInMemoryLock() {
        return acquiredInMemoryLock(uuid());
    }

    @NotNull
    public static ReactorDistributedLockMock releasedInMemoryLock(@NotNull String lockId) {
        expectNonEmpty(lockId, "lockId");
        return inMemoryLock(lockId, false);
    }

    @NotNull
    public static ReactorDistributedLockMock acquiredInMemoryLock(@NotNull String lockId) {
        expectNonEmpty(lockId, "lockId");
        return inMemoryLock(lockId, true);
    }

    private static ReactorDistributedLockMock inMemoryLock(@NotNull String lockId, boolean state) {
        expectNonEmpty(lockId, "lockId");
        return of(InMemoryDistributedLockStub.inMemoryLock(LockId.of(lockId), state));
    }

    @NotNull
    public static ReactorDistributedLockMock releasedReentrantInMemoryLock() {
        return releasedReentrantInMemoryLock(uuid());
    }

    @NotNull
    public static ReactorDistributedLockMock acquiredReentrantInMemoryLock() {
        return acquiredReentrantInMemoryLock(uuid());
    }

    @NotNull
    public static ReactorDistributedLockMock releasedReentrantInMemoryLock(@NotNull String lockId) {
        expectNonEmpty(lockId, "lockId");
        return reentrantInMemoryLock(lockId, false);
    }

    @NotNull
    public static ReactorDistributedLockMock acquiredReentrantInMemoryLock(@NotNull String lockId) {
        expectNonEmpty(lockId, "lockId");
        return reentrantInMemoryLock(lockId, true);
    }

    private static ReactorDistributedLockMock reentrantInMemoryLock(@NotNull String lockId, boolean state) {
        expectNonEmpty(lockId, "lockId");
        return of(InMemoryDistributedLockStub.reentrantInMemoryLock(LockId.of(lockId), state));
    }

    @NotNull
    public static ReactorDistributedLockMock lockStub(boolean result) {
        return lockStub(uuid(), result);
    }

    @NotNull
    public static ReactorDistributedLockMock lockStub(boolean acquireResult, boolean releaseResult) {
        return lockStub(uuid(), acquireResult, releaseResult);
    }

    @NotNull
    public static ReactorDistributedLockMock lockStub(@NotNull String lockId, boolean result) {
        expectNonEmpty(lockId, "lockId");
        return of(lockStub(lockId, result, result));
    }

    @NotNull
    public static ReactorDistributedLockMock lockStub(
            String lockId,
            boolean acquireResult,
            boolean releaseResult
    ) {
        expectNonEmpty(lockId, "lockId");
        SequencedDistributedLockStub lock = new SequencedDistributedLockStub(
                lockId, List.of(acquireResult), List.of(releaseResult));
        return of(lock);
    }

    @NotNull
    public static ReactorDistributedLockMock sequencedLock(
            @NotNull List<Boolean> acquireResults,
            @NotNull List<Boolean> releaseResults
    ) {
        expectNonNull(acquireResults, "acquireResults");
        expectNonNull(releaseResults, "releaseResults");
        return of(sequencedLock(uuid(), acquireResults, releaseResults));
    }

    @NotNull
    public static ReactorDistributedLockMock sequencedLock(
            @NotNull String lockId,
            @NotNull List<Boolean> acquireResults,
            @NotNull List<Boolean> releaseResults
    ) {
        expectNonEmpty(lockId, "lockId");
        expectNonNull(acquireResults, "acquireResults");
        expectNonNull(releaseResults, "releaseResults");
        return of(new SequencedDistributedLockStub(lockId, acquireResults, releaseResults));
    }

    private static ReactorDistributedLockMock of(@NotNull ReactorDistributedLock lock) {
        expectNonNull(lock, "lock");
        return new ReactorDistributedLockMock(lock);
    }

    private final ReactorDistributedLock lock;
    private final AtomicInteger releases = new AtomicInteger(0);
    private final AtomicInteger acquisitions = new AtomicInteger(0);
    private final AtomicInteger successfulReleases = new AtomicInteger(0);
    private final AtomicInteger successfulAcquisitions = new AtomicInteger(0);

    private ReactorDistributedLockMock(ReactorDistributedLock lock) {
        this.lock = lock;
    }

    @Override
    @NotNull
    public String getId() {
        return lock.getId();
    }

    @Override
    @NotNull
    public Mono<AcquireResult> acquire() {
        return lock.acquire()
                .map(this::incrementAcquireCounters);
    }

    @Override
    @NotNull
    public Mono<AcquireResult> acquire(@NotNull Duration duration) {
        expectNonNull(duration, "duration");
        return acquire();
    }

    @Override
    @NotNull
    public Mono<AcquireResult> acquireForever() {
        return acquire();
    }

    private AcquireResult incrementAcquireCounters(AcquireResult result) {
        expectNonNull(result, "result");
        acquisitions.incrementAndGet();
        if (result.isAcquired()) {
            successfulAcquisitions.incrementAndGet();
        }
        return result;
    }

    @Override
    @NotNull
    public Mono<ReleaseResult> release() {
        return lock.release()
                .map(this::incrementReleaseCounters);
    }

    private ReleaseResult incrementReleaseCounters(ReleaseResult result) {
        expectNonNull(result, "result");
        releases.incrementAndGet();
        if (result.isReleased()) {
            successfulReleases.incrementAndGet();
        }
        return result;
    }

    /**
     * @return the count of successful releases
     */
    public int successfulReleases() {
        return successfulReleases.get();
    }

    /**
     * @return the count of successful acquisitions
     */
    public int successfulAcquisitions() {
        return successfulAcquisitions.get();
    }

    /**
     * @return the count of all releases (successful and unsuccessful)
     */
    public int releases() {
        return releases.get();
    }

    /**
     * @return the count of all acquisitions (successful and unsuccessful)
     */
    public int acquisitions() {
        return acquisitions.get();
    }

    /**
     * @return the count of rejected releases
     */
    public int rejectedReleases() {
        return releases() - successfulReleases();
    }

    /**
     * @return the count of rejected acquisitions
     */
    public int rejectedAcquisitions() {
        return acquisitions() - successfulAcquisitions();
    }

    /**
     * @return true if lock was successfully acquired at least once
     */
    public boolean wasAcquired() {
        return successfulAcquisitions() > 0;
    }

    /**
     * @return true if lock was successfully released at least once
     */
    public boolean wasReleased() {
        return successfulReleases() > 0;
    }

    /**
     * @return true if lock was successfully acquired and released
     */
    public boolean wasAcquiredAndReleased() {
        return wasAcquired() && wasReleased();
    }

    /**
     * @return true if lock was acquired without success at least once
     */
    public boolean wasAcquireRejected() {
        return successfulAcquisitions() < acquisitions();
    }

    /**
     * @return true if lock was released without success at least once
     */
    public boolean wasReleaseRejected() {
        return successfulReleases() < releases();
    }

    /**
     * @return true if acquire operation was invoked at least once
     */
    public boolean wasAcquireInvoked() {
        return acquisitions() > 0;
    }

    /**
     * @return true if release operation was invoked at least once
     */
    public boolean wasReleaseInvoked() {
        return releases() > 0;
    }

    private static class InMemoryDistributedLockStub implements ReactorDistributedLock {
        private final LockId lockId;
        private final boolean reentrant;
        private AtomicBoolean acquired;

        static InMemoryDistributedLockStub reentrantInMemoryLock(LockId lockId, boolean acquired) {
            return new InMemoryDistributedLockStub(lockId, true, acquired);
        }

        static InMemoryDistributedLockStub inMemoryLock(LockId lockId, boolean acquired) {
            return new InMemoryDistributedLockStub(lockId, false, acquired);
        }

        private InMemoryDistributedLockStub(LockId lockId, boolean reentrant, boolean acquired) {
            this.lockId = expectNonNull(lockId, "lockId");
            this.reentrant = reentrant;
            this.acquired = new AtomicBoolean(acquired);
        }

        @Override
        @NotNull
        public String getId() {
            return lockId.getValue();
        }

        @Override
        @NotNull
        public Mono<AcquireResult> acquire() {
            return Mono.fromCallable(this::acquireSync)
                    .map(AcquireResult::of);
        }

        private boolean acquireSync() {
            if (reentrant) {
                acquired.set(true);
                return true;
            } else {
                return acquired.compareAndSet(false, true);
            }
        }

        @Override
        @NotNull
        public Mono<AcquireResult> acquire(@NotNull Duration duration) {
            expectNonNull(duration, "duration");
            return acquire();
        }

        @Override
        @NotNull
        public Mono<AcquireResult> acquireForever() {
            return acquire();
        }

        @Override
        @NotNull
        public Mono<ReleaseResult> release() {
            return Mono.fromCallable(this::releaseSync)
                    .map(ReleaseResult::of);
        }

        private boolean releaseSync() {
            return acquired.compareAndSet(true, false);
        }
    }

    static class SequencedDistributedLockStub implements ReactorDistributedLock {
        private final LockId lockId;
        private final ConcurrentLinkedQueue<Boolean> acquireResults;
        private final ConcurrentLinkedQueue<Boolean> releaseResults;
        private final boolean defaultAcquireResult;
        private final boolean defaultReleaseResult;

        private SequencedDistributedLockStub(
                String lockId,
                List<Boolean> acquireResults,
                List<Boolean> releaseResults
        ) {
            this(LockId.of(lockId), acquireResults, releaseResults);
        }

        private SequencedDistributedLockStub(
                LockId lockId,
                List<Boolean> acquireResults,
                List<Boolean> releaseResults
        ) {
            expectNonNull(lockId, "lockId");
            expectNonEmpty(acquireResults, "acquireResults");
            expectNonEmpty(releaseResults, "releaseResults");
            this.lockId = expectNonNull(lockId, "lockId");
            this.acquireResults = new ConcurrentLinkedQueue<>(acquireResults);
            this.releaseResults = new ConcurrentLinkedQueue<>(releaseResults);
            this.defaultAcquireResult = acquireResults.get(acquireResults.size() - 1);
            this.defaultReleaseResult = releaseResults.get(releaseResults.size() - 1);
        }

        @Override
        @NotNull
        public String getId() {
            return lockId.getValue();
        }

        @Override
        @NotNull
        public Mono<AcquireResult> acquire() {
            return pollOrDefault(acquireResults, defaultAcquireResult)
                    .map(AcquireResult::of);
        }

        @Override
        @NotNull
        public Mono<AcquireResult> acquire(@NotNull Duration duration) {
            expectNonNull(duration, "duration");
            return acquire();
        }

        @Override
        @NotNull
        public Mono<AcquireResult> acquireForever() {
            return acquire();
        }

        @Override
        @NotNull
        public Mono<ReleaseResult> release() {
            return pollOrDefault(releaseResults, defaultReleaseResult)
                    .map(ReleaseResult::of);
        }

        private Mono<Boolean> pollOrDefault(
                ConcurrentLinkedQueue<Boolean> queue, boolean defaultValue) {
            return Mono.fromCallable(() -> {
                Boolean value = queue.poll();
                return value != null ? value : defaultValue;
            });
        }
    }
}
