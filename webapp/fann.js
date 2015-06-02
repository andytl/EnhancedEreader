/*jslint node: true */
"use strict";

var util = require('util');
var cp = require('child_process');
// Setup Express
var express = require('express');
var router = express.Router();

router.post('/train', function (req, res, next) {
  runTask(
      'fann_trainer', 
      null, 
      {
        timeout: 30000,
        stdio: ['pipe', 'pipe', 'pipe']
      },
      function (stdout, stderr) { },
      function (stdout, stderr) { }
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
  task.on('close', function(exitcode) {
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
