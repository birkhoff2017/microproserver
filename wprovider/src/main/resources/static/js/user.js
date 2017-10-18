var openid = "";
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
var code = Request['code'];
$(function () {
    $.ajax({
        url: "cloudAccount/userInfo",
        type: "POST",
        data: {"code": code},
        dataType: "json",
        success: function (result) {

            if (null != result) {
                if (result.code.toString() == "0" & result.msg.toString() == "成功") {
                    var user_info = result.data;
                    openid = user_info.openid;
                    $('.userinfo-avatar').prop("src", user_info.headimgurl);
                    $('.userinfo-name').text(user_info.nickname);
                    $('.meWallet01').text('账户余额: ' + user_info.usablemoney + '元');
                }
            }
        }
    })
})

$('.orderingsum').click(function () {
    location.href = "deposit.html?openid=" + openid;
}),
    $('.orderingrent').click(function () {
        location.href = "rent.html?openid=" + openid;
    }),
    $('.orderingrecord').click(function () {
        location.href = "depositrecord.html?openid=" + openid;
    }),
    $('.orderinghelp').click(function () {
        location.href = "help.html?";
    })
