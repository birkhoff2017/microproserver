$('.page_bd').click(function(){
	$(this).children('.body_hide').slideToggle();
//	$(this).children('.body_hide').slideToggle().parents('.page_bd').siblings('.page_bd').children('.body_hide').hide();
	$(this).find(".lookpack").toggle();
	$(this).find('.dian').toggle();
})
$('.listOne1').click(function(){
	location.href = "helprentone.html";
})
$('.listOne2').click(function(){
	location.href = "helprenttwo.html";
})
$('.listOne3').click(function(){
	location.href = "helprentthree.html";
})
$('.listTwo1').click(function(){
	location.href = "helpback.html";
})
$('.listTwo2').click(function(){
	location.href = "helpbacktwo.html";
})
$('.listTwo3').click(function(){
	location.href = "helpbackthree.html";
})
$('.listFour1').click(function(){
	location.href = "helpuseone.html";
})
$('.listFour2').click(function(){
	location.href = "helpusetwo.html";
})
$('.listFour3').click(function(){
	location.href = "helpusethree.html";
})

//拨打电话
var btn = document.querySelector('#lianxikefu');
btn.addEventListener('click', function(){
    ap.makePhoneCall($('#contact_phone').html());
});