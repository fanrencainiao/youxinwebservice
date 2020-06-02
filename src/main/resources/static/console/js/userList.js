var page=0;
var lock=0;
var ids = new Array();
var name;
var id;

var currentPageIndex;// 当前页码数
var currentCount;// 当前总数
layui.use(['form','layer','laydate','table','laytpl'],function(){
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        laydate = layui.laydate,
        laytpl = layui.laytpl,
        table = layui.table;


    //根据系统邀请码类型移除对应邀请码的按钮
//    if(localStorage.getItem("registerInviteCode")!=2){
//      $(".create_populer_inviteCode").remove();
//    }else{
//     $(".btn_create_register_InviteCode").remove();
//    }

	  //用户列表
    var baseTable = table.render({
      elem: '#body_list'
      ,toolbar: '#toolbarConfigs'
      ,url:request("/console/userList")
      ,id: 'body_list'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           //{type:'checkbox',fixed:'left'}// 多选
          //,
          {field: 'id', title: 'ID',sort:'true', width:120}
          ,{field: 'accid', title: 'accid',sort:'true', width:255}
          ,{field: 'account', title: '账号',sort:'true', width:180}
          ,{field: 'name', title: '昵称',sort:'true', width:120}
          ,{field: 'icon', title: '头像',sort:'true', width:110,align: "center",templet: function(d){
              // console.log("log    :"+JSON.stringify(d.loginLog));
      		if(d.icon==undefined||d.icon==""){
      			return "";
      		}else{
      			return '<img src="'+d.icon+'" style="height:100px"/>';
      		}

          }}
        
          ,{field: 'token', title: '云信token',sort:'true', width:255}
          ,{field: 'mobile', title: '手机号',sort:'true', width:140}
          ,{field: 'balance', title: '余额',sort:'true', width:80}
          ,{field: 'totalRecharge', title: '充值总金额',sort:'true', width:80}
          ,{field: 'totalConsume', title: '消费总金额',sort:'true', width:80}
          ,{field: 'createTime', title: '注册时间',sort: true, width:200,templet : function (d) {
              return d.createTime?UI.getLocalTime(d.createTime):"";
          }}
          ,{fixed: 'right', title:"操作", align:'left',width:200,style: "height:111px;", toolbar: '#baseListBar'}
        ]]
		  ,done:function(res, curr, count){
               if(count==0&&lock==1){
                 layer.msg("暂无数据",{"icon":2});
                 renderTable();
               }
//               $("table").css("width", "100%");    
               var tableWidth = layui.$('.layui-table-header').width();
               lock=0;
                $("#baseList").show();
                $("#base_table").show();
                $("#addConfig").hide();
               
                var pageIndex = baseTable.config.page.curr;//获取当前页码
                var resCount = res.count;// 获取table总条数
                currentCount = resCount;
                currentPageIndex = pageIndex;
            
                console.log(window.screen.width);
                
		  }

    });
 /*   $(".nickName").val('');*/
    window.onresize = function(){
    	

    }
  //列表操作
  table.on('tool(body_list)', function(obj){
      var layEvent = obj.event,
            data = obj.data;
             
      if(layEvent === 'delete'){//禁用    
    	  Base.disableUser(data.id,data.accid,-1);
      }else if(layEvent === 'undelete'){// 解禁    
    	  Base.disableUser(data.id,data.accid,1);
      }else if(layEvent === 'inTeam'){// 所在群
    	   	localStorage.setItem("currAccid", data.accid);
    	   	layer.open({
    	   	  title : "",
			  type: 2,
			  skin: 'layui-layer-rim', //加上边框
			  area: ['850px', '600px'], //宽高
			  content: 'userTeam.html'
			  ,success: function(index, layero){

			  }

			});
      }else if(layEvent === 'update'){// 修改用户    
    	  Base.update(obj.data,obj.data.id);
      }else if(layEvent==='recharge'){ //后台充值


		  layer.prompt({title: '请输入充值金额', formType: 0,value: '50'}, function(money, index){
            // 充值金额（正整数）的正则校验
			
				Common.invoke({
				      path : request('/console/recharge'),
				      data : {
				      	money:money,
				      	userId:data.id
				      },
				      successMsg : "成功",
				      errorMsg :  "失败，请稍后重试",
				      successCb : function(result) {

				        var data = result.data; //DataSort(result.data);
				      	layer.close(index); //关闭弹框
				      	renderTable();

				      },
				      errorCb : function(result) {

				      }
			    });

		  });

  }else if(layEvent === 'sendMsg'){// sendMsg
      layer.prompt({title: '请输入消息内容', formType: 2,value: '账户异常'}, function(contents, index){
			$.ajax({
				url:request('/console/sendMsg'),
				data:{
					text:contents,
					userId:data.id
				},
				dataType:'json',
				async:false,
				success:function(result){
					if(result.code==1){
						layer.alert("发送成功");
					}else{
						if(typeof(result.data) == "undefined"){
							layer.alert(result.msg);
						}
						layer.alert(result.data.resultMsg);
					}
	
				},
				error:function(result){
					if(result.code==0){
						layer.alert(result.msg);
					}
				}
			});

		  });
  }else if(layEvent==='bill'){ //用户账单

   	localStorage.setItem("currClickUser", data.id);
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



    //搜索用户
    $(".search_base").on("click",function(){
        // 关闭超出宽度的弹窗
        $(".layui-layer-content").remove();

        table.reload("body_list",{
            page: {
                curr: 1 //重新从第 1 页开始
            },
            where: {
            	online:$("#online").val(),// 在线状态
                keyWorld : $(".nickName").val()  //搜索的关键字
            }
        })
        lock=1;
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
        	online:$("#online").val(),// 在线状态
            keyWorld : $(".nickName").val()  //搜索的关键字
        }
    })
  });
 }

var configTime="";

var Base={
	list:function(e,pageSize){
		var html="";
		if(e==undefined){
			e=0;
		}else if(pageSize==undefined){
			pageSize=10;
		}
		$.ajax({
			type:'POST',
			url:request('/console/userList'),
			data:{
				pageIndex:(e==0?"0":e-1),
				pageSize:pageSize
			},
			dataType:'json',
			async:false,
			success:function(result){
				if(result.data.pageData.length!=0){
					console.log(result.data.allPageCount);
					$("#pageCount").val(result.data.allPageCount);
					for(var i=0;i<result.data.pageData.length;i++){
							if(result.data.pageData[i].configTime!=0){
								configTime=UI.getLocalTime(result.data.pageData[i].configTime);
							}
					
					}

			/*		$("#userList_table").empty();
					$("#userList_table").append(html);*/
					$("#baseList").show();
					$("#base_table").show();
					$("#addConfig").hide();
				}

			}
		})
	},


	//  新增用户
	add:function(){
		$("#baseList").hide();
		$("#addConfig").show();
        $("#userId").val("");
        $("#accid").val("");
        $("#name").val("");
        $("#mobile").val("");
        $("#password").val("");
        $("#birth").val("");
        $("#gender").val("");
        
        // 重新渲染
        layui.form.render();
		$("#addConfigTitle").empty();
		$("#addConfigTitle").append("新增用户");
	},
	// 提交新增用户
	commit_add:function(){
		
		if($("#name").val()==""){
			layui.layer.alert("请输入昵称");
			return;
		}
		if($("#mobile").val()==""){
			layui.layer.alert("请输入手机号");
			return;
		}else{
			 var patrn = /^[0-9]*$/;
             if (patrn.exec($("#mobile").val()) == null || $("#telephone").val() == "") {
                 layui.layer.alert("请使用手机号注册");
                 return;
             }
		}
		if($("#userId").val()==""){
			if($("#password").val()==""){
				layui.layer.alert("请输入密码");
				return;
			}
		}
		
		if($("#birth").val()==""){
			layui.layer.alert("请输入生日");
			return;
		}
		if($("#gender").val()==""){
			layui.layer.alert("请输入性别");
			return;
		}
		
		$.ajax({
			url:request('/console/updateUser'),
			data:{
				id:$("#userId").val(),
				accid:$("#accid").val(),
				name:$("#name").val(),
				mobile:$("#mobile").val(),
				password:$("#password").val(),
				birth:$("#birth").val(),
				gender:$("#gender").val(),
				updateUserId:localStorage.getItem("account")
			},
			dataType:'json',
			async:false,
			success:function(result){
				if(result.code==1){
					if($("#userId").val()==""){
						layer.alert("添加成功");
                        $("#baseList").show();
                        $("#addConfig").hide();
                        layui.table.reload("body_list",{
                            page: {
                                curr: 1 //重新从第 1 页开始
                            },
                            where: {

                            }
                        })

					}else{
						layer.alert("修改成功");
                        $("#baseList").show();
                        $("#addConfig").hide();
                        renderTable();
					}

				}else{
					
					layer.alert(result.msg);
				}

			},
			error:function(result){
				if(result.code==0){
					layer.alert(result.msg);
				}
			}
		})
	},
	// 修改用户
	update:function(data,id){
		myFn.invoke({
			url:request('/console/getUpdateUser'),
			data:{
				userId:id
			},
			success:function(result){
                
				if(result.data!=null){
					$("#userId").val(result.data.id),
					$("#accid").val(result.data.accid),
					$("#name").val(result.data.name),
					$("#mobile").val(result.data.mobile),
					$("#password").val(result.data.password),
					$("#birth").val(result.data.birth),
				    $("#gender").val(result.data.gender)
					
				}
				$("#addConfigTitle").empty();
				$("#addConfigTitle").append("修改用户");
				$("#baseList").hide();
				$("#addConfig").show();
                layui.form.render();
               
			}
		});

	},

        // 多选禁用用户
        checkDeletes:function(){
            // 多选操作
            var checkStatus = layui.table.checkStatus('body_list'); //idTest 即为基础参数 id 对应的值
            console.log("新版："+checkStatus.data) //获取选中行的数据
            console.log("新版："+checkStatus.data.length) //获取选中行数量，可作为是否有选中行的条件
            console.log("新版："+checkStatus.isAll ) //表格是否全选
		for (var i = 0; i < checkStatus.data.length; i++){
			ids.push(checkStatus.data[i].accid);
		}
		console.log(id);
		if(0 == checkStatus.data.length){
			layer.msg("请勾选要禁用的行");
			return;
		}
		Base.checkDeletesImpl(ids.join(","),checkStatus.data.length);
	},

    checkDeletesImpl:function(id,checkLength){
        layer.confirm('确定禁用指定用户',{icon:3, title:'提示消息',yes:function () {
                myFn.invoke({
                    url:request('/console/blockUser'),
                    data:{
                    	id:id
                    },
                    success:function(result){
                        if(result.code==1){
                            layer.msg("禁用成功",{"icon":1});
                            ids = [];
                            // renderTable();
                            Common.tableReload(currentCount,currentPageIndex,checkLength,"body_list");
                        }
                    }
                })
            },btn2:function () {
            	ids = [];
            },cancel:function () {
            	ids = [];
            }});
    },
    disableUser:function(id,accid,disableUser){
        layer.confirm('确定禁用指定用户',{icon:3, title:'提示消息',yes:function () {
                myFn.invoke({
                    url:request('/console/blockUser'),
                    data:{
                    	id:id,
                    	accid:accid,
                    	disableUser:disableUser
                    },
                    success:function(result){
                        if(result.code==1){
                            layer.msg("禁用成功",{"icon":1});
                            ids = [];
                            renderTable();
                        }else{
                        	layer.msg(result.msg);
                        }
                    }
                })
            },btn2:function () {
            	ids = [];
            },cancel:function () {
            	ids = [];
            }});
    },

    button_back:function(){

  		$("#baseList").show();
  		$("#base_table").show();
      /*$(".user_btn_div").show();*/
  		$("#addConfig").hide();
    
	}
}