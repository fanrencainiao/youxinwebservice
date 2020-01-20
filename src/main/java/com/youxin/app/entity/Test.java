package com.youxin.app.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bouncycastle.crypto.prng.ThreadedSeedGenerator;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexed;

import com.youxin.app.utils.DateUtil;

import lombok.Data;

@Entity(value = "test", noClassnameStored = true)
@Data
public class Test {
	
	public static void main(String[] args) {
		Double moeny = 10d;
		int num = 3;
		Double max = 400d;
		Double min = 0.01d;

		System.out.println("最终结果"+randwonMoney(moeny, num , max, min));
		
	}

	private static Double randwonMoney(Double moeny, int num, Double max, Double min) {
		DecimalFormat df = new DecimalFormat("#.00");
		df.setRoundingMode(RoundingMode.HALF_EVEN);
		Double resultMoney=0.0d;
		Random r = new Random();
		long currentTimeMilliSeconds = DateUtil.currentTimeMilliSeconds();
		if (num == 1) {
			System.out.println(moeny);
			resultMoney = moeny;
		} else {
			if (moeny / num == max) {
				System.out.println(max);
				resultMoney = max;
			} else if (moeny / num == min) {
				System.out.println(min);
				resultMoney = min;
			} else if (moeny / num > max || moeny / num < min) {
				System.out.println("无法分配");
			} else {

				double nd = r.nextDouble();
				Double rmoney = 0.0d;

				Double lesMoney = Double.valueOf(df.format(moeny - (num - 1) * max));
				if (lesMoney > 0) {
					rmoney = (max - lesMoney) * nd + lesMoney;
					System.out.println("1:" + rmoney);
				} else {
					// 计算此次最大分配金额，保证此次之后剩余金额与数量足够最小分配
					if ((moeny - (num - 1) * min) < max) {
						rmoney = (moeny - (num - 1) * min) * nd;
						System.out.println("2:" + rmoney);
					} else {
						rmoney = (max - (num - 1) * min) * nd;
						System.out.println("3:" + rmoney);
					}
				}
				rmoney = Double.valueOf(df.format(rmoney));
				if (rmoney == 0.0)
					rmoney = 0.01;
				System.out.println("结果：" + rmoney);
				resultMoney = rmoney;
			}
		}
		System.out.println("抢红包耗时："+(DateUtil.currentTimeMilliSeconds() - currentTimeMilliSeconds));
		return resultMoney;
	}

}
