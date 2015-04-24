function OcelotController() {
	var ws;
	if(document.location.href.toString().indexOf(document.location.protocol+"//"+document.location.hostname + ":" + document.location.port+"%CTXPATH%")===0) {
		ws = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "%CTXPATH%/endpoint");
	} else {
		ws = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "/endpoint");
	}
	ws.topicHandlers = {};
	ws.resultHandlers = {};
	ws.faultHandlers = {};

	this.subscribe = function (token) {
		var command = "{\"topic\":\"" + token.topic + "\",\"cmd\":\"subscribe\"}";
		ws.send(command);
		ws.topicHandlers[token.topic] = token.onMessage;
	};
	this.unsubscribe = function (token) {
		ws.topicHandlers[token.topic] = null;
	};
	this.call = function (token) {
		var uuid = new UUID().random();
		ws.resultHandlers[uuid] = token.onResult;
		ws.faultHandlers[uuid] = token.onFault;
		var msg = "{\"id\":\"" + uuid + "\",\"ds\":\"" + token.dataservice + "\",\"op\":\"" + token.operation + "\", \"args\":"+JSON.stringify(token.args)+"}";
		var command = "{\"topic\":\"" + token.factory + "\",\"cmd\":\"call\",\"msg\":" + msg + "}";
		ws.send(command);
	};
	ws.onmessage =function (evt) {
		var msgToClient = JSON.parse(evt.data);
		if(msgToClient.result) {
			if(this.topicHandlers[msgToClient.id]) {
				this.topicHandlers[msgToClient.id](msgToClient.result);
			} else if(this.resultHandlers[msgToClient.id]) {
				this.resultHandlers[msgToClient.id](msgToClient.result);
			}
		} else if(msgToClient.fault) {
			this.faultHandlers[msgToClient.id](msgToClient.fault);
		}
	};
	ws.onopen = function (evt) {
		alert(evt.toString());
	};
	ws.onerror = function (evt) {
		alert(evt.toString());
	};
}
function UUID() {
	this.random = function () {
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
			var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
			return v.toString(16);
		});
	};
}
var controller;
document.addEventListener("subscribe", function (event) {
	controller.subscribe(event);
});
document.addEventListener("unsubscribe", function (event) {
	controller.unsubscribe(event);
});
document.addEventListener("call", function (event) {
	controller.call(event);
});
window.addEventListener("load", function (event) {
	controller = new OcelotController();
});
function Mdb(topic) {
	var topic = topic;
	this.subscribe = function() {
		var subEvent = document.createEvent("Event");
		subEvent.initEvent("subscribe", true, false);
		subEvent.topic = topic;
		subEvent.onMessage = this.onMessage;
		document.dispatchEvent(subEvent);
	};
	this.unsubscribe = function() {
		var unsubEvent = document.createEvent("Event");
		unsubEvent.initEvent("unsubscribe", true, false);
		unsubEvent.topic = topic;
		document.dispatchEvent(unsubEvent);
	};
	this.onMessage = function(msg) {};
}
