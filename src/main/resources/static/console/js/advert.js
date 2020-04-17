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
	 


	  //用户列表
    var baseTable = table.render({
      elem: '#body_list'
      ,toolbar: '#toolbarConfigs'
      ,url:request("/console/adList")
      ,id: 'body_list'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cellMinWidth: 100
      ,height: 'full-200'
      ,cols: [[ //表头
           {type:'checkbox',fixed:'left',style: "height:110px;"}// 多选
          ,{field: 'id', title: 'ID',sort:'true',align: "center", width:100}
          ,{field: 'img', title: '图片',sort:'true',width:110,align: "center",templet: function(d){
              // console.log("log    :"+JSON.stringify(d.loginLog));
        		if(d.img==undefined||d.img==""){
        			return "";
        		}else{
        			return '<img src="'+d.img+'" style="height:100px"/>';
        		}

        }}
          ,{field: 'title', title: '标题',sort:'true', width:100}
          ,{field: 'content', title: '内容',sort:'true', width:100}
          ,{field: 'targetUrl', title: '跳转地址',sort:'true', width:100}
          ,{field: 'des', title: '备注',sort:'true', width:100}
          ,{field: 'type', title: '类型',sort:'true', width:100}
          ,{field: 'state',title:'状态',width:100,templet: function(d){
        	  if(d.state==1)
        		  return "生效";
        	  else
        		  return "未生效";
          }}
         
          ,{field: 'updateTime',title:'修改时间',width:195,templet: function(d){
              return UI.getLocalTime(d.updateTime);
          }}
          ,{field: 'createTime',title:'创建时间',width:195,templet: function(d){
              return UI.getLocalTime(d.createTime);
          }}
          ,{fixed: 'right', width: 150,title:"操作", align:'left',style: "height:110px;", toolbar: '#baseListBar'}
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
    	if( !Number.isInteger(parseInt($("#type").val()))){
			layui.layer.alert("type应为数字");
			return;
		}
		
	
		var myform = new FormData();

        myform.append('cid', $("#id").val());
        myform.append('title', $("#title").val());
        myform.append('content',$("#content").val());
        myform.append('targetUrl',$("#targetUrl").val());
        myform.append('des',$("#des").val());
        myform.append('state',$("#state").val());
        myform.append('type',$("#type").val());
        myform.append('file', $("#img")[0].files[0]);
        if($("#imgvo").val()!=""){
        	myform.append('img', $("#imgvo").val());
		}
		$.ajax({
			url:'/console/saveAd',
			type:'post',
			contentType: false,
			data:myform,
			processData: false,
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
     
      if(layEvent === 'delete'){//删除
    	  Base.checkDeletesImpl(data.id,1);
      }else if(layEvent === 'update'){// 修改用户    
    	  Base.update(obj.data,obj.data.id);
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
	


	//  新增
	add:function(){
		$("#baseList").hide();
		$("#addConfig").show();
		$("#id").val("");
        $("#type").val("");
        $("#img").val("");
        $("#imgvo").val("");
        $("#state").val("");
        $("#des").val("");
        $("#title").val("");
        $("#content").val("");
        $("#targetUrl").val("");
     
        // 重新渲染
        layui.form.render();
		$("#addConfigTitle").empty();
		$("#addConfigTitle").append("新增用户");
	},
	
	// 修改用户
	update:function(data,id,layedit,lbd){
		myFn.invoke({
			url:request('/console/getAd'),
			data:{
				id:id
			},
			success:function(result){
                
				if(result.data!=null){
					$("#id").val(result.data.id),
					$("#title").val(result.data.title),
					$("#content").val(result.data.content),
					$("#des").val(result.data.des),
					$("#img").val(""),
					$("#imgvo").val(result.data.img),
					$("#targetUrl").val(result.data.targetUrl),
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
                    url:request('/console/delAd'),
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