/**
 *
 * @returns
 */

"use strict";

function ConferenceManager(initialValues)
{
	var thiz = this;
	this.websocket_url = initialValues.websocket_url;
	this.callback = initialValues.callback;

	this.webSocketAdaptor = new WebSocketAdaptor();

	this.joinRoom = function (roomName, streamId) {
		thiz.roomName = roomName;

		var jsCmd = {
				command : "joinRoom",
				room: roomName,
				streamId: streamId,
		}

		thiz.webSocketAdaptor.send(JSON.stringify(jsCmd));
	}

	this.leaveFromRoom = function() {
		var jsCmd = {
				command : "leaveFromRoom",
		};

		thiz.webSocketAdaptor.send(JSON.stringify(jsCmd));
	}

	function WebSocketAdaptor() {
		var wsConn = new WebSocket(thiz.websocket_url);

		var connected = false;

		var pingTimerId = -1;

		var clearPingTimer = function() {
			if (pingTimerId != -1) {
				if (thiz.debug) {
					console.debug("Clearing ping message timer");
				}
				clearInterval(pingTimerId);
				pingTimerId = -1;
			}
		}

		var sendPing = function() {
			var jsCmd = {
					command : "ping"
			};
			wsConn.send(JSON.stringify(jsCmd));
		}

		this.close = function() {
			wsConn.close();
		}

		wsConn.onopen = function() {
			if (thiz.debug) {
				console.log("websocket connected");
			}

			pingTimerId = setInterval(() => {
				//sendPing();
			}, 3000);

			connected = true;
			thiz.callback("initialized");
		}

		this.send = function(text) {

			if (wsConn.readyState == 0 || wsConn.readyState == 2 || wsConn.readyState == 3) {
				thiz.callbackError("WebSocketNotConnected");
				return;
			}
			wsConn.send(text);
			console.log("sent message:" +text);
		}

		this.isConnected = function() {
			return connected;
		}

		wsConn.onmessage = function(event) {
			var obj = JSON.parse(event.data);

			thiz.callback(obj);
		}

		wsConn.onerror = function(error) {
			console.log(" error occured: " + JSON.stringify(error));
			clearPingTimer();
			thiz.callbackError(error)
		}

		wsConn.onclose = function(event) {
			connected = false;
			console.log("connection closed.");
			clearPingTimer();
			thiz.callback("closed", event);
		}
	}
}
