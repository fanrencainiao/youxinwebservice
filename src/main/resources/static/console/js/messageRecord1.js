var page=0;
var lock=0;
var ids = new Array();
var name;
var id;

var currentPageIndex;// 当前页码数
var currentCount;// 当前总数
var startTime;
var endTime;
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;

    console.log('userTeam  Test  tid'+localStorage.getItem("teamid") );
  
    
	  //列表
    var baseTable = table.render({
      elem: '#body_list'
      ,toolbar: '#toolbarConfigs'
      ,url:"/console/getMessageReceiveList?convType=TEAM&to="+localStorage.getItem("teamid")
      ,id: 'body_list'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           //{type:'checkbox',fixed:'left'}// 多选
          //,
          {field: 'msgidServer', title: 'ID',sort:'true', width:150}
//        ,{field: 'eventType', title: '消息类型',sort: true, width:90,templet : function (d) {
//          if(d.eventType=="1"){
//          	return "会话类型";
//          }
//          else if(d.eventType=="2"){
//          	return "登录事件";
//          }
//          else  if(d.eventType=="3"){
//          	return "登出/离线事件";
//          }
//          else  if(d.eventType=="7"){
//          	return "单聊消息撤回";
//          }
//          else  if(d.eventType=="8"){
//          	return "群聊消息撤回";
//          }
//          else if(d.eventType=="30"){
//          	return "点对点消息已读回执";
//          }else{
//          	return d.eventType;
//          }
//          	
//        }}
        ,{field: 'convType', title: '消息具体类型',sort:'true', width:70,templet : function (d) {
            if(d.convType=="PERSON"){
            	return "个人";
            }
            else if(d.convType=="TEAM"){
            	return "群聊";
            }
            else  if(d.convType=="CUSTOM_PERSON"){
            	return "点对点自定义系统通知及内置好友系统通知";
            }
            else  if(d.convType=="CUSTOM_TEAM"){
            	return "群聊自定义系统通知及内置群聊系统通知";
            }else{
            	return d.convType;
            }
            	
          }}
        ,{field: 'to', title: '接收方id(群或用户ID)',sort:'true', width:125}
        ,{field: 'fromAccount', title: '发送者账号',sort:'true', width:140}
        ,{field: 'icon', title: '头像',sort:'true', width:110,align: "center",templet: function(d){
            // console.log("log    :"+JSON.stringify(d.loginLog));
    		if(d.icon==undefined||d.icon==""){
    			return "";
    		}else{
    			return '<img src="'+d.icon+'" style="height:100px"/>';
    		}
    		
        }}
        ,{field: 'body', title: '消息内容',sort:'true', width:255,style: "height:111px;",templet : function (d) {
      	  if(d.msgType=="PICTURE"){
      		  return '<img src="'+JSON.parse(d.attach).url+'" style="height:100px"/>';
      	  }
      	 if(d.msgType=="VIDEO"){
      		  return '<video src="'+JSON.parse(d.attach).url+'" style="height:100px" controls="controls"> your browser does not support the video tag</video>';
      	  	}
      	  return (d.body==undefined||d.body==""||!d.body)?'':d.body;
        }}
        ,{field: 'fromNick', title: '发送方昵称',sort:'true', width:80}
        
        ,{field: 'attach', title: '附加消息',sort:'true', width:100}
        ,{field: 'msgType', title: '消息内容类型',sort:'true', width:120,templet : function (d) {
            if(d.msgType=="TEXT"){
            	return "文本消息";
            }
            else if(d.msgType=="PICTURE"){
            	return "图片消息";
            }
            else  if(d.msgType=="AUDIO"){
            	return "语音消息";
            }
            else  if(d.msgType=="VIDEO"){
            	return "视频消息";
            }else  if(d.msgType=="LOCATION"){
            	return "地理位置消息";
            }else  if(d.msgType=="NOTIFICATION"){
            	return "群通知消息";
            }else  if(d.msgType=="FILE"){
            	return "文件消息";
            }else  if(d.msgType=="TIPS"){
            	return "提示消息";
            }else  if(d.msgType=="CUSTOM"){
            	return "自定义消息";
            }else  if(d.msgType=="FRIEND_ADD"){
            	return "对方 请求/已经 添加为好友";
            }else  if(d.msgType=="FRIEND_DELETE"){
            	return "被对方删除好友";
            }else  if(d.msgType=="CUSTOM_P2P_MSG"){
            	return "点对点自定义系统通知";
            }else  if(d.msgType=="TEAM_APPLY"){
            	return "申请入群";
            }else  if(d.msgType=="TEAM_APPLY_REJECT"){
            	return "拒绝入群申请";
            }else  if(d.msgType=="TEAM_INVITE"){
            	return "邀请进群";
            }else  if(d.msgType=="TEAM_INVITE_REJECT"){
            	return "拒绝邀请";
            }else  if(d.msgType=="CUSTOM_TEAM_MSG"){
            	return "群组自定义系统通知";
            }else{
            	return d.msgType;
            }
            	
          }}
     
        ,{field: 'msgTimestamp', title: '消息发送时间',sort: true, width:160,templet : function (d) {
            return UI.getLocalTime(d.msgTimestamp/1000);
        }}
       
        ,{field: 'msgidClient', title: '端生成的消息id',sort:'true', width:120}
        ,{field: 'resendFlag', title: '重发标记',sort:'true', width:50}
        ,{field: 'customSafeFlag', title: '自定义系统通知消息是否存离线',sort:'true', width:50}
        ,{field: 'customApnsText', title: '自定义系统通知消息推送文本',sort:'true', width:100}
        ,{field: 'tMembers', title: '当前群成员accid列表',sort:'true', width:255}
        ,{field: 'ext', title: '消息扩展字段',sort:'true', width:100}
        ,{field: 'antispam', title: '标识是否被反垃圾',sort:'true', width:50}
        ,{field: 'yidunRes', title: '易盾反垃圾的原始处理细节',sort:'true', width:100}
        ,{field: 'blacklist', title: '标识点对点消息是否黑名单',sort:'true', width:50}
        ,{field: 'fromClientType', title: '发送客户端类型',sort:'true', width:80}
        ,{field: 'fromDeviceId', title: '发送设备id',sort:'true', width:80}
        ,{field: 'ip', title: '客户端IP地址',sort:'true', width:100}
        ,{field: 'port', title: '客户端端口号',sort:'true', width:50}
      ]]
		  ,done:function(res, curr, count){
             if(count==0&&lock==1){
               layer.msg("暂无数据",{"icon":2});
               renderTable();
             }
//             $("table").css("width", "100%");    
             var tableWidth = layui.$('.layui-table-header').width();
             lock=0;
              $("#baseList").show();
              $("#base_table").show();
              $("#addConfig").hide();
             
              var pageIndex = baseTable.config.page.curr;//获取当前页码
              var resCount = res.count;// 获取table总条数
              currentCount = resCount;
              currentPageIndex = pageIndex;
		  }

  });

    layui.laydate.render({
        elem: '#msgTimestampDate'
        ,range: "~"
        ,done: function(value, date, endDate){  // choose end
            //console.log("date callBack====>>>"+value); //得到日期生成的值，如：2017-08-18
             startTime = value.split("~")[0];
             endTime = value.split("~")[1];

        }
        ,max: 0
    });
    //搜索用户
    $(".search_base").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();

        table.reload("body_list",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
            	fromAccount:$(".fromAccount").val(),// 
            	startTime :startTime,
            	endTime : endTime
            }
        })
        lock=1;
    });
    //删除一月之前的消息记录
    $(".btn_delOneMonth").on("click",function(){
		$.ajax({
			type:'POST',
			url:'/console/delMessageReceive',
			data:{},
			dataType:'json',
			async:false,
			success:function(result){
				if(result.code==1){
					layer.msg("删除成功",{"icon":2});
				}

			}
		})
    	renderTable();
    });
    
})


   //重新渲染表单
function renderTable(){
  layui.use('table', function(){
   var table = layui.table;//高版本建议把括号去掉，有的低版本，需要加()
   // table.reload("user_list");
   table.reload("body_list",{
        page: {
            curr: 1 //重新从第 1 页开始
        },
        where: {
        	fromAccount:$(".fromAccount").val(),// 
        	to : $(".to").val(),  //
        	convType : $("#convType").val(),  //
        	startTime :startTime,
        	endTime : endTime
        }
    })
  });
 }
