function GetRequest() {
    var url = location.search; //获取url中"?"符后的字串
    var theRequest = new Object();
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        strs = str.split("&");
        for (var i = 0; i < strs.length; i++) {
            theRequest[strs[i].split("=")[0]] = unescape(strs[i].split("=")[1]);
        }
    }
    return theRequest;
}
var Request = new Object();
Request = GetRequest();
var sid = Request['sid'];
var session = Request['session'];
var pastDate = Request['pastDate'];

$(function () {
        var d = new Date();
        if (pastDate==undefined || (d.getTime()-pastDate) < 180*1000){
            $.ajax({
                type: "post",
                url: "http://www.duxinyuan.top/machineinfo/getMachineInfo",
                data: {
                    sid: sid,
                    session: session
                },
                dataType: "json",
                success: function (data) {
                    var adisabled = false,
                        cdisabled = false;
                    if (data.data.cable_type[2] <= 0) {
                        adisabled = true;
                    }
                    if (data.data.cable_type[3] <= 0) {
                        cdisabled = true;
                    }
                    $('.footertexttwo').html(data.data.fee_strategy);
                    if (adisabled) {
                        $('.button-right').attr("disabled", true);
                        $(".button-right").css("background", "#cccccc");
                    }
                    if (cdisabled) {
                        $('.button-left').attr("disabled", true);
                        $(".button-left").css("background", "#cccccc");
                    }
                }
            })
        }else {
            alert("请使用支付宝扫一扫功能，扫描设备上的二维码。");
            AlipayJSBridge.call('popWindow');
        }
})
$('.button-right').click(function () {
    $.ajax({
        type: "post",
        url: "http://www.duxinyuan.top/creditcreate/createOrder",
        data: {
            sid: sid,
            session: session,
            cable_type: "2"
        },
        dataType: "JSON",
        success: function (data) {
            if (data.msg == 'success'){
                location.href = data.url;
            }
        }
    })
})
$('.button-left').click(function () {
    $.ajax({
        type: "post",
        url: "http://www.duxinyuan.top/creditcreate/createOrder",
        data: {
            sid: sid,
            session: session,
            cable_type: "3"
        },
        dataType: "JSON",
        success: function (data) {
            if (data.msg == 'success'){
                location.href = data.url;
            }
        }
    })
})