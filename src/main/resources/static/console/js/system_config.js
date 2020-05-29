/**
 * 系统配置页面相关的js
 */
 
var obj;
function num(){
	obj = $(".giftRatio").val();
	var reg = new RegExp("^[0-9]+(.[0-9]{0,2})?$", "g");
	(!reg.test(obj)?layer.alert("直播礼物分成比率请保持小数点后两位有效数字"):"");
}

$(function () {
    //非超级管理员登录屏蔽操作按钮
    if(localStorage.getItem("role")==1 || localStorage.getItem("role")==5){
        $(".save").remove();
    }
})

//填充数据方法
function fillParameter(data){
    //判断字段数据是否存在
    function nullData(data){
        if(data == '' || data == "undefined" || data==null){
            return "";
        }else{
            return data;
        }
    }

    //数据回显
     
    $(".apiUrl").val(nullData(data.apiUrl));   
    $(".regNotice").val(nullData(data.regNotice));   
    $(".afee").val(nullData(data.afee)); 
    $(".bfee").val(nullData(data.bfee));  
    $(".todayLimitMoney").val(nullData(data.todayLimitMoney));
    $(".nowlimitMoney").val(nullData(data.nowlimitMoney));
    $(".todayLimitNumber").val(nullData(data.todayLimitNumber));
    $(".isAutoAddressBook").val(data.isAutoAddressBook);
    $(".isOpenSwagger").val(data.isOpenSwagger);
    $(".authApi").val(data.isAuthApi);
    $(".bankMsgState").val(data.bankMsgState);//是否开启银行手机通知
    /*=======零钱相关===========*/
    $(".aliState").val(data.aliState);//
    $(".wxState").val(data.wxState);//
    $(".yeeState").val(data.yeeState);//
    $(".redPacketState").val(data.redPacketState);//
    $(".aliRedPacketState").val(data.aliRedPacketState);//
    $(".transferState").val(data.transferState);//
    $(".bankState").val(data.bankState);//
    $(".rrShopState").val(data.rrShopState);//
    $(".moneyState").val(data.moneyState);//
    $(".aliCodeState").val(data.aliCodeState);//
    $(".codeReceiveState").val(data.codeReceiveState);//
    $(".configChangeNotice").val(nullData(data.configChangeNotice));   
    
    
    
//    $(".isHideFinance").val(data.isHideFinance);//是否隐藏app零钱相关功能
    $(".sendPhone").val(nullData(data.sendPhone));  
    
    $(".androidVersion").val(nullData(data.androidVersion));
    $(".androidGoVersion").val(nullData(data.androidGoVersion));
    $(".androidVersionDisable").val(nullData(data.androidVersionDisable));
    $(".androidDownUrl").val(nullData(data.androidDownUrl));
    $(".androidExplain").val(nullData(data.androidExplain));

    $(".iosVersion").val(nullData(data.iosVersion));
    $(".iosVersionDisable").val(nullData(data.iosVersionDisable));
    $(".iosVersionVerify").val(nullData(data.iosVersionVerify)); 
    $(".iosDownUrl").val(nullData(data.iosDownUrl));
    $(".iosExplain").val(nullData(data.iosExplain));

   
    //重新渲染
    layui.form.render();

}

layui.use(['form','jquery',"layer"],function() {
    var form = layui.form,
        $ = layui.jquery,
        layer = parent.layer === undefined ? layui.layer : top.layer;

        
    //获取当前系统配置
    if(window.sessionStorage.getItem("systemConfig")){
        var systemConfig = JSON.parse(window.sessionStorage.getItem("systemConfig"));
        fillParameter(systemConfig);
    }else{
        /*$.ajax({
            url : "../json/systemParameter.json",
            type : "get",
            dataType : "json",
            success : function(data){
                fillParameter(data);
            }
        })*/

        Common.invoke({
            path : request('/console/config'),
            data : {},
            successMsg : false,
            errorMsg : "获取数据失败,请检查网络",
            successCb : function(result) {
                fillParameter(result.data);
            },
            errorCb : function(result) {
            }

        });
    }
    //非管理员登录屏蔽操作按钮
    if(localStorage.getItem("IS_ADMIN")==0){
      $(".save").remove();
    }
    
    //提交保存配置
    form.on("submit(systemConfig)",function(data){
       
       var systemConfig = {};
        systemConfig.id = 10000;
   
        systemConfig.apiUrl = $(".apiUrl").val();   
        
        systemConfig.regNotice = $(".regNotice").val();   
        
        systemConfig.isAuthApi = $(".authApi").val();

        systemConfig.isAutoAddressBook = $(".isAutoAddressBook").val();
        
        systemConfig.isOpenSwagger = $(".isOpenSwagger").val();
        
        systemConfig.bankMsgState = $(".bankMsgState").val();
        
        systemConfig.aliState = $(".aliState").val();
        systemConfig.wxState = $(".wxState").val();
        systemConfig.yeeState = $(".yeeState").val();
        systemConfig.redPacketState = $(".redPacketState").val();
        systemConfig.aliRedPacketState = $(".aliRedPacketState").val();
        systemConfig.transferState = $(".transferState").val();
        systemConfig.bankState = $(".bankState").val();
        systemConfig.rrShopState = $(".rrShopState").val();
        systemConfig.moneyState = $(".moneyState").val();
        systemConfig.aliCodeState = $(".aliCodeState").val();
        systemConfig.codeReceiveState = $(".codeReceiveState").val();
        systemConfig.configChangeNotice = $(".configChangeNotice").val();   
        
//        systemConfig.isHideFinance = $(".isHideFinance").val();
        
        systemConfig.sendPhone = $(".sendPhone").val();
        if($(".afee").val()>=0){
        	systemConfig.afee = $(".afee").val();
        }else{
        	layer.alert("固定金额需大于0");
        	return;
        }
        if($(".bfee").val()>=0&&$(".bfee").val()<1){
        	systemConfig.bfee = $(".bfee").val();
        }else{
        	layer.alert("百分比需在0-1之间");
        	return;
        }
        systemConfig.todayLimitMoney = $(".todayLimitMoney").val();
        systemConfig.nowlimitMoney = $(".nowlimitMoney").val();
        systemConfig.todayLimitNumber = $(".todayLimitNumber").val();

        if($(".androidVersion").val()=="" || $(".androidVersion").val() == null){
            systemConfig.androidVersion = 0;
        }else {
            systemConfig.androidVersion = $(".androidVersion").val();
        }
        if($(".androidGoVersion").val()=="" || $(".androidGoVersion").val() == null){
            systemConfig.androidGoVersion = 0;
        }else {
            systemConfig.androidGoVersion = $(".androidGoVersion").val();
        }
        if($(".androidVersionDisable").val()=="" || $(".androidVersionDisable").val() == null){
            systemConfig.androidVersionDisable = 0;
        }else {
            systemConfig.androidVersionDisable = $(".androidVersionDisable").val();
        }
        
        systemConfig.androidDownUrl = $(".androidDownUrl").val();
        systemConfig.androidExplain = $(".androidExplain").val();

        if($(".iosVersion").val()=="" || $(".iosVersion").val() == null){
            systemConfig.iosVersion = 0;
        }else {
            systemConfig.iosVersion = $(".iosVersion").val();
        }
        if($(".iosVersionDisable").val()=="" || $(".iosVersionDisable").val() == null){
            systemConfig.iosVersionDisable = 0;
        }else {
            systemConfig.iosVersionDisable = $(".iosVersionDisable").val();
        }
        if($(".iosVersionVerify").val()=="" || $(".iosVersionVerify").val() == null){
            systemConfig.iosVersionVerify = 0;
        }else {
            systemConfig.iosVersionVerify = $(".iosVersionVerify").val();
        }
        systemConfig.iosDownUrl = $(".iosDownUrl").val();
        systemConfig.iosExplain = $(".iosExplain").val();

 
        //弹出loading
        //var index = top.layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
        Common.invoke({
            path : request('/console/config/set'),
            data : systemConfig,
            successMsg : "系统配置修改成功",
            errorMsg : "修改系统配置失败,请检查网络",
            successCb : function(result) {
              
                localStorage.setItem("registerInviteCode",systemConfig.registerInviteCode); //更新系统邀请码模式
            },
            errorCb : function(result) {

            }

        });
       
        return false;
    });
    
    function validationNumber(value, num) {
        var regu = /^[0-9]+\.?[0-9]*$/;
        if (value != "") {
            if (!regu.test(value)) {
                alert("请输入正确的数字");
                value = valusubstring(0, valulength - 1);
                focus();
            } else {
                if (num == 0) {
                    if (valuindexOf('.') > -1) {
                        value = valusubstring(0, valulength - 1);
                        focus();
                    }
                }
                if (valuindexOf('.') > -1) {
                    if (valusplit('.')[1].length > num) {
                        value = valusubstring(0, valulength - 1);
                        focus();
                    }
                }

            }
        }
    }

})



