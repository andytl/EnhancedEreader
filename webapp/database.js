/*jslint node: true */
"use strict";

/*
 */

// Core
var fs = require("fs");

// Dependancies
var sqlite3 = require('sqlite3').verbose();

var file = 'data/database.sqlite';
var exists = fs.existsSync(file);

// Load the database
var db = new sqlite3.Database(file);

if (!exists) {
  db.serialize(function () {
    db.run('CREATE TABLE user ' + 
        '(username VARCHAR(255), password VARCHAR(255))');
    db.run('CREATE TABLE entry ' + 
        //'(eid INTEGER, uid INTEGER, ' +
        '(eid INTEGER, username REFERENCES user(username), ' +
        'focusrate DOUBLE, date INTEGER)');
  });
}

/*
 * Query for user and password
 * callback(err, res)
 * Callback is passed either uid or null
 * err is set if something bad happened
 */
function authUser(username, password, callback) {
  db.get('SELECT * FROM user WHERE username=? AND password=?',
      [username, password],
      function (err, row) {
        if (err) {
          //TODO
        } else {
          return callback(null, row ? row : null);
        }
      });
}

function getUserByName(username, callback) {
  db.get('SELECT * FROM user WHERE username=?', [username],
      function (err, row) {
        if (err) {
          //TODO
        } else {
          return callback(null, row ? row : null);
        }
      });
}

function makeUser(username, password, callback) {
  db.run('INSERT INTO user (username, password) VALUES (?, ?)',
      [username, password],
      function (err) {
        if (err) {
          //TODO
        } else {
          return callback(null);
        }
      });
}

function addEntry(uid, focusrate, date, callback) {
  // TODO: Ensure that we only put in one entry per datetime per user
  db.run('INSERT INTO entry (uid, focusrate, date) VALUES (?, ?, ?)',
      [uid, focusrate, date],
      function (err) {
        if (err) {
          //TODO
        } else {
          return callback(null);
        }
      });
}

function getEntries(uid, callback) {
  db.all('SELECT * FROM entry WHERE uid=?',
      [uid],
      function (err, rows) {
        if (err) {
          //TODO
        } else {
          return callback(null, rows ? rows : null);
        }
      });
}

module.exports = {
  authUser: authUser,
  getUserByName: getUserByName,
  makeUser: makeUser,
  addEntry: addEntry,
  getEntries: getEntries
};

