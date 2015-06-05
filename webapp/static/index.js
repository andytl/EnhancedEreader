(function () {

window.onload = function() {
  loadUsers();
  loadAll();
};

function loadUsers() {
  $.get('api/users')
    .done(function(data) {
      $('#userlist').html('').append($('<select>').addClass('form-control')
          .append(data.map(function(d) {
            return $('<option>').html(d).val(d);
          })).on('change', loadUser));
    })
    .fail(function(err) {
      console.log(err);
    });
}

function loadUser() {
  var username = $(this).val();
  $.get('api/entry/' + username)
    .done(function(data) {
      $('#visualization svg').empty();
      plotData([getSeries(data.username, data.entries)], '#visualization svg');
    })
    .fail(function(err) {
      console.log(err);
    });
}

function loadAll() {
  $.get('api/entry')
    .done(function(data) {
      var series = [];
      Object.keys(data).forEach(function (user) {
        series.push(getSeries(user, data[user]));
      });
      plotData(series, '#visualization_all svg');
    })
    .fail(function(err) {
      console.log(err);
    });
}

function getSeries(user, entries) {
    return {
      key: user,
      values: entries.map(getPair)
    };
}

function getPair(d) {
  return {
    x: d.timestamp,
    y: d.focusrate
  };
}

function plotData(data, domstring) {
  nv.addGraph(function() {
    var chart = nv.models.lineChart()
      .margin({left: 100})            //Adjust chart margins to give the x-axis some breathing room.
      .useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
    //.transitionDuration(350)        //how fast do you want the lines to transition?
      .showLegend(true)               //Show the legend, allowing users to turn on/off line series.
      .showYAxis(true)                //Show the y-axis
      .showXAxis(true);               //Show the x-axis

    chart.xAxis     //Chart x-axis settings
      .axisLabel('Timestamp (ms, epoch)')
      .tickFormat(d3.format(',r'));

    chart.yAxis     //Chart y-axis settings
      .axisLabel('Focusrate (?)')
      .tickFormat(d3.format('.02f'));

    /* Done setting the chart up? Time to render it!*/

    d3.select(domstring) //Select the <svg> element you want to render the chart in.   
      .datum(data)       //Populate the <svg> element with chart data...
      .call(chart);      //Finally, render the chart!

    //Update the chart when window resizes.
    nv.utils.windowResize(function() { chart.update(); });
    return chart;
  });
}

})();
