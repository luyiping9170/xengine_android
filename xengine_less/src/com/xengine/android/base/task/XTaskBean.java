package com.xengine.android.base.task;

/**
 * <pre>
 * 任务数据的接口。
 * User: jasontujun
 * Date: 13-9-28
 * Time: 下午7:15
 * </pre>
 */
public interface XTaskBean {

    // ============ 任务的状态值 ============ //
    public static final int STATUS_ERROR = -1;// 错误状态
    public static final int STATUS_TODO = 0;// 未执行状态
    public static final int STATUS_DOING = 1;// 正在执行状态
    public static final int STATUS_DONE = 2;// 已完成状态

	/**
	 * 获取下载任务的唯一Id，用于区分不同的下载任务。
	 * @return 返回该下载任务的唯一Id
	 */
	String getId();

    /**
     * 获取任务的自定义类型。
     * @return 返回任务所属的类型值
     */
    int getType();

	/**
	 * 获取当前状态。
	 * @return 返回状态值
     * @see #STATUS_ERROR
     * @see #STATUS_TODO
     * @see #STATUS_DOING
     * @see #STATUS_DONE
	 */
	int getStatus();

	/**
	 * 设置当前状态
	 * @param status 状态值
     * @see #STATUS_ERROR
     * @see #STATUS_TODO
     * @see #STATUS_DOING
     * @see #STATUS_DONE
	 */
	void setStatus(int status);
}