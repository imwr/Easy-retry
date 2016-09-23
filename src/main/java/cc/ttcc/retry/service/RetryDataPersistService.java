package cc.ttcc.retry.service;

import java.util.List;

/**
 * 回滚服务数据持久化接口
 * 
 * @author rui
 *
 */
public interface RetryDataPersistService<T> {

	public void save(T t);

	public long delete(T t);

	public List<T> getAll();

	public long size();
}
