var page=0;
var sum=0;
var lock=0;
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;


	console.log("页面加载");
    // 红包列表
    var tableIns = table.render({

        elem: '#bankRecord_table'
        ,url:request("/console/getBankList")
        ,id: 'bankRecord_table'
        ,page: true
        ,curr: 0
        ,limit:Common.limit
        ,limits:Common.limits
        ,groups: 7
        ,cols: [[ //表头
            {field: 'userId', title: '用户Id',sort: true,width:120}
            ,{field: 'bankCard', title: '银行卡',sort: true,width:180}
            ,{field: 'name', title: '户主',sort: true, width:80}
            ,{field: 'totalFee', title: '总金额',sort: true, width:80}
             ,{field: 'fee', title: '手续费',sort: true, width:120}
              ,{field: 'realFee', title: '真实金额',sort: true, width:120}
            ,{field: 'status', title: '状态',sort: true, width:70, templet : function (d) {
            		console.log(d);
					var statusMsg;
            		(d.status == 0 ? statusMsg="创建" : (d.status == 1) ? statusMsg = "完成"  : "")
            	
					return statusMsg;
                }}
            ,{field: 'payTime',title:'提现时间',width:195,templet: function(d){
                    return UI.getLocalTime(d.payTime)=="NaN-NaN-NaN NaN:NaN:NaN"?"":UI.getLocalTime(d.payTime);
                }}
             ,{field: 'createTime',title:'创建时间',width:195,templet: function(d){
                    return UI.getLocalTime(d.createTime);
                }}
            ,{fixed: 'right', width: 250,title:"操作", align:'left', toolbar: '#bankListBar'}
        ]]
        ,done:function(res, curr, count){
            if(count==0&&lock==1){
                // layui.layer.alert("暂无数据",{yes:function(){
                //   renderTable();
                //   layui.layer.closeAll();
                // }});
                layer.msg("暂无数据",{"icon":2});
                renderTable();
              }
              lock=0; 
           for(var i in res.data){	
           		var item = res.data[i]
           		 if(item.status==1){
           		 	 $('tr[data-index=' + i + ']')[0].style.color="red";
           		 }
           }

        }
    });

    // 列表操作
    table.on('tool(bankRecord_table)', function(obj){
        var layEvent = obj.event,
            data = obj.data;
        console.log(data);
        if(layEvent === 'overSendBank'){// 完成转账
           overSendBank(data);
        }
         if(layEvent === 'userBill'){// 完成转账
          	localStorage.setItem("currClickUser", data.userId);
         	layer.open({
         	  title : "",
    			  type: 2,
    			  skin: 'layui-layer-rim', //加上边框
    			  area: ['750px', '500px'], //宽高
    			  content: 'userBill.html'
    			  ,success: function(index, layero){

    			  }

    			});
        }
    });
    
    table.on('sort(bankRecord_table)', function(obj) {
    console.log(this);
	    table.reload('bankRecord_table', {
	       
	         where: {
                userId : $("#userId").val()  //搜索的关键字
            },
            page: {
                curr: 1 //重新从第 1 页开始
            }
	    })
	});

    //首页搜索
    $(".search_live").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();
        table.reload("bankRecord_table",{
            where: {
                userId : $("#userId").val()  //搜索的关键字
            },
            page: {
                curr: 1 //重新从第 1 页开始
            }
        })
        lock=1;
        $("#userId").val("");
    });

});

//重新渲染表单
function overSendBank(data){
	
		if(data.status==1){
			return ;
		}
		$.ajax({
			url:request('/console/updateStatus'),
			data:{
				id:data.id,
				status:1
			},
			dataType:'json',
			async:false,
			success:function(result){
				if(result.code==1){
				
						layer.alert("修改成功");
                      
                        renderTable();

				}else{
					if(typeof(result.data) == "undefined"){
						layer.alert(result.msg);
					}
					layer.alert(result.data.msg);
				}

			},
			error:function(result){
				if(result.code==0){
					layer.alert(result.msg);
				}
			}
		})

 }
 
    //重新渲染表单
function renderTable(){
  layui.use('table', function(){
   var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
   table.reload("bankRecord_table",{
        page: {
            curr: 1 //重新从第 1 页开始
        },
        where: {
//            onlinestate:$("#status").val(),// 在线状态
//            keyWorld : $(".nickName").val()  //搜索的关键字
        }
    })
  });
 }
