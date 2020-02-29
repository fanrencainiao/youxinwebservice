layui.use(['form'], function() {
    var form = layui.form;
    
    // checkLogin();
    //提交
    form.on('submit(sysUser_login)', function(obj) {
        // obj.field.verkey = codeKey;
        
        obj.field.password = $.md5(obj.field.password); 
        console.log("登录密码："+obj.field.password);

        layer.load(1);

        $.post("/console/login",obj.field, function(data) {
            if (data.code == 1) {
                layer.msg("登录成功",{icon: 1});
                console.log("Login data:"+JSON.stringify(data))
//                   localStorage.setItem("access_Token",data.data.access_Token);
                   localStorage.setItem("role",data.data.role);
//                   localStorage.setItem("account",data.data.account);
//                   localStorage.setItem("adminId",data.data.adminId);
//                   localStorage.setItem("apiKey",data.data.apiKey);
                   localStorage.setItem("id",data.data.id);
                   localStorage.setItem("name",data.data.name);
//                   localStorage.setItem("registerInviteCode",data.data.registerInviteCode); //系统邀请码模式

                setTimeout(function() {
                    location.replace("index.html");
                }, 1000);
                
            } else {
                layer.closeAll('loading');
                //layer.msg(data.resultMsg,{icon: 2});
                layer.msg(data.msg,{icon: 2});
                
            }
        }, "json");

        return false; //阻止表单跳转。如果需要表单跳转，去掉这段即可。

    });

    $(".getImgCode").on("click",function(){
    	var userId=$("#name").val();
    	if(userId==""){
    		 layer.msg("请输入用户名");
    		return;
    	} 
    	$(".getImgCode").attr("src","/other/getImgCode?telephone="+userId+"&time="+new Date());
    	
//    	$.ajax({
//			type:'GET',
//			url:'/other/getImgCode',
//			data:{
//				telephone:userId
//			},
//			
//			async:false,
//			success:function(result){
//				$(".getImgCode").attr("src",window.URL.createObjectURL(result));
//				},
//			error:function(result){
//				layer.msg("获取失败");
//				}
//
//			
//		})
    });
    //表单输入效果
    $(".loginBody .input-item").click(function(e){
        e.stopPropagation();
        $(this).addClass("layui-input-focus").find(".layui-input").focus();
    })
    $(".loginBody .layui-form-item .layui-input").focus(function(){
        $(this).parent().addClass("layui-input-focus");
    })
    $(".loginBody .layui-form-item .layui-input").blur(function(){
        $(this).parent().removeClass("layui-input-focus");
        if($(this).val() != ''){
            $(this).parent().addClass("layui-input-active");
        }else{
            $(this).parent().removeClass("layui-input-active");
        }
    })
    
    
});