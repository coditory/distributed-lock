# Sherlock Lock Types 

There are 3 types of locks:

- `SingleEntrantDistributedLock` - lock owner cannot acquire the same lock for the second time
- `ReentrantDistributedLock` - lock owner can acquire the same lock multiple times
- `OverridingDistributedLock` - lock that acquires and releases the lock with `sudo` rights

`SingleEntrantDistributedLock` and `ReentrantDistributedLock` handles the problem of [releasing a lock](#disclaimer) by using the expiration mechanism.

`OverridingDistributedLock` was created for purely administrative tasks.

## SingleEntrantDistributedLock

Owner of a `SingleEntrantDistributedLock` cannot acquire the same lock twice.

Acquiring lock with `SingleEntrantDistributedLock`:

```java
DistributedLock lock = sherlock.createLock("single-entrant");
Instance A                     | Instance B
assert lock.acquire() == true; |
                               | assert lock.lock() == false
assert lock.acquire() == false;|
```

Releasing `SingleEntrantDistributedLock` (the same as `ReentrantDistributedLock`):
```java
Instance A                     | Instance B
assert lock.acquire() == true; |
                               | assert lock.lock() == false
                               | assert lock.release() == false
assert lock.release() == true; |
assert lock.release() == false;|
```

## ReentrantDistributedLock

Owner of a `ReentrantDistributedLock` can acquire the same lock multiple times

Acquiring `ReentrantDistributedLock`:
```java
DistributedLock lock = sherlock.createReentrantLock("reentrant");
Instance A                     | Instance B
assert lock.acquire() == true; |
                               | assert lock.lock() == false
assert lock.acquire() == true; |
```

Releasing `ReentrantDistributedLock` (the same as `SingleEntrantDistributedLock`):
```java
Instance A                     | Instance B
assert lock.acquire() == true; |
                               | assert lock.lock() == false
                               | assert lock.release() == false
assert lock.release() == true; |
assert lock.release() == false;|
```

## OverridingDistributedLock

`OverridingDistributedLock` lock may be acquired and/or released any time.
It was created for purely administrative tasks, like releasing a lock that was blocked in acquired state. 

Acquiring a `OverridingDistributedLock`
```java
DistributedLock lock = sherlock.createOverridingLock("overriding");
Instance A                     | Instance B
assert lock.acquire() == true; |
                               | assert lock.lock() == true
assert lock.acquire() == true; |
assert lock.acquire() == true; |
```

Releasing a `OverridingDistributedLock`
```java
Instance A                     | Instance B
assert lock.acquire() == true; |
                               | assert lock.lock() == true
                               | assert lock.release() == true
assert lock.release() == false;|
assert lock.release() == false;|
```
