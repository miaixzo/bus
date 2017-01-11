var MessageDigest = java.security.MessageDigest;
var String = java.lang.String;
var StringBuffer = java.lang.StringBuffer;
var Integer = java.lang.Integer;
var Hashtable = java.util.Hashtable;
var Iteractor = java.util.Iteractor;
var DecimalFormat = java.text.DecimalFormat;
var URI = org.apache.commons.httpclient.URI;
var TreeMap = java.util.TreeMap;
var Date = java.util.Date;

var URLEncoder = java.net.URLEncoder;
var Base64 = org.apache.commons.codec.binary.Base64;
var RSA = org.ukettle.ehub.agent.client.RSA;

function TaobaoMD5(treeMap){
	this.ht = treeMap;
}

TaobaoMD5.prototype.put = function (key, value) {
		this.ht.put(key, value);
}

TaobaoMD5.prototype.encrypt = function (app_secret) {
		var orgin = new StringBuffer();
		orgin.append(app_secret);

		var map = new TreeMap();
		map.putAll(this.ht);

		var iter = map.keySet().iterator();
		while (iter.hasNext()) {
			var name = String.valueOf(iter.next());
			if (new String(map.get(name)).length() > 0) {
				orgin.append(name).append(map.get(name));
			}
		}

		orgin.append(app_secret);

		// get the md5 digest algorithm
		var algorithm = MessageDigest.getInstance("MD5");

		// get the input as bytes
		var bytes = new String(orgin).getBytes("UTF-8");

		// calculate the digest
		algorithm.reset();
		algorithm.update(bytes);
		var md5Digest = algorithm.digest();

		// turn the digest into a hex-string representation
		var hexString = new StringBuffer();

		for (var i = 0; i < md5Digest.length; i++){
			var hex = Integer.toHexString(md5Digest[i] & 0XFF);
			if (hex.length() == 1){
				hexString.append('0').append("");
			}
			hexString.append(hex);
		}

		// write output value
		var sign = hexString.toString().toUpperCase();
		return sign
}

TaobaoMD5.prototype.URLEncode = function (ru, rp) {
	var request_url = ru;
	var request_params = rp;
	for (var it = this.ht.entrySet().iterator(); it.hasNext();) {
		var e = it.next();
		if (new String(e.getValue()).length() > 0) {
			request_params.append("&").append(e.getKey()).append("=").append(e.getValue());
		}
	}
	var url = request_url + "?" + request_params.toString().substring(1);

//	return url;
	return new URI(url).toString();
}

TaobaoMD5.prototype.EncodeURL = function (ru, rp) {
	var request_url = ru;
	var request_params = rp;
	for (var it = this.ht.entrySet().iterator(); it.hasNext();) {
		var e = it.next();
		if (new String(e.getValue()).length() > 0) {
			request_params.append("&").append(e.getKey()).append("=").append(e.getValue());
		}
	}
	var url = request_url + "?" + request_params.toString().substring(1);
	
	if(null != url){
		var rsaEncode = RSA.getENCRYPTInstance();
		var data = rsaEncode.encryptData(new String(url).getBytes("UTF-8"));
		url = URLEncoder.encode(new String(Base64.encodeBase64(data)),"UTF-8")
	}
	return url;
}