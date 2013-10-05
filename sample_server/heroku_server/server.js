////////////////////////////////////////////////////////////////////
// SAMPLE WEB SERVICES FOR A MOBILE CLIENT USING THE PAYPAL HERE SDK
////////////////////////////////////////////////////////////////////

// Modify config.js - you shouldn't need to modify this file. But feel free to crib and
// rewrite and re-purpose at will.
var config = require("./config.js");

var express = require('express'),
    fs = require('fs'),
    stylus = require('stylus'),
    domain = require('domain'),
    crypto = require('crypto'),
    nStore = require('nstore'),
    request = require('request'),
    ppCrypto = require('./lib/pp-crypto'),
    oauth = require('./lib/oauth');

/* In the real world we should wait for the db to be open, but this is a sample app so we'll just
 * pretend it's super fast and never fails.
 */
var users = nStore.new('users.db');

var app = express();
app.configure(function () {
    app.use(express.logger());
    app.use(express.compress());
    var static = express.static(__dirname + "/public", {redirect:false});
    app.use(static);
    app.set('static', static);
    app.use(express.methodOverride());
    app.use(express.bodyParser());

    /////////////////////////////////////////////////////////////////////////////////
    // Setup per-request error handling to avoid server crash on exceptions
    /////////////////////////////////////////////////////////////////////////////////
    app.use(function (req, res, next) {
        var reqd = domain.create();
        req.activeDomain = reqd;

        reqd.on('error', function (err) {
            try {
                next(err, req, res, next);
                res.on('close', function () {
                    // forcibly shut down any other things added to this domain
                    reqd.dispose();
                });
            } catch (er) {
                console.log("Next failed", er);
                try {
                    reqd.dispose();
                } catch (er2) {
                }
            }
        });
        reqd.run(next);
    });
    /////////////////////////////////////////////////////////////////////////////////
    app.use(app.router);
    app.use(express.errorHandler({ showStack:true, dumpExceptions:true }));
});

var port = process.env.PORT;// || 5000;
console.log("Using Port " + port);
app.listen(port);
console.log("Server up and running at " + oauth.getLocalUrl());

// Advertise the server via Bonjour/zero-conf to make development easier - you'll only
// need one server per development team.
//var ad = mdns.createAdvertisement(mdns.tcp("hereandthere"), config.MY_PORT);
//ad.start();

/**
 * In a real login system, you would want to give back some sort of dynamic
 * credential to the client, but for simplicity this example just hands back
 * a GUID that was generated when the account was created. The important part
 * is that you're validating the user against your own account system before
 * enabling them to get PayPal credentials using your appid.
 */
app.post('/login', function (req, res, next) {
    var sha = crypto.createHash('sha1');
    sha.update(req.body.password);
    users.get(req.body.username, function (err, doc) {
        if (err) {
            console.log(err);
            res.json(err, 500);
            return;
        }
        //res.json({ticket:sha.digest("hex")});

        var bizname = "Acme Widgets";
        var serverurl = config.PAYPAL_ACCESS_BASEURL;

        if (doc && sha.digest("hex") == doc.password) {
            bizname = req.body.username.concat("'s Widgets");
            console.log("serverurl is " + serverurl);

            var ret = {
                ticket:doc.client_key,
                // Hardcoded merchant details for development. But regardless, somehow you need to get this
                // info to the SDK
                //
                    merchant:{
                        //businessName:'Acme Widgets',
                        businessName:bizname,
                        country:"US", line1:'Address Not Specified',
                        city:'Boston', state:'MA', postalCode:'02110',
                        currency: "USD"
                    },
                    serverinfo: {
                        paypalserver:serverurl
                    }
            };
            if (config.CENTRAL_SERVER_MODE && doc.refresh) {
                oauth.importToken(users, doc, function (err,info) {
                   if (err) {
                       console.log(err);
                       next(err);
                   } else {
                       info.merchant = ret.merchant;
                       info.ticket = ret.ticket;
                       res.json(info);
                   }
                });
            } else {
                res.json(ret);
            }
        } else {
            res.json({ticket:null});
        }

    });
});

/**
 * Get the PayPal link for PayPal access and include our return URL
 * First we validate that the ticket is good, then either get the
 * PayPal Access credentials from our local storage, or send them
 * to a URL to get credentials for us
 */
app.post('/goPayPal', function (req, res, next) {
    users.get(req.body.username, function (err, doc) {
        if (err) {
            console.log(err);
            res.json(err, 500);
            return;
        }
        // Make sure the user has the right credentials for our service
        if (doc && doc.client_key == req.body.ticket) {
            // Ok, now if we have a refresh/access token, let's sort it out and return it directly
            // TODO this shouldn't happen anymore because login returns it
            if (config.CENTRAL_SERVER_MODE && doc.access) {
                oauth.importToken(users, doc, function (err,info) {
                    if (err) {
                        console.log(err);
                        next(err);
                    } else {
                        res.json(info);
                    }
                });
                return;
            }
            // Otherwise, it's off to PayPal they go.
            var returnUrl = oauth.getLocalUrl() + "/goApp?u=" + encodeURIComponent(req.body.username);
            var url = config.PAYPAL_ACCESS_BASEURL + "auth/protocol/openidconnect/v1/authorize?scope=" +
                encodeURIComponent(config.PAYPAL_SCOPES) + "&response_type=code&redirect_uri=" +
                encodeURIComponent(returnUrl) + "&nonce=123465908&client_id=" + config.PAYPAL_APP_ID;
            console.log("Sending to", url);
            res.json({url:url});
        } else {
            console.log("Unknown user called goPayPal.");
            res.json({url:null, error:"Couldn't find user account."});
        }
    });
})

/**
 * PayPal will send the browser back here after they login
 */
app.get('/goApp', function (req, res, next) {
    console.log("Back from PayPal", req.url);
    users.get(req.query.u, function (err, userDoc) {
        request.post({
            url:config.PAYPAL_ACCESS_BASEURL + "auth/protocol/openidconnect/v1/tokenservice",
            auth:{
                user:config.PAYPAL_APP_ID,
                pass:config.PAYPAL_SECRET,
                sendImmediately:true
            },
            form:{
                grant_type:"authorization_code",
                code:req.query.code
            }
        }, oauth.jsonWrap(function (error, response, info) {
            if (error) {
                next(error);
            } else {
                try {
                    if (info && info.access_token && info.refresh_token) {
                        oauth.buildAppURL(users, userDoc, info, function (err, url) {
                            if (err) {
                                next(err);
                            }
                            else {
                                res.redirect(url);
                            }
                        });
                    } else {
                        res.json({error:"Return value missing",info:info}, 500);
                    }
                } catch (x) {
                    console.log("Failed to redirect", x, info);
                    res.json({error:x}, 500);
                }
            }
        }));
    });
});

/**
 * If you're using the center server version where the server manages tokens,
 * this call will proxy all calls to the PayPal Here API.
 */
app.all('/paypal/*', function (req, res, next) {
    var auth = req.headers.Authorization
    users.get(req.params.user, function (err, doc) {
        if (err) {
            console.log(err);
            res.json(err, 500);
            return;
        }
        if (doc && doc.client_key == req.body.ticket) {
            res.json({v:req.params[0]}, 500);
        }
    });
});

/**
 * Your app (via the SDK) should call back this URL to refresh tokens if it manages its own calls to the PayPal API.
 * It will pass the URL we gave it during initial setup. If you're using the server proxy approach,
 * this is never needed because we do the refreshing under the covers.
 */
app.get('/refresh/:user/*', function (req, res, next) {
    console.log("Refresh token request for", req.params.user);
    var token = req.params[0];
    users.get(req.params.user, function (err, userDoc) {
        if (err) {
            res.json(err,500);
            return;
        }
        userDoc.expires = 0; // force a refresh
        ppCrypto.decryptToken(token, userDoc.server_secret, function (cError, plainText) {
            if (cError) {
                console.log("Decryption failed.");
                res.json(cError, 500);
                return;
            }

            if (userDoc.refresh == plainText) {
                oauth.ensureToken(users, userDoc, function (err,info) {
                    if (err) { res.json(err, 500); return; }
                    // TODO the SDK doesn't support getting an encrypted access token for refresh yet.
                    ppCrypto.encryptToken(userDoc.refresh, userDoc.server_secret, function (err2, refresh_token) {
                        if (err2) {
                            cb(err2);
                        }
                        res.json({
                           access_token: info.access_token,
                           refresh_url: oauth.getLocalUrl() + "/refresh/" +
                               encodeURIComponent(userDoc.username) + "/" + refresh_token,
                           expires_in: info.expires_in
                        });
                    });
                });
            } else {
                console.log("Invalid refresh token presented.");
                res.json({error:"Invalid token"}, 401);
            }
            request.post({
                url:config.PAYPAL_ACCESS_BASEURL + "auth/protocol/openidconnect/v1/tokenservice",
                auth:{
                    user:config.PAYPAL_APP_ID,
                    pass:config.PAYPAL_SECRET,
                    sendImmediately:true
                },
                form:{
                    grant_type:"refresh_token",
                    refresh_token:plainText
                }
            }, function (error, response, body) {
                if (error) {
                    res.json(error, 500);
                }
            });
        });
    });
});
