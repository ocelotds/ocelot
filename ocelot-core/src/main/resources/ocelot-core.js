/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
function OcelotController() {
	var ws;
	this.init = function () {
		if (!ws || ws.status !== "OPEN") {
			if (document.location.href.toString().indexOf(document.location.protocol + "//" + document.location.hostname + ":" + document.location.port + "%CTXPATH%") === 0) {
				ws = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "%CTXPATH%/endpoint");
			} else {
				ws = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "/endpoint");
			}
			ws.tokens = {};
			ws.topicHandlers = {};
			ws.status = "OPEN";
			ws.onmessage = function (evt) {
				var msgToClient = JSON.parse(evt.data);
				if (msgToClient.result) {
					if (this.topicHandlers[msgToClient.id]) {
						this.topicHandlers[msgToClient.id](msgToClient.result);
					} else if (this.tokens[msgToClient.id] && this.tokens[msgToClient.id].onResult) {
						// Si le msgToClient à une date d'expiration alors on stocke dans le cache
						if(msgToClient.deadline) {
							localStorage.setItem(msgToClient.id, JSON.stringify(msgToClient));
						}
						this.tokens[msgToClient.id].onResult(msgToClient.result);
					}
				} else if (msgToClient.fault) {
					this.tokens[msgToClient.id].onFault(msgToClient.fault);
				}
			};
			ws.onopen = function (evt) {
				this.status = "OPEN";
				if (this.topicHandlers["ocelot-status"])
					this.topicHandlers["ocelot-status"](this.status);
			};
			ws.onerror = function (evt) {
				this.status = "ERROR";
				if (this.topicHandlers["ocelot-status"])
					this.topicHandlers["ocelot-status"](this.status);
			};
			ws.onclose = function (evt) {
				this.status = "CLOSED";
				if (this.topicHandlers["ocelot-status"])
					this.topicHandlers["ocelot-status"](this.status);
			};
		}
	};
	this.close = function () {
		if (ws) {
			ws.close();
		}
	};
	this.subscribe = function (token) {
		if (ws.status === "OPEN") {
			ws.topicHandlers[token.message] = token.onMessage;
			if (token.message === "ocelot-msg") {
			} else {
				var command = "{\"cmd\":\"subscribe\",\"msg\":\"" + token.message + "\"}";
				ws.send(command);
			}
		} else {
			this.showErrorSocketIsClosed();
		}
	};
	this.unsubscribe = function (token) {
		if (ws.status === "OPEN") {
			ws.topicHandlers[token.message] = null;
			if (token.message !== "ocelot-status" && token.message !== "ocelot-msg") {
				var command = "{\"cmd\":\"unsubscribe\",\"msg\":\"" + token.message + "\"}";
				ws.send(command);
			}
		} else {
			this.showErrorSocketIsClosed();
		}
	};
	this.call = function (token) {
		// vérification dans le cache
		var res = localStorage.getItem(token.id);
		if(!token.ignoreCache && res) {
			var msgToClient = JSON.parse(res);
			// si present vérification de la péremption
			var now = new Date().getTime();
			if(now < msgToClient.deadline) {
				// si present et non périmé, on retourne le resultat sans faire call
				token.onResult(msgToClient.result);
				return;
			}
		}
		// sinon on call
		if (ws.status === "OPEN") {
			ws.tokens[token.id] = token;
			ws.send("{\"cmd\":\"call\",\"msg\":" + token.getMessage() + "}");
		} else {
			this.showErrorSocketIsClosed();
		}
	};
	this.showErrorSocketIsClosed = function () {
		alert("WebSocket is not open");
	};
	this.init();
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
window.addEventListener("beforeunload", function (e) {
	if (ocelotController) {
		ocelotController.close();
	}
});
function Mdb(topic) {
	var topic = topic;
	this.subscribe = function () {
		var subEvent = document.createEvent("Event");
		subEvent.initEvent("subscribe", true, false);
		subEvent.message = topic;
		subEvent.onMessage = this.onMessage;
		document.dispatchEvent(subEvent);
	};
	this.unsubscribe = function () {
		var unsubEvent = document.createEvent("Event");
		unsubEvent.initEvent("unsubscribe", true, false);
		unsubEvent.message = topic;
		document.dispatchEvent(unsubEvent);
	};
	this.onMessage = function (msg) {
	};
}
var getOcelotEvent = function getOcelotEvent(id, op, args) {
	var evt = document.createEvent("Event");
	evt.initEvent("call", true, false);
	evt.dataservice = this.ds;
	evt.ignoreCache = false;
	evt.operation = op;
	evt.args = args;
	evt.delay = 0;
	evt.id = id;
	evt.getMessage = function() {
		return "{\"id\":\"" + this.id + "\",\"ds\":\"" + this.dataservice + "\",\"op\":\"" + this.operation + "\", \"args\":" + JSON.stringify(this.args) + "}";
	};
	evt.onResult = function (msg) {
	};
	evt.onFault = function (fault) {
	};
	setTimeout(function () {
		setTimeout(function () {
			document.dispatchEvent(evt);
		}, evt.delay);
	}, 1);
	return evt;
};

/**
 * Calculate a 32 bit FNV-1a hash
 * @returns {String}
 */
String.prototype.hashCode = function () {
	var i, l;
	var hval = 0x811c9dc5;
	for (i = 0, l = this.length; i < l; i++) {
		hval ^= this.charCodeAt(i);
		hval += (hval << 1) + (hval << 4) + (hval << 7) + (hval << 8) + (hval << 24);
	}
	return ("0000000" + (hval >>> 0).toString(16)).substr(-8);
}
