function GetRequest() {
	var url = location.search; //获取url中"?"符后的字串
	var theRequest = new Object();
	if(url.indexOf("?") != -1) {
		var str = url.substr(1);
		strs = str.split("&");
		for(var i = 0; i < strs.length; i++) {
			theRequest[strs[i].split("=")[0]] = unescape(strs[i].split("=")[1]);
		}
	}
	return theRequest;
}
var Request = new Object();
Request = GetRequest();
var openid = Request['openid'];
var usablemoney = '';
$(function() {
	$.ajax({
		url: "refund/getUserMessage",
		type: "POST",
		data: {
			'openid': openid
		},
		dataType: "json",
		success: function(result) {

			if(null != result) {
				if(result.code.toString() == "0" & result.msg.toString() == "成功") {
					var user = result.data;
					$('#deposit').text(user.deposit + '元') //押金
					$('#usablemoney').text(user.usablemoney + '元');
					usablemoney = user.usablemoney;
					if(user.usablemoney == 0) {
						$('#refund_div').css("background", "#d6d6d6");
						$('#refund_div').prop('disabled', "true");
					}
					$('#refund').text(user.refund + '元');
				}
			}
		},
	})
});
function look_rent() {
	location.href = "rent.html?openid=" + openid;
};

function go_refund() {

    if(usablemoney == 0) {
        layer.open({
            content: '您的账户余额不足不能提现!',
            btn: ['确定']
        });
    } else{
        layer.open({
            content: '提现到帐时间为0-5个工作日。不提现押金余额，下次使用更快捷。是否继续提现押金余额?',
            btn: ['确定', '取消']
            ,yes: function(index){
                $.ajax({
                    url: "refund/doRefund",
                    type: "POST",
                    data: {
                        'openid': openid
                    },
                    dataType: "json",
                    success: function(result) {
						if(result.code != "0") {
							layer.open({
								content: '申请提现失败!',
								btn: ['确定']
							});
						}else{
						    layer.open({
                                content: '申请提现成功!',
                                btn: ['确定'],
                                    yes:function(){
                                        parent.location.reload();
                                    }
                            });
						}
                    }
                })
            }
        });
    }
}