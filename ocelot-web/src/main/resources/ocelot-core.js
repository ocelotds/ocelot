/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
// globals variables or static class pattern
if ("WebSocket" in window) {
var ocelotController, OcelotCacheManager, OcelotTokenFactory, OcelotEventFactory;
/**
 * Ocelot Controller
 * @returns {OcelotController}
 */
/* ready state : CONNECTING = 0, OPEN = 1, CLOSING = 2, CLOSED = 3; */
if (document.location.href.toString().indexOf(document.location.protocol + "//" + document.location.hostname + ":" + document.location.port + "%CTXPATH%") === 0) {
	ocelotController = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "%CTXPATH%/ocelot-endpoint");
} else {
	ocelotController = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "/ocelot-endpoint");
}
/**
 * Add ocelotController events to document
 * @param {Event} event
 */
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
		ocelotController.dispose();
	}
});
ocelotController.stateLabels = ["CONNECTING", "OPEN", "CLOSING", "CLOSED"];
ocelotController.tokens = {};
ocelotController.topicHandlers = {};
ocelotController.openHandlers = [];
ocelotController.closeHandlers = [];
ocelotController.errorHandlers = [];
/**
 * Add listener launch when ws will be open
 * @param {Function} listener
 */
ocelotController.addOpenEventListener = function (listener) {
	this.openHandlers.push(listener);
};
ocelotController.addCloseEventListener = function (listener) {
	this.closeHandlers.push(listener);
};
ocelotController.addErrorEventListener = function (listener) {
	this.errorHandlers.push(listener);
};
/**
 * Close connection to ws
 * @returns {undefined}
 */
ocelotController.dispose = function () {
	document.dispatchEvent(OcelotTokenFactory.createUnsubscribeToken("ocelot-cleancache"));
	this.close();
};
/**
 * Subscribe to topic 
 * @param {type} token
 * @returns {undefined}
 */
ocelotController.subscribe = function (token) {
	if (this.readyState === 1) {
		this.topicHandlers[token.message] = token.onMessage;
		if (token.message === "ocelot-status") {
		} else {
			this.send("{\"cmd\":\"subscribe\",\"msg\":\"" + token.message + "\"}");
		}
	} else {
		this.showErrorSocketIsNotReady("Subscribe to " + token.message + " fail.");
	}
};
/**
 * Unsubscribe to topic
 * @param {type} token
 * @returns {undefined}
 */
ocelotController.unsubscribe = function (token) {
	if (this.readyState === 1) {
		this.topicHandlers[token.message] = null;
		if (token.message !== "ocelot-status") {
			var command = "{\"cmd\":\"unsubscribe\",\"msg\":\"" + token.message + "\"}";
			this.send(command);
		}
	} else {
		this.showErrorSocketIsNotReady("Unsubscribe to " + token.message + " fail.");
	}
};
ocelotController.call = function (token) {
	// console.debug("Call request " + JSON.stringify(token));
	// check entry cache
	var msgToClient = OcelotCacheManager.getResultInCache(token.id, token.ignoreCache);
	if (msgToClient) {
		//	console.debug("Cache valid send message");
		// present and valid, return result without call
		this.processResult(token, OcelotEventFactory.createResultEventFromToken(token, msgToClient.result));
		return;
	}
	// else call
	if (this.readyState === 1) {
		this.tokens[token.id] = token;
		this.send("{\"cmd\":\"call\",\"msg\":" + token.getMessage() + "}");
	} else {
		this.showErrorSocketIsNotReady("Call " + token.getMessage() + " fail.");
	}
};
ocelotController.showErrorSocketIsNotReady = function (msg) {
	alert("WebSocket is not ready : " + msg + "\nCode : " + this.stateLabels[this.readyState]);
};
ocelotController.processMessage = function (token, evt) {
	var i;
	if (token.onMessage) {
		token.onMessage(evt);
	}
	for (i = 0; i < token.messageHandlers.length; i++) {
		token.messageHandlers[i](evt);
	}
};
ocelotController.processResult = function (token, evtResult) {
	var i;
	this.processMessage(token, evtResult);
	if (token.onResult) {
		token.onResult(evtResult);
	}
	for (i = 0; i < token.resultHandlers.length; i++) {
		token.resultHandlers[i](evtResult);
	}
	if (token.success) {
		token.success(evtResult.result);
	}
};
ocelotController.processFault = function (token, evtFault) {
	var i;
	this.processMessage(token, evtFault);
	if (token.onFault) {
		token.onFault(evtFault);
	}
	for (i = 0; i < token.faultHandlers.length; i++) {
		token.faultHandlers[i](evtFault);
	}
	if (token.fail) {
		token.fail(evtFault.fault);
	}
};
ocelotController.onmessage = function (evt) {
	var token, msgToClient = JSON.parse(evt.data);
	// Receipt fault
	if (msgToClient.fault) {
		token = this.tokens[msgToClient.id];
		this.processFault(token, OcelotEventFactory.createFaultEventFromToken(token, msgToClient.fault));
	} else { // Receipt result
		if (this.topicHandlers[msgToClient.id]) {
			this.topicHandlers[msgToClient.id](msgToClient.result);
		} else if (this.tokens[msgToClient.id]) {
			token = this.tokens[msgToClient.id];
			// if msgToClient has dead line so we stock in cache
			OcelotCacheManager.putResultInCache(msgToClient);
			this.processResult(token, OcelotEventFactory.createResultEventFromToken(token, msgToClient.result));
		}
	}
	delete this.tokens[msgToClient.id];
};
ocelotController.onopen = function (evt) {
	var ocelotServices, mdb, token, i;
	// Controller subscribe to ocelot-cleancache topic
	mdb = new TopicConsumer("ocelot-cleancache");
	mdb.onMessage = function (id) {
		//	console.debug("Clean cache " + id);
		if (id === "ALL") {
			OcelotCacheManager.clearCache();
		} else {
			OcelotCacheManager.removeEntryInCache(id);
		}
	};
	mdb.subscribe();
	// Get Locale from server or cache and re-set it
	ocelotServices = new OcelotServices();
	token = ocelotServices.getLocale();
	token.success = function (locale) {
		ocelotServices.setLocale(locale);
	};
	// send states or current objects in cache with lastupdate
	token = ocelotServices.getOutDatedCache(OcelotCacheManager.lastUpdateManager.getLastUpdateCache());
	token.success = function (list) {
		OcelotCacheManager.removeEntries(list);
	};
	for (i = 0; i < this.openHandlers.length; i++) {
		this.openHandlers[i](evt);
	}
	if (this.topicHandlers["ocelot-status"]) {
		this.topicHandlers["ocelot-status"](this.readyState);
	}
};
ocelotController.onerror = function (evt) {
	var i;
	for (i = 0; i < this.errorHandlers.length; i++) {
		this.errorHandlers[i](evt);
	}
	if (this.topicHandlers["ocelot-status"]) {
		this.topicHandlers["ocelot-status"](this.readyState);
	}
};
ocelotController.onclose = function (evt) {
	var i;
	for (i = 0; i < this.closeHandlers.length; i++) {
		this.closeHandlers[i](evt);
	}
	if (this.topicHandlers["ocelot-status"]) {
		this.topicHandlers["ocelot-status"](this.readyState);
	}
};
/**
 * instance to manage cache
 * @type OcelotCacheManager
 */
OcelotCacheManager = {
	addHandlers: [],
	removeHandlers: [],
	lastUpdateManager: {
		addEntry: function (id) {
			var lastUpdates = this.getLastUpdateCache();
			lastUpdates[id] = Date.now();
			localStorage.setItem("ocelot-lastupdate", JSON.stringify(lastUpdates));
		},
		removeEntry: function (id) {
			var lastUpdates = this.getLastUpdateCache();
			delete lastUpdates[id];
			localStorage.setItem("ocelot-lastupdate", JSON.stringify(lastUpdates));
		},
		getLastUpdateCache: function () {
			var lastUpdates = localStorage.getItem("ocelot-lastupdate");
			if (!lastUpdates) {
				lastUpdates = {};
			} else {
				lastUpdates = JSON.parse(lastUpdates);
			}
			return lastUpdates;
		}
	},
	/**
	 * Add listener for receive cache event
	 * @param {String} type event : add, remove
	 * @param {Function} listener
	 */
	addEventListener: function (type, listener) {
		if (type === "add") {
			this.addHandlers.push(listener);
		} else if (type === "remove") {
			this.removeHandlers.push(listener);
		}
	},
	manageAddEvent: function (msgToClient) {
		var i, evt = document.createEvent("Event");
		evt.initEvent("add", true, false);
		evt.msg = msgToClient;
		for (i = 0; i < this.addHandlers.length; i++) {
			this.openHandlers[i](evt);
		}
	},
	manageRemoveEvent: function (compositeKey) {
		var i, evt = document.createEvent("Event");
		evt.initEvent("remove", true, false);
		evt.key = compositeKey;
		for (i = 0; i < this.removeHandlers.length; i++) {
			this.removeHandlers[i](evt);
		}
	},
	/**
	 * Add result in cache storage
	 * @param {MessageToClient} msgToClient
	 */
	putResultInCache: function (msgToClient) {
		var ids, json, obj;
		this.lastUpdateManager.addEntry(msgToClient.id);
		this.manageAddEvent(msgToClient);
		ids = msgToClient.id.split("_");
		json = localStorage.getItem(ids[0]);
		obj = {};
		if (json) {
			obj = JSON.parse(json);
		}
		obj[ids[1]] = msgToClient;
		json = JSON.stringify(obj);
//		console.debug("Cache new entry " + ids[0] + " : " + json);
		localStorage.setItem(ids[0], json);
	},
	/**
	 * get entry from cache
	 * @param {String} compositeKey
	 * @param {boolean} ignoreCache
	 * @returns {MessageToClient}
	 */
	getResultInCache: function (compositeKey, ignoreCache) {
		if (ignoreCache) {
//			console.debug("Cache ignore");
			return null;
		}
		var ids, json, msgToClient, obj, now;
//		console.debug("Looking Cache for compositeKey " + compositeKey);
		ids = compositeKey.split("_");
		msgToClient = null;
		json = localStorage.getItem(ids[0]);
		if (json) {
			obj = JSON.parse(json);
			msgToClient = obj[ids[1]];
		}
		if (msgToClient) {
//			console.debug("Cache entry " + compositeKey + " found : " + JSON.stringify(msgToClient));
			now = new Date().getTime();
			// check validity
			if (now > msgToClient.deadline) {
				this.removeEntryInCache(compositeKey);
				msgToClient = null; // invalid
			}
		}
		return msgToClient;
	},
	/**
	 * Remove sub entry or entry in cache
	 * @param {String} compositeKey
	 */
	removeEntryInCache: function (compositeKey) {
		var ids, entry, obj;
		this.lastUpdateManager.removeEntry(compositeKey);
		this.manageRemoveEvent(compositeKey);
		ids = compositeKey.split("_");
		entry = localStorage.getItem(ids[0]);
		if (entry) {
			obj = JSON.parse(entry);
			if (ids.length === 2) {
//				console.debug("Remove Cache entry for compositeKey " + compositeKey);
				delete obj[ids[1]];
				localStorage.setItem(ids[0], JSON.stringify(obj));
			} else {
//				console.debug("Remove Cache entries for key " + ids[0]);
				localStorage.removeItem(ids[0]);
			}
		}
	},
	/**
	 * Remove all entries defined in list
	 * @param {array} list
	 */
	removeEntries: function (list) {
		var index, len;
		for (index = 0, len = list.length; index < len; index++) {
			this.removeEntryInCache(list[index]);
		}
	},
	/**
	 * Clear cache storage
	 */
	clearCache: function () {
		localStorage.clear();
	}
};
/**
 * Consumer Class
 * @param {String} topic
 * @returns {TopicConsumer}
 */
function TopicConsumer(topic) {
	this.topic = topic;
	this.on = false;
	this.onMessage = function (msg) {
	};
}
TopicConsumer.prototype = {
	subscribe: function (topic) {
		if(topic) {
			if(this.on) {
				this.unsubscribe();
			}
			this.topic = topic;
		}
		if(this.topic) {
			document.dispatchEvent(OcelotTokenFactory.createSubscribeToken(this.topic, this.onMessage));
			this. on = true;
		}
	},
	unsubscribe: function () {
		if(this.topic && this.on) {
			document.dispatchEvent(OcelotTokenFactory.createUnsubscribeToken(this.topic));
			this.on = false;
		}
	}
};
/**
 * Events Factory
 */
OcelotEventFactory = function () {
	return {
		createResultEventFromToken: function (token, result) {
			var evt = this.createEventFromToken("result", token);
			evt.result = result;
			return evt;
		},
		createFaultEventFromToken: function (token, fault) {
			var evt = this.createEventFromToken("fault", token);
			evt.fault = fault;
			return evt;
		},
		createEventFromToken: function (type, token) {
			var evt = document.createEvent("Event");
			evt.initEvent(type, true, false);
			evt.dataservice = token.dataservice;
			evt.operation = token.operation;
			evt.args = token.args;
			return evt;
		}
	};
}();
/**
 * Tokens Factory
 */
OcelotTokenFactory = function () {
	return {
		/**
		 * Create Token
		 * @param {type} ds
		 * @param {type} id
		 * @param {type} op
		 * @param {type} argNames
		 * @param {type} args
		 * @returns {TokenFactory.createToken.evt|Event}
		 */
		createCallToken: function (ds, id, op, argNames, args) {
			var evt = document.createEvent("Event");
			evt.initEvent("call", true, false);
			evt.dataservice = ds;
			evt.ignoreCache = false;
			evt.operation = op;
			evt.args = args;
			evt.argNames = argNames;
			evt.delay = 0;
			evt.id = id;
			evt.resultHandlers = [];
			evt.faultHandlers = [];
			evt.messageHandlers = [];
			evt.getMessage = function () {
				return "{\"id\":\"" + this.id + "\",\"ds\":\"" + this.dataservice + "\",\"op\":\"" + this.operation + "\",\"argNames\":" + JSON.stringify(this.argNames) + ",\"args\":" + JSON.stringify(this.args) + "}";
			};
			evt.addEventListener = function (type, listener) {
				if (type === "result") {
					this.resultHandlers.push(listener);
				}
				if (type === "fault") {
					this.faultHandlers.push(listener);
				}
				if (type === "message") {
					this.messageHandlers.push(listener);
				}
			};
			evt.onMessage = function (evt) {
			};
			evt.onResult = function (resultEvt) {
			};
			evt.onFault = function (faultEvt) {
			};
			evt.success = function (msg) {
			};
			evt.fail = function (fault) {
			};
			setTimeout(function () {
				setTimeout(function () {
					document.dispatchEvent(evt);
				}, evt.delay);
			}, 1);
			return evt;
		},
		/**
		 * Create Subscribe Token
		 * @param {type} topic
		 * @param {type} messageHandler
		 * @returns {Event|TokenFactory.createSubscribeToken.evt}
		 */
		createSubscribeToken: function (topic, messageHandler) {
			var evt = document.createEvent("Event");
			evt.initEvent("subscribe", true, false);
			evt.message = topic;
			evt.onMessage = messageHandler;
			return evt;
		},
		/**
		 * Create Unsubscribe token
		 * @param {type} topic
		 * @returns {Event|TokenFactory.createUnsubscribeToken.evt}
		 */
		createUnsubscribeToken: function (topic) {
			var evt = document.createEvent("Event");
			evt.initEvent("unsubscribe", true, false);
			evt.message = topic;
			return evt;
		}
	};
}();
/*
 * Take a string and return the hex representation of its MD5.
 * 10637920c62fe58f57cbdb1afaa7ad3e
 * 
 */
String.prototype.md5 = function () {
	x = str2blks_MD5(this);
	a = 1732584193;
	b = -271733879;
	c = -1732584194;
	d = 271733878;
	for (i = 0; i < x.length; i += 16) {
		olda = a;
		oldb = b;
		oldc = c;
		oldd = d;
		a = ff(a, b, c, d, x[i + 0], 7, -680876936);
		d = ff(d, a, b, c, x[i + 1], 12, -389564586);
		c = ff(c, d, a, b, x[i + 2], 17, 606105819);
		b = ff(b, c, d, a, x[i + 3], 22, -1044525330);
		a = ff(a, b, c, d, x[i + 4], 7, -176418897);
		d = ff(d, a, b, c, x[i + 5], 12, 1200080426);
		c = ff(c, d, a, b, x[i + 6], 17, -1473231341);
		b = ff(b, c, d, a, x[i + 7], 22, -45705983);
		a = ff(a, b, c, d, x[i + 8], 7, 1770035416);
		d = ff(d, a, b, c, x[i + 9], 12, -1958414417);
		c = ff(c, d, a, b, x[i + 10], 17, -42063);
		b = ff(b, c, d, a, x[i + 11], 22, -1990404162);
		a = ff(a, b, c, d, x[i + 12], 7, 1804603682);
		d = ff(d, a, b, c, x[i + 13], 12, -40341101);
		c = ff(c, d, a, b, x[i + 14], 17, -1502002290);
		b = ff(b, c, d, a, x[i + 15], 22, 1236535329);
		a = gg(a, b, c, d, x[i + 1], 5, -165796510);
		d = gg(d, a, b, c, x[i + 6], 9, -1069501632);
		c = gg(c, d, a, b, x[i + 11], 14, 643717713);
		b = gg(b, c, d, a, x[i + 0], 20, -373897302);
		a = gg(a, b, c, d, x[i + 5], 5, -701558691);
		d = gg(d, a, b, c, x[i + 10], 9, 38016083);
		c = gg(c, d, a, b, x[i + 15], 14, -660478335);
		b = gg(b, c, d, a, x[i + 4], 20, -405537848);
		a = gg(a, b, c, d, x[i + 9], 5, 568446438);
		d = gg(d, a, b, c, x[i + 14], 9, -1019803690);
		c = gg(c, d, a, b, x[i + 3], 14, -187363961);
		b = gg(b, c, d, a, x[i + 8], 20, 1163531501);
		a = gg(a, b, c, d, x[i + 13], 5, -1444681467);
		d = gg(d, a, b, c, x[i + 2], 9, -51403784);
		c = gg(c, d, a, b, x[i + 7], 14, 1735328473);
		b = gg(b, c, d, a, x[i + 12], 20, -1926607734);
		a = hh(a, b, c, d, x[i + 5], 4, -378558);
		d = hh(d, a, b, c, x[i + 8], 11, -2022574463);
		c = hh(c, d, a, b, x[i + 11], 16, 1839030562);
		b = hh(b, c, d, a, x[i + 14], 23, -35309556);
		a = hh(a, b, c, d, x[i + 1], 4, -1530992060);
		d = hh(d, a, b, c, x[i + 4], 11, 1272893353);
		c = hh(c, d, a, b, x[i + 7], 16, -155497632);
		b = hh(b, c, d, a, x[i + 10], 23, -1094730640);
		a = hh(a, b, c, d, x[i + 13], 4, 681279174);
		d = hh(d, a, b, c, x[i + 0], 11, -358537222);
		c = hh(c, d, a, b, x[i + 3], 16, -722521979);
		b = hh(b, c, d, a, x[i + 6], 23, 76029189);
		a = hh(a, b, c, d, x[i + 9], 4, -640364487);
		d = hh(d, a, b, c, x[i + 12], 11, -421815835);
		c = hh(c, d, a, b, x[i + 15], 16, 530742520);
		b = hh(b, c, d, a, x[i + 2], 23, -995338651);
		a = ii(a, b, c, d, x[i + 0], 6, -198630844);
		d = ii(d, a, b, c, x[i + 7], 10, 1126891415);
		c = ii(c, d, a, b, x[i + 14], 15, -1416354905);
		b = ii(b, c, d, a, x[i + 5], 21, -57434055);
		a = ii(a, b, c, d, x[i + 12], 6, 1700485571);
		d = ii(d, a, b, c, x[i + 3], 10, -1894986606);
		c = ii(c, d, a, b, x[i + 10], 15, -1051523);
		b = ii(b, c, d, a, x[i + 1], 21, -2054922799);
		a = ii(a, b, c, d, x[i + 8], 6, 1873313359);
		d = ii(d, a, b, c, x[i + 15], 10, -30611744);
		c = ii(c, d, a, b, x[i + 6], 15, -1560198380);
		b = ii(b, c, d, a, x[i + 13], 21, 1309151649);
		a = ii(a, b, c, d, x[i + 4], 6, -145523070);
		d = ii(d, a, b, c, x[i + 11], 10, -1120210379);
		c = ii(c, d, a, b, x[i + 2], 15, 718787259);
		b = ii(b, c, d, a, x[i + 9], 21, -343485551);
		a = add(a, olda);
		b = add(b, oldb);
		c = add(c, oldc);
		d = add(d, oldd);
	}
	return rhex(a) + rhex(b) + rhex(c) + rhex(d);
};
/*
 * A JavaScript implementation of the RSA Data Security, Inc. MD5 Message
 * Digest Algorithm, as defined in RFC 1321.
 * Copyright (C) Paul Johnston 1999 - 2000.
 * Updated by Greg Holt 2000 - 2001.
 * See http://pajhome.org.uk/site/legal.html for details.
 */
/*
 * Convert a 32-bit number to a hex string with ls-byte first
 */
var hex_chr = "0123456789abcdef";
function rhex(num) {
	str = "";
	for (j = 0; j <= 3; j++) {
		str += hex_chr.charAt((num >> (j * 8 + 4)) & 0x0F) + hex_chr.charAt((num >> (j * 8)) & 0x0F);
	}
	return str;
}
/*
 * Convert a string to a sequence of 16-word blocks, stored as an array.
 * Append padding bits and the length, as described in the MD5 standard.
 */
function str2blks_MD5(str) {
	nblk = ((str.length + 8) >> 6) + 1;
	blks = new Array(nblk * 16);
	for (i = 0; i < nblk * 16; i++) {
		blks[i] = 0;
	}
	for (i = 0; i < str.length; i++) {
		blks[i >> 2] |= str.charCodeAt(i) << ((i % 4) * 8);
	}
	blks[i >> 2] |= 0x80 << ((i % 4) * 8);
	blks[nblk * 16 - 2] = str.length * 8;
	return blks;
}
/*
 * Add integers, wrapping at 2^32. This uses 16-bit operations internally 
 * to work around bugs in some JS interpreters.
 */
function add(x, y) {
	var lsw, msw;
	lsw = (x & 0xFFFF) + (y & 0xFFFF);
	msw = (x >> 16) + (y >> 16) + (lsw >> 16);
	return (msw << 16) | (lsw & 0xFFFF);
}
/*
 * Bitwise rotate a 32-bit number to the left
 */
function rol(num, cnt) {
	return (num << cnt) | (num >>> (32 - cnt));
}
/*
 * These functions implement the basic operation for each round of the
 * algorithm.
 */
function cmn(q, a, b, x, s, t) {
	return add(rol(add(add(a, q), add(x, t)), s), b);
}
function ff(a, b, c, d, x, s, t) {
	return cmn((b & c) | ((~b) & d), a, b, x, s, t);
}
function gg(a, b, c, d, x, s, t) {
	return cmn((b & d) | (c & (~d)), a, b, x, s, t);
}
function hh(a, b, c, d, x, s, t) {
	return cmn(b ^ c ^ d, a, b, x, s, t);
}
function ii(a, b, c, d, x, s, t) {
	return cmn(c ^ (b | (~d)), a, b, x, s, t);
}
} else {
	alert("Sorry, but your browser doesn't support websocket");
}