/*
package com.bobo.copy_redpacket.common.utils;

import io.github.pleuvoir.redpack.exception.LockInterruptedException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Locker {

	private LockRegistry lockRegistry;

	public Locker(RedisConnectionFactory connectionFactory, String registryKey) {
		this.lockRegistry = new RedisLockRegistry(connectionFactory, registryKey, 10_000);
	}

	public <T> T lock(String lockKey, Callable<T> runnable) throws Exception {

		Lock lock = lockRegistry.obtain(lockKey);

		try {

			boolean acquired = lock.tryLock(10, TimeUnit.SECONDS);

			if (!acquired)
				throw new LockInterruptedException(String.format("Failed to acquire lock [%s]", lockKey));

			return runnable.call();

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new LockInterruptedException(String.format("Interrupted in locking [%s]", lockKey));
		} finally {
			lock.unlock();
		}

	}

}
*/
