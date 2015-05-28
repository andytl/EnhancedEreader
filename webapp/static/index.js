(function () {

window.onload = function() {
  loadUsers();
};

function loadUsers() {
  $.get('api/users')
    .done(function(data) {
      $('#userlist').append($('<select>').addClass('form-control')
          .append(data.map(function(d) {
            return $('<option>').html(d);
          })));
    })
    .fail(function(err) {
      console.log(err);
    });
}



})();
