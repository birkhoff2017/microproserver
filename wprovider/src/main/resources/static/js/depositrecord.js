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
var openid = Request['openid'];
$(function () {
    $.ajax({
        type: "post",
        url: "refund/getRefundList",
        data: {
            "openid": openid
        },
        dataType: "JSON",
        success: function (data) {
            if (data.data.refund_logs.length > 0) {
                var obj = data.data.refund_logs;
                var box = "";
                var flag = true;
                $.each(obj, function (n, value) {
                    var refundnum = value.refund;
                    var requesttime = formatDateTime(value.request_time);
                    var refundtime = value.refund_time;
                    if (refundtime != "" && refundtime != null) {
                        refundtime = formatDateTime(refundtime);
                    }
                    var status = value.status;
                    var underway = "<p class='_head_price' style='color: #c00'>提现中</p></div>";
                    var complete = "<p class='_head_price1' style='color: #0c9'>已提现</p></div>";
                    var _head = "<div class='_head'><p class='_head_title'>提现金额:<span class='refund'>" + refundnum + "</span>元</p>";

                    if (status == 1) {
                        if (flag) {
                            _head += underway;
                            var conterbox = "<div class='firstconterbox'><div class ='middle' ><div class = 'middleleft' ><div class = 'bar' ><img class = 'depositImg display_show21'src ='img/tixianstart.png' ></img></div></div><div class ='middleright' ><div class = 'help' ><div class ='weui_cell_bd_p1'>提现申请成功 </div><div class = 'help_img'><img src = 'img/query.png'></img><span class = 'weui_cell_bd_p2' onclick = 'help(this)'>退款帮助 </span></div></div><div class = 'put' ><div class = 'weui_cell_bd_p2' >已将退款申请交至微信</div><div class = 'weui_cell_bd_p2 request_time' >" + requesttime + "</div></div><div class ='pass'><div class ='weui_cell_bd_p'>审核通过 </div><div class = 'weui_cell_bd_p1'><div class ='weui_cell_bd_p2'>您的资金转至微信处理 </div><div class = 'weui_cell_bd_p2 refund_time'>" + refundtime + "</div><div class = 'weui_cell_bd_p2' ></div></div></div><div class ='goto'><div class = 'weui_cell_bd_p'>已到帐</div><div class = 'weui_cell_bd_p1'>微信将退款原路返回到您的支付账户中 </div></div></div></div></div>";
                            var divH = "<div class='display_show divH'><div class ='bottom_textdiv'>提现申请成功 </div><div class ='cost'><div class ='bottom_textdiv1'>金额:<span class ='refund1'>" + refundnum + "</span>元</div><div class ='contraction'><div class ='firstlookup showhide'><img class ='up' src='img/jdtlook.png'></img><span class ='look'>查看 </span></div><div class ='firstpackdown showhide'><img class ='down' src='img/jdtpack.png'></img><span class ='pack'>收起 </span></div></div></div></div>";
                            box += "<div class='navigator' onclick = 'firsttoggle(this)'>" + _head + conterbox + divH + "</div>";
                            flag = false;
                        } else {
                            _head += underway;
                            var conterbox = "<div class='conterbox'><div class ='middle' ><div class = 'middleleft' ><div class = 'bar' ><img class = 'depositImg display_show21'src ='img/tixianstart.png' ></img></div></div><div class ='middleright' ><div class = 'help' ><div class ='weui_cell_bd_p1'>提现申请成功 </div><div class = 'help_img'><img src ='img/query.png'></img><span class = 'weui_cell_bd_p2' onclick = 'help(this)'>退款帮助 </span></div></div><div class = 'put' ><div class = 'weui_cell_bd_p2' >已将退款申请交至微信</div><div class = 'weui_cell_bd_p2 request_time' >" + requesttime + "</div></div><div class ='pass'><div class ='weui_cell_bd_p'>审核通过 </div><div class = 'weui_cell_bd_p1'><div class ='weui_cell_bd_p2'>您的资金转至微信处理 </div><div class = 'weui_cell_bd_p2 refund_time'>" + refundtime + "</div><div class = 'weui_cell_bd_p2' ></div></div></div><div class ='goto'><div class = 'weui_cell_bd_p'>已到帐</div><div class = 'weui_cell_bd_p1'>微信将退款原路返回到您的支付账户中 </div></div></div></div></div>";
                            var divH = "<div class='display_show divH'><div class ='bottom_textdiv'>提现申请成功 </div><div class ='cost'><div class ='bottom_textdiv1'>金额:<span class ='refund1'>" + refundnum + "</span>元</div><div class ='contraction'><div class ='lookup showhide'><img class ='up' src='img/jdtlook.png'></img><span class ='look'>查看 </span></div><div class ='packdown showhide'><img class ='down' src='img/jdtpack.png'></img><span class ='pack'>收起 </span></div></div></div></div>";
                            box += "<div class='navigator' onclick = 'one(this)'>" + _head + conterbox + divH + "</div>";
                        }
                    } else if (status == 2) {
                        if (flag) {
                            _head += complete;
                            var conterbox = "<div class='firstconterbox'><div class ='middle' ><div class = 'middleleft' ><div class = 'bar' ><img class = 'depositImg display_show23' src ='img/tixianwancheng.png'></img></div></div><div class = 'middleright' ><div class = 'help' ><div class ='weui_cell_bd_p1 weui_cell_bd1'>提现申请成功 </div><div class = 'help_img'><img src ='img/query.png'></img><span class = 'weui_cell_bd_p2' onclick = 'help(this)'>退款帮助 </span></div></div><div class = 'put' ><div class = 'weui_cell_bd_p2' >已将退款申请交至微信</div><div class = 'weui_cell_bd_p2 request_time' >" + requesttime + "</div></div><div class ='pass'><div class ='weui_cell_bd1'>审核通过 </div><div class ='weui_cell_bd_p1'><div class = 'weui_cell_bd_p2'>您的资金转至微信处理 </div><div class = 'weui_cell_bd_p2 refund_time'>" + refundtime + "</div><div class = 'weui_cell_bd_p2'></div></div></div><div class ='goto'><div class = 'weui_cell_bd1'>已到帐</div><div class = 'weui_cell_bd_p1'>微信将退款原路返回到您的支付账户中 </div></div></div></div></div>";
                            var divH = "<div class='display_show divH'><div class ='bottom_textdiv'>提现申请成功 </div><div class ='cost'><div class ='bottom_textdiv1'>金额:<span class ='refund1'>" + refundnum + "</span>元</div><div class='contraction'><div class='firstlookup showhide'><img class='up' src='img/look.png'></img><span class='look'>查看</span></div><div class='firstpackdown showhide'><img class='down' src='img/pack.png'></img><span class='pack'>收起</span></div></div></div></div>";
                            box += "<div class='navigator1' onclick = 'firsttoggle(this)'>" + _head + conterbox + divH + "</div>";
                            flag = false;
                        } else {
                            _head += complete;
                            var conterbox = "<div class='conterbox'><div class = 'middle' ><div class = 'middleleft' ><div class = 'bar' ><img class = 'depositImg display_show23' src ='img/tixianwancheng.png'></img></div></div><div class = 'middleright' ><div class = 'help' ><div class ='weui_cell_bd_p1 weui_cell_bd1'>提现申请成功 </div><div class = 'help_img'><img src = 'img/query.png'></img><span class = 'weui_cell_bd_p2' onclick = 'help(this)'>退款帮助 </span></div></div><div class = 'put' ><div class = 'weui_cell_bd_p2' >已将退款申请交至微信</div><div class = 'weui_cell_bd_p2 request_time' >" + requesttime + "</div></div><div class ='pass'><div class ='weui_cell_bd1'>审核通过 </div><div class ='weui_cell_bd_p1'><div class = 'weui_cell_bd_p2'>您的资金转至微信处理 </div><div class = 'weui_cell_bd_p2 refund_time'>" + refundtime + "</div><div class = 'weui_cell_bd_p2'></div></div></div><div class ='goto'><div class = 'weui_cell_bd1'>已到帐</div><div class = 'weui_cell_bd_p1'>微信将退款原路返回到您的支付账户中 </div></div></div></div></div>";
                            var divH = "<div class='display_show divH'><div class = 'bottom_textdiv'>提现申请成功 </div><div class ='cost'><div class ='bottom_textdiv1'>金额:<span class ='refund1'>" + refundnum + "</span>元</div><div class='contraction'><div class='lookup showhide'><img class='up' src='img/look.png'></img><span class='look'>查看</span></div><div class='packdown showhide'><img class='down' src='img/pack.png'></img><span class='pack'>收起</span></div></div></div></div>";
                            box += "<div class='navigator1' onclick = 'one(this)'>" + _head + conterbox + divH + "</div>";
                        }
                    }
                })
                $('body').html(box);
            }
        }
    })
})

function one(two) {
    $(two).children('.conterbox').toggle();
    $(two).children('.divH').children('.cost').children('.contraction').children().toggle();
}

function firsttoggle(two) {
    $(two).children('.firstconterbox').toggle();
    $(two).children('.divH').children('.cost').children('.contraction').children().toggle();
}

function help(two) {
    location.href = "deposithelp.html";
}
function formatDateTime(inputTime) {
    var date = new Date(inputTime * 1000);
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    var minute = date.getMinutes();
    var second = date.getSeconds();
    minute = minute < 10 ? ('0' + minute) : minute;
    second = second < 10 ? ('0' + second) : second;
    return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
};