package com.youxin.app.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youxin.app.entity.PublicPermission;
import com.youxin.app.repository.PPSRepository;
import com.youxin.app.service.PublicPermissionService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.PageResult;

import jodd.util.StringUtil;
@Service
public class PublicPermissionServiceImpl implements PublicPermissionService {

	@Autowired
	private PPSRepository ppr;
	@Override
	public PageResult<PublicPermission> pageList() {
		Query<PublicPermission> q = ppr.createQuery();
		List<PublicPermission> pplist = ppr.find().asList();
		return new PageResult<>(pplist, ppr.count());
	}

	@Override
	public void SaveOrUpdatePP(PublicPermission pp) {
		
		
		if(StringUtil.isEmpty(pp.getSid())) {
			pp.setCreateTime(DateUtil.currentTimeSeconds());
			pp.setUpdateTime(DateUtil.currentTimeSeconds());
		}else {
			PublicPermission rpp = ppr.findOne("_id", new ObjectId(pp.getSid()));
			pp.setId(new ObjectId(pp.getSid()));
			pp.setUpdateTime(DateUtil.currentTimeSeconds());
			pp.setCreateTime(rpp.getCreateTime());
		}
			
		ppr.save(pp);
	}

	@Override
	public PublicPermission getPP(String id) {
		return ppr.findOne("_id", new ObjectId(id));
	}

	@Override
	public void delPP(String id) {
		ppr.deleteById(new ObjectId(id));
	}

	@Override
	public List<PublicPermission> getPPlist(PublicPermission pp) {
		Query<PublicPermission> q = ppr.createQuery();
		if(!StringUtil.isEmpty(pp.getToObj()))
			q.field("toObj").equal(pp.getToObj());
		if(!StringUtil.isEmpty(pp.getUrl()))
			q.field("url").equal(pp.getUrl());
		if(pp.getState()!=0)
			q.field("state").equal(pp.getState());
		return ppr.find(q).asList();
	}

}
