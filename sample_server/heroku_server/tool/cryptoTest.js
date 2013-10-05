/**
 * Should you have the painful task of doing encryption between node and objective-c
 * or any other language pair, this may be helpful for verifying your result. It's a
 * total sideshow to the PayPal Here SDK, but it was painful for me so I figured it
 * might be useful for you.
 *
 * The packet format we're going for (using aes-256-cbc) is:
 *  salt (16), IV (16), HMAC (32), Cipher Text (variable)
 */
var crypto = require('crypto');

if (process.argv[2] == "crypt") {
    var password = process.argv[3];
    var plainText = process.argv[4];
    console.log("Password  :",password);
    console.log("Plain Text:",plainText);

    var salt = new Buffer(crypto.randomBytes(16), 'binary');
    var iv = new Buffer(crypto.randomBytes(16), 'binary');

    console.log("Salt      :",salt.toString("base64"));
    console.log("IV        :",iv.toString("base64"));
    crypto.pbkdf2(password, salt, 1000, 32, function (err, key) {
        if (err) {
            console.log("Failed to generate key.",err);
            return;
        }
        console.log("Key       :",new Buffer(key, 'binary').toString("base64"));
        var cipher = crypto.createCipheriv('aes-256-cbc',key,iv);
        var buffer = new Buffer(cipher.update(plainText,'utf8','binary'), 'binary');
        buffer = Buffer.concat([buffer, new Buffer(cipher.final('binary'), 'binary')]);

        var hashKey = crypto.createHash('sha1').update(key).digest('binary');
        console.log("Hash Key  :",new Buffer(hashKey, 'binary').toString("base64"));
        var hmac = new Buffer(crypto.createHmac('sha1', hashKey).update(buffer).digest('binary'), 'binary');
        console.log("HMAC      :", hmac.toString('base64'));

        buffer = Buffer.concat([salt,iv,hmac,buffer]);
        console.log("Cipher Text:");
        console.log(buffer.toString("base64"));
    });
} else {
    var password = process.argv[3];
    var cipher = new Buffer(process.argv[4], 'base64');

    var salt = cipher.slice(0,16);
    var iv = cipher.slice(16,32);
    var hmac = cipher.slice(32,52);
    var cipherText = cipher.slice(52);

    console.log("Salt      :",salt.toString("base64"));
    console.log("IV        :",iv.toString("base64"));
    console.log("HMAC      :", hmac.toString('base64'));

    crypto.pbkdf2(password, salt, 1000, 32, function (err, key) {
        if (err) {
            console.log("Failed to generate key.",err);
            return;
        }
        console.log("Key       :",new Buffer(key, 'binary').toString("base64"));
        var cipher = crypto.createDecipheriv('aes-256-cbc', key, iv);

        // Verify the HMAC first
        var hashKey = crypto.createHash('sha1').update(key).digest('binary');
        console.log("Hash Key  :",new Buffer(hashKey, 'binary').toString("base64"));
        var hmacgen = new Buffer(crypto.createHmac('sha1', hashKey).update(cipherText).digest('binary'), 'binary');
        console.log("HMAC      :", hmacgen.toString('base64'));
        if (hmacgen.toString('base64') != hmac.toString('base64')) {
            console.log("HMAC Mismatch!");
            return;
        }
        var buffer = new Buffer(cipher.update(cipherText),'binary');
        buffer = Buffer.concat([buffer, new Buffer(cipher.final('binary'))]);
        console.log("Plain Text:",buffer.toString('utf8'));
    });
}
