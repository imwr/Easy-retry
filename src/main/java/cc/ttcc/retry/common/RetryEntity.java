package cc.ttcc.retry.common;

import java.util.Arrays;
import java.util.Date;

/**
 * 重试对象
 * 
 * @author wr
 *
 */
public class RetryEntity {

	private static final int[] DEFAULT_INTERVALS = new int[] { 1 * 60, 5 * 60, 10 * 60, 30 * 60, 60 * 60, 2 * 3600,
			5 * 3600, 10 * 600, 15 * 3600, 24 * 3600 };

	String uuid; // 时间戳唯一标识，格式：六位随机字母字符串-时间戳
	String remark; // 备注，如优惠券、余额等文本
	String method; // 回调方法
	String params; // 回调方法参数
	private int[] intervals; // 重试间隔

	int retryTimes = 1; // 当前重试次数

	public RetryEntity() {
	}

	public RetryEntity(String remark, String method, String paramsJson) {
		this.uuid = getRandomString(6) + "-" + new Date().getTime();
		this.remark = remark;
		this.method = method;
		this.params = paramsJson;
		this.intervals = DEFAULT_INTERVALS;
	}

	public RetryEntity(String remark, String method, String paramsJson, int[] intervals) {
		this.uuid = getRandomString(6) + "-" + new Date().getTime();
		this.remark = remark;
		this.method = method;
		this.params = paramsJson;
		if (null != intervals && intervals.length > 0) {
			Arrays.sort(intervals);
			this.intervals = intervals;
		} else {
			this.intervals = DEFAULT_INTERVALS;
		}
	}

	private static String getRandomString(int count) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < count; i++) {
			sb.append("abcdefghijklmnopqrstuvwxyz".charAt((int) Math.round(Math.random() * (25))));
		}
		return sb.toString();
	}

	public int getNextTime() {
		if (this.retryTimes >= this.intervals.length) {
			return -1;
		}
		return intervals[this.retryTimes];
	}

	public String getUuid() {
		return uuid;
	}

	public int[] getIntervals() {
		return intervals;
	}

	public int getRetryTimes() {
		return retryTimes;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setRetryTimes(int retryTimes) {
		this.retryTimes = retryTimes;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setIntervals(int[] intervals) {
		this.intervals = intervals;
	}
}
