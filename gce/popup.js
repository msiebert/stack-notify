

// var HOST = '184.73.152.240';
var HOST = 'localhost:9000';

var SE = {

  redirect_uri : 'http://' + HOST + '/oauth/callback',

  client_id : '2836',

  url : 'https://stackexchange.com/oauth',

  connect : function() {
    window.location.href = SE.url + '?client_id=' + SE.client_id + '&redirect_uri=' + SE.redirect_uri;
  }
}

function StackNotify(extensionId) {
	console.log('extensionid : ' + extensionId);

	this.googleId = extensionId;
}

StackNotify.prototype.register = function(name) {
	var me = this;

	$.ajax({
		url : 'http://' + HOST + '/users' ,
		type : 'POST',
		data : {
			googleId : me.googleId,
			name: name
		},
		success : function(xhr) {
			console.log("register.success");
		},
		error : function(xhr) {
			console.log("register.error");
			console.dir(xhr);
		}
	});
}

StackNotify.prototype.setChannelId = function(id) {
	var me = this;

	$.ajax({
		url : 'http://' + HOST + '/users/' + me.googleId + '/channelId',
		type : 'POST',
		data : {
			channelId : me.channelId = id.channelId,
		},
		success : function(xhr) {
			console.log("setChannelId.success");
		},
		error : function(xhr) {
			console.log("setChannelId.error");
			console.dir(xhr);
		}
	});
}

StackNotify.prototype.messageCallback = function(x) {
	console.log('messageCallback.start');

	console.dir(x);
	// TODO write questions to the viewq

	console.log('messageCallback.end');
}

StackNotify.prototype.setupMessaging = function() {
	chrome.pushMessaging.onMessage.addListener(this.messageCallback);
}

$(function () {
  console.log("loaded.start");

  var stackNotify = new StackNotify(chrome.i18n.getMessage("@@extension_id"));
  
  $('#login-button').click(function() {
    console.log("login-button.start");

    var name = $('#login-name').val();
    console.log('name : ' + name);
    stackNotify.register(name);

    chrome.pushMessaging.getChannelId(false, stackNotify.setChannelId);
    console.log('channelId : ' + stackNotify.channelId);

    
    SE.connect();

    console.log("login-button.end");
  });

  console.log("loaded.end");
});
