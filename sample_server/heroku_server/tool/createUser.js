var fs = require('fs'),
    program = require('commander'),
    nstore = require('nstore'),
    crypto = require('crypto'),
    uuid = require('node-uuid');

program.
    version("1.0").
    option('-u, --username [identifier]', 'Identifier for the user', 'tester').
    option('-p, --password [password]', 'Password for the user', 'password').
    parse(process.argv);

var users = nstore.new('../users.db', function () {

    var sha = crypto.createHash('sha1');
    sha.update(program.password);
    users.save(program.username, {
        username: program.username,
        password: sha.digest('hex'),
        client_key: uuid.v4(),
        server_secret: uuid.v4()
    }, function (err) {
        if (err) {
            throw err;
        }
        console.log("Created user successfully.");
    });

});
