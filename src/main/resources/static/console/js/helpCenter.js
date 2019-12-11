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
        table = layui.table,
		layedit = layui.layedit;
	   /* layedit.set({
	    	  uploadImage: {
	    	    url: 'http://www.baidu.com' //接口url
	    	    ,type: 'post' //默认post
	    	  }
	    	});*/
 	var lbd=layedit.build('content', {
         height: 180,
         uploadImage: {
             url: "http://localhost:9898/console/uploadSdkImage"
         }
     }); //建立编辑器

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
      ,url:request("/console/helpCenterList")
      ,id: 'body_list'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {type:'checkbox',fixed:'left'}// 多选
          ,{field: 'id', title: 'ID',sort:'true', width:100}
          ,{field: 'title', title: '标题',sort:'true', width:100}
          ,{field: 'content', title: '内容',sort:'true', width:100}
          ,{field: 'state',title:'状态',width:100,templet: function(d){
              return d.state==0?"未反馈过":"已反馈";
          }}
          ,{field: 'noUserIds',title:'未解决人',width:100,templet: function(d){
        	  if(d.noUserIds){
        		  return d.noUserIds.join(",");
        	  }
              return "";
          }}
          ,{field: 'overUserIds',title:'未解决人',width:100,templet: function(d){
        	  if(d.overUserIds){
        		  return d.overUserIds.join(",");
        	  }
              return "";
          }}
          ,{field: 'type',title:'类型',width:100,templet: function(d){
        	  if(d.type==1){
        		  return "帮助中心";
        	  }
            return "";
          }}
          ,{field: 'userId', title: '反馈人id集合',sort:'true', width:100}
          ,{field: 'updateTime',title:'修改时间',width:195,templet: function(d){
              return UI.getLocalTime(d.updateTime);
          }}
          ,{field: 'createTime',title:'创建时间',width:195,templet: function(d){
              return UI.getLocalTime(d.createTime);
          }}
          ,{fixed: 'right', width: 400,title:"操作", align:'left', toolbar: '#baseListBar'}
        ]]
		  ,done:function(res, curr, count){
               if(count==0&&lock==1){
                 layer.msg("暂无数据",{"icon":2});
                 renderTable();
               }
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
 /*   $(".nickName").val('');*/
    $(".commit_add").on("click",function(){
		var content=layedit.getContent(lbd);
		
		if($("#title").val()==""){
			layui.layer.alert("请输入标题");
			return;
		}
		if(!content){
			layui.layer.alert("请输入内容");
			return;
		}
		if($("#type").val()==""){
			layui.layer.alert("请选择类型");
			return;
		}
		if($("#state").val()==""){
			layui.layer.alert("请选择状态");
			return;
		}
		
		$.ajax({
			url:request('/console/saveCenterList'),
			data:{
				hcid:$("#id").val(),
				title:$("#title").val(),
				content:content,
				type:$("#type").val(),
				state:$("#state").val()
			},
			dataType:'json',
			async:false,
			success:function(result){
				if(result.code==1){
					if($("#id").val()==""){
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
					if(result.data == null){
						layer.alert(result.msg);
						
					}else{
						layer.alert(result.data.msg);
					}
					
				}

			},
			error:function(result){
				if(result.code==0){
					layer.alert(result.resultMsg);
				}
			}
		})
	});

  //列表操作
  table.on('tool(body_list)', function(obj){
      var layEvent = obj.event,
            data = obj.data;
      console.log(layedit);  
      if(layEvent === 'delete'){//删除
    	  Base.checkDeletesImpl(data.id,1);
      }else if(layEvent === 'update'){// 修改用户    
    	  Base.update(obj.data,obj.data.id,layedit,lbd);
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
//              
                state : $(".state").val(), //状态
                type : $(".type").val(),
                nickName : $(".nickName").val()
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
//            onlinestate:$("#status").val(),// 在线状态
        	  state : $(".state").val(), //状态
              type : $(".type").val(),
              nickName : $(".nickName").val()
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
        $("#id").val("");
        $("#title").val("");
        $("#content").val("");
        $("#state").val("-1");
        $("#type").val("");
     
        // 重新渲染
        layui.form.render();
		$("#addConfigTitle").empty();
		$("#addConfigTitle").append("新增用户");
	},
	/*// 提交新增用户
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
		if($("#password").val()==""){
			layui.layer.alert("请输入密码");
			return;
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
					if(result.data == null){
						layer.alert(result.msg);
					}
					layer.alert(result.data);
				}

			},
			error:function(result){
				if(result.code==0){
					layer.alert(result.msg);
				}
			}
		})
	},*/
	// 修改用户
	update:function(data,id,layedit,lbd){
		myFn.invoke({
			url:request('/console/getCenterList'),
			data:{
				id:id
			},
			success:function(result){
                
				if(result.data!=null){
					$("#id").val(result.data.id),
					$("#title").val(result.data.title),
					$("#content").val(result.data.content),
					layedit.setContent(lbd, result.data.content, false),
					$("#state").val(result.data.state),
					$("#type").val(result.data.type)
				}
				$("#addConfigTitle").empty();
				$("#addConfigTitle").append("修改");
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
			ids.push(checkStatus.data[i].id);
		}
		console.log(id);
		if(0 == checkStatus.data.length){
			layer.msg("请勾选要删除的行");
			return;
		}
		Base.checkDeletesImpl(ids.join(","),checkStatus.data.length);
	},

    checkDeletesImpl:function(id,checkLength){
        layer.confirm('确定删除吗',{icon:3, title:'提示消息',yes:function () {
                myFn.invoke({
                    url:request('/console/delCenterList'),
                    data:{
                    	id:id
                    },
                    success:function(result){
                        if(result.code==1){
                            layer.msg("删除成功",{"icon":1});
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
  

    button_back:function(){

  		$("#baseList").show();
  		$("#base_table").show();
      /*$(".user_btn_div").show();*/
  		$("#addConfig").hide();
    
	}
}