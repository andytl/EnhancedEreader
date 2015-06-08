/*jslint node: true */
"use strict";

/*
 * This endpoint generates some random data for the API to aid in testing
 * of the application.
 */

var util = require('util');

var chance = require('chance').Chance();
var bodyParser = require('body-parser');
var express = require('express');
var router = express.Router();

var database = require('./database.js');
var lib = require('./library.js');

router.use(bodyParser.json());
router.use(lib.logBody);

router.post('/user', function (req, res, next) {
  var name = chance.first();
  var pass = chance.string({length: 3, pool: 'abc'});
  database.makeUser(
      name,
      pass,
      confirmResponse(req, res, next, {
        username: name,
        password: pass
      })
  );
});

// Generate sequential random datapoints from
// u: username
// n: number of points
// s: start timestamp
// i: interval of timestamp
router.post('/entry', function (req, res, next) {
  var params = req.body;
  if (lib.checkMissingParams(params, ['u', 'n', 's', 'i'], next)) {
    return;
  }
  var created = [];
  var data = {
    timestamp: parseInt(params.s, 10) + 1433709320362,
    focusrate: chance.floating({min: 0, max: 1, floating: 3}),
    totaltime: chance.integer({min: 360000, max: 3600000}),
    timereading: chance.integer({min: 136000, max: 1200000}),
    dartingrate: chance.floating({min: 0, max: 1, floating: 3})
  };
  created.push(data);
  for (var i = 1; i < params.n; i++) {
    var newdata = {
      timestamp: data.timestamp + parseInt(params.i, 10),
      focusrate: nextRandom(data.focusrate, 0, 1, 0.15, 3),
      totaltime: nextRandom(data.totaltime, 360000, 3600000, 36000, 0),
      timereading: nextRandom(data.timereading, 136000, 1200000, 13600, 0),
      dartingrate: nextRandom(data.dartingrate, 0, 1, 0.15, 3)
    };
    created.push(newdata);
    data = newdata;
  }
  console.log(util.inspect(created));
  database.addEntries(
      created.map(function (e) {
        e.username = params.u;
        return e;
      }),
      confirmResponse(req, res, next, {
        username: params.u,
        count: params.n,
        entries: created
      })
  );
});

function nextRandom(input, min, max, stddev, places) {
  var temp = input + chance.normal({ dev: stddev });
  var output = Math.min(max, Math.max(min, temp));
  return Math.round(output * Math.exp(10, places)) / Math.exp(10, places);
}

// Datafctn should take the database data and return the json to send back
function confirmResponse(req, res, next, response) {
  return function (err) {
    if (err) {
      var ex = new Error('Internal Database Error');
      ex.message = err.toString();
      ex.status = 500;
      next(ex);
    } else {
      res.json(buildResponse(response));
    }
  };
}

function buildResponse(newThing) {
  return {
    random: 'success',
    created: newThing
  };
}

module.exports = router;

