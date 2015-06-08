(function () {

var studentName;

window.onload = function() {
  studentName = $('#student-username');
  Shared.killForms();
  studentName.on('keyup', lookupStudent);
};

function lookupStudent(evt) {
  var name = studentName.val();
  if (evt.keyCode === 13) {
    $.get('api/user/' + name)
      .done(function (data) {
        if (data !== null) {
          loadStudent(name);
        }
      })
      .fail(function (err) {
        console.log(err);
        //TODO Pretty error message
      });
  }
}

function loadStudent(username) {
  $.get('api/entry/' + username)
    .done(function (data) {
      var entries = data.entries;
      var columns = Object.keys(entries[0]);
      var all = {};
      columns.forEach(function (colName) {
        all[colName] = entries.map(getFieldMapper(colName));
      });
      plotGraphs(all);
    })
    .fail(function (err) {
      conosle.log(err);
      //TODO Pretty error message
    });
}

function getFieldMapper(fieldName) {
  return function(entry) {
    var res = {
      x: entry.timestamp
    };
    res.y = entry[fieldName];
    return res;
  };
}

function plotGraphs(all) {
  Shared.plotData(
      [
        Shared.getSeries('Focusrate', all.focusrate),
        Shared.getSeries('Timeread', all.timereading)
      ],
      '#chart-focusrate svg', 'Date', 'Focusrate',
      Shared.timeFormatter, Shared.floatFormatter
  );
  Shared.plotData(
      [Shared.getSeries('DartingRate', all.dartingrate)],
      '#chart-dartrate svg', 'Date', 'DartingRate',
      Shared.timeFormatter, Shared.floatFormatter
  );
}


// Timeread/focusrate are together

})();
