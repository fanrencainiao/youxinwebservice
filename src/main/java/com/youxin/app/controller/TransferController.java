package com.youxin.app.controller;

import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.Transfer;
import com.youxin.app.entity.User;
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

/**
 * 
 * @Description: TODO(用户转账接口)
 * @author zhm
 * @date 2019年2月18日 下午3:22:43
 * @version V1.0
 */
@RestController
@RequestMapping("/skTransfer")
public class TransferController extends AbstractController {
	@Autowired
	private ConsumeRecordManagerImpl recordManager;
	@Autowired
	private UserService userManager;
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
	@RequestMapping(value = "/sendTransfer")
	public Result sendTransfer(Transfer transfer, @RequestParam(defaultValue = "") String money,
			@RequestParam(defaultValue = "0") long time, @RequestParam(defaultValue = "") String secret) {
		Integer userId = ReqUtil.getUserId();
		String token = getAccess_token();

		User user = userManager.getUserFromDB(userId);
		transfer.setUserId(user.getId());
		transfer.setUserName(user.getName());
		// 转账授权校验
		if (!AuthServiceUtils.authRedPacketV1(user.getPayPassword(), userId + "", token, time, money, secret)) {
			return Result.error("支付密码错误!");
		}

		Result result = tm.sendTransfer(userId, money, transfer);
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
	@RequestMapping(value = "/receiveTransfer")
	public Result receiverTransfer(@RequestParam(defaultValue = "") String id,
			@RequestParam(defaultValue = "0") long time, @RequestParam(defaultValue = "") String secret) {
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
	@RequestMapping(value = "/transferRecedeMoney")
	public Result transferRecedeMoney(@RequestParam(defaultValue = "0") Integer toUserId,
			@RequestParam(defaultValue = "0") double money, @RequestParam(defaultValue = "") String transferId) {
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
		if (transfer.getStatus() == CommTask.TRANSFER_START) {
			if(df.format(money).compareTo(df.format(transfer.getMoney()))!=0) {
				return Result.error("金额有误，若已经转账，将在24小时内退还到原账户");
			}
			// 更新状态
			transfer.setStatus(CommTask.TRANSFER_RECEDE);
			dfds.save(transfer);
			
			ConsumeRecord record = new ConsumeRecord();
			String tradeNo = AliPayUtil.getOutTradeNo();
			record.setTradeNo(tradeNo);
			record.setMoney(money);
			record.setUserId(userId);
			record.setType(KConstants.ConsumeType.REFUND_TRANSFER);
			record.setPayType(KConstants.PayType.BALANCEAY);
			record.setTime(DateUtil.currentTimeSeconds());
			record.setStatus(KConstants.OrderStatus.END);
			record.setDesc("转账退款");

			recordManager.saveConsumeRecord(record);

			userManager.rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);

			User sysUser = userManager.getUser(1100);
			transfer.setId(null);
//			MessageBean messageBean = new MessageBean();
//			messageBean.setType(KXMPPServiceImpl.REFUNDTRANSFER);
//
//			messageBean.setFromUserId(sysUser.getUserId().toString());
//			messageBean.setFromUserName(sysUser.getNickname());
//
//			messageBean.setContent(JSONObject.toJSON(transfer));
//			messageBean.setToUserId(userId.toString());
//			messageBean.setMsgType(0);// 单聊消息
//			messageBean.setMessageId(StringUtil.randomUUID());
			
			Msg messageBean = new Msg();
			messageBean.setFrom(sysUser.getId().toString());
			messageBean.setMsgtype(0);//点对点
			messageBean.setOpe(0);
			messageBean.setTo(userId.toString());
			messageBean.setAttach("");//自定义内容 json
			messageBean.setPushcontent(JSONObject.toJSONString(transfer));//推送文案，android以此为推送显示文案；ios若未填写payload，显示文案以pushcontent为准。超过500字符后，会对文本进行截断。
//			messageBean.setPayload("");//ios 推送对应的payload,必须是JSON,不能超过2k字符
//			messageBean.setSound("");//声音
			messageBean.setSave(2);//会存离线
//			messageBean.setOption("");
			messageBean.setMsgid(StringUtil.randomUUID());
			try {
				JSONObject sendAttachMsg = SDKService.sendAttachMsg(messageBean);
				if(sendAttachMsg.getInteger("code")!=200) 
					log.debug("付款 sdk消息发送失败");
			} catch (Exception e) {
				e.printStackTrace();
				log.debug("付款 sdk消息发送失败"+e.getMessage());
			}
			System.out.println(userId + "  发出转账,剩余金额   " + money + "  未收钱  退回余额!");
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
	@RequestMapping(value = "/getTransferInfo")
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
	@RequestMapping(value = "/getTransferList")
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
	@RequestMapping(value = "getReceiveList")
	public Result getReceiveList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize) {
		Object data = tm.getTransferReceiveList(ReqUtil.getUserId(), pageIndex,
				pageSize);
		return Result.success(data);
	}
}
