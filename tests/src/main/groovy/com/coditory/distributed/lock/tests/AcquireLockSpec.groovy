package com.coditory.distributed.lock.tests

import com.coditory.distributed.lock.DistributedLock
import spock.lang.Unroll

import java.time.Duration
import java.time.Instant

import static com.coditory.distributed.lock.tests.base.LockTypes.OVERRIDING
import static com.coditory.distributed.lock.tests.base.LockTypes.REENTRANT
import static com.coditory.distributed.lock.tests.base.LockTypes.SINGLE_ENTRANT
import static com.coditory.distributed.lock.tests.base.LockTypes.allLockTypes

abstract class AcquireLockSpec extends LocksBaseSpec {
  String lockId = "acquire-id"
  String instanceId = "instance-id"
  String otherInstanceId = "other-instance-id"

  @Unroll
  def "only one of two different instances may acquire lock - #type"() {
    given:
      DistributedLock lock = createLock(type, lockId, instanceId)
      DistributedLock otherLock = createLock(type, lockId, otherInstanceId)
    when:
      boolean firstResult = lock.acquire()
      boolean secondResult = otherLock.acquire()
    then:
      firstResult == true
    and:
      secondResult == false
    where:
      type << [REENTRANT, SINGLE_ENTRANT]
  }

  def "overriding lock may acquire lock acquired by a different instance"() {
    given:
      DistributedLock lock = createLock(REENTRANT, lockId, instanceId)
      DistributedLock overridingLock = createLock(OVERRIDING, lockId, otherInstanceId)
    when:
      boolean firstResult = lock.acquire()
      boolean secondResult = overridingLock.acquire()
    then:
      firstResult == true
    and:
      secondResult == true
  }

  @Unroll
  def "two locks with different lock ids should not block each other - #type"() {
    given:
      DistributedLock lock = createLock(type, lockId, instanceId)
      DistributedLock otherLock = createLock(REENTRANT, "other-acquire", otherInstanceId)
    when:
      boolean firstResult = lock.acquire()
      boolean secondResult = otherLock.acquire()
    then:
      firstResult == true
    and:
      secondResult == true
    where:
      type << allLockTypes()
  }

  @Unroll
  def "newer lock operation should overwrite previous one - #type"() {
    given:
      DistributedLock lock = createLock(type, lockId, instanceId)
      Duration duration = Duration.ofHours(1)

    when:
      lock.acquire(duration)
    then:
      assertLocked(lockId, duration)

    when:
      lock.acquireForever()
    then:
      assertLockedInfinitely(lockId)

    when:
      lock.acquire()
    then:
      assertLocked(lock.id, defaultLockDuration)

    where:
      type << [REENTRANT, OVERRIDING]
  }

  void assertLockedInfinitely(String lockId) {
    DistributedLock otherLock = createLock(REENTRANT, lockId, "assert-instance")
    assert otherLock.acquire() == false
    Instant backup = fixedClock.instant()
    fixedClock.tick(Duration.ofDays(100_000))
    assert otherLock.acquire() == false
    fixedClock.setup(backup)
  }

  void assertLocked(String lockId, Duration duration = defaultLockDuration) {
    DistributedLock otherLock = createLock(REENTRANT, lockId, "assert-instance")
    assert otherLock.acquire() == false
    Instant backup = fixedClock.instant()
    fixedClock.tick(duration)
    assert otherLock.acquire() == true
    otherLock.release()
    fixedClock.setup(backup)
  }
}