/*jslint node: true */
"use strict";

/*
 */

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
  for (var i = 0; i < params.n; i++) {
    created.push({
      timestamp: i * params.i + params.s,
      focusrate: chance.floating({min: 0, max: 1, floating: 3}),
      totaltime: chance.integer({min: 10, max: 100}),
      timereading: chance.integer({min: 1, max: 20}),
      dartingrate: chance.floating({min: 0, max: 1, floating: 3})
    });
  }
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
