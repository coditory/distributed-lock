package com.coditory.sherlock.reactor;

import com.coditory.sherlock.reactive.ReactiveSherlock;
import com.coditory.sherlock.reactive.connector.InitializationResult;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Creates and manages reactive distributed locks with reactor api.
 */
public interface ReactorSherlock {
  /**
   * Maps reactive sherlock to a one using Reactor's {@link reactor.core.publisher.Mono} and {@link
   * reactor.core.publisher.Flux}
   *
   * @param locks reactive locks to be wrapped in Reactor api
   * @return reactor version of sherlock locks
   */
  static ReactorSherlock wrapReactiveSherlock(ReactiveSherlock locks) {
    return new ReactorSherlockWrapper(locks);
  }

  /**
   * Initializes underlying infrastructure. If it's database then indexes and tables are created. If
   * this method is not invoked directly then it is invoked implicitly when acquiring or releasing a
   * lock for the first time.
   */
  Mono<InitializationResult> initialize();

  /**
   * Create a distributed lock. Lock expires after configured duration.
   *
   * @param lockId the lock id
   * @return the lock
   * @see ReactorSherlock#createLock(String, Duration)
   */
  ReactorDistributedLock createLock(String lockId);

  /**
   * Create a lock. Created lock may be acquired only once by the same application instance:
   *
   * <pre>{@code
   * assert reentrantLock.acquire() == true
   * assert reentrantLock.acquire() == false
   * }</pre>
   *
   * @param lockId the lock id
   * @param duration after that time lock expires and is released
   * @return the lock
   */
  ReactorDistributedLock createLock(String lockId, Duration duration);

  /**
   * Create a distributed reentrant lock. Lock expires after configured duration.
   *
   * @param lockId the lock id
   * @return the reentrant lock
   * @see ReactorSherlock#createReentrantLock(String, Duration)
   */
  ReactorDistributedLock createReentrantLock(String lockId);

  /**
   * Create a distributed reentrant lock. Reentrant lock maybe acquired multiple times by the same
   * application instance:
   *
   * <pre>{@code
   * assert reentrantLock.acquire() == true
   * assert reentrantLock.acquire() == true
   * }</pre>
   *
   * @param lockId the lock id
   * @param duration after that time lock expires and is released
   * @return the reentrant lock
   */
  ReactorDistributedLock createReentrantLock(String lockId, Duration duration);

  /**
   * Create a distributed overriding lock. Lock expires after configured duration.
   *
   * @param lockId the lock id
   * @return the reentrant lock
   * @see ReactorSherlock#createOverridingLock(String, Duration)
   */
  ReactorDistributedLock createOverridingLock(String lockId);

  /**
   * Create a distributed overriding lock. Returned lock overrides lock state without checking if it
   * was released.
   *
   * @param lockId the lock id
   * @param duration after that time lock expires and is released
   * @return the reentrant lock
   */
  ReactorDistributedLock createOverridingLock(String lockId, Duration duration);
}
