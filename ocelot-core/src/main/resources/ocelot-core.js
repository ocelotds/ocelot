/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
function OcelotController() {
	var ws;
	this.init = function() {
		if(!ws || ws.status !== "OPEN") {
			if(document.location.href.toString().indexOf(document.location.protocol+"//"+document.location.hostname + ":" + document.location.port+"%CTXPATH%")===0) {
				ws = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "%CTXPATH%/endpoint");
			} else {
				ws = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "/endpoint");
			}
			ws.topicHandlers = {};
			ws.resultHandlers = {};
			ws.faultHandlers = {};
			ws.status = "OPEN";
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
				this.status = "OPEN";
				if(this.topicHandlers["ocelot-status"]) this.topicHandlers["ocelot-status"](this.status);
			};
			ws.onerror = function (evt) {
				this.status = "ERROR";
				if(this.topicHandlers["ocelot-status"]) this.topicHandlers["ocelot-status"](this.status);
			};
			ws.onclose = function (evt) {
				this.status = "CLOSED";
				if(this.topicHandlers["ocelot-status"]) this.topicHandlers["ocelot-status"](this.status);
			};
		}
	};
	this.init();
	this.subscribe = function (token) {
		if(ws.status === "OPEN") {
			ws.topicHandlers[token.topic] = token.onMessage;
			if(token.topic === "ocelot-msg") {
			} else {
				var command = "{\"topic\":\"" + token.topic + "\",\"cmd\":\"subscribe\"}";
				ws.send(command);
			}
		} else this.showError();
		if(ws.topicHandlers["ocelot-status"]) ws.topicHandlers["ocelot-status"](ws.status);
	};
	this.unsubscribe = function (token) {
		if(ws.topicHandlers["ocelot-status"]) ws.topicHandlers["ocelot-status"](ws.status);
		if(ws.status === "OPEN") {
			ws.topicHandlers[token.topic] = null;
			if(token.topic !== "ocelot-status" && token.topic !== "ocelot-msg") {
				var command = "{\"topic\":\"" + token.topic + "\",\"cmd\":\"unsubscribe\"}";
				ws.send(command);
			}
		} else this.showError();
	};
	this.call = function (token) {
		if(ws.topicHandlers["ocelot-status"]) ws.topicHandlers["ocelot-status"](ws.status);
		if(ws.status === "OPEN") {
			var uuid = new UUID().random();
			ws.resultHandlers[uuid] = token.onResult;
			ws.faultHandlers[uuid] = token.onFault;
			var msg = "{\"id\":\"" + uuid + "\",\"ds\":\"" + token.dataservice + "\",\"op\":\"" + token.operation + "\", \"args\":"+JSON.stringify(token.args)+"}";
			var command = "{\"topic\":\"" + token.factory + "\",\"cmd\":\"call\",\"msg\":" + msg + "}";
			ws.send(command);
		} else this.showError();
	};
	this.showError= function () {
		alert("WebSocket is not open");
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
var ocelotController = new OcelotController();
document.addEventListener("subscribe", function (event) {
	ocelotController.subscribe(event);
});
document.addEventListener("unsubscribe", function (event) {
	ocelotController.unsubscribe(event);
});
document.addEventListener("call", function (event) {
	ocelotController.call(event);
});
window.addEventListener("load", function (event) {
//	ocelotController = new OcelotController();
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
var getOcelotEvent = function getOcelotEvent(op, args) {
	var evt = document.createEvent("Event");
	evt.initEvent("call", true, false);
	evt.factory = this.fid;
	evt.dataservice = this.ds;
	evt.operation = op;
	evt.args = args;
	evt.onResult = function (msg) {};
	evt.onFault = function (fault) {};
	setTimeout(function () {document.dispatchEvent(evt);}, 1);
	return evt;
};
