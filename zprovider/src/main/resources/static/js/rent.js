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
var session = Request['session'];

$(function () {
    $.ajax({
        type: "post",
        url: urlObject.getOrderList,
        data: {
            session: session
        },
        dataType: "JSON",
        success: function (data) {
            if (data.data.orders.length > 0) {
                var obj = data.data.orders;
                var doneview = "";
                var flag = true;
                $.each(obj, function (n, value) {
                    var orderid = value.orderid;
                    var borrowname = value.borrow_name;
                    var borrowtime = value.borrow_time;
                    var feestrategy = value.fee_strategy;
                    var returnname = value.return_name;
                    var returntime = value.return_time;
                    var lasttime = value.last_time;
                    var usefee = value.use_fee;
                    var status = value.status;
                    var underway = "<p class='_head_price' style='color: #c00'>进行中</p></div>";
                    var complete = "<p class='_head_price1' style='color: #0c9'>已完成</p></div>";
                    var _head = "<div class='_head'><p class='_head_title'>订单编号:<span class='number'>" + orderid + "<span></p>";

                    if ("12568".indexOf(status) > -1) {
                        if (flag) {
                            var middle = "<div class='firstmiddle'><p class='weui_cell_bd_p'>租借地点:<span class='borrow_name'>" + borrowname + "</span></p><p class='weui_cell_bd_p'>租借时间:<span class='borrow_time'>" + borrowtime + "</span></p><p class='weui_cell_bd_p'>收费标准:<span class='fee_strategy'>" + feestrategy + "</span></p><p class='weui_cell_bd_p'>归还地点:<span class='return_name'>" + returnname + "</span></p><p class='weui_cell_bd_p'>归还时间:<span class='return_time'>" + returntime + "</span></p></div>"
                            var viewH = "<div class='viewH'><p class='bottom_textview'>租借时长:<span class='last_time'>" + lasttime + "</span></p><div class='viewH-bottom'><div class='cost'><div class='bottom_textview'>产生费用:<span class='use_fee'>" + usefee + "</span></div><div class='contraction' ><div class='firstlookup showhide'><img class='up' src='img/jdtlook.png'></img><span class='look'>查看</span></div><div class='firstpackdown showhide'><img class='down' src='img/jdtpack.png'></img><span class='pack'>收起</span></div></div></div></div></div>"
                            _head += underway;
                            doneview += "<div class='doneview1' onclick = 'firsttoggle(this)'>" + _head + middle + viewH + "</div>";
                            flag = false;
                        } else {
                            var middle = "<div class='middle'><p class='weui_cell_bd_p'>租借地点:<span class='borrow_name'>" + borrowname + "</span></p><p class='weui_cell_bd_p'>租借时间:<span class='borrow_time'>" + borrowtime + "</span></p><p class='weui_cell_bd_p'>收费标准:<span class='fee_strategy'>" + feestrategy + "</span></p><p class='weui_cell_bd_p'>归还地点:<span class='return_name'>" + returnname + "</span></p><p class='weui_cell_bd_p'>归还时间:<span class='return_time'>" + returntime + "</span></p></div>"
                            var viewH = "<div class='viewH'><p class='bottom_textview'>租借时长:<span class='last_time'>" + lasttime + "</span></p><div class='viewH-bottom'><div class='cost'><div class='bottom_textview'>产生费用:<span class='use_fee'>" + usefee + "</span></div><div class='contraction' ><div class='lookup showhide'><img class='up' src='img/jdtlook.png'></img><span class='look'>查看</span></div><div class='packdown showhide'><img class='down' src='img/jdtpack.png'></img><span class='pack'>收起</span></div></div></div></div></div>"
                            _head += underway;
                            doneview += "<div class='doneview1' onclick = 'one(this)'>" + _head + middle + viewH + "</div>";
                        }
                    } else {
                        if (flag) {
                            var middle = "<div class='firstmiddle'><p class='weui_cell_bd_p'>租借地点:<span class='borrow_name'>" + borrowname + "</span></p><p class='weui_cell_bd_p'>租借时间:<span class='borrow_time'>" + borrowtime + "</span></p><p class='weui_cell_bd_p'>收费标准:<span class='fee_strategy'>" + feestrategy + "</span></p><p class='weui_cell_bd_p'>归还地点:<span class='return_name'>" + returnname + "</span></p><p class='weui_cell_bd_p'>归还时间:<span class='return_time'>" + returntime + "</span></p></div>"
                            var viewH = "<div class='viewH'><p class='bottom_textview'>租借时长:<span class='last_time'>" + lasttime + "</span></p><div class='viewH-bottom'><div class='cost'><div class='bottom_textview'>产生费用:<span class='use_fee'>" + usefee + "</span></div><div class='contraction' ><div class='firstlookup showhide'><img class='up' src='img/look.png'></img><span class='look'>查看</span></div><div class='firstpackdown showhide'><img class='down' src='img/pack.png'></img><span class='pack'>收起</span></div></div></div></div></div>"
                            _head += complete;
                            doneview += "<div class='doneview' onclick = 'firsttoggle(this)'>" + _head + middle + viewH + "</div>";
                            flag = false;
                        } else {
                            var middle = "<div class='middle'><p class='weui_cell_bd_p'>租借地点:<span class='borrow_name'>" + borrowname + "</span></p><p class='weui_cell_bd_p'>租借时间:<span class='borrow_time'>" + borrowtime + "</span></p><p class='weui_cell_bd_p'>收费标准:<span class='fee_strategy'>" + feestrategy + "</span></p><p class='weui_cell_bd_p'>归还地点:<span class='return_name'>" + returnname + "</span></p><p class='weui_cell_bd_p'>归还时间:<span class='return_time'>" + returntime + "</span></p></div>"
                            var viewH = "<div class='viewH'><p class='bottom_textview'>租借时长:<span class='last_time'>" + lasttime + "</span></p><div class='viewH-bottom'><div class='cost'><div class='bottom_textview'>产生费用:<span class='use_fee'>" + usefee + "</span></div><div class='contraction' ><div class='lookup showhide'><img class='up' src='img/look.png'></img><span class='look'>查看</span></div><div class='packdown showhide'><img class='down' src='img/pack.png'></img><span class='pack'>收起</span></div></div></div></div></div>"
                            _head += complete;
                            doneview += "<div class='doneview' onclick = 'one(this)'>" + _head + middle + viewH + "</div>";
                        }
                    }
                });
                $('body').html(doneview);
            }
        }
    });
})

function one(two) {
    $(two).children('.middle').toggle();
    $(two).children('.viewH').children('.viewH-bottom').children('.cost').children('.contraction').children().toggle();
}

function firsttoggle(two) {
    $(two).children('.firstmiddle').toggle();
    $(two).children('.viewH').children('.viewH-bottom').children('.cost').children('.contraction').children().toggle();
}
