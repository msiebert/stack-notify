
var SE = {
  oauth: "https://stackexchange.com/oauth?client_id=2836&redirect_uri=http://localhost/callback",

  connect : function(url) {
    window.location.href = url;
  }
}

$(function () {
  console.log("loaded");
  
  $('#login-button').click(function() {
    console.log("login button clicked");
    SE.connect(SE.oauth);
  });

});
