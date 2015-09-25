var needle = require('needle');
var program = require('commander');

program.
    version("1.0").
    option('-r, --returnUrl [url]', 'Return URL for the application').
    option('-n, --name [name]', 'Display name for the application').
    option('-c, --client [name]', 'The client id for the application')
    .parse(process.argv);

if (!program.returnUrl || !program.name || !program.client) {
    program.help();
}

needle.post("https://www.stage2pph03.qa.paypal.com/webapps/auth/protocol/openidconnect/v1/clientregistrationservice",
    {
        required_permissions: "firstname lastname fullname street city state country emailID dateofbirth https://uri.paypal.com/services/paypalhere payerID emailVerified phoneNumber language accountType accountCreationDate businessname",
        supported_protocols: "openid_connect",
        type: "client_associate",
        client_id: program.client,
        client_secret: "A8VERY8SECRET8VALUE0",
        contacts: "sp-us-b1@paypal.com",
        application_name: program.name,
        application_type: "web",
        display_name: program.name,
        token_endpoint_auth_type: "client_secret_post",
        redirect_uris: program.returnUrl,
        user_id_type: "public",
        display_new_ui: "true",
        security_namespace: "SSO",
        group_id: "test_group_id",
        merchant_tier: "merchant_tier_0"
    }, {
        rejectUnauthorized: false
    }, function (err, response, body) {
    console.log(err||response.statusCode);
    console.log(body);
});
