var request = require('request');
var config = require('../config');
var zlib = require('zlib');
var ppCrypto = require('./pp-crypto');
var querystring = require('querystring');

exports.getLocalUrl = function () {
    if ((config.MY_HOSTNAME.indexOf("http:") == 0 && config.MY_PORT == 80) ||
        (config.MY_HOSTNAME.indexOf("https:") == 0 && config.MY_PORT == 443)) {
        return config.MY_HOSTNAME;
    }
    return config.MY_HOSTNAME + ":" + config.MY_PORT;
}

/**
 * Make a request to the PayPal Here API for a user
 * The user must have a
 * @param options Request passed to the request module once OAuth is sorted out
 * @param userDoc The current document from nstore
 * @param nstore The nstore database which will need to be updated if the ticket needs refreshing
 * @param cb The callback when we're complete
 */
exports.request = function (nstore, userDoc, options, cb) {

};

/**
 * Make sure the access token is fresh, or refresh it if necessary
 * @param nstore The nstore database which will need to be updated if the ticket needs refreshing
 * @param userDoc The current document from nstore
 * @param cb The callback when we're complete
 */
exports.ensureToken = function (nstore, userDoc, cb) {
    // Make sure the access token in userDoc is "likely" to be valid
    var now = new Date().getTime()/1000;
    // make sure there's more than 20 seconds left on this token (20 is arbitrary, meant to avoid a wasted refresh
    // call from the client soon after issuance)
    if (userDoc.expires - now > 20) {
        console.log("Using existing token for",userDoc.username,"with",userDoc.expires - now,"seconds remaining:",userDoc.access);
        cb(null, {
           access_token: userDoc.access,
           refresh_token: userDoc.refresh,
           expires_in: Math.floor(userDoc.expires - now)
        });
    } else {
        console.log("Refreshing token for",userDoc.username);
        // Need to refresh the token
        // TODO multicaller safety during an attempt to refresh. In a real scenario you can use a db for this,
        // but we just have this little old flat file in the sample. While recovery from an overlapped refresh
        // isn't horrible, it's not great either.
        request.post({
            url:config.PAYPAL_ACCESS_BASEURL + "auth/protocol/openidconnect/v1/tokenservice",
            auth:{
                user:config.PAYPAL_APP_ID,
                pass:config.PAYPAL_SECRET,
                sendImmediately:true
            },
            form:{
                grant_type:"refresh_token",
                refresh_token:userDoc.refresh
            }
        }, exports.jsonWrap(function (error, response, info) {
            if (error) {
                cb(error,null);
            } else {
                if (!info || !info.access_token) {
                    cb(info||"Empty response from token service",null);
                    return;
                }
                console.log("New access token", info.access_token);
                userDoc.access = info.access_token;
                userDoc.refresh = info.refresh_token||userDoc.refresh;
                userDoc.expires = (new Date().getTime()/1000) + parseFloat(info.expires_in);
                nstore.save(userDoc.username, userDoc, function (err) {
                    cb(null, {
                        access_token: userDoc.access,
                        refresh_token: userDoc.refresh,
                        expires_in: Math.floor(userDoc.expires - now)
                    });
                });
            }
        }));
    }
};

exports.importToken = function (nstore,userDoc,cb) {
    exports.ensureToken(nstore,userDoc,function (err,info) {
        if (err) { cb(err,null); return; }
        ppCrypto.encryptToken(info.access_token, userDoc.client_key, function (err1, access_token) {
            ppCrypto.encryptToken(info.refresh_token, userDoc.server_secret, function (err2, refresh_token) {
                if (err1 || err2) {
                    cb(err1 || err2);
                }
                info.access_token = access_token;
                delete info["refresh_token"];
                info.refresh_url = exports.getLocalUrl() + "/refresh/" +
                    encodeURIComponent(userDoc.username) + "/" + refresh_token;
                // Put all the info on the query string of a URL which will launch the app
                cb(null,info);
            });
        });
    })
}

exports.buildAppURL = function (nstore, userDoc, info, cb) {
    // If you want the server to store the tokens, this is all you need here.
    if (config.CENTRAL_SERVER_MODE) {
        userDoc.access = info.access_token;
        userDoc.refresh = info.refresh_token;
        userDoc.expires = (new Date().getTime()/1000) + parseFloat(info.expires_in);
        nstore.save(userDoc.username, userDoc, function () {}); // again, we're just assuming this db write always works
    }
    // However, if you want to give the mobile device the power to make the calls and manage the
    // tokens independently (allowing many simultaneous OAuth credentials, but at the cost of forcing
    // every device to go through the OAuth setup, then use the below. By doing both here, we
    // are letting the mobile device decide which approach to take.
    //
    // Encrypt the access and refresh tokens using the guid, which only the client
    // should have.
    ppCrypto.encryptToken(info.access_token, userDoc.client_key, function (err1, access_token) {
        ppCrypto.encryptToken(info.refresh_token, userDoc.server_secret, function (err2, refresh_token) {
            if (err1 || err2) {
                cb(err1 || err2);
            }
            info.access_token = access_token;
            delete info["refresh_token"];
            info.refresh_url = exports.getLocalUrl() + "/refresh/" +
                encodeURIComponent(userDoc.username) + "/" + refresh_token;
            // Put all the info on the query string of a URL which will launch the app
            var toApp = config.MY_APP_URL + "?" + querystring.stringify(info);
            console.log(toApp);
            cb(null, toApp);
        });
    });
};

// There appears to be a bug with some Akamai stuff that is returning gzip to us even when we don't want it,
// and request isn't handling it properly. So this will fix that.
exports.jsonWrap = function (cb) {
    return function (error, response, body) {
        var json = null;
        if ((response && response.headers['content-encoding'] && response.headers['content-encoding'].toLowerCase().indexOf('gzip') >= 0) ||
            (Buffer.isBuffer(body) && body[0] == 0x1f && body[1] == 0x8b)) { // Gzip magic numbers
            if(!Buffer.isBuffer(body)) {
                body = new Buffer(body);
            }
            zlib.gunzip(body, function (error, dat) {
                if(!error) {
                    json = JSON.parse(dat);
                } else {
                    console.log("Gunzip failed",error,dat);
                    cb(error, response, null);
                    return;
                }
            });
            return;
        } else {
            json = body ? JSON.parse(body) : null;
        }
        cb(error,response,json);
    };
}