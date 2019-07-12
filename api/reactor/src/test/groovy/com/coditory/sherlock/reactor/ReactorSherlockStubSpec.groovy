package com.coditory.sherlock.reactor


import spock.lang.Specification

import java.time.Duration

import static com.coditory.sherlock.reactor.ReactorDistributedLockMock.alwaysOpenedLock
import static com.coditory.sherlock.reactor.base.DistributedLockAssertions.assertAlwaysClosedLock
import static com.coditory.sherlock.reactor.base.DistributedLockAssertions.assertAlwaysOpenedLock

class ReactorSherlockStubSpec extends Specification {
  def "should create sherlock with custom properties"() {
    given:
      String instanceId = "tested-instance-id"
      Duration duration = Duration.ofHours(1)

    when:
      ReactorSherlock sherlock = ReactorSherlockStub.withOpenedLocks()
          .withLockDuration(duration)
          .withServiceInstanceId(instanceId)

    then:
      sherlock.lockDuration == duration
      sherlock.instanceId == instanceId
  }

  def "should create sherlock returning always opened locks"() {
    given:
      String lockId = "some-lock"
      ReactorSherlock sherlock = ReactorSherlockStub.withOpenedLocks()

    expect:
      assertAlwaysOpenedLock(sherlock.createLock(lockId), lockId)
      assertAlwaysOpenedLock(sherlock.createReentrantLock(lockId), lockId)
      assertAlwaysOpenedLock(sherlock.createOverridingLock(lockId), lockId)
  }

  def "should create sherlock returning always closed locks"() {
    given:
      String lockId = "some-lock"
      ReactorSherlock sherlock = ReactorSherlockStub.withClosedLocks()

    expect:
      assertAlwaysClosedLock(sherlock.createLock(lockId), lockId)
      assertAlwaysClosedLock(sherlock.createReentrantLock(lockId), lockId)
      assertAlwaysClosedLock(sherlock.createOverridingLock(lockId), lockId)
  }

  def "should create sherlock returning closed locks by default and opened lock for specific id"() {
    given:
      String lockId = "some-lock"
      ReactorSherlock sherlock = ReactorSherlockStub.withClosedLocks()
          .withLock(alwaysOpenedLock(lockId))

    expect:
      assertAlwaysClosedLock(sherlock.createLock("other-lock"))
      assertAlwaysOpenedLock(sherlock.createLock(lockId))
  }
}