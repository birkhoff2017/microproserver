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
var session = Request['session'];

$(function() {
	$.ajax({
		type: "post",
		url: urlObject.getOrderList,
		data: {
			session: session,
		},
		dataType: "JSON",
		success: function(data) {
			if (data.data.orders.length > 0) {
                    for (var i = 0; i < data.data.orders.length; i++) {
                    	var orderid = data.data.orders[i].orderid;
						$('.number').html(orderid);
						var borrowname = data.data.orders[i].borrow_name;
						$('.borrow_name').html(borrowname);
						var borrowtime = data.data.orders[i].borrow_time;
						$('.borrow_time').html(borrowtime);
						var feestrategy = data.data.orders[i].fee_str;
						$('.fee_strategy').html(feestrategy);
						var returnname = data.data.orders[i].return_name;
						$('.return_name').html(returnname);
						var returntime = data.data.orders[i].return_time;
						$('.return_time').html(returntime);
						var lasttime = data.data.orders[i].last_time;
						$('.last_time').html(lasttime);
						var usefee = data.data.orders[i].use_fee;
						$('.use_fee').html(usefee);
	
                        if ("12568".indexOf(data.data.orders[i].status) > -1) {
                           $('._head_price1').css('display','none');
                           $('.doneview').addClass('doneview1');
                        } else {
                        	$('._head_price').css('display','none');
                            $('.doneview').addClass('doneview');
                        }
                       
                    }
                } else {
                	$('.doneview').css('display','none');
				}
		}
	})
})
$('.contraction').click(function(){
	 $('.middle').toggle();
	 $(".showhide").toggle();
})