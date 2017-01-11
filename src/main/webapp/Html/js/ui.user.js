$(function(){
	$("#iUser").click(function() {
		var name = $.trim($("#name").val());
		if (!name) {
			alert($.i18n.get('org.ukettle.iQuartz.Verify.User.Name'));
			return false;
		}
		var reg = /^(?:[a-z\d]+[_\-\+\.]?)*[a-z\d]+@(?:([a-z\d]+\-?)*[a-z\d]+\.)+([a-z]{2,})+$/i;
		var email = $.trim($("#email").val());
		if(!reg.test(email)){
			alert($.i18n.get('org.ukettle.iQuartz.Verify.User.Email'));
			return false;
		}
		var password = $.trim($("#password").val());
		var password1 = $.trim($("#password1").val());
		if (!password || !password1 || password != password1) {
			alert($.i18n.get('org.ukettle.iQuartz.Verify.User.Password'));
			return false;
		}
		$('#formId').submit();
	});
});