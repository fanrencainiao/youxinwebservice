package com.youxin.app.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.entity.AddressBook;
import com.youxin.app.entity.User;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.AddressBookServiceImpl;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "通讯录管理")
@RestController
@RequestMapping("/addressBook")
public class AddressBookController extends AbstractController{
	@Autowired
	private AddressBookServiceImpl abs;
	@Autowired
	private UserService us;
	@ApiOperation(value = "加载通讯录",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "deleteStr", value = "需要删除的手机号", paramType = "query")
	,@ApiImplicitParam(name = "uploadStr", value = "通讯录手机号集合(格式参考友信)", paramType = "query"),
	@ApiImplicitParam(name = "uploadJsonStr", value = "新版通讯录手机号集合(格式参考友信)", paramType = "query"),
	})
	@PostMapping(value = "/upload")
	public Result upload(@RequestParam(defaultValue="")String deleteStr,@RequestParam(defaultValue="")String uploadStr,@RequestParam(defaultValue="")String uploadJsonStr){
		Integer userId = ReqUtil.getUserId();
		List<AddressBook> uploadTelephone = null;
		if(StringUtil.isEmpty(deleteStr) && StringUtil.isEmpty(uploadStr) && StringUtil.isEmpty(uploadJsonStr))
			return new Result(KConstants.ResultCode.ParamsLack,"");
		if(!StringUtil.isEmpty(uploadStr) && !StringUtil.isEmpty(uploadJsonStr))
			return new Result(KConstants.ResultCode.ParamsLack,"参数有误");
		User user = us.getUser(userId);
		uploadTelephone = abs.uploadTelephone(user,deleteStr, uploadStr, uploadJsonStr);
		return Result.success(uploadTelephone);
	}
	
	
	/** @Description:（查询通讯录好友） 
	* @param pageIndex
	* @param pageSize
	* @return
	**/ 
	@ApiOperation(value = "查询通讯录好友",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "pageIndex", value = "页码",defaultValue="0", paramType = "query")
	,@ApiImplicitParam(name = "pageSize", value = "查询长度", defaultValue="20",paramType = "query"),
	})
	@GetMapping(value = "/getAll")
	public Result getAll(@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue="20") int pageSize) {
		Integer userId = ReqUtil.getUserId();
		User user = us.getUser(userId);
		List<AddressBook> data=abs.getAll(user.getMobile(),pageIndex, pageSize);
		if(null==data){
			return Result.error("没有通讯录好友");
		}else {
			return Result.success(data);
		}
			
	}
	/** @Description:（查询已注册的通讯录好友） 
	* @param pageIndex
	* @param pageSize
	* @return
	**/ 
	@ApiOperation(value = "查询已注册的通讯录好友",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "pageIndex", value = "页码",defaultValue="0", paramType = "query")
	,@ApiImplicitParam(name = "pageSize", value = "查询长度", defaultValue="20",paramType = "query"),
	})
	@GetMapping(value = "/getRegisterList")
	public Result getRegisterList(@RequestParam(defaultValue="0") int pageIndex,@RequestParam(defaultValue="20") int pageSize) {
		List<AddressBook> data=abs.findRegisterList(getSession(), pageIndex, pageSize);
		return Result.success(data);
	}
	

}