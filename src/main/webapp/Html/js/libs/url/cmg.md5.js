var MessageDigest = java.security.MessageDigest;
var StringBuffer = java.lang.StringBuffer;
var Integer = java.lang.Integer;


function CMGMD5(){}

CMGMD5.prototype.encrypt = function (strSource) {
	var algorithm = MessageDigest.getInstance("MD5");

	// get the input as bytes
	var bytes = new java.lang.String(strSource).getBytes("UTF-8");

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
	return hexString.toString();
}