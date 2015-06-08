(function () {

window.Shared = {
  timeFormatter: function (t) { return d3.time.format('%x')(new Date(t)); },

  floatFormatter: d3.format('.02f'),

  intFormatter: d3.format(',r'),

  killForms: function() {
    $('form').on('submit', function() { return false; });
  },

  plotData: function plotData(data, domstring, xAxisLabel, yAxisLabel, xTickFormatFcn, yTickFormatFcn) {
    nv.addGraph(function() {
      var chart = nv.models.lineChart()
        .margin({left: 100})            //Adjust chart margins to give the x-axis some breathing room.
        .useInteractiveGuideline(true)  //We want nice looking tooltips and a guideline!
      //.transitionDuration(350)        //how fast do you want the lines to transition?
        .showLegend(true)               //Show the legend, allowing users to turn on/off line series.
        .showYAxis(true)                //Show the y-axis
        .showXAxis(true);               //Show the x-axis

      chart.xAxis     //Chart x-axis settings
        .axisLabel(xAxisLabel)
        .tickFormat(xTickFormatFcn);
      chart.yAxis     //Chart y-axis settings
        .axisLabel(yAxisLabel)
        .tickFormat(yTickFormatFcn);

      /* Done setting the chart up? Time to render it!*/

      d3.select(domstring) //Select the <svg> element you want to render the chart in.   
        .datum(data)       //Populate the <svg> element with chart data...
        .call(chart);      //Finally, render the chart!

      //Update the chart when window resizes.
      nv.utils.windowResize(function() { chart.update(); });
      return chart;
    });
  },

  chartData: function (data, domstring, yLabel, yTickFormatFcn) {
    nv.addGraph(function() {
      var chart = nv.models.discreteBarChart()
          .x(function(d) { return d.label; })    //Specify the data accessors.
          .y(function(d) { return d.value; })
          .staggerLabels(true)    //Too many bars and not enough room? Try staggering labels.
          .tooltips(false)        //Don't show tooltips
          .showValues(true)       //...instead, show the bar value right on top of each bar.
        //.transitionDuration(350)
          ;

      chart.yAxis
        .axisLabel(yLabel)
        .tickFormat(yTickFormatFcn);

      d3.select(domstring)
          .datum(data)
          .call(chart);

      nv.utils.windowResize(chart.update);

      return chart;
    });
  },

  getSeries: function (key, entries) {
      return {
        key: key,
        values: entries
      };
  }
};

window.onload = function() {
  if ($('#visualization_all').length) {
    loadAll();
    loadAllFr();
  }
};

function loadAllFr() {
  $.get('api/entry')
    .done(function(data) {
      var series = [];
      Object.keys(data).forEach(function (user) {
        series.push(Shared.getSeries(user, data[user].map(getPair)));
      });
      Shared.plotData(series, '#visualization_all_fr svg',
         'Date', 'Focusrate', Shared.timeFormatter, Shared.floatFormatter);
    })
    .fail(function(err) {
      console.log(err);
    });
}

function loadAll() {
  $.get('api/entries/cumulative')
    .done(function(data) {
      var values = [];
      Object.keys(data).forEach(function (user) {
        values.push({
          label: user,
          value: data[user] / 3600000
        });
      });
      Shared.chartData([Shared.getSeries('Cumulative Time Reading', values)],
          '#visualization_all svg', 'Time (Hr)',
          Shared.floatFormatter
      );
    })
    .fail(function(err) {
      console.log(err);
    });
}

function getPair(d) {
  return {
    x: d.timestamp,
    y: d.focusrate
  };
}

})();
