/*jslint node: true */
"use strict";

/*
 * Database interactions
 */

// Core
var fs = require("fs");

// Dependancies
var sqlite3 = require('sqlite3').verbose();
var squel = require('squel');

var file = 'data/database.sqlite';
var exists = fs.existsSync(file);
var db = new sqlite3.Database(file); // Load the database
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
        'totaltime INTEGER, ' +
        'timereading INTEGER, ' +
        'dartingrate DOUBLE, ' +
        'PRIMARY KEY (username, timestamp) ' +
        ')');
  });
}

function getUsers(callback) {
  db.all('SELECT user.username FROM user', resultCallback.bind(null, callback));
}

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

function addEntry(username, focusrate, timestamp, totaltime, timereading,
    dartingrate, callback) {
  db.run('INSERT INTO entry ' +
      '(username, focusrate, timestamp, totaltime, timereading, dartingrate)' +
      'VALUES (?, ?, ?, ?, ?, ?)',
      [username, focusrate, timestamp, totaltime, timereading, dartingrate],
      confirmCallback.bind(null, callback));
}

function getEntries(username, callback) {
  db.all('SELECT * FROM entry WHERE username=?',
      [username],
      resultCallback.bind(null, callback));
}

function getAllEntries(callback) {
  db.all('SELECT * FROM entry',
      resultCallback.bind(null, callback));
}

function getCumulativeTimes(callback) {
  db.all('SELECT username, SUM(totaltime) AS cumulative FROM entry GROUP BY username',
      resultCallback.bind(null, callback));
}


function addEntries(entries, callback) {
  var query = squel
    .insert()
    .into('entry')
    .setFieldsRows(entries)
    .toString();
  db.run(query, resultCallback.bind(null, callback));
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
  getUsers: getUsers,
  getAllEntries: getAllEntries,
  addEntries: addEntries,
  getCumulativeTimes: getCumulativeTimes
};

