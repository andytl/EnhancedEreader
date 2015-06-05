/*jslint node: true */
"use strict";

var util = require('util');
var fs = require('fs');
var cp = require('child_process');
// Setup Express
var express = require('express');
var router = express.Router();
var concat = require('concat-stream');


if (!fs.existsSync('tmp/')) {
  fs.mkdirSync('tmp/');
}

router.use(function (req, res, next) {
  req.pipe(concat(function (data) {
    req.body = data;
    next();
  }));
});

router.post('/train', function (req, res, next) {
  var fname = 'tmp/' + Math.random() + 'data';
  if (req.body === undefined) {
    var err = new Error('No train data specified');
    err.status = 400;
    next(err);
    return;
  }
  console.log('Timing:');
  console.time('fanntrain');
  fs.writeFileSync(fname + '.in', req.body);
  runTask(
      'eyetrain', 
      [fname + '.in', fname + '.out'], 
      {
        timeout: 30000,
      },
      function (stdout, stderr) {
        console.timeEnd('fanntrain');
        var err = new Error('Fann Processing failed');
        err.message = stderr.toString();
        err.status = 500;
        fs.unlinkSync(fname + '.in');
        next(err);
      },
      function (stdout, stderr) {
        console.timeEnd('fanntrain');
        res.send(fs.readFileSync(fname + '.out'));
        fs.unlinkSync(fname + '.in');
        fs.unlinkSync(fname + '.out');
      }
  );
});

// succ(output)
// err(output)
function runTask(command, args, opts, err, succ) {
  var stdoutFrags = [];
  var stderrFrags = [];
  var task;
  if (args === null) {
    task = cp.spawn(command, opts);
  } else {
    task = cp.spawn(command, args, opts);
  }
  task.stdout.on('data', function (data) { stdoutFrags.push(data); });
  task.stderr.on('data', function (data) { stderrFrags.push(data); });
  task.on('close', function (exitcode) {
    var stdout = Buffer.concat(stdoutFrags);
    var stderr = Buffer.concat(stderrFrags);
    if (exitcode !== 0) {
      err(stdout, stderr);
    } else {
      succ(stdout, stderr);
    }
  });
}

module.exports = router;
