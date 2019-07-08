package com.coditory.sherlock


import spock.lang.Specification

import java.time.Duration

import static com.coditory.sherlock.base.DistributedLockAssertions.assertAlwaysClosedLock
import static com.coditory.sherlock.base.DistributedLockAssertions.assertAlwaysOpenedLock

class SherlockStubSpec extends Specification {
  def "should create sherlock with custom properties"() {
    given:
      String instanceId = "tested-instance-id"
      Duration duration = Duration.ofHours(1)

    when:
      Sherlock sherlock = SherlockStub.withOpenedLocks()
          .withLockDuration(duration)
          .withServiceInstanceId(instanceId)

    then:
      sherlock.lockDuration == duration
      sherlock.instanceId == instanceId
  }

  def "should create sherlock returning always opened locks"() {
    given:
      String lockId = "some-lock"
      Sherlock sherlock = SherlockStub.withOpenedLocks()

    expect:
      assertAlwaysOpenedLock(sherlock.createLock(lockId), lockId)
      assertAlwaysOpenedLock(sherlock.createReentrantLock(lockId), lockId)
      assertAlwaysOpenedLock(sherlock.createOverridingLock(lockId), lockId)
  }

  def "should create sherlock returning always closed locks"() {
    given:
      String lockId = "some-lock"
      Sherlock sherlock = SherlockStub.withClosedLocks()

    expect:
      assertAlwaysClosedLock(sherlock.createLock(lockId), lockId)
      assertAlwaysClosedLock(sherlock.createReentrantLock(lockId), lockId)
      assertAlwaysClosedLock(sherlock.createOverridingLock(lockId), lockId)
  }

  def "should create sherlock returning closed locks by default and opened lock for specific id"() {
    given:
      String lockId = "some-lock"
      Sherlock sherlock = SherlockStub.withClosedLocks()
          .withLock(DistributedLockMock.alwaysOpenedLock(lockId))

    expect:
      assertAlwaysClosedLock(sherlock.createLock("other-lock"))
      assertAlwaysOpenedLock(sherlock.createLock(lockId))
  }
}
