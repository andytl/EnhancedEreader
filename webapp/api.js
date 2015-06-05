/*jslint node: true */
"use strict";

/*
 */

var util = require('util');
// Setup Express
var express = require('express');
var bodyParser = require('body-parser');
var router = express.Router();

var database = require('./database.js');
var lib = require('./library.js');

router.use(bodyParser.json());
router.use(lib.logBody);

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
  if (lib.checkMissingParams(params, ['username'], next)) {
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
  if (lib.checkMissingParams(params, ['username', 'password'], next)) {
    return;
  }
  database.makeUser(params.username, params.password,
      confirmResponse.bind(null, res, next));
});

router.get('/entry', function(req, res, next) {
  database.getAllEntries(
      dataResponse.bind(null, res, next, function (data) {
        var result = {};
        data.forEach(function (d) {
          if (!result[d.username]) {
            result[d.username] = [];
          }
          result[d.username].push({
            focusrate: d.focusrate,
            timestamp: d.timestamp,
            totaltime: d.totaltime,
            timereading: d.timereading,
            dartingrate: d.dartingrate
          });
        });
        return result;
      })
  );
});

router.get('/entry/:username', function(req, res, next) {
  var params = req.params;
  if (lib.checkMissingParams(params, ['username'], next)) {
    return;
  }
  database.getEntries(params.username,
      dataResponse.bind(null, res, next, function (data) {
        var filteredData = data.map(function (d) {
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
  if (lib.checkMissingParams(params,
        ['username', 'focusrate', 'timestamp', 'totaltime', 'timereading', 'dartingrate'],
        next)) {
    return;
  }
  database.addEntry(params.username, params.focusrate, params.timestamp,
      params.totaltime, params.timereading, params.dartingrate,
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

module.exports = router;

