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

function YihaodianRESTAPI(baseURL, treeMap){
	this.baseURL = baseURL;
	this.ht = treeMap;
}

YihaodianRESTAPI.prototype.put = function (key, value) {
	this.ht.put(key, value);
}

YihaodianRESTAPI.prototype.encrypt = function (app_secret) {
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
		if (md5Digest[i] >= 0 && md5Digest[i] < 16) {
			hexString.append('0');
		}
		hexString.append(Integer.toHexString(md5Digest[i] & 0XFF));
	}

	// write output value
	var sign = hexString.toString();
	return sign
}

YihaodianRESTAPI.prototype.URLEncode = function (rp) {
	var request_params = rp;
	for (var it = this.ht.entrySet().iterator(); it.hasNext();) {
		var e = it.next();
		if (new String(e.getValue()).length() > 0) {
			request_params.append("&").append(e.getKey()).append("=").append(e.getValue());
		}
	}
	var url = this.baseURL + "?" + request_params.toString().substring(1);

	return new URI(url).toString();
}