/*jslint node: true */
"use strict";

/*
 */

var util = require('util');
// Setup Express
var express = require('express');
var router = express.Router();

var database = require('./database.js');

router.get('/users', function(req, res, next) {
  database.getUsers(dataResponse.bind(null, res, next, function (data) {
    var newData = data.map(function (d) {
      return d.username;
    });
    return newData;
  }));
});



// TODO: remove this
router.get('/user/:username', function (req, res, next) {
  var params = req.params;
  if (checkMissingParams(params, ['username'], next)) {
    return;
  }
  database.getUserByName(params.username,
      dataResponse.bind(null, res, next, function (data) {
        return data;
      })
  );
});

router.post('/user', function (req, res, next) {
  var params = req.body;
  if (checkMissingParams(params, ['username', 'password'], next)) {
    return;
  }
  database.makeUser(params.username, params.password,
      confirmResponse.bind(null, res, next));
});


router.get('/entry/:username', function(req, res, next) {
  var params = req.params;
  if (checkMissingParams(params, ['username'], next)) {
    return;
  }
  database.getEntries(params.username,
      dataResponse.bind(null, res, next, function (data) {
        var filteredData = data.map(function(d) {
          delete d.username;
          return d;
        });
        return {
          username: params.username,
          entries: filteredData
        };
      })
  );
});

router.post('/entry', function(req, res, next) {
  var params = req.body;
  if (checkMissingParams(params, ['username', 'focusrate', 'timestamp'], next)) {
    return;
  }
  database.addEntry(params.username, params.focusrate, params.timestamp,
      confirmResponse.bind(null, res, next));
});


function confirmResponse(res, next, err) {
  if (err) {
    var ex = new Error('Internal Database Error');
    ex.message = err.toString();
    ex.status = 500;
    next(ex);
  } else {
    res.json({ success: true });
  }
}

// Datafctn should take the database data and return the json to send back
function dataResponse(res, next, datafctn, err, data) {
  if (err) {
    var ex = new Error('Internal Database Error');
    ex.message = err.toString();
    ex.status = 500;
    next(ex);
  } else {
    res.json(datafctn(data));
  }
}

// Checks that required parameters are present
// Returns if all parameters are satisfied
// Automatically sends response if parameters are missing
function checkMissingParams(params, required, next) {
  var missing = [];
  required.forEach(function(r) {
    if (!params[r]) {
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


/*
function sendResponse(res, next) {
  fs.readFile('data/' + filename + '.json', function(fserr, data) {
    if (fserr) {
      var err = new Error(http.STATUS_CODES[500]);
      err.status = 500;
      err.message = 'Could not open file: ' + filename;
      next(err);
    } else {
      res.set('Content-Type', 'application/json');
      res.send(data);
    }
  });
}
*/

module.exports = router;
