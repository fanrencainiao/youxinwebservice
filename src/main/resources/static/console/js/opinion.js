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
      ,url:request("/console/opinionList")
      ,id: 'body_list'
      ,page: true
      ,curr: 0
      ,limit:Common.limit
      ,limits:Common.limits
      ,groups: 7
      ,cols: [[ //表头
           {type:'checkbox',fixed:'left'}// 多选
          ,{field: 'id', title: 'ID',sort:'true', width:100}
          ,{field: 'userId', title: '反馈人id',sort:'true', width:100}
          ,{field: 'opinion', title: '意见',sort:'true', width:100}
          ,{field: 'createTime',title:'提交时间',width:195,templet: function(d){
              return UI.getLocalTime(d.createTime);
          }}
          ,{field: 'updateTime',title:'处理时间',width:195,templet: function(d){
              return UI.getLocalTime(d.updateTime);
          }}
          ,{field: 'state',title:'处理状态',width:100,templet: function(d){
              return d.state==1?"处理":"未处理";
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

  //列表操作
  table.on('tool(body_list)', function(obj){
      var layEvent = obj.event,
            data = obj.data;
             
   if(layEvent === 'undelete'){//  处理   
    	  Base.checkDeletesImpl(data.id,1);
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
//                onlinestate:$("#status").val(),// 在线状态
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
//            onlinestate:$("#status").val(),// 在线状态
            keyWorld : $(".nickName").val()  //搜索的关键字
        }
    })
  });
 }

var configTime="";

var Base={
	

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
        layer.confirm('确定处理吗',{icon:3, title:'提示消息',yes:function () {
                myFn.invoke({
                    url:request('/console/overOpinion'),
                    data:{
                    	id:id,
                    	state:1
                    },
                    success:function(result){
                        if(result.code==1){
                            layer.msg("成功",{"icon":1});
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