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

// Views
app.set('view engine', 'jade');
app.set('views', './views');
// APIs
app.use('/api', api);
app.use('/random', random);
app.use('/fann', fann);

// Static Paths
app.use('/static', express.static('static'));
app.use('/:page', function (req, res, next) {
    res.render(req.params.page);
});
app.use('/', function (req, res, next) {
    res.render('index');
});


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
  console.log(chalk.green.bold('Recieved request'));
  console.log(chalk.blue('Time   ') + new Date());
  console.log(chalk.blue('From   ') + util.inspect(req.ip));
  console.log(chalk.blue('For    ') + req.path);
  console.log(chalk.blue('Params ') + util.inspect(req.params));
  next();
}

function logerror(why) {
  console.log(chalk.red.bold('Request Error'));
  console.log(chalk.blue('Status  ') + why.status);
  console.log(chalk.blue('Message ') + why.message);
}

module.exports = app;

