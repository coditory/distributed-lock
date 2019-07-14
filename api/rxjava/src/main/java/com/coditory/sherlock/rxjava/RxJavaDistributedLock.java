package com.coditory.sherlock.rxjava;

import com.coditory.sherlock.reactive.connector.LockResult;
import com.coditory.sherlock.reactive.connector.ReleaseResult;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.time.Duration;
import java.util.function.Supplier;

import static com.coditory.sherlock.rxjava.RxJavaDistributedLockExecutor.executeOnAcquired;
import static com.coditory.sherlock.rxjava.RxJavaDistributedLockExecutor.executeOnReleased;

/**
 * A lock for distributed environment consisting of multiple application instances. Acquire a
 * distributed lock when only one application instance should execute a specific action.
 *
 * @see RxJavaSherlock
 */
public interface RxJavaDistributedLock {
  /**
   * Return the lock id.
   *
   * @return the lock id
   */
  String getId();

  /**
   * Try to acquire the lock. Lock is acquired for a configured duration.
   *
   * @return {@link LockResult#SUCCESS}, if lock is acquired
   */
  Single<LockResult> acquire();

  /**
   * Try to acquire the lock for a given duration.
   *
   * @param duration how much time must pass for the acquired lock to expire
   * @return {@link LockResult#SUCCESS}, if lock is acquired
   */
  Single<LockResult> acquire(Duration duration);

  /**
   * Try to acquire the lock without expiring date. It is potentially dangerous. Lookout for a
   * situation where the lock owning instance goes down with out releasing the lock.
   *
   * @return {@link LockResult#SUCCESS}, if lock is acquired
   */
  Single<LockResult> acquireForever();

  /**
   * Release the lock
   *
   * @return {@link ReleaseResult#SUCCESS}, if lock was released in this call. If lock could not be
   *     released or was released by a different instance then {@link ReleaseResult#FAILURE} is
   *     returned.
   */
  Single<ReleaseResult> release();

  /**
   * Acquire a lock and release it after action is executed.
   *
   * @param <T> type od value emitted by the action
   * @param action to be executed when lock is acquired
   * @return true if lock is acquired.
   * @see RxJavaDistributedLock#acquire()
   */
  default <T> Maybe<T> acquireAndExecute(Supplier<Single<T>> action) {
    return executeOnAcquired(acquire(), action, this::release);
  }

  /**
   * Acquire a lock for a given duration and release it after action is executed.
   *
   * @param <T> type od value emitted by the action
   * @param duration how much time must pass for the acquired lock to expire
   * @param action to be executed when lock is acquired
   * @return true, if lock is acquired
   * @see RxJavaDistributedLock#acquire(Duration)
   */
  default <T> Maybe<T> acquireAndExecute(Duration duration, Supplier<Single<T>> action) {
    return executeOnAcquired(acquire(duration), action, this::release);
  }

  /**
   * Acquire a lock without expiration time and release it after action is executed.
   *
   * @param <T> type od value emitted by the action
   * @param action to be executed when lock is acquired
   * @return true, if lock is acquired
   * @see RxJavaDistributedLock#acquireForever()
   */
  default <T> Maybe<T> acquireForeverAndExecute(Supplier<Single<T>> action) {
    return executeOnAcquired(acquireForever(), action, this::release);
  }

  /**
   * Run the action when lock is released
   *
   * @param <T> type od value emitted by the action
   * @param action to be executed when lock is released
   * @return true, if lock was release
   * @see RxJavaDistributedLock#release()
   */
  default <T> Maybe<T> releaseAndExecute(Supplier<Single<T>> action) {
    return executeOnReleased(release(), action);
  }
}
