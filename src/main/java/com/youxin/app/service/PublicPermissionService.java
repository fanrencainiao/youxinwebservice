package com.youxin.app.service;

import java.util.List;

import com.youxin.app.entity.PublicPermission;
import com.youxin.app.utils.PageResult;

/**
 * 平台授权
 * @author cf
 * @date 2020年2月25日 下午2:25:32
 */
public interface PublicPermissionService {
	/**
	 * 获取授权列表
	 * @return
	 */
	PageResult<PublicPermission> pageList();
	/**
	 * 保存或者修改授权对象
	 * @param PP
	 */
	void SaveOrUpdatePP(PublicPermission PP);
	/**
	 * 获取授权对象
	 * @param id
	 * @return
	 */
	PublicPermission getPP(String id);
	/**
	 * 删除对象
	 * @param id
	 */
	void delPP(String id);
	/**
	 * 获取授权列表
	 * @param pp
	 * @return
	 */
	List<PublicPermission> getPPlist(PublicPermission pp);

}
