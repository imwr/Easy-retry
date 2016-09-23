package cc.ttcc.retry.service;

import java.util.ArrayList;
import java.util.List;

import cc.ttcc.retry.common.RetryDelayed;
import cc.ttcc.retry.service.RetryDataPersistService;
import cc.ttcc.retry.utils.RedisUtil;

import com.alibaba.fastjson.JSON;

/**
 * 回滚服务数据持久化接口实现类
 * 
 * @author rui
 *
 */
public class RetryRedisPersistServiceImpl implements RetryDataPersistService<RetryDelayed> {

	RedisUtil redisUtil;

	private static String retry_key = "Retry-order-key";

	private String buildDelayKey(RetryDelayed retryService) {
		return retry_key + "_" + retryService.getUuid();
	}

	@Override
	public void save(RetryDelayed retryService) {
		String key = this.buildDelayKey(retryService);
		redisUtil.lpush(retry_key, key);
		redisUtil.set(key, JSON.toJSONString(retryService));
	}

	@Override
	public long delete(RetryDelayed retryService) {
		String key = this.buildDelayKey(retryService);
		redisUtil.lrem(retry_key, 1, key);
		redisUtil.del(key);
		return this.size();
	}

	@Override
	public List<RetryDelayed> getAll() {
		long size = this.size();
		if (size() > 0) {
			List<RetryDelayed> list = new ArrayList<RetryDelayed>();
			for (int i = 0; i < size; i++) {
				try {
					String stringkey = redisUtil.rpop(retry_key);
					if (stringkey != null) {
						RetryDelayed retryDelayed = JSON.parseObject(redisUtil.get(stringkey), RetryDelayed.class);
						if (retryDelayed == null || retryDelayed.getRetryEntity() == null) {
							continue;
						}
						list.add(retryDelayed);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return list;
		}
		return null;
	}

	@Override
	public long size() {
		return redisUtil.llen(retry_key);
	}
}
