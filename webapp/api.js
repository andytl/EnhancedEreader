/*jslint node: true */
"use strict";

/*
 */

var util = require('util');
// Setup Express
var express = require('express');
var router = express.Router();

var database = require('./database.js');

// TODO: remove this
router.get('/user/:username', function (req, res, next) {
  req.send('hello');
});

router.post('/user', function (req, res, next) {
  console.log(util.inspect(req.body));
  database.makeUser(req.body.user, req.body.pass, function (err) {
    if (err) {
      res.status(500).json({ error: 'message' });
    } else {
      res.json({ success: true });
    }
  });
});


router.get('/entry/:userid', function(req, res, next) {
  res.send('hello');
});

router.post('/entry', function(req, res, next) {
  res.send('hello world');
});





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
