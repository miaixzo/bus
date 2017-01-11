var String = java.lang.String;
var StringBuffer = java.lang.StringBuffer;
var Integer = java.lang.Integer;
var Hashtable = java.util.Hashtable;
var Iteractor = java.util.Iteractor;
var TreeMap = java.util.TreeMap;
var Date = java.util.Date;
var Mac = javax.crypto.Mac;
var SecretKeySpec = javax.crypto.spec.SecretKeySpec;
var Base64 = org.apache.commons.codec.binary.Base64;
var URLEncoder = java.net.URLEncoder;


function WangGouRESTAPI(treeMap){
	this.ht = treeMap;
}

WangGouRESTAPI.prototype.put = function (key, value) {
		this.ht.put(key, value);
}

WangGouRESTAPI.prototype.encrypt = function (key, method,queryMethod) {
		var orgin = new StringBuffer();
		var map = new TreeMap();
		map.putAll(this.ht);

		var iter = map.keySet().iterator();
		var i = 0;
		while (iter.hasNext()) {
			var name = String.valueOf(iter.next());
			if (new String(map.get(name)).length() > 0) {
				if (i > 0){
					orgin.append("&").append(name).append("=" + map.get(name));
				} else {
					orgin.append(name).append("=" + map.get(name));
					i = i + 1;
				}
			}
		}
		var param = queryMethod + "&" + encodeURIComponent(method, "UTF-8") + "&" + encodeURIComponent(new String(orgin));
		var signingKey = new SecretKeySpec(new String(key).getBytes("UTF-8"), "HmacSHA1");  
		var sha256_HMAC = Mac.getInstance("HmacSHA1");
		sha256_HMAC.init(signingKey);
		var sign = sha256_HMAC.doFinal(new String(param).getBytes("UTF-8"));
		var signBase64 = new Base64().encodeBase64(sign);
		return new String(signBase64, "UTF-8");
}

WangGouRESTAPI.prototype.URLEncode = function (ru, rp) {
	var request_url = ru;
	var request_params = rp;
	for (var it = this.ht.entrySet().iterator(); it.hasNext();) {
		var e = it.next();
		if (new String(e.getValue()).length() > 0) {
			request_params.append("&").append(e.getKey()).append("=").append(encodeURIComponent(e.getValue()));
		}
	}
	
	var url = request_url + "?" + new String(request_params.toString().substring(1));
	return url;
}