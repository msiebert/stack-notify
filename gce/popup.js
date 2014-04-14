
/**
 * Contains all network data
 * @type {Object}
 */
var Net = {
	/**
	 * @type {String}
	 */
	HOST : '184.73.152.240',
	//HOST: 'localhost:9000',

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
  getCallbackUrl : function(id) {
  	return Net.PROTOCAL + Net.HOST + '/oauth/' + id + '/callback';
  },

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
  getOauthRequestUrl : function(id) {
    return this.OAUTH_URL + 
    	'?client_id=' + this.CLIENT_ID + 
    	'&scope=private_info' +
    	'&redirect_uri=' + this.getCallbackUrl(id);
  },

  /**
   * Performs authentication
   * @return {void} [description]
   */
  authenticate : function(id) {
  	chrome.identity.launchWebAuthFlow({
		'url': this.getOauthRequestUrl(id),
		'interactive' : true
	}, function(redirect_url) {});
  }
}

/**
 * Client interface for StackNotify server
 * @type {Object}
 */
var StackNotifyClient = {

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
	register : function(name, channelId) {
		console.log('channel id : ');
		console.dir(channelId);
		var me = this;

		me.channelId = channelId.channelId
		me.googleId = me.channelId;

		$.ajax({
			url : Net.PROTOCAL + Net.HOST + '/users' ,
			type : 'POST',
			data : {
				googleId : me.channelId,
				name: name,
				channelId : me.channelId,
			},
			success : function(xhr) {
				console.log("register.success");
				console.dir(xhr);
			},
			error : function(xhr) {
				console.log("register.error");
				console.dir(xhr);
				UI.setErrorState();
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
		UI.appendQuestion(payload);
	},

	/**
	 * Initializes GCM
	 * @return {[type]} [description]
	 */
	initGCM : function() {
		chrome.pushMessaging.onMessage.addListener(this.messageCallback);
	},

	/**
	 * Checks the registration status of the user
	 * @return {[type]} [description]
	 */
	checkUserAuthStatus : function() {
		$.ajax({
			url : Net.PROTOCAL + Net.HOST + '/users/' + this.googleId + '/authenticated' ,
			type : 'GET',
			success : function(xhr) {
				console.dir(xhr);

				UI.loading().hide();

				if (xhr.result && xhr.result.authenticated) {
					UI.setQuestionState();
				} else {
					UI.setLoginState();
				}
			},
			error : function(xhr) {
				console.log("checkUserAuthStatus.error");

				UI.loading().hide();
				UI.setErrorState();
			}
		});
	}
}

/**
 * Contains all methods and data for User Interface
 * @type {Object}
 */
var UI = {

	/**
	 * Closure for container div
	 * @return {jQuery}
	 *
	 * @private
	 */
	container : function() {
		return $('.container');
	},

	/**
	 * Closure for Loading img
	 * @type {jQuery}
	 *
	 * @private
	 */
	loading: function() {
		return $('#loading');
	},

	/**
	 * Reprsents current state of UI
	 * @type {String}
	 *
	 * @private
	 */
	state : 'login',

	/**
	 * Changes the state to the given state
	 * @param  {string} state
	 * @return {void}
	 *
	 * @private
	 */
	changeState : function(state) {
		console.log('UI state change to ' + state);
		this.state = state;
		this.container().empty();
	},

	/**
	 * Sets UI state to Error
	 */
	setErrorState : function() {
		this.changeState('error');
		this.container().append('<h1 style="color: red;">Epic Fail!</h1>')
	},

	/**
	 * Sets UI state to Login
	 */
	setLoginState : function() {
		this.changeState('login');
		this.container().append('' + 
			'<div class="login">' +
            	'<input id="login-name" type="text" placeholder="Enter your name">' +
            	'<button class="btn btn-primary" id="login-button">Connect to Stack Overflow</button>' +
        	'</div>'
        );

		$('#login-button').click(function() {
		  var name = $('#login-name').val();
		 
		  chrome.pushMessaging.getChannelId(false, function(id) {
		  	 StackNotifyClient.register(name, id);
		  });

		  console.log('finished registration');
		  StackExchange.authenticate(StackNotifyClient.googleId);
		});
	},

	/**
	 * Sets UI state to Questions
	 */
	setQuestionState : function() {
		this.changeState('questions');
		this.container().append('' + 
			'<h2>SO Questions</h2>' +
			'<div class="qs">' +
            	'<ul id="qs-list">' + 
            	'</ul>' +
        	'</div>'
        );
	},

	/**
	 * Appends a question to the current list
	 * @param  {Object<String,String>} question Object of the form {'title' : ..., 'link' : ...}
	 * @return {void}
	 */
	appendQuestion : function(question) {
		if (this.state != 'questions') {
			console.error('wrong state! Current state is ' + this.state);
			return;
		}

		$('#qs-list').append('<li><span><a href="' + question.link + '">' + question.title + '</a>');
	}
}


/**
 * On Page load
 */
$(function () {
	setTimeout(function() {
		StackNotifyClient.checkUserAuthStatus();
	}, 1000);
});
