var page=0;
var sum=0;
var startDate;
var endDate;
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;
    
    layui.laydate.render({
        elem: '#totalBillDate'
        ,range: "~"
        ,done: function(value, date, endDate){  // choose end
            //console.log("date callBack====>>>"+value); //得到日期生成的值，如：2017-08-18
             startDate = value.split("~")[0];
             endDate = value.split("~")[1];

        }
        ,max: 0
    });

    layer.ready(function(){
    	gettotal();
    	
      });
   

    $(".search_bill").on("click",function(){
    	gettotal();
//    	layui.layer.alert("搜索完成");
    });
    
    function gettotal(){
    	$.ajax({
			type:'POST',
			url:request('/console/systemTotalBill'),
			data:{
				userId:$(".userId").val(),
				startDate:startDate,
				endDate:endDate
			},
			dataType:'json',
			async:false,
			success:function(result){
				if(result.code==1){
					$("#totalRecharge").html(result.data.totalRecharge);
					$("#totalCash").html(result.data.totalCash);
					$("#totalBalance").html(result.data.totalBalance);
					$("#wxTotalRecharge").html(result.data.wxTotalRecharge);
					$("#aliTotalRecharge").html(result.data.aliTotalRecharge);
					$("#sysTotalRecharge").html(result.data.sysTotalRecharge);
					$("#sysTotalReduce").html(result.data.sysTotalReduce);
					$("#totalSendRedPacket").html(result.data.totalSendRedPacket);
					$("#totalGetRedPacket").html(result.data.totalGetRedPacket);
					$("#totalBackRedPacket").html(result.data.totalBackRedPacket);
					$("#totalTransferMoney").html(result.data.totalTransferMoney);
					$("#totalGetTransferMoney").html(result.data.totalGetTransferMoney);
					$("#totalBackTransferMoney").html(result.data.totalBackTransferMoney);
					$("#totalCodePay").html(result.data.totalCodePay);
					$("#totalGetCodePay").html(result.data.totalGetCodePay);
					$("#totalQRCodePay").html(result.data.totalQRCodePay);
					$("#totalGetQRCodePay").html(result.data.totalGetQRCodePay);
					$("#totalVipRecharge").html(result.data.totalVipRecharge);
					$("#totalVipRechargeProfit").html(result.data.totalVipRechargeProfit);
				}

			}
		})
    }


});

var appRecharge={

    // 删除账单记录

    btn_back:function(){
        $("#redEnvelope").show();
        $("#receiveWater").hide();

    }

}