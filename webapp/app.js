/*jslint node: true */
"use strict";

/*
 *
 */

var util = require('util');

// Setup Dependencies
var http = require('http');

// Setup Core Module
var api = require('./api.js');

// Setup Express
var express = require('express');
var bodyParser = require('body-parser');
var app = express();

app.use(bodyParser.json());

app.use('/api', api);

app.use(express.static('static'));

// Error Handlers
app.use(function(req, res, next) {
  // Catch unseen routes
  var err = new Error(http.STATUS_CODES[404]);
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
  }
  if (err.message) {
    // Append message if one included
    why.message = err.message;
  }
  res.json(why);
});

module.exports = app;

