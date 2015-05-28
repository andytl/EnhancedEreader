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
    db.run('CREATE TABLE user ( ' + 
        'username VARCHAR(255) PRIMARY KEY, ' +
        'password VARCHAR(255) ' +
        ')');
    db.run('CREATE TABLE entry ( ' +
        'username REFERENCES user(username), ' +
        'focusrate DOUBLE, ' +
        'timestamp INTEGER, ' +
        'PRIMARY KEY (username, timestamp) ' +
        ')');
  });
}

function getUsers(callback) {
  db.all('SELECT user.username FROM user', resultCallback.bind(null, callback));
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
      resultCallback.bind(null, callback));
}

function getUserByName(username, callback) {
  db.get('SELECT * FROM user WHERE username=?', [username],
      resultCallback.bind(null, callback));
}

function makeUser(username, password, callback) {
  db.run('INSERT INTO user (username, password) VALUES (?, ?)',
      [username, password],
      confirmCallback.bind(null, callback));
}

function addEntry(username, focusrate, timestamp, callback) {
  db.run('INSERT INTO entry (username, focusrate, timestamp) VALUES (?, ?, ?)',
      [username, focusrate, timestamp],
      confirmCallback.bind(null, callback));
}

function getEntries(username, callback) {
  db.all('SELECT * FROM entry WHERE username=?',
      [username],
      resultCallback.bind(null, callback));
}

function resultCallback(callback, err, rows) {
  if (err) {
    //TODO
    callback(err);
  } else {
    return callback(null, rows ? rows : null);
  }
}

function confirmCallback(callback, err) {
  if (err) {
    //TODO
    callback(err);
  } else {
    return callback(null);
  }
}

module.exports = {
  authUser: authUser,
  getUserByName: getUserByName,
  makeUser: makeUser,
  addEntry: addEntry,
  getEntries: getEntries,
  getUsers: getUsers
};

