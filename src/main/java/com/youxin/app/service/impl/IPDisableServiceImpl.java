package com.youxin.app.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youxin.app.entity.IPDisable;
import com.youxin.app.repository.IPDisableRepository;
import com.youxin.app.service.IPDisableService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.PageResult;

import jodd.util.StringUtil;
@Service
public class IPDisableServiceImpl implements IPDisableService {

	@Autowired
	private IPDisableRepository repository;
	@Override
	public PageResult<IPDisable> pageList(int disable,String ip,int page,int size) {
		Query<IPDisable> q = repository.createQuery();
		List<IPDisable> pplist = repository.find().asList();
		
		return new PageResult<>(pplist, repository.count());
	}

	@Override
	public void SaveOrUpdateObj(IPDisable pp) {
		
		
		if(StringUtil.isEmpty(pp.getSid())) {
			pp.setCreateTime(DateUtil.currentTimeSeconds());
			pp.setUpdateTime(DateUtil.currentTimeSeconds());
		}else {
			IPDisable rpp = repository.findOne("_id", new ObjectId(pp.getSid()));
			pp.setId(new ObjectId(pp.getSid()));
			pp.setUpdateTime(DateUtil.currentTimeSeconds());
			pp.setCreateTime(rpp.getCreateTime());
		}
			
		repository.save(pp);
	}
	@Override
	public void updisable(String id,int disable) {
		IPDisable rpp = repository.findOne("_id", new ObjectId(id));
		rpp.setUpdateTime(DateUtil.currentTimeSeconds());
		rpp.setDisable(disable);
		repository.save(rpp);
	}
	@Override
	public IPDisable getObj(String id) {
		return repository.findOne("_id", new ObjectId(id));
	}

	@Override
	public void delObj(String id) {
		repository.deleteById(new ObjectId(id));
	}

	@Override
	public List<IPDisable> getObjlist(IPDisable pp) {
		Query<IPDisable> q = repository.createQuery();
		
		return repository.find(q).asList();
	}

}
