package cc.ttcc.retry.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.ttcc.retry.common.RetryDelayed;
import cc.ttcc.retry.service.RetryDataPersistService;

/**
 * 回滚服务数据持久化接口实现类
 * 
 * @author rui
 * 
 */
public class RetryDataPersistTestServiceImpl implements RetryDataPersistService<RetryDelayed> {

	private Map<String, RetryDelayed> db = new HashMap<String, RetryDelayed>();

	@Override
	public List<RetryDelayed> getAll() {
		List<RetryDelayed> list = new ArrayList<RetryDelayed>();
		for (Map.Entry<String, RetryDelayed> entry : db.entrySet()) {
			list.add(entry.getValue());
		}
		return list;
	}

	@Override
	public void save(RetryDelayed t) {
		db.put(t.getUuid(), t);
	}

	@Override
	public long delete(RetryDelayed t) {
		db.remove(t.getUuid());
		return 0;
	}

	@Override
	public long size() {
		return db.size();
	}
}
