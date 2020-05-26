package com.youxin.app.controller;

import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.Config;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.Transfer;
import com.youxin.app.entity.User;
import com.youxin.app.service.ConfigService;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.service.impl.TransferManagerImpl;
import com.youxin.app.utils.AuthServiceUtils;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.scheduleds.CommTask;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 
 * @Description: TODO(用户转账接口)
 * @author zhm
 * @date 2019年2月18日 下午3:22:43
 * @version V1.0
 */
@Api(tags = "用户转账管理")
@RestController
@RequestMapping("/skTransfer")
public class TransferController extends AbstractController {
	@Autowired
	private ConsumeRecordManagerImpl recordManager;
	@Autowired
	private UserService userManager;
	@Autowired
	private ConfigService cs;
	@Autowired
	private TransferManagerImpl tm;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	/**
	 * 用户转账
	 * 
	 * @param transfer
	 * @param money
	 * @param time
	 * @param secret
	 * @return
	 */
	@ApiOperation(value = "用户转账", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "moneyStr", value = "加密金额",required = true, paramType = "query"),
			@ApiImplicitParam(name = "time", value = "加密时间", required = true, paramType = "query"),
			@ApiImplicitParam(name = "secret", value = "安全加密 md5(md5(apikey+time+money) +userid+token+payPassword)", required = true, paramType = "query") })
	@PostMapping(value = "/sendTransfer")
	public Result sendTransfer(@RequestBody Transfer transfer, @RequestParam(defaultValue = "") String moneyStr,
			@RequestParam(defaultValue = "0") long time, @RequestParam(defaultValue = "") String secret) {
		Config config = cs.getConfig();
		Assert.isTrue(config.getTransferState()<1, JSON.toJSONString(Result.error("转账功能暂不可用", config)));
		Integer userId = ReqUtil.getUserId();
		String token = getAccess_token();

		User user = userManager.getUserFromDB(userId);
		transfer.setUserId(user.getId());
		transfer.setUserName(user.getName());
		transfer.setAccid(user.getAccid());
		// 转账授权校验
		if (!AuthServiceUtils.authRedPacketV1(user.getPayPassword(), userId + "", token, time,  moneyStr, secret)) {
			return Result.error("支付密码错误!");
		}

		Result result = tm.sendTransfer(userId,  moneyStr, transfer);
		return result;
	}

	/**
	 * 用户接受转账
	 * 
	 * @param id
	 * @param time
	 * @param secret
	 * @return
	 */
	@ApiOperation(value = "用户接受转账", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "转账id",required = true, paramType = "query"),
			@ApiImplicitParam(name = "time", value = "加密时间", required = true, paramType = "query"),
			@ApiImplicitParam(name = "secret", value = "安全加密 md5( md5(apikey+time) +userid+token) ", required = true, paramType = "query") })
	@PostMapping(value = "/receiveTransfer")
	public Result receiverTransfer(@RequestParam(defaultValue = "") String id,
			@RequestParam(defaultValue = "0") long time, @RequestParam(defaultValue = "") String secret) {
		Config config = cs.getConfig();
		Assert.isTrue(config.getTransferState() < 1, JSON.toJSONString(Result.error("转账功能暂不可用", config)));
		
		String token = getAccess_token();
		Integer userId = ReqUtil.getUserId();
		// 接口授权校验
		if (!AuthServiceUtils.authRedPacket(userId + "", token, time, secret)) {
			return Result.error("权限验证失败!");
		}
		Result result = tm.receiveTransfer(userId, new ObjectId(id));
		return result;
	}

	/**
	 * 转账退回
	 * 
	 * @param toUserId
	 * @param money
	 * @param transferId
	 */
	@ApiOperation(value = "转账退回", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "toUserId", value = "退回人",required = true, paramType = "query"),
			@ApiImplicitParam(name = "money", value = "退回金额", required = true, paramType = "query"),
			@ApiImplicitParam(name = "transferId", value = "转账id", required = true, paramType = "query") })
	@PostMapping(value = "/transferRecedeMoney")
	public Result transferRecedeMoney(@RequestParam(defaultValue = "0") Integer toUserId,
			@RequestParam(defaultValue = "0") double money, @RequestParam(defaultValue = "") String transferId) {
		
		Config config = cs.getConfig();
		Assert.isTrue(config.getTransferState() < 1, JSON.toJSONString(Result.error("转账功能暂不可用", config)));
		
		ObjectId tid = null;
		// 格式化数据
		DecimalFormat df = new DecimalFormat("#.00");
		if (0 < money && 1100 < toUserId.intValue()) {
			money = Double.valueOf(df.format(money));
		} else
			return Result.error("数字异常，将在24小时内退还到原账户");
		if (StringUtils.isNotBlank(transferId)) {
			tid = parse(transferId);
		} else
			return Result.error("转账信息异常，将在24小时内退还到原账户");
		Integer userId = ReqUtil.getUserId();
		if (userId == null || userId < 1100)
			return Result.error("违法操作，若已经转账，将在24小时内退还到原账户");

		Transfer transfer =dfds.createQuery(Transfer.class).field("_id").equal(tid).get();
		log.debug(toUserId);
		log.debug(transfer.getUserId());
		log.debug(transfer.getUserId().equals(toUserId));
//		Assert.isTrue(transfer.getUserId().equals(toUserId),"非法操作");
		if (transfer.getStatus() == CommTask.TRANSFER_START) {
			if(df.format(money).compareTo(df.format(transfer.getMoney()))!=0) {
				return Result.error("金额有误，若已经转账，将在24小时内退还到原账户");
			}
			// 接收退回时间
			transfer.setReceiptTime(DateUtil.currentTimeSeconds());
			transfer.setStatus(CommTask.TRANSFER_RECEDE);
			dfds.save(transfer);
			
			ConsumeRecord record = new ConsumeRecord();
			String tradeNo = AliPayUtil.getOutTradeNo();
			record.setTradeNo(tradeNo);
			record.setMoney(money);
			record.setUserId(transfer.getUserId());
			record.setType(KConstants.ConsumeType.REFUND_TRANSFER);
			record.setPayType(KConstants.PayType.BALANCEAY);
			record.setTime(DateUtil.currentTimeSeconds());
			record.setStatus(KConstants.OrderStatus.END);
			record.setDesc("转账退款");

			recordManager.saveConsumeRecord(record);

			userManager.rechargeUserMoeny(transfer.getUserId(), money, KConstants.MOENY_ADD);

//			User sysUser = userManager.getUser(1100);
//			transfer.setId(null);		
//			MsgRequest messageBean = new MsgRequest();
//			messageBean.setFrom(sysUser.getAccid());
//			messageBean.setType(100);// 文本
//		
//			messageBean.setOpe(0);// 个人消息
//			messageBean.setTo(transfer.getAccid());
//			
////			messageBean.setBody("{\"msg\":"+JSON.toJSONString(transfer)+"}");
//			messageBean.setBody("{\"type\":"+KConstants.MsgType.TRANSFERBACK+",\"data\":"+JSON.toJSONString(transfer)+"}");
//			
//			try {
//				JSONObject json=SDKService.sendMsg(messageBean);
//				if(json.getInteger("code")!=200) 
//					log.debug("退款 sdk消息发送失败");
//			} catch (Exception e) {
//				e.printStackTrace();
//				log.debug("退款 sdk消息发送失败"+e.getMessage());
//			}
			
			log.debug(userId + "  发出转账,剩余金额   " + money + "  未收钱  退回余额!");
			return Result.success(userId + "  发出转账,剩余金额   " + money + "  未收钱  退回余额!");
		} else {
			return Result.error("该转账金额已经退回");
		}
	}

	/**
	 * 获取转账信息
	 * 
	 * @param id
	 * @return
	 */
	@ApiOperation(value = "获取转账信息", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "转账Id",required = true, paramType = "query"),
			 })
	@GetMapping(value = "/getTransferInfo")
	public Result getTransferInfo(@RequestParam(defaultValue = "") String id) {
		Result result = tm.getTransferById(ReqUtil.getUserId(), new ObjectId(id));
		return result;
	}

	/**
	 * 获取用户转账列表
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@ApiOperation(value = "获取用户转账列表", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageIndex", value = "页码",required = true, paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "长度",required = true, paramType = "query"),
			 })
	@GetMapping(value = "/getTransferList")
	public Result getTransferList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize) {
		Object data = tm.getTransferList(ReqUtil.getUserId(), pageIndex, pageSize);
		return Result.success(data);
	}

	/**
	 * 获取用户接受转账列表
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@ApiOperation(value = "获取用户接受转账列表", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageIndex", value = "页码",required = true, paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "长度",required = true, paramType = "query"),
			 })
	@GetMapping(value = "/getReceiveList")
	public Result getReceiveList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize) {
		Object data = tm.getTransferReceiveList(ReqUtil.getUserId(), pageIndex,
				pageSize);
		return Result.success(data);
	}
}
