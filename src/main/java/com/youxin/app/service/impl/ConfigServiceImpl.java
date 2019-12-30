package com.youxin.app.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youxin.app.entity.Config;
import com.youxin.app.repository.ConfigRepository;
import com.youxin.app.service.ConfigService;
import com.youxin.app.utils.BeanUtils;
import com.youxin.app.utils.KSessionUtil;



@Service
public class ConfigServiceImpl implements ConfigService {
	Log log=LogFactory.getLog(getClass());
	
	@Autowired
	private ConfigRepository cr;
	@Override
	public Config getConfig() {
		Config config=null;
		try {
			config=KSessionUtil.getConfig();
			if(null==config){
				config = cr.createQuery().field("_id").notEqual(null).get();
				if(null==config)
					config=initConfig();
				KSessionUtil.setConfig(config);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			config = cr.createQuery().field("_id").notEqual(null).get();
		}
		
		return config;
	}
	
	public Config getServerConfig() {
		Config config=null;
		try {
			config=KSessionUtil.getConfig();
			if(null==config){
				config = cr.createQuery().field("_id").notEqual(null).get();
				if(null==config)
					config=initConfig();
				KSessionUtil.setConfig(config);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			config = cr.createQuery().field("_id").notEqual(null).get();
		}
		
		return config;
	}
	
	@Override
	public void setConfig(Config config) {
		
		Config dest = getConfig();
		BeanUtils.copyProperties(config,dest);
		cr.save(dest);
		KSessionUtil.setConfig(dest);
		
	}
	@Override
	public Config initConfig() {
		Config config=new Config();
		config.setId(10000);
		config.setIsAutoAddressBook(0);
		config.setIsAuthApi(0);
		config.setIsOpenSwagger(0);
		cr.save(config);
		
		return config;
	}

}
