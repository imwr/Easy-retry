package cc.ttcc.retry.common;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 回滚服务线程池管理
 * 
 * @author rui
 *
 */
public class RetryPool {

	private final ExecutorService executor;

	private static RetryPool instance = new RetryPool();

	private RetryPool() {
		this.executor = Executors.newFixedThreadPool(5);
	}

	public static RetryPool getInstance() {
		return instance;
	}

	public static <T> Future<T> execute(final Callable<T> runnable) {
		return getInstance().executor.submit(runnable);
	}

	public static Future<?> execute(final Runnable runnable) {
		return getInstance().executor.submit(runnable);
	}
}
