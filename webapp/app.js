/*jslint node: true */
"use strict";

/*
 * Eyetracker Webapp
 */

var util = require('util');
var chalk = require('chalk');

// Setup Dependencies
var http = require('http');

// Setup Modules
var api = require('./api.js');
var random = require('./randomdata.js');
var fann = require('./fann.js');

// Setup Express
var express = require('express');
var app = express();

app.use(logger); // Echo requests for debug

app.use('/api', api);
app.use('/random', random);
app.use('/fann', fann);

app.use(express.static('static'));

// Error Handlers
app.use(function(req, res, next) {
  // Catch unseen routes
  var err = new Error(http.STATUS_CODES[404] + ' - ' + req.path);
  err.status = 404;
  next(err);
});
app.use(function(err, req, res, next) {
  // Catch all to send an error
  var why = {
    'stacktrace': err.stack
  };
  if (err.status) {
    res.status(err.status);
    why.status = err.status;
  } else {
    // Other error is an internal fault
    res.status(500);
    why.status = 500;
  }
  if (err.message) {
    // Append message if one included
    why.message = err.message;
  }
  logerror(why);
  res.json(why);
});

function logger(req, res, next) {
  console.log(chalk.green('======>') + ' Got request');
  console.log('Time   ' + new Date());
  console.log('From   ' + util.inspect(req.ip));
  console.log('For    ' + util.inspect(req.path));
  console.log('Params ' + util.inspect(req.params));
  next();
}

function logerror(why) {
  console.log(chalk.red('======>') + ' Error');
  console.log('Status  ' + why.status);
  console.log('Message ' + why.message);
}

module.exports = app;

