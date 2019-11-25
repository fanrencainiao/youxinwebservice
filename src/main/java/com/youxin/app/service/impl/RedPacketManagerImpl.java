package com.youxin.app.service.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.LastWallet;
import com.youxin.app.entity.RedPacket;
import com.youxin.app.entity.RedReceive;
import com.youxin.app.entity.User;
import com.youxin.app.entity.UserWallet;
import com.youxin.app.entity.WalletFour;
import com.youxin.app.entity.msgbody.MsgBody;
import com.youxin.app.repository.LastWalletRepository;
import com.youxin.app.repository.RedPacketRepository;
import com.youxin.app.repository.RedReceiveRepository;
import com.youxin.app.repository.UserWalletRepository;
import com.youxin.app.repository.WalletFourRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.NumberUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgRequest;



@Service
public class RedPacketManagerImpl{
	protected Log log=LogFactory.getLog("pay");
	@Autowired
	private UserService userManager;
	@Autowired
	private RedPacketRepository redPacketRepository;
	@Autowired
	private RedReceiveRepository redReceiveRepository;
	@Autowired
	private WalletFourRepository walletFourRepository;
	@Autowired
	private UserWalletRepository userWalletRepository;
	@Autowired
	private LastWalletRepository lastWalletRepository;
	@Autowired
	private ConsumeRecordManagerImpl crmi;

	public RedPacket saveRedPacket(RedPacket entity) {
		ObjectId id = (ObjectId) redPacketRepository.save(entity).getId();
		entity.setId(id);
		return entity;
	}

	public synchronized Result getRedPacketById(Integer userId, ObjectId id) {
		RedPacket packet = redPacketRepository.get(id);
		Map<String, Object> map = Maps.newHashMap();
		map.put("packet", packet);
		// 判断红包是否超时
		if (DateUtil.currentTimeSeconds() > packet.getOutTime()) {
			map.put("list", getRedReceivesByRedId(packet.getId()));
			return Result.failure(0,"该红包已超过24小时!", map);
		}
		/*
		 * if(1==packet.getType()&&packet.getUserId().equals(userId)){ map.put("list",
		 * getRedReceivesByRedId(packet.getId())); return
		 * JSONMessage.failureAndData(null, map); //你已经领过了 ! }
		 */

		// 判断红包是否已领完
		List<RedReceive> redReceivesByRedId = getRedReceivesByRedId(packet.getId());
//		Collections.reverse(redReceivesByRedId);
		if (packet.getCount() > packet.getReceiveCount()) {
			// 判断当前用户是否领过该红包
			if (null == packet.getUserIds() || !packet.getUserIds().contains(userId)) {
				if(packet.getStatus()==-1)
					return  Result.error("红包已过期", map);
				map.put("list", redReceivesByRedId);
				return Result.success(map);
			} else {
				map.put("list", redReceivesByRedId);
				return Result.error("你已经领过了", map); // 你已经领过了 !
			}
		} else {// 红包已经领完了
			map.put("list", redReceivesByRedId);
			return Result.error("红包已经领完了", map);
		}
	}

	public synchronized Result openRedPacketById(Integer userId, ObjectId id) {
		RedPacket packet = redPacketRepository.get(id);
		Map<String, Object> map = Maps.newHashMap();
		map.put("packet", packet);
		// 判断红包是否超时
		if (DateUtil.currentTimeSeconds() > packet.getOutTime()) {
			map.put("list", getRedReceivesByRedId(packet.getId()));
			return Result.error("该红包已超过24小时!", map);
		}
		// 判断红包是否已领完
		if (packet.getCount() > packet.getReceiveCount()) {
			// 判断当前用户是否领过该红包
			//
			if (null == packet.getUserIds() || !packet.getUserIds().contains(userId)) {
				packet = openRedPacket(userId, packet);
				map.put("packet", packet);
				map.put("list", getRedReceivesByRedId(packet.getId()));
				return Result.success(map);
			} else {
				map.put("list", getRedReceivesByRedId(packet.getId()));
				return Result.error(null, map); // 你已经领过了 !
			}
		} else { // 你手太慢啦 已经被领完了
			map.put("list", getRedReceivesByRedId(packet.getId()));
			return Result.error("你手太慢啦  已经被领完了!", map);
		}
	}

	private synchronized RedPacket openRedPacket(Integer userId, RedPacket packet) {
		int overCount = packet.getCount() - packet.getReceiveCount();
		User user =userManager.getUser(userId);
		WalletFour walletFour = getWalletFour(packet.getUserId(), packet.getRoomJid());
		UserWallet userWallet = getUserWallet(userId);
		LastWallet lastWallet = getLastWallet(packet.getRoomJid());
		Double money = 0.0;
		DecimalFormat df = new DecimalFormat("#.00");
		if(walletFour != null && walletFour.getIsSetUpMoney()==1) {
			// 普通红包
			if (1 == packet.getType()) {
				if (1 == packet.getCount() - packet.getReceiveCount()) {
					// 剩余一个 领取剩余红包
					if (walletFour.getListRedPackgeMap() == null) {
						money = packet.getOver();
					} else {
//						money = walletFour.getListRedPackgeNumber().get(packet.getReceiveCount()).getRedPackgeMoney();
						money = walletFour.getListRedPackgeMap().get(String.valueOf(packet.getReceiveCount()+1));
						//最后一个红包领取之后设置失效
						walletFour.setIsSetUpMoney(0);
						updateUserWallet(walletFour);
					}
				} else {
					if (walletFour.getListRedPackgeMap() == null) {
						money = packet.getMoney() / packet.getCount();
					} else {
//						money = walletFour.getListRedPackgeNumber().get(packet.getReceiveCount()).getRedPackgeMoney();
						money = walletFour.getListRedPackgeMap().get(String.valueOf(packet.getReceiveCount()+1));
					}
				}
			} else {
				// 拼手气红包或者口令红包
				if (walletFour.getListRedPackgeMap() != null) {
					// if(walletFour.getListNumber().get(packet.getReceiveCount()).getRedPackgeId()
					// == packet.getReceiveCount())
//					money = walletFour.getListRedPackgeNumber().get(packet.getReceiveCount()).getRedPackgeMoney();
					money = walletFour.getListRedPackgeMap().get(String.valueOf(packet.getReceiveCount()+1));
					if (1 == packet.getCount() - packet.getReceiveCount()) {
						walletFour.setIsSetUpMoney(0);
						updateUserWallet(walletFour);
					}
				} else {
					money = getRandomMoney(overCount, packet.getOver());
				}
			}
			
		}else if(lastWallet!=null && lastWallet.getRoomJid()!=null && packet.getCount()-1>0 &&
				NumberUtil.subtract(df.format(packet.getMoney()), df.format(lastWallet.getRedPackgeMoney()))
				.divide(new BigDecimal(packet.getCount()-1), 2, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal("0.01")) >=0) {
			// 普通红包
			if (1 == packet.getType()) {
				if (1 == packet.getCount() - packet.getReceiveCount()) {
					// 剩余一个 领取剩余红包
					money = packet.getOver();
					//尾包控制领取之后失效
					lastWallet.setState(0);
					updateLastWallet(lastWallet);
				} else {
					money = (packet.getMoney()-lastWallet.getRedPackgeMoney()) / (packet.getCount()-1);
				}
			} else{ // 拼手气红包或者口令红包
				if(packet.getCount() - packet.getReceiveCount()>1) {
					money = getRandomMoney(overCount-1, packet.getOver()-lastWallet.getRedPackgeMoney());
				}else {
					money = getRandomMoney(overCount, packet.getOver());
					//尾包控制领取之后失效
					lastWallet.setState(0);
					updateLastWallet(lastWallet);
				}
					
			}
				
			
		}else if (userWallet != null&& packet.getCount()-1>0) {
			// 普通红包
			if (1 == packet.getType()) {
				if (1 == packet.getCount() - packet.getReceiveCount()) {
					// 剩余一个 领取剩余红包
					money = packet.getOver();
					if(userWallet.getRedPackgeMoney()==money) {
						userWallet.setState(0);
						updateUserWallet(userWallet);
					}
				} else {
					if (NumberUtil.subtract(df.format(packet.getOver()), df.format(userWallet.getRedPackgeMoney()))
							.divide(new BigDecimal(overCount-1), 2, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal("0.01")) >=0) {
						money = userWallet.getRedPackgeMoney();
						userWallet.setState(0);
						updateUserWallet(userWallet);
					} else
						money = packet.getMoney() / packet.getCount();
				}
			} else { // 拼手气红包或者口令红包
				
				if (NumberUtil.subtract(df.format(packet.getOver()), df.format(userWallet.getRedPackgeMoney()))
						.divide(new BigDecimal(overCount-1), 2, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal("0.01")) >=0
						&& 1 < packet.getCount() - packet.getReceiveCount()) {
					money = userWallet.getRedPackgeMoney();
					userWallet.setState(0);
					updateUserWallet(userWallet);
					
				} else {
					money = getRandomMoney(overCount, packet.getOver());
					if(userWallet.getRedPackgeMoney()==money) {
						userWallet.setState(0);
						updateUserWallet(userWallet);
					}
				}
			}
			
		} else {
			// 普通红包
			if (1 == packet.getType()) {
				if (1 == packet.getCount() - packet.getReceiveCount()) {
					// 剩余一个 领取剩余红包
					money = packet.getOver();
				} else {
					money = packet.getMoney() / packet.getCount();
				}
			} else // 拼手气红包或者口令红包
				money = getRandomMoney(overCount, packet.getOver());
		}

		// 保留两位小数
		Double over = (packet.getOver() - money);
		
		money=Double.valueOf(df.format(money));
		packet.setOver(Double.valueOf(df.format(over)));
		packet.getUserIds().add(userId);
		
		Query<RedPacket> q = redPacketRepository.createQuery();
		q.field("_id").equal(packet.getId());
		UpdateOperations<RedPacket> ops = redPacketRepository.createUpdateOperations();
		ops.set("receiveCount", packet.getReceiveCount() + 1);
		ops.set("over", packet.getOver());
		ops.set("userIds", packet.getUserIds());
		if (0 == packet.getOver()) {
			ops.set("status", 2);
			packet.setStatus(2);
		}
		redPacketRepository.update(q, ops);

		// 实例化一个红包接受对象
		RedReceive receive = new RedReceive();
		receive.setMoney(money);
		receive.setUserId(userId);
		receive.setAccid(user.getAccid());
		receive.setSendId(packet.getUserId());
		receive.setRedId(packet.getId());
		receive.setTime(DateUtil.currentTimeSeconds());
		receive.setUserName(userManager.getUser(userId).getName());
		receive.setSendName(userManager.getUser(packet.getUserId()).getName());
		ObjectId id = (ObjectId) redReceiveRepository.save(receive).getId();
		receive.setId(id);

		// 修改金额
		userManager.rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);
		final Double num = money;

		MsgRequest messageBean = new MsgRequest();
		messageBean.setFrom(user.getAccid());
		messageBean.setType(100);// 文本
		if (StringUtil.isEmpty(packet.getRoomJid())) {
			messageBean.setOpe(0);// 个人消息
			messageBean.setTo(packet.getAccid());
		}else {
			messageBean.setOpe(1);// 群消息
			messageBean.setTo(packet.getRoomJid());
		}
		packet.setRid(packet.getId().toString());
//		messageBean.setBody("{\"data\":"+JSON.toJSONString(packet)+"}");
//		messageBean.setBody("{\"type\":"+KConstants.MsgType.OPENREDPACKET+",\"data\":"+JSON.toJSONString(packet)+"}");
		messageBean.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.OPENREDPACKET, packet)));
		try {
			JSONObject json=SDKService.sendMsg(messageBean);
			if(json.getInteger("code")!=200) 
				log.debug("红包领取 sdk消息发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("红包领取 sdk消息发送失败"+e.getMessage());
		}

		// 开启一个线程 添加一条消费记录
		new Thread(new Runnable() {
			@Override
			public void run() {
				String tradeNo = AliPayUtil.getOutTradeNo();
				// 创建充值记录
				ConsumeRecord record = new ConsumeRecord();
				record.setUserId(userId);
				record.setTradeNo(tradeNo);
				record.setMoney(num);
				record.setStatus(KConstants.OrderStatus.END);
				record.setType(KConstants.ConsumeType.RECEIVE_REDPACKET);
				record.setPayType(KConstants.PayType.BALANCEAY); // 余额支付
				record.setDesc("红包接受");
				record.setTime(DateUtil.currentTimeSeconds());
				crmi.saveConsumeRecord(record);
			}
		}).start();
		return packet;
	}

	private synchronized Double getRandomMoney(int remainSize, Double remainMoney) {
		// remainSize 剩余的红包数量
		// remainMoney 剩余的钱
		Double money = 0.0;
		if (remainSize == 1) {
			remainSize--;
			money = (double) Math.round(remainMoney * 100) / 100;
			System.out.println("=====> " + money);
			return money;
		}
		Random r = new Random();
		double min = 0.01; //
		double max = remainMoney / remainSize * 2;
		money = r.nextDouble() * max;
		money = money <= min ? 0.01 : money;
		money = Math.floor(money * 100) / 100;
		System.out.println("=====> " + money);
		remainSize--;
		remainMoney -= money;
		DecimalFormat df = new DecimalFormat("#.00");
		return Double.valueOf(df.format(money));
	}

	public void replyRedPacket(String id, String reply) {
		Integer userId = ReqUtil.getUserId();
		Query<RedReceive> query = redReceiveRepository.createQuery().field("userId").equal(userId);
		query.filter("redId", new ObjectId(id));
		UpdateOperations<RedReceive> operations = redReceiveRepository.createUpdateOperations();
		operations.set("reply", reply);
		redReceiveRepository.update(query, operations);
	}

	// 根据红包Id 获取该红包的领取记录
	public synchronized List<RedReceive> getRedReceivesByRedId(ObjectId redId) {
		List<RedReceive> redReceivesByRedId = redReceiveRepository.createQuery().field("redId").equal(redId).asList();
//				(List<RedReceive>) getEntityListsByKey(RedReceive.class, "redId", redId, null);
		Collections.reverse(redReceivesByRedId);
		return redReceivesByRedId;
	}

	// 发送的红包
	public List<RedPacket> getSendRedPacketList(Integer userId, int pageIndex, int pageSize) {
		Query<RedPacket> q = redPacketRepository.createQuery().field("userId").equal(userId);
		return q.order("-sendTime").offset(pageIndex * pageSize).limit(pageSize).asList();
	}

	// 收到的红包
	public List<RedReceive> getRedReceiveList(Integer userId, int pageIndex, int pageSize) {
		return (List<RedReceive>) redReceiveRepository.createQuery().field("userId").equal(userId).asList();
	}

	// 发送的红包
	public PageResult<RedPacket> getRedPacketList(String userName,int userId,int toUserId, int pageIndex, int pageSize) {
		PageResult<RedPacket> result = new PageResult<RedPacket>();
		Query<RedPacket> q = redPacketRepository.createQuery().order("-sendTime");
		if (!StringUtil.isEmpty(userName))
			q.field("userName").equal(userName);
		if(userId>0) {
			q.field("userId").equal(userId);
		}
		if(toUserId>0) {
			q.or(q.criteria("toUserId").equal(toUserId),q.criteria("userIds").hasThisOne(toUserId));
		}
		
		result.setCount(q.count());
		result.setData(q.asList(MongoUtil.pageFindOption(pageIndex, pageSize)));
		return result;
	}

	// 发送的红包
	public PageResult<RedReceive> receiveWater(String redId, int pageIndex, int pageSize) {
		PageResult<RedReceive> result = new PageResult<RedReceive>();
		Query<RedReceive> q = redReceiveRepository.createQuery().field("redId").equal(new ObjectId(redId))
				.order("-time");
		result.setCount(q.count());
		result.setData(q.asList(MongoUtil.pageFindOption(pageIndex, pageSize)));
		return result;
	}
	
	
	
	/**
	 * 获取红包固定设置金额信息
	 * @param userId
	 * @param jid
	 * @return
	 */
	public WalletFour getWalletFour(Integer userId, String jid) {
		try {
			WalletFour query = walletFourRepository.createQuery().field("userId")
					.equal(userId).field("jid").equal(jid).get();

			return query;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 更新红包固定设置金额信息
	 * @param w
	 * @return
	 */
	public void updateUserWallet(WalletFour w) {
		try {
			walletFourRepository.save(w);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	public UserWallet getUserWallet(Integer userId) {
		try {
			UserWallet query = userWalletRepository.createQuery()
					.field("userId").equal(userId).field("state").equal(1).get();

			return query;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void updateUserWallet(UserWallet w) {
		try {
			userWalletRepository.save(w);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	public LastWallet getLastWallet(String roomJid) {
		try {
			LastWallet query = lastWalletRepository.createQuery()
					.field("roomJid").equal(roomJid).field("state").equal(1).get();

			return query;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void updateLastWallet(LastWallet w) {
		try {
			lastWalletRepository.save(w);
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	/**
	 * 获取红包固定设置金额信息
	 * @param userId
	 * @param jid
	 * @return
	 */
	public WalletFour getUserWallet(Integer userId, String jid) {
		try {
			WalletFour query = walletFourRepository.createQuery().field("userId")
					.equal(userId).field("jid").equal(jid).get();

			return query;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
}
