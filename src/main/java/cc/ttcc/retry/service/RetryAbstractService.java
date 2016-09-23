package cc.ttcc.retry.service;

import cc.ttcc.retry.common.RetryEntity;

/**
 * 自定义回滚回调service接口
 * 
 * @author rui
 *
 */
public interface RetryAbstractService {

	// 最终失败了的时候会回调，只一次
	public void retryFailed(RetryEntity retryEntity);

	// 最终成功了的时候会回调，只一次
	public void retrySuccessed(RetryEntity retryEntity);

}
