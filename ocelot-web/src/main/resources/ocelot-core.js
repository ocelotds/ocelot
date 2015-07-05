/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. 
 */
// globals variables or static class pattern
var ocelotController, OcelotCacheManager, OcelotTokenFactory, OcelotEventFactory;
/**
 * Ocelot Controller
 * @returns {OcelotController}
 */
ocelotController = function () {
	var ws;
	if (document.location.href.toString().indexOf(document.location.protocol + "//" + document.location.hostname + ":" + document.location.port + "%CTXPATH%") === 0) {
		ws = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "%CTXPATH%/endpoint");
	} else {
		ws = new WebSocket("ws://" + document.location.hostname + ":" + document.location.port + "/endpoint");
	}
	ws.tokens = {};
	ws.topicHandlers = {};
	ws.status = "OPEN";
	ws.onmessage = function (evt) {
		var token, e, i, msgToClient = JSON.parse(evt.data);
		// Receipt fault
		if (msgToClient.fault) {
			token = this.tokens[msgToClient.id];
			e = OcelotEventFactory.createFaultEventFromToken(token, msgToClient.fault);
			if (token.onFault) {
				token.onFault(e);
			}
			for (i = 0; i < token.faultHandlers.length; i++) {
				token.faultHandlers[i](e);
			}
			if (token.fail) {
				token.fail(msgToClient.fault);
			}
		} else { // Receipt result
			if (this.topicHandlers[msgToClient.id]) {
				this.topicHandlers[msgToClient.id](msgToClient.result);
			} else if (this.tokens[msgToClient.id]) {
				token = this.tokens[msgToClient.id];
				// if msgToClient has dead line so we stock in cache
				if (msgToClient.store === "SESSION") {
					OcelotCacheManager.putResultInCache(sessionStorage, msgToClient);
				} else if (msgToClient.store === "APPLICATION") {
					OcelotCacheManager.putResultInCache(localStorage, msgToClient);
				}
				e = OcelotEventFactory.createResultEventFromToken(token, msgToClient.result);
				if (token.onResult) {
					token.onResult(e);
				}
				for (i = 0; i < token.resultHandlers.length; i++) {
					token.resultHandlers[i](e);
				}
				if (token.success) {
					token.success(msgToClient.result);
				}
			}
		}
		delete this.tokens[msgToClient.id];
	};
	ws.onopen = function (evt) {
		this.status = "OPEN";
		if (this.topicHandlers["ocelot-status"]) {
			this.topicHandlers["ocelot-status"](this.status);
		}
		var ocelotServices, mdb, token;
		// Controller subscribe to ocelot-cleancache topic
		mdb = new TopicConsumer("ocelot-cleancache");
		mdb.onMessage = function (id) {
			console.debug("Clean cache " + id);
			if (id === "APPLICATION") {
				OcelotCacheManager.clearCache(localStorage);
			} else if (id === "SESSION") {
				OcelotCacheManager.clearCache(sessionStorage);
			} else {
				OcelotCacheManager.removeEntryInCache(localStorage, id);
				OcelotCacheManager.removeEntryInCache(sessionStorage, id);
			}
		};
		mdb.subscribe();
		// Get Locale from server or cache and re-set it
		ocelotServices = new OcelotServices();
		token = ocelotServices.getLocale();
		token.success = function (locale) {
			ocelotServices.setLocale(locale);
		};
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
	return  {
		/**
		 * Close connection to ws
		 * @returns {undefined}
		 */
		close: function () {
			if (ws) {
				document.dispatchEvent(OcelotTokenFactory.createUnsubscribeToken("ocelot-cleancache"));
				ws.close();
			}
		},
		/**
		 * Subscribe to topic 
		 * @param {type} token
		 * @returns {undefined}
		 */
		subscribe: function (token) {
			if (ws.status === "OPEN") {
				ws.topicHandlers[token.message] = token.onMessage;
				if (token.message === "ocelot-status") {
				} else {
					var command = "{\"cmd\":\"subscribe\",\"msg\":\"" + token.message + "\"}";
					ws.send(command);
				}
			} else {
				this.showErrorSocketIsClosed();
			}
		},
		/**
		 * Unsubscribe to topic
		 * @param {type} token
		 * @returns {undefined}
		 */
		unsubscribe: function (token) {
			if (ws.status === "OPEN") {
				ws.topicHandlers[token.message] = null;
				if (token.message !== "ocelot-status") {
					var command = "{\"cmd\":\"unsubscribe\",\"msg\":\"" + token.message + "\"}";
					ws.send(command);
				}
			} else {
				this.showErrorSocketIsClosed();
			}
		},
		call: function (token) {
			console.debug("Call request " + JSON.stringify(token));
			// check entry cache
			var msgToClient = OcelotCacheManager.getResultInCache(localStorage, token.id, token.ignoreCache) || OcelotCacheManager.getResultInCache(sessionStorage, token.id, token.ignoreCache);
			if (msgToClient) {
				console.debug("Cache valid send message");
				// present and valid, return result without call
				token.onResult(OcelotEventFactory.createResultEventFromToken(token, msgToClient.result));
				token.success(msgToClient.result);
				return;
			}
			// else call
			if (ws.status === "OPEN") {
				ws.tokens[token.id] = token;
				ws.send("{\"cmd\":\"call\",\"msg\":" + token.getMessage() + "}");
			} else {
				this.showErrorSocketIsClosed();
			}
		},
		showErrorSocketIsClosed: function () {
			alert("WebSocket is not open");
		}
	};
}();
/**
 * instance to manage cache
 * @type OcelotCacheManager
 */
OcelotCacheManager = function () {
	return {
		/**
		 * Add result in cache storage
		 * @param {Storage JS Plateform} storage
		 * @param {MessageToClient} msgToClient
		 */
		putResultInCache: function (storage, msgToClient) {
			var ids, json, obj;
			ids = msgToClient.id.split("_");
			json = storage.getItem(ids[0]);
			obj = {};
			if (json) {
				obj = JSON.parse(json);
			}
			obj[ids[1]] = msgToClient;
			json = JSON.stringify(obj);
			console.debug("Cache new entry " + ids[0] + " : " + json);
			storage.setItem(ids[0], json);
		},
		/**
		 * 
		 * @param {Storage JS Plateform} storage
		 * @param {String} compositeKey
		 * @param {boolean} ignoreCache
		 * @returns {MessageToClient}
		 */
		getResultInCache: function (storage, compositeKey, ignoreCache) {
			if (ignoreCache) {
				console.debug("Cache ignore");
				return null;
			}
			var ids, json, msgToClient, obj, now;
			console.debug("Looking Cache for compositeKey " + compositeKey);
			ids = compositeKey.split("_");
			msgToClient = null;
			json = storage.getItem(ids[0]);
			if (json) {
				obj = JSON.parse(json);
				msgToClient = obj[ids[1]];
			}
			if (msgToClient) {
				console.debug("Cache entry " + compositeKey + " found : " + JSON.stringify(msgToClient));
				now = new Date().getTime();
				// check validity
				if (now > msgToClient.deadline) {
					OcelotCacheManager.removeEntryInCache(storage, compositeKey);
					msgToClient = null; // invalid
				}
			}
			return msgToClient;
		},
		/**
		 * Remove sub entry or entry in cache
		 * @param {Storage JS Plateform} storage
		 * @param {String} compositeKey
		 */
		removeEntryInCache: function (storage, compositeKey) {
			var ids, entry, obj;
			ids = compositeKey.split("_");
			entry = storage.getItem(ids[0]);
			if (entry) {
				obj = JSON.parse(entry);
				if (ids.length === 2) {
					console.debug("Remove Cache entry for compositeKey " + compositeKey);
					delete obj[ids[1]];
					storage.setItem(ids[0], JSON.stringify(obj));
				} else {
					console.debug("Remove Cache entries for key " + ids[0]);
					storage.removeItem(ids[0]);
				}
			}
		},
		/**
		 * Clear specific cache storage
		 * @param {Storage JS Plateform} storage
		 */
		clearCache: function (storage) {
			storage.clear();
		}
	};
}();
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
/**
 * Consumer Class
 * @param {String} topic
 * @returns {TopicConsumer}
 */
function TopicConsumer(topic) {
	var token;
	this.topic = topic;
	this.subscribe = function () {
		token = OcelotTokenFactory.createSubscribeToken(this.topic, this.onMessage);
		document.dispatchEvent(token);
	};
	this.unsubscribe = function () {
		token = OcelotTokenFactory.createUnsubscribeToken(this.topic);
		document.dispatchEvent(token);
	};
	this.onMessage = function (msg) {
	};
}
/**
 * Events Factory
 */
OcelotEventFactory = function () {
	return {
		createResultEventFromToken: function (token, result) {
			var evt = createEventFromToken("result", token);
			evt.result = result;
			return evt;
		},
		createFaultEventFromToken: function (token, fault) {
			var evt = createEventFromToken("fault", token);
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

/**
 * Classe of ocelot services
 * @author hhfrancois
 */
function OcelotServices() {
	this.ds = "fr.hhdev.ocelot.OcelotServices";
	/**
	 * @param locale
	 */
	this.setLocale = function (locale) {
		var id0, id1, id, cleanid, nextYear, msgToClient;
		id0 = (this.ds + ".setLocale").md5();
		id1 = JSON.stringify([locale]).md5();
		id = id0 + "_" + id1;
		id0 = (this.ds + ".getLocale").md5();
		id1 = JSON.stringify([]).md5();
		cleanid = "\"" + id0 + "_" + id1 + "\"";
		nextYear = new Date();
		nextYear.setFullYear(nextYear.getFullYear() + 1);
		msgToClient = {"id": cleanid, "deadline": nextYear.getTime(), "store": "APPLICATION", "result": locale};
		OcelotCacheManager.putResultInCache(localStorage, msgToClient);
		return OcelotTokenFactory.createCallToken(this.ds, id, "setLocale", ["locale"], [locale]);
	};
	/**
	 * @return locale
	 */
	this.getLocale = function () {
		var id0, id1, id;
		id0 = (this.ds + ".getLocale").md5();
		id1 = JSON.stringify([]).md5();
		id = id0 + "_" + id1;
		return OcelotTokenFactory.createCallToken(this.ds, id, "getLocale", [], []);
	};
}

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
