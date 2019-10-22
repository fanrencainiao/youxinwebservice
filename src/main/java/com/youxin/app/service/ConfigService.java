package com.youxin.app.service;

import com.youxin.app.entity.Config;



public interface ConfigService {
	/**
	 * 获取系统配置
	 * @return
	 */
	Config getConfig();

    void setConfig(Config config);

}
