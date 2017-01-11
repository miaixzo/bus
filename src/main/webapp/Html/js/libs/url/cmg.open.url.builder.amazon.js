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


function AmazonRESTAPI(treeMap){
	this.ht = treeMap;
}

AmazonRESTAPI.prototype.put = function (key, value) {
		this.ht.put(key, value);
}

AmazonRESTAPI.prototype.encrypt = function (url, method, version, app_secret) {
		var orgin = new StringBuffer();
		orgin.append("POST\n");
		orgin.append(url+"\n");
		orgin.append("/"+method+"/"+version+"\n");
		

		var map = new TreeMap();
		map.putAll(this.ht);

		var iter = map.keySet().iterator();
		var i = 0;
		while (iter.hasNext()) {
			var name = String.valueOf(iter.next());
			if (new String(map.get(name)).length() > 0) {
				if (i > 0){
					orgin.append("&").append(name).append("=" + URLEncoder.encode(map.get(name),"utf-8"));
				} else {
					orgin.append(name).append("=" + URLEncoder.encode(map.get(name),"utf-8"));
					i = i + 1;
				}
			}
		}
		//Alert(new String(orgin));
		//var sign = CryptoJS.HmacSHA256("abc", "dJ9CtVeqWTKXsnqzavUf+AQTOnRqA+0EzPVWN2t1");
		//Alert(new String(app_secret));
		var sha256_HMAC = Mac.getInstance("HmacSHA256");
		var secret_key = new SecretKeySpec(new String(app_secret).getBytes(), new String("HmacSHA256"));
		sha256_HMAC.init(secret_key);
		var sign = sha256_HMAC.doFinal(new String(orgin).getBytes());

		var signBase64 = new Base64().encodeBase64(new String(sign).getBytes());

		return new String(signBase64);
}

AmazonRESTAPI.prototype.URLEncode = function (ru, rp) {
	var request_url = ru;
	var request_params = rp;
	for (var it = this.ht.entrySet().iterator(); it.hasNext();) {
		var e = it.next();
		if (new String(e.getValue()).length() > 0) {
			request_params.append("&").append(e.getKey()).append("=").append(URLEncoder.encode(e.getValue(),"utf-8"));
		}
	}
	var url = request_url + "?" + request_params.toString().substring(1);
	return url;
}