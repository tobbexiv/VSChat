var Chat = new function() {
	var _lastId				= null;
	var _recievedMessages	= new Map();
	
	var _destroy = function() {
		_recievedMessages.clear();
		$('#chatContent').html('');
		_lastId = null;
	}
	
	var _addMessages = function(messages) {
		$(messages).each(function(index, message) {
			var newerMessageId		= 0;
			var newerMessageSent	= 0;
			
			if(!_recievedMessages.has(message.id)) {
				for (var id of _recievedMessages.keys()) {
					var sentTime = _recievedMessages.get(id);
					
					if(sentTime > message.sentTimeAsLong && (newerMessageSent > sentTime || newerMessageSent === 0)) {
						newerMessageId		= id;
						newerMessageSent	= sentTime;
					}
				}
				
				var messageString = '<tr id="message' + message.id + '">' +
					'<td class="col-sm-3">' + message.sentTime + '</td>' +
					'<td class="col-sm-2">' + message.sender + '</td>' +
					'<td>' + message.message + '</td>' +
				'</tr>';
				
				if(newerMessageId === 0)
					$('#chatContent').append(messageString);
				else
					$(messageString).insertBefore('#message' + newerMessageId);
				
				_recievedMessages.set(message.id, message.sentTimeAsLong);
				
				if(_lastId < message.id || _lastId === null)
					_lastId = message.id;
			}
			
			$('#chatWrapper').scrollTop($('#chatWrapper').prop('scrollHeight'));
		});
	}
	
	this.submitMessage = function() {
		var message = $('#message').val();
		$('#message').val('');
		
		$.ajax({
			url:			sendMessage,
			contentType:	'application/json; charset=utf-8',
			dataType:		'json',
			data:			JSON.stringify({ message: message }),
			method:			'post'
		}).done(function(data) {
			_addMessages(JSON.parse(data));
		});
	};
	
	this.loadMessages = function() {
		$.ajax({
			url:			'/messages/byTime/' + $('#getOld option:selected').val(),
			contentType:	'application/json; charset=utf-8',
			method:			'GET'
		}).done(function(data) {
			_destroy();
			_addMessages(JSON.parse(data));
		});
	};
	
	this.checkForNew = function() {
		if(_lastId === null) {
			$.ajax({
				url:			getLastId,
				contentType:	'application/json; charset=utf-8',
				method:			'GET'
			}).done(function(data) {
				if(_lastId === null) {
					_lastId = data.latestId;
					Chat.checkForNew;
				}
			});
			
			return;
		}
		
		$.ajax({
			url:			'/messages/byId/' + _lastId,
			contentType:	'application/json; charset=utf-8',
			method:			'GET'
		}).done(function(data) {
			_addMessages(JSON.parse(data));
		});
	};
}

$('#submit').on('click', function(event){
	Chat.submitMessage();
});

$('#message').keypress(function(e) {
	var keycode = (e.keyCode ? e.keyCode : e.which);
	
	if(keycode == '13') {
		e.preventDefault();
		Chat.submitMessage();
	}
});

$('#load').on('click', function(event){
	Chat.loadMessages();
});

setInterval(function() {
	Chat.checkForNew();
}, 1000);