function ready(callback) {
    // 如果jsbridge已经注入则直接调用
    if (window.AlipayJSBridge) {
        callback && callback();
    } else {
        // 如果没有注入则监听注入的事件
        document.addEventListener('AlipayJSBridgeReady', callback, false);
    }
}
ready(function() {

    AlipayJSBridge.call('getCurrentLocation', {
        bizType: 'didi'
    }, function(result) {
        if(result.error) {
            alert(result.errorMessage);
            return;
        }
        var latitude = result.latitude;
        var longitude = result.longitude;
        if(latitude != null && longitude != null) {

            $.ajax({
                type: "post",
                url: urlObject.getShopList_url,
                data: {
                    longitude: longitude,
                    latitude: latitude
                },
                dataType: "json",
                success: function(data) {
                    $('#box1').css("display","none");
                    for(var i = 0; i < data.data.shops.length; i++) {
                        var dis = parseInt(getDistance(longitude, latitude, data.data.shops[i].longitude, data.data.shops[i].latitude));
                        var address = data.data.shops[i].address;
                        var canborrow = 0;
                        var canReturn = 0;
                        var shop_id = data.data.shops[i].id;

                        for(var j = 0; j < data.data.shops[i].shop_station.length; j++) {
                            canborrow += data.data.shops[i].shop_station[j].usable;
                            canReturn += data.data.shops[i].shop_station[j].empty;
                        }
                        if(canborrow > 10) {
                            canborrow = "10+";
                        }
                        if(canReturn > 10) {
                            canReturn = "10+";
                        }
                        var shop_item = $("<div class='viewH'><img style='width: 5rem; height: 5rem;' src='img/list/lblogo.png'></img><div class='viewV'><div class='bottom '><p class='bottom_textview'>"+data.data.shops[i].name+"</p><p class='weui_cell_bd_p bottom_distance'>距离<span class='dis'>"+dis+"</span>m</p></div><div class='bottom'><img class='lend' style='width:2.5rem; height: 2.5rem;' src='img/list/b/l_"+canborrow+".png'></img><span class='weui_cell_bd_p weui_cell_bd_p1'>可借</span><img class='item_img repay' style='width: 2.5rem; height: 2.5rem;' src='img/list/b/l_"+canReturn+".png'></img><span class='weui_cell_bd_p weui_cell_bd_p2'>可还</span></div><div class='bottom'><p class='weui_cell_bd_p weui_cell_bd_p3'>"+address+"</p></div></div><input value='"+shop_id+"' hidden='true'> </div>");
                        $('.box').append(shop_item);
                    }

                    $('.viewH').click(function () {
                        location.href = urlObject.shopDetails_url + this.lastElementChild.value;
                    });
                }
            })
        }
    });
});

function getDistance(lat1, lng1, lat2, lng2) {
    lat1 = lat1 || 0;
    lng1 = lng1 || 0;
    lat2 = lat2 || 0;
    lng2 = lng2 || 0;

    var rad1 = lat1 * Math.PI / 180.0;
    var rad2 = lat2 * Math.PI / 180.0;
    var a = rad1 - rad2;
    var b = lng1 * Math.PI / 180.0 - lng2 * Math.PI / 180.0;

    var r = 6378137;
    return r * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(rad1) * Math.cos(rad2) * Math.pow(Math.sin(b / 2), 2)))
};