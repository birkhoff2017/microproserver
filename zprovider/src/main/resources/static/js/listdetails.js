$(function () {
    var Request = new Object();
    Request = GetRequest();
    var shop_id = Request['shop_id'];

    var latitude;
    var longitude;
    var name;

    $.ajax({
        type: "post",
        url: urlObject.getShopInfo,
        data: {
            shop_id: shop_id
        },
        dataType: "json",
        success: function (data) {
            latitude = data.data.shop.latitude;
            longitude = data.data.shop.longitude;
            var usable = 0;
            var empty = 0;
            name = data.data.shop.name;
            $('.shop_name').html(name);
            var shopaddress = data.data.shop.address;
            $('.bottom_textview3').html(shopaddress);
            var stime = data.data.shop.stime;
            $('.stime').html(stime);
            var etime = data.data.shop.etime;
            $('.etime').html(etime);
            var phone = data.data.shop.phone;
            $('.call').html(phone);
            var cost = data.data.shop.cost;
            $('.cost').html(cost);
            for (var i = 0; i < data.data.shop.shop_station.length; i++) {
                usable += data.data.shop.shop_station[i].usable;
                empty += data.data.shop.shop_station[i].empty;
            }
            if (usable > 10) {
                usable = "10+";
            }
            if (empty > 10) {
                empty = "10+";
            }
            var canborrow = usable;
            var canReturn = empty;
            $('.lend').attr('src', "img/list/b/l_" + canborrow + ".png");
            $('.repay').attr('src', "img/list/b/l_" + canReturn + ".png");

            $("#J_btn_scanQR").click(function () {
                window.alert("请使用支付宝扫一扫，扫描充电柜支付宝二维码");
                return false;
            });
            // //马上导航
            // $('#M_btn_scanQR').click(function(){
            // 	location.href = encodeURI(encodeURI("map.html?name="+data.data.shop.name+"&longitude="+data.data.shop.longitude+"&latitude="+data.data.shop.latitude));
            // })
        }
    })
    //拨打电话
    var btn = document.querySelector('#contact_phone');
    btn.addEventListener('click', function () {
        ap.makePhoneCall($('#contact_phone').html());
    });
    //马上导航
    $('#M_btn_scanQR').click(function () {
        //调用高德地图
        location.href = 'http://m.amap.com/share/index/__q=' + latitude + ',' + longitude + ',' + name + '&src=jsapi&callapp=0&lnglat=' + longitude + ',' + latitude + '&name=' + name;
    })
})

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

