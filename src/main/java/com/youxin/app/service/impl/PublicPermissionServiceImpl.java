package com.youxin.app.service.impl;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youxin.app.entity.PublicPermission;
import com.youxin.app.repository.PublicPermissionRepository;
import com.youxin.app.service.PublicPermissionService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.PageResult;

import jodd.util.StringUtil;
@Service
public class PublicPermissionServiceImpl implements PublicPermissionService {

	@Autowired
	private PublicPermissionRepository pository;
	@Override
	public PageResult<PublicPermission> pageList() {
		Query<PublicPermission> q = pository.createQuery();
		List<PublicPermission> pplist = pository.find().asList();
		return new PageResult<>(pplist, pository.count());
	}

	@Override
	public void SaveOrUpdatePP(PublicPermission pp) {
		
		
		if(StringUtil.isEmpty(pp.getSid())) {
			pp.setCreateTime(DateUtil.currentTimeSeconds());
			pp.setUpdateTime(DateUtil.currentTimeSeconds());
		}else {
			PublicPermission rpp = pository.findOne("_id", new ObjectId(pp.getSid()));
			pp.setUpdateTime(DateUtil.currentTimeSeconds());
			pp.setCreateTime(rpp.getCreateTime());
		}
			
		pository.save(pp);
	}

	@Override
	public PublicPermission getPP(String id) {
		return pository.findOne("_id", new ObjectId(id));
	}

	@Override
	public void delPP(String id) {
		pository.deleteById(new ObjectId(id));
	}

	@Override
	public List<PublicPermission> getPPlist(PublicPermission pp) {
		Query<PublicPermission> q = pository.createQuery();
		if(StringUtil.isEmpty(pp.getToObj()))
			q.field("toObj").equal(pp.getToObj());
		if(StringUtil.isEmpty(pp.getUrl()))
			q.field("url").equal(pp.getUrl());
		return pository.find().asList();
	}

}
