package com.youxin.app.service;

import java.util.List;

import com.youxin.app.entity.IPDisable;
import com.youxin.app.utils.PageResult;

/**
 * ip禁用
 * @author cf
 * @date 2020年5月7日 上午10:40:24
 */
public interface IPDisableService {
	/**
	 * 获取添加的ip列表
	 * @return
	 */
	PageResult<IPDisable> pageList(int disable,String ip,int page,int size);
	/**
	 * 保存或者修改对象
	 * @param PP
	 */
	void SaveOrUpdateObj(IPDisable obj);
	/**
	 * 获取对象
	 * @param id
	 * @return
	 */
	IPDisable getObj(String id);
	/**
	 * 删除对象
	 * @param id
	 */
	void delObj(String id);
	/**
	 * 获取列表
	 * @param pp
	 * @return
	 */
	List<IPDisable> getObjlist(IPDisable obj);
	/**
	 * 修改状态
	 * @param id
	 * @param disable
	 */
	void updisable(String id, int disable);

}
