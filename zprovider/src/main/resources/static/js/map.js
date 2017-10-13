$(function() {
	var mapObj = new AMap.Map('container', { //初始化地图
		resizeEnable: true,
		zoom: 10, //缩放比例
		center: [116.53154, 39.9177], //标注的点的位置
		mapStyle: 'amap://styles/normal'
	});

	function addMarker(j, w) {
		marker = new AMap.Marker({
			icon: new AMap.Icon({
				image: "img/map/u4.png", //标注图！！
				image:"img/map/坐标.png",
				size: new AMap.Size(23, 29), //图标大小
				imageSize: new AMap.Size(23, 29)
			}),
			position: new AMap.LngLat(j, w) //标注位置（经纬度）
		});
		marker.setMap(mapObj); //在地图上添加点
	}
	addMarker(116.53154, 39.9177); //实例化
//	$('.butmap').on('click', function(e) {
		marker.markOnAMAP({
			name: '云充吧',
			position: marker.getPosition()
		})
//	})

	mapObj.plugin('AMap.Geolocation', function() {
		geolocation = new AMap.Geolocation({
			enableHighAccuracy: true, //是否使用高精度定位，默认:true
			timeout: 10000, //超过10秒后停止定位，默认：无穷大
			maximumAge: 0, //定位结果缓存0毫秒，默认：0
			convert: true, //自动偏移坐标，偏移后的坐标为高德坐标，默认：true
			showButton: true, //显示定位按钮，默认：true
			buttonPosition: 'LB', //定位按钮停靠位置，默认：'LB'，左下角
			buttonOffset: new AMap.Pixel(10, 20), //定位按钮与设置的停靠位置的偏移量，默认：Pixel(10, 20)
			showMarker: true, //定位成功后在定位到的位置显示点标记，默认：true
			showCircle: true, //定位成功后用圆圈表示定位精度范围，默认：true
			panToLocation: true, //定位成功后将定位到的位置作为地图中心点，默认：true
			zoomToAccuracy: true //定位成功后调整地图视野范围使定位位置及精度范围视野内可见，默认：false
		});
		mapObj.addControl(geolocation);
		geolocation.getCurrentPosition();
		AMap.event.addListener(geolocation, 'complete', onComplete); //返回定位信息
		AMap.event.addListener(geolocation, 'error', onError); //返回定位出错信息
	})
});