
/**
 * Contains all network data
 * @type {Object}
 */
var Net = {
	/**
	 * @type {String}
	 */
	HOST : '184.73.152.240',

	/**
	 * @type {String}
	 */
	PROTOCAL : 'http://'
}

/**
 * Contains operations for oauth with Stack Exchange
 * @type {Object}
 */
var StackExchange = {

  /**
   * OAuth callback url
   * @type {String}
   */
  CALLBACK_URL : Net.PROTOCAL + Net.HOST + '/oauth/callback',

  /**
   * client id provided by StackApp
   * @type {string}
   */
  CLIENT_ID : '2836',

  /**
   * Stack Exchange auth url
   * @type {string}
   */
  OAUTH_URL : 'https://stackexchange.com/oauth', 

  /**
   * Returns the oauth request url
   * @return {string} [description]
   */
  getOauthRequestUrl : function() {
    return this.OAUTH_URL + 
    	'?client_id=' + this.CLIENT_ID + 
    	'&scope=private_info' +
    	'&redirect_uri=' + this.CALLBACK_URL;
  },

  /**
   * Performs authentication
   * @return {void} [description]
   */
  authenticate : function() {
  	chrome.identity.launchWebAuthFlow({
		'url': this.getOauthRequestUrl(),
		'interactive' : true
	}, function(redirect_url) {});
  }
}

/**
 * Contains operations for StackNotify operations
 * @type {Object}
 */
var StackNotify = {

	/**
	 * extension identification 
	 * @type {string}
	 */
	googleId : chrome.runtime.id,

	/**
	 * Registers the user's name and googleId
	 * @param  {string} name
	 * @return {void}
	 */
	register : function(name) {
		var me = this;

		$.ajax({
			url : Net.PROTOCAL + Net.HOST + '/users' ,
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
	},

	/**
	 * Sets the channel id of StackNotify object and posts to server
	 * @param {string} id
	 */
	setChannelId : function(id) {
		console.log('channel id : ' + id);
		var me = this;

		$.ajax({
			url : Net.PROTOCAL + Net.HOST + '/users/' + me.googleId + '/channelId',
			type : 'POST',
			data : {
				channelId : me.channelId = id.channelId,
			},
			success : function(xhr) {
				console.log("setChannelId.success");
				console.dir(xhr);
			},
			error : function(xhr) {
				console.log("setChannelId.error");
				console.dir(xhr);
			}
		});
	},

	/**
	 * GCM callback
	 * @param  {Object} payload - object format {'title' : ..., 'link' : ....}
	 * @return {void}
	 */
	messageCallback : function(payload) {
		console.log('messageCallback.start');
		// TODO write payload to the view

		console.log('messageCallback.end');
	},

	/**
	 * Initializes GCM
	 * @return {[type]} [description]
	 */
	initGCM : function() {
		chrome.pushMessaging.onMessage.addListener(this.messageCallback);
	}

}


$(function () {
  
  // TODO alter the view based on if this user has an access_token

  $('#login-button').click(function() {
    var name = $('#login-name').val();

    StackNotify.register(name);
    chrome.pushMessaging.getChannelId(false, StackNotify.setChannelId);

    StackExchange.authenticate();
  });

});
