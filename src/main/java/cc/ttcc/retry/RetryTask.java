package cc.ttcc.retry;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.DelayQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.ttcc.retry.common.RetryDelayed;
import cc.ttcc.retry.common.RetryEntity;
import cc.ttcc.retry.common.RetryPool;
import cc.ttcc.retry.service.RetryAbstractService;
import cc.ttcc.retry.service.RetryDataPersistService;

/**
 * 重试主任务
 * 
 * @author rui.wang
 */
public class RetryTask {

	private DelayQueue<RetryDelayed> delayQueue = new DelayQueue<RetryDelayed>();

	private static final Logger log = LoggerFactory.getLogger(RetryTask.class);

	private boolean start = false;

	private RetryAbstractService callbackService;
	private RetryDataPersistService<RetryDelayed> persistService;

	public RetryTask(RetryAbstractService callbackService) {
		this.callbackService = callbackService;
	}

	public RetryTask(RetryAbstractService callbackService, RetryDataPersistService<RetryDelayed> persistService) {
		this.callbackService = callbackService;
		this.persistService = persistService;
	}

	/**
	 * 如果存在未执行完毕的任务，应用启动以后调用一次
	 * 
	 **/

	@PostConstruct
	public void checkCache() {
		if (persistService == null) {
			return;
		}
		List<RetryDelayed> list = persistService.getAll();
		log.info("[RetryTask] checkCache size " + (null != list ? list.size() : 0) + " ...");
		if (null != list && list.size() > 0) { // 如果启动时，缓存中有待重试任务
			long now = new Date().getTime();
			for (RetryDelayed retryDelayed : list) {
				if (retryDelayed.getStartTime() < now) { // 重启恢复任务，如果延时时间大于了当前时间，直接开始
					retryDelayed.resume();
				}
				this.add(retryDelayed);
			}
			this.start();
		}
	}

	public void start() {
		if (start) {
			return;
		}
		log.info("[RetryTask] start at " + new SimpleDateFormat("YY-MM-dd HH:mm:sss").format(new Date()) + " ...");
		start = true;
		new Thread(new Runnable() {
			public void run() {
				while (start) {
					try {
						// DelayQueue的take方法，把优先队列拿出来（peek），如果没有达到延时阀值，则进行await处理
						final RetryDelayed task = delayQueue.take();
						if (task != null) {
							RetryPool.execute(new Runnable() {
								public void run() {
									retry(task);
								}
							});
							if (persistService != null) {
								persistService.delete(task);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void retry(RetryDelayed retryDelayed) {
		RetryEntity retryEntity = retryDelayed.getRetryEntity();
		boolean success = false;
		try {
			Method method = callbackService.getClass().getMethod(retryEntity.getMethod(), RetryEntity.class);
			success = (Boolean) method.invoke(callbackService, retryEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!success) {
			log.error("[RetryTask] " + retryDelayed.getRetryEntity().getRemark() + " retry "
					+ retryDelayed.getRetryEntity().getRetryTimes() + " failed");
			RetryDelayed next = this.getNextTask(retryEntity);
			if (next != null) {
				add(next);
			} else {
				callbackService.retryFailed(retryEntity);
				remove(retryDelayed);
			}
		} else {
			log.info("[RetryTask] " + retryDelayed.getRetryEntity().getRemark() + " retry "
					+ retryDelayed.getRetryEntity().getRetryTimes() + " successed");
			callbackService.retrySuccessed(retryEntity);
			remove(retryDelayed);
		}
	}

	public RetryDelayed getNextTask(RetryEntity retryEntity) {
		int[] interval = retryEntity.getIntervals();
		int retrytimes = retryEntity.getRetryTimes();
		if (retrytimes < interval.length) {
			retryEntity.setRetryTimes(retrytimes + 1);
			return new RetryDelayed(retryEntity, interval[retrytimes]);
		}
		return null;
	}

	public void add(RetryEntity retryEntity) {
		if (null == retryEntity || null == retryEntity.getMethod() || null == retryEntity.getIntervals()) {
			return;
		}
		this.add(new RetryDelayed(retryEntity, retryEntity.getIntervals()[0]));
	}

	private void add(final RetryDelayed retryDelayed) {
		this.start();
		RetryPool.execute(new Runnable() {
			public void run() {
				delayQueue.put(retryDelayed);
				log.info("[RetryTask] add retry: " + retryDelayed.getRetryEntity().getRemark() + " [interval "
						+ retryDelayed.getInterval() + "], current retry: " + size());
				if (persistService != null) {
					persistService.save(retryDelayed);
				}
			}
		});
	}

	public void remove(final RetryDelayed target) {
		RetryPool.execute(new Runnable() {
			public void run() {
				if (target == null) {
					return;
				}
				delayQueue.remove(target);
				log.info("[RetryTask] remove retry: " + target.getRetryEntity().getRemark() + " [interval "
						+ target.getInterval() + "], current retry: " + size());
			}
		});
	}

	public int size() {
		return delayQueue.size();
	}

	public boolean isStart() {
		return start;
	}
}
