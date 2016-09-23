package cc.ttcc.retry.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import cc.ttcc.retry.common.RetryEntity;
import cc.ttcc.retry.service.RetryAbstractService;

public class RetryRollBackTestService implements RetryAbstractService {

	private static SimpleDateFormat time = new SimpleDateFormat("HH:mm:sss");

	/**
	 * 重试回调方法
	 * 
	 * @param retryEntity
	 * @return false 等待下次重试；true 从重试队列移除
	 */
	public boolean rollbackCoupons(RetryEntity retryEntity) {
		System.out.println("[" + time.format(new Date()) + "] " + retryEntity.getRemark() + " 开始第["
				+ retryEntity.getRetryTimes() + "]次重试, parameters: " + retryEntity.getParams());
		boolean result = new Random().nextInt(100) > 80;// 模拟回滚优惠券
		if (!result) { // 重试失败
			System.out.println("[" + retryEntity.getUuid() + "]" + retryEntity.getRemark() + " 第["
					+ retryEntity.getRetryTimes() + "]次重试失败");
		}
		return result;
	}

	public boolean rollbackBalance(RetryEntity retryEntity) {
		System.out.println("[" + time.format(new Date()) + "] " + retryEntity.getRemark() + " 开始第["
				+ retryEntity.getRetryTimes() + "]次重试, parameters: " + retryEntity.getParams());
		boolean result = new Random().nextInt(100) > 80; // 模拟回滚余额
		if (!result) { // 重试失败
			System.out.println("[" + retryEntity.getUuid() + "]" + retryEntity.getRemark() + " 第["
					+ retryEntity.getRetryTimes() + "]次重试失败");
		}
		return result;
	}

	// 最终失败了的时候会回调，只一次
	@Override
	public void retryFailed(RetryEntity retryEntity) {
		System.out.println("[" + retryEntity.getUuid() + "]" + retryEntity.getRemark() + " 最终失败");
		// TODO 统一监控报警
	}

	// 最终成功了的时候会回调，只一次
	@Override
	public void retrySuccessed(RetryEntity retryEntity) {
		System.out.println("[" + retryEntity.getUuid() + "]" + retryEntity.getRemark() + " 最终在第["
				+ retryEntity.getRetryTimes() + "]次重试中成功");
	}
}
