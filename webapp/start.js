/*jslint node: true */
"use strict";

/*
 * Start.js
 * This file launches the server
 */

var chalk = require('chalk');
var app = require('./app.js');

app.set('port', 3777);

var server = app.listen(app.get('port'), function() {
  var host = server.address().address;
  var port = server.address().port;

  console.log(chalk.bold('Eyetracker app server started'));
  console.log(chalk.bold('You can start making requests on ' + host + ' on port ' + port));
  console.log();
});
