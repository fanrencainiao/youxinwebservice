--9baolei1   8baolei1   7baolei1   6baolei1   5baolei1
--9baolei2   8baolei2   7baolei2   6baolei2   5baolei2
--9baolei3   8baolei3   7baolei3   6baolei3   5baolei3
--9baolei4   8baolei4   7baolei4   6baolei4   5baolei4
--9baolei5   8baolei5   7baolei5   6baolei5   5baolei5
--9baolei6   8baolei6   7baolei6   6baolei6
--9baolei7   8baolei7   7baolei7
--9baolei8   8baolei8
--9baolei9

b = {
	{
		["baolei1"] = "呵呵",
		["baolei2"] = 100,
		["baolei3"] = 9,
		["baolei4"] = "100*159",
		["baolei5"] = "呵呵",
		["baolei6"] = 100,
		["baolei7"] = 9,
		["baolei8"] = "100*159",
		["baolei9"] = "100*159"
	},
	{
		["baolei1"] = "呵呵",
		["baolei2"] = 100,
		["baolei3"] = 9,
		["baolei4"] = "100*159",
		["baolei5"] = "呵呵",
		["baolei6"] = 100,
		["baolei7"] = 9,
		["baolei8"] = "100*159",
		["baolei9"] = "100*159"
	},
	{
		["baolei1"] = "呵呵",
		["baolei2"] = 100,
		["baolei3"] = 9,
		["baolei4"] = "100*159",
		["baolei5"] = "呵呵",
		["baolei6"] = 100,
		["baolei7"] = 9,
		["baolei8"] = "100*159",
		["baolei9"] = "100*159"
	},
	{
		["baolei1"] = "呵呵",
		["baolei2"] = 100,
		["baolei3"] = 9,
		["baolei4"] = "100*159",
		["baolei5"] = "呵呵",
		["baolei6"] = 100,
		["baolei7"] = 9,
		["baolei8"] = "100*159",
		["baolei9"] = "100*159"
	},
	{
		["baolei1"] = "呵呵",
		["baolei2"] = 100,
		["baolei3"] = 9,
		["baolei4"] = "100*159",
		["baolei5"] = "呵呵",
		["baolei6"] = 100,
		["baolei7"] = 9,
		["baolei8"] = "100*159",
		["baolei9"] = "100*159"
	},
	{
		["baolei1"] = "呵呵",
		["baolei2"] = 100,
		["baolei3"] = 9,
		["baolei4"] = "100*159",
		["baolei5"] = "呵呵",
		["baolei6"] = 100,
		["baolei7"] = 9,
		["baolei8"] = "100*159",
		["baolei9"] = "100*159"
	},
	{
		["baolei1"] = "呵呵",
		["baolei2"] = 100,
		["baolei3"] = 9,
		["baolei4"] = "100*159",
		["baolei5"] = "呵呵",
		["baolei6"] = 100,
		["baolei7"] = 9,
		["baolei8"] = "100*159",
		["baolei9"] = "100*159"
	},
	{
		["baolei1"] = "呵呵",
		["baolei2"] = 100,
		["baolei3"] = 9,
		["baolei4"] = "100*159",
		["baolei5"] = "呵呵",
		["baolei6"] = 100,
		["baolei7"] = 9,
		["baolei8"] = "100*159",
		["baolei9"] = "100*159"
	},
	{
		["baolei1"] = "呵呵",
		["baolei2"] = 100,
		["baolei3"] = 9,
		["baolei4"] = "100*159",
		["baolei5"] = "呵呵",
		["baolei6"] = 100,
		["baolei7"] = 9,
		["baolei8"] = "100*159",
		["baolei9"] = "100*159"
	},
}

a = {
	{
		["FaBaoRen"] = "呵呵",
		["FaBaoJinE"] = 100,
		["FaBaoGeShu"] = 9,
		["HBZhuFuYu"] = "100/123"
	},
	{
		["QiangBaoRen"] = "阿一",
		["QiangBaoJinE"] = 1.11,
	},
	{
		["QiangBaoRen"] = "阿二",
		["QiangBaoJinE"] = 2.22,
	},
	{
		["QiangBaoRen"] = "阿三",
		["QiangBaoJinE"] = 3.33,
	},
	{
		["QiangBaoRen"] = "阿四",
		["QiangBaoJinE"] = 4.44,
	},
	{
		["QiangBaoRen"] = "阿五",
		["QiangBaoJinE"] = 5.55,
	},
	{
		["QiangBaoRen"] = "阿六",
		["QiangBaoJinE"] = 6.66,
	},
	{
		["QiangBaoRen"] = "阿七",
		["QiangBaoJinE"] = 7.77,
	},
	{
		["QiangBaoRen"] = "阿八",
		["QiangBaoJinE"] = 8.88,
	},
	{
		["QiangBaoRen"] = "阿九",
		["QiangBaoJinE"] = 9.99,
	},
}

local transcoder =  {}

function lua_string_split(str, split_char)----字符串分割函数END
    local sub_str_tab = {};
    for mu_id in string.gmatch(str, "(%d+)|*") do
        table.insert(sub_str_tab, mu_id)
    end
    return sub_str_tab;
end

--list = lua_string_split("1.2.3.4", ".");
--print(list[1]..list[2]);
--print(a[1]["FaBaoRen"]);

function PanDuanShiFouZhongLei(ShuZu) ----传入一段数值，判断是否中雷。如果中雷返回"埋雷成功"，不中雷反水"埋雷失败"，发包格式错误返回发包格式错误
	-- body
	print("祝福语个数："..#ShuZu[1]["HBZhuFuYu"]);--打印祝福语个数
	if (#ShuZu[1]["HBZhuFuYu"] <= 3)  ---判断祝福语个数小于3那么久结束
	then
		print("发包格式错误,红包祝福语不正确！");
		return(ShuZu[1]["FaBaoRen"].."\n发包格式错误,红包祝福语不正确！");
	end
	
	for var= 1, #ShuZu[1]["HBZhuFuYu"] do        ------------判断发包祝福语ASCII值是否超出了ASCII值码内，超出了就是包含文字了
		print(string.byte(ShuZu[1]["HBZhuFuYu"],var));--取单个数值的ASCII值
		print(string.sub(ShuZu[1]["HBZhuFuYu"], var, var));---打印单个数值
		
		if (string.byte(ShuZu[1]["HBZhuFuYu"],var) > 127 )---判断祝福语个数大于127那么就结束
		then
			print("发包格式错误,祝福语内包含汉字！");
			return(ShuZu[1]["FaBaoRen"].."\n发包格式错误,祝福语内包含汉字！");
		end
	end

	FaoBaoLeiZhi = lua_string_split(ShuZu[1]["HBZhuFuYu"], "."); ----取后面的雷值。如果没取到，就发包格式错误
	if (FaoBaoLeiZhi[2] == nil)
	then
		print("发包格式错误,雷值没取到！");
		return(ShuZu[1]["FaBaoRen"].."\n发包格式错误,雷值没取到！");
	end
	print("雷值等于："..FaoBaoLeiZhi[2]);
	
	ChengGongCiShu =0
	for var= 1, #FaoBaoLeiZhi[2] do   -----取雷值多少个，就循环判断多少次
		-- body
		for var2= 1, ShuZu[1]["FaBaoGeShu"] do ---------取抢包人多少人就判断多少次
			if (string.sub(ShuZu[var2+1]["QiangBaoJinE"], -1, -1) == string.sub(FaoBaoLeiZhi[2],var,var))
			then
				ChengGongCiShu = ChengGongCiShu + 1
				print("成功："..ChengGongCiShu.."次。");
				break;
			end	
		end
	end

	if ChengGongCiShu == #FaoBaoLeiZhi[2]----判断成功次数是不是等于雷值数
	then
		print("埋雷成功");
		return("埋雷成功");
	else
		print("埋雷失败");
		return("埋雷失败");
	end	
end


function transcoder.main()
	-- body
if (PanDuanShiFouZhongLei(a) ~= "埋雷成功" and PanDuanShiFouZhongLei(a) ~= "埋雷失败") 
then
	print("---------------------------------------------------------------------------------------");
	print("---------------------------------------------------------------------------------------");
	print("---------------------------------------------------------------------------------------");
	FanHuiZhi_MaiLeiShiBai = PanDuanShiFouZhongLei(a) ----返回埋雷失败的原因
	print(FanHuiZhi_MaiLeiShiBai);
	return(FanHuiZhi_MaiLeiShiBai);
end

if (PanDuanShiFouZhongLei(a) == "埋雷成功")-----可发可抢的
then
	print("---------------------------------------------------------------------------------------");
	print("---------------------------------------------------------------------------------------");
	print("---------------------------------------------------------------------------------------");
	local FaBaoGeShu1 = a[1]["FaBaoGeShu"]--取出发包个数
	local FaoBaoLeiZhiGeShu = "baolei"..#FaoBaoLeiZhi[2]---取出发包雷值个数

	BeiLv = b[FaBaoGeShu1][FaoBaoLeiZhiGeShu] ---算出倍率
	FanHuiZhi = "发包人:"..a[1]["FaBaoRen"].."￥:"..a[1]["FaBaoJinE"].."雷:"..FaoBaoLeiZhi[2]

	for var= 1, #FaoBaoLeiZhi[2] do   -----取雷值多少个，就循环判断多少次
		-- body
		for var2= 1, a[1]["FaBaoGeShu"] do ---------取抢包人多少人就判断多少次
			if (string.sub(a[var2+1]["QiangBaoJinE"], -1, -1) == string.sub(FaoBaoLeiZhi[2],var,var))
			then
				FanHuiZhi = FanHuiZhi.."\n"..a[var2+1]["QiangBaoRen"].."应赔付："..a[1]["FaBaoJinE"]*BeiLv
			end	
		end
	end
	FanHuiZhi = FanHuiZhi.."\n赔付完毕！该包倍率："..BeiLv
	print(FanHuiZhi);
	return(FanHuiZhi);
end

if (PanDuanShiFouZhongLei(a) == "埋雷成功") ----禁枪的
then
	print("---------------------------------------------------------------------------------------");
	print("---------------------------------------------------------------------------------------");
	print("---------------------------------------------------------------------------------------");
	local FaBaoGeShu1 = a[1]["FaBaoGeShu"]--取出发包个数
	local FaoBaoLeiZhiGeShu = "baolei"..#FaoBaoLeiZhi[2]---取出发包雷值个数

	BeiLv = b[FaBaoGeShu1][FaoBaoLeiZhiGeShu] ---算出倍率
	FanHuiZhi = "发包人:"..a[1]["FaBaoRen"].."￥:"..a[1]["FaBaoJinE"].."雷:"..FaoBaoLeiZhi[2]

	for var= 1, #FaoBaoLeiZhi[2] do   -----取雷值多少个，就循环判断多少次
		-- body
		for var2= 1, a[1]["FaBaoGeShu"] do ---------取抢包人多少人就判断多少次
			if (string.sub(a[var2+1]["QiangBaoJinE"], -1, -1) == string.sub(FaoBaoLeiZhi[2],var,var))
			then
				FanHuiZhi = FanHuiZhi.."\n应赔付："..a[1]["FaBaoJinE"]*BeiLv.."\n赔付倍率："..BeiLv
				print(FanHuiZhi);
	            return(FanHuiZhi);
			end	
		end
	end

end

end


return transcoder















