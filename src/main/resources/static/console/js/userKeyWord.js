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
      ,url:request("/console/userKeyWordList")
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
          ,{field: 'accid', title: '用户accid',sort:'true', width:200}
          ,{field: 'msgid', title: '消息记录id',sort:'true', width:200}
          ,{field: 'keyWord', title: '敏感词',sort:'true', width:300}
         
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
 
  //列表操作
  table.on('tool(body_list)', function(obj){
      var layEvent = obj.event,
            data = obj.data;
     
      if(layEvent === 'delete'){//删除
    	  Base.checkDeletesImpl(data.id,1);
      }else if(layEvent === 'getmsg'){
    	  
    	  localStorage.setItem("teamid", "");
    	  localStorage.setItem("fromid", data.accid);
    	  layer.open({
      	   	  title : "",
    		  type: 2,
    		  skin: 'layui-layer-rim', //加上边框
    		  area: ['750px', '500px'], //宽高
    		  content: 'messageRecord1.html'
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
            	accid : $(".accid").val(), //状态
                keyWord : $(".keyWord").val()
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
        	accid : $(".accid").val(), //状态
            keyWord : $(".keyWord").val()
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
                    url:request('/console/delUserKeyWord'),
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