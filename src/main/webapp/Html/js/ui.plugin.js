var plugin = {};

plugin.locale = function(language){
	$.i18n.init({
		name:'message',
		path:ctx+'/Html/js/messages/',
		language: language,
		mode:'map',
		encoding: 'UTF-8', 
	    callback: function() {
	    } 
	});
};
plugin.isWidthChar = function(val) {
	var pattern = /^[a-zA-Z]([a-zA-Z0-9]|[_]){6,64}$/;
	return pattern.exec(val);
};
plugin.language = function(val) {
	$.post(ctx+"/Locale?language="+val+"&url="+location.href, function(data) {});
	window.location.reload();
};
$(function(){
	plugin.locale(language);
});