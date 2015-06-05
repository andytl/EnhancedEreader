/*jslint node: true */
"use strict";

var util = require('util');

/*
 * Shared code between modules
 */

// Checks that required parameters are present
// Returns if all parameters are satisfied
// Automatically sends response if parameters are missing
function checkMissingParams(params, required, next) {
  var missing = [];
  required.forEach(function(r) {
    if (params[r] === undefined) {
      missing.push(r);
    }
  });
  if (missing.length > 0) {
    var err = new Error('Missing parameters to API call');
    err.status = 400;
    err.message = 'Missing parameters: ' + missing.join(', ');
    next(err);
  }
  return missing.length > 0;
}

function logBody(req, res, next) {
  console.log('Body ' + util.inspect(req.body));
  next();
}

module.exports = {
  checkMissingParams: checkMissingParams,
  logBody: logBody
};

