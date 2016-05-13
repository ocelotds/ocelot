var ocelotController, OcelotPromiseFactory, MD5Tools;
var Subscriber = (function (topic) {
	var promise = ocelotServices.subscribe(topic);
	Object.defineProperty(promise, "topic", { value: topic, writable: false }); // add topic property, readonly
	delete promise.constraint;
	promise.unsubscribe = function () {
		return ocelotServices.unsubscribe(topic);
	};
	return promise;
});
OcelotPromiseFactory = function () {
	var MSG = "MESSAGE", RES = "RESULT", FAULT = "FAULT", CONSTRAINT = "CONSTRAINT";
	return {
		createPromise: function (ds, id, op, argNames, args) {
			return (function (ds, id, op, argNames, args) {
				var fault, handler, evt = null, thenHandlers = [], catchHandlers = [], constraintHandlers = [], eventHandlers = [], messageHandlers = [];
				function process() {
					if (!evt) {
						return;
					}
					if (evt.type !== MSG) {
						while (handler = eventHandlers.shift()) {
							handler(evt);
						}
						switch (evt.type) {
							case RES:
								while (handler = thenHandlers.shift()) {
									handler(evt.response);
								}
								break;
							case CONSTRAINT:
								while (handler = constraintHandlers.shift()) {
									handler(evt.response);
								}
								break;
							case FAULT:
								fault = evt.response;
								console.error(fault.classname + "(" + fault.message + ")");
								if (ocelotController.options.debug) console.table(fault.stacktrace);
								while (handler = catchHandlers.shift()) {
									handler(fault);
								}
								break;
						}
					} else {
						messageHandlers.forEach(function (messageHandler) {
							messageHandler(evt.response);
						});
					}
				}
				var promise = {
					id: id, key: id, dataservice: ds, operation: op, args: args, argNames: argNames, cacheIgnored: false, t: new Date().getTime(),
					set response(e) {
						evt = e;
						process();
					},
					ignoreCache: function (ignore) {
						this.cacheIgnored = ignore;
						return this;
					},
					then: function (onFulfilled, onRejected) {
						if (onFulfilled) {
							thenHandlers.push(onFulfilled);
						}
						if (onRejected) {
							catchHandlers.push(onRejected);
						}
						process();// event already receive ?
						return this;
					},
					catch: function (onRejected) {
						if (onRejected) {
							catchHandlers.push(onRejected);
						}
						process();// event already receive ?
						return this;
					},
					constraint: function (onConstraint) {
						if (onConstraint) {
							constraintHandlers.push(onConstraint);
						}
						process();// event already receive ?
						return this;
					},
					event: function (onEvented) {
						if (onEvented) {
							eventHandlers.push(onEvented);
						}
						process();// event already receive ?
						return this;
					},
					message: function (onMessaged) {
						if (onMessaged) {
							messageHandlers.push(onMessaged);
						}
						process();// event already receive ?
						return this;
					},
					get json() {
						return { "id": this.id, "ds": this.dataservice, "op": this.operation, "argNames": this.argNames, "args": this.args };
					}
				};
				var e = document.createEvent("Event");
				e.initEvent("call", true, false);
				e.promise = promise;
				setTimeout(function () {
					document.dispatchEvent(e);
				}, 1);
				return promise;
			})(ds, id, op, argNames, args);
		}
	};
} ();
/**
 * Add ocelotController events to document
 * @param {Event} event
 */
document.addEventListener("call", function (event) {
	ocelotController.addPromise(event.promise);
});
window.addEventListener("beforeunload", function (e) {
	if (ocelotController) {
		ocelotController.close("ONUNLOAD");
	}
});
/**
 * MD5Tools
 */
MD5Tools = function () {
	return {
		hex_chr: function (num) {
			return "0123456789abcdef".charAt(num);
		},
		/*
		 * Convert a 32-bit number to a hex string with ls-byte first
		 */
		rhex: function (num) {
			var j, str = "";
			for (j = 0; j <= 3; j++) {
				str += this.hex_chr((num >> (j * 8 + 4)) & 0x0F) + this.hex_chr((num >> (j * 8)) & 0x0F);
			}
			return str;
		},
		/*
		 * Convert a string to a sequence of 16-word blocks, stored as an array.
		 * Append padding bits and the length, as described in the MD5 standard.
		 */
		str2blks_MD5: function (str) {
			var blks, i, nblk = ((str.length + 8) >> 6) + 1;
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
		},
		/*
		 * Add integers, wrapping at 2^32. This uses 16-bit operations internally 
		 * to work around bugs in some JS interpreters.
		 */
		add: function (x, y) {
			var lsw, msw;
			lsw = (x & 0xFFFF) + (y & 0xFFFF);
			msw = (x >> 16) + (y >> 16) + (lsw >> 16);
			return (msw << 16) | (lsw & 0xFFFF);
		},
		/*
		 * Bitwise rotate a 32-bit number to the left
		 */
		rol: function (num, cnt) {
			return (num << cnt) | (num >>> (32 - cnt));
		},
		/*
		 * These functions implement the basic operation for each round of the
		 * algorithm.
		 */
		cmn: function (q, a, b, x, s, t) {
			return this.add(this.rol(this.add(this.add(a, q), this.add(x, t)), s), b);
		},
		ff: function (a, b, c, d, x, s, t) {
			return this.cmn((b & c) | ((~b) & d), a, b, x, s, t);
		},
		gg: function (a, b, c, d, x, s, t) {
			return this.cmn((b & d) | (c & (~d)), a, b, x, s, t);
		},
		hh: function (a, b, c, d, x, s, t) {
			return this.cmn(b ^ c ^ d, a, b, x, s, t);
		},
		ii: function (a, b, c, d, x, s, t) {
			return this.cmn(c ^ (b | (~d)), a, b, x, s, t);
		}
	};
} ();

/*
 * Take a string and return the hex representation of its MD5.
 * 10637920c62fe58f57cbdb1afaa7ad3e
 * 
 */
String.prototype.md5 = function () {
	var x, a, b, c, d, olda, oldb, oldc, oldd, i;
	x = MD5Tools.str2blks_MD5(this);
	a = 1732584193;
	b = -271733879;
	c = -1732584194;
	d = 271733878;
	for (i = 0; i < x.length; i += 16) {
		olda = a;
		oldb = b;
		oldc = c;
		oldd = d;
		a = MD5Tools.ff(a, b, c, d, x[i + 0], 7, -680876936);
		d = MD5Tools.ff(d, a, b, c, x[i + 1], 12, -389564586);
		c = MD5Tools.ff(c, d, a, b, x[i + 2], 17, 606105819);
		b = MD5Tools.ff(b, c, d, a, x[i + 3], 22, -1044525330);
		a = MD5Tools.ff(a, b, c, d, x[i + 4], 7, -176418897);
		d = MD5Tools.ff(d, a, b, c, x[i + 5], 12, 1200080426);
		c = MD5Tools.ff(c, d, a, b, x[i + 6], 17, -1473231341);
		b = MD5Tools.ff(b, c, d, a, x[i + 7], 22, -45705983);
		a = MD5Tools.ff(a, b, c, d, x[i + 8], 7, 1770035416);
		d = MD5Tools.ff(d, a, b, c, x[i + 9], 12, -1958414417);
		c = MD5Tools.ff(c, d, a, b, x[i + 10], 17, -42063);
		b = MD5Tools.ff(b, c, d, a, x[i + 11], 22, -1990404162);
		a = MD5Tools.ff(a, b, c, d, x[i + 12], 7, 1804603682);
		d = MD5Tools.ff(d, a, b, c, x[i + 13], 12, -40341101);
		c = MD5Tools.ff(c, d, a, b, x[i + 14], 17, -1502002290);
		b = MD5Tools.ff(b, c, d, a, x[i + 15], 22, 1236535329);
		a = MD5Tools.gg(a, b, c, d, x[i + 1], 5, -165796510);
		d = MD5Tools.gg(d, a, b, c, x[i + 6], 9, -1069501632);
		c = MD5Tools.gg(c, d, a, b, x[i + 11], 14, 643717713);
		b = MD5Tools.gg(b, c, d, a, x[i + 0], 20, -373897302);
		a = MD5Tools.gg(a, b, c, d, x[i + 5], 5, -701558691);
		d = MD5Tools.gg(d, a, b, c, x[i + 10], 9, 38016083);
		c = MD5Tools.gg(c, d, a, b, x[i + 15], 14, -660478335);
		b = MD5Tools.gg(b, c, d, a, x[i + 4], 20, -405537848);
		a = MD5Tools.gg(a, b, c, d, x[i + 9], 5, 568446438);
		d = MD5Tools.gg(d, a, b, c, x[i + 14], 9, -1019803690);
		c = MD5Tools.gg(c, d, a, b, x[i + 3], 14, -187363961);
		b = MD5Tools.gg(b, c, d, a, x[i + 8], 20, 1163531501);
		a = MD5Tools.gg(a, b, c, d, x[i + 13], 5, -1444681467);
		d = MD5Tools.gg(d, a, b, c, x[i + 2], 9, -51403784);
		c = MD5Tools.gg(c, d, a, b, x[i + 7], 14, 1735328473);
		b = MD5Tools.gg(b, c, d, a, x[i + 12], 20, -1926607734);
		a = MD5Tools.hh(a, b, c, d, x[i + 5], 4, -378558);
		d = MD5Tools.hh(d, a, b, c, x[i + 8], 11, -2022574463);
		c = MD5Tools.hh(c, d, a, b, x[i + 11], 16, 1839030562);
		b = MD5Tools.hh(b, c, d, a, x[i + 14], 23, -35309556);
		a = MD5Tools.hh(a, b, c, d, x[i + 1], 4, -1530992060);
		d = MD5Tools.hh(d, a, b, c, x[i + 4], 11, 1272893353);
		c = MD5Tools.hh(c, d, a, b, x[i + 7], 16, -155497632);
		b = MD5Tools.hh(b, c, d, a, x[i + 10], 23, -1094730640);
		a = MD5Tools.hh(a, b, c, d, x[i + 13], 4, 681279174);
		d = MD5Tools.hh(d, a, b, c, x[i + 0], 11, -358537222);
		c = MD5Tools.hh(c, d, a, b, x[i + 3], 16, -722521979);
		b = MD5Tools.hh(b, c, d, a, x[i + 6], 23, 76029189);
		a = MD5Tools.hh(a, b, c, d, x[i + 9], 4, -640364487);
		d = MD5Tools.hh(d, a, b, c, x[i + 12], 11, -421815835);
		c = MD5Tools.hh(c, d, a, b, x[i + 15], 16, 530742520);
		b = MD5Tools.hh(b, c, d, a, x[i + 2], 23, -995338651);
		a = MD5Tools.ii(a, b, c, d, x[i + 0], 6, -198630844);
		d = MD5Tools.ii(d, a, b, c, x[i + 7], 10, 1126891415);
		c = MD5Tools.ii(c, d, a, b, x[i + 14], 15, -1416354905);
		b = MD5Tools.ii(b, c, d, a, x[i + 5], 21, -57434055);
		a = MD5Tools.ii(a, b, c, d, x[i + 12], 6, 1700485571);
		d = MD5Tools.ii(d, a, b, c, x[i + 3], 10, -1894986606);
		c = MD5Tools.ii(c, d, a, b, x[i + 10], 15, -1051523);
		b = MD5Tools.ii(b, c, d, a, x[i + 1], 21, -2054922799);
		a = MD5Tools.ii(a, b, c, d, x[i + 8], 6, 1873313359);
		d = MD5Tools.ii(d, a, b, c, x[i + 15], 10, -30611744);
		c = MD5Tools.ii(c, d, a, b, x[i + 6], 15, -1560198380);
		b = MD5Tools.ii(b, c, d, a, x[i + 13], 21, 1309151649);
		a = MD5Tools.ii(a, b, c, d, x[i + 4], 6, -145523070);
		d = MD5Tools.ii(d, a, b, c, x[i + 11], 10, -1120210379);
		c = MD5Tools.ii(c, d, a, b, x[i + 2], 15, 718787259);
		b = MD5Tools.ii(b, c, d, a, x[i + 9], 21, -343485551);
		a = MD5Tools.add(a, olda);
		b = MD5Tools.add(b, oldb);
		c = MD5Tools.add(c, oldc);
		d = MD5Tools.add(d, oldd);
	}
	return MD5Tools.rhex(a) + MD5Tools.rhex(b) + MD5Tools.rhex(c) + MD5Tools.rhex(d);
};
if ("WebSocket" in window) {
	ocelotController = (function () {
		var opts = { "monitor": false, "debug": false, "reconnect": false }, MSG = "MESSAGE", CONSTRAINT = "CONSTRAINT", RES = "RESULT", FAULT = "FAULT",
			ALL = "ALL", EVT = "Event", ADD = "add", RM = "remove", CLEANCACHE = "ocelot-cleancache", STATUS = "ocelot-status",
			OSRV = "org.ocelotds.OcelotServices", SUB = "subscribe", UNSUB = "unsubscribe",
			stateLabels = ['CONNECTING', 'OPEN', 'CLOSING', 'CLOSED'], closetimer, promises = {}, path, ws,
			_cacheManager = (function () {
				var LU = "ocelot-lastupdate", addHandlers = [], removeHandlers = [],
					lastUpdateManager = (function () {
						function _addEntry(id) {
							var lastUpdates = _getLastUpdateCache();
							lastUpdates[id] = Date.now();
							localStorage.setItem(LU, JSON.stringify(lastUpdates));
						}
						function _removeEntry(id) {
							var lastUpdates = _getLastUpdateCache();
							delete lastUpdates[id];
							localStorage.setItem(LU, JSON.stringify(lastUpdates));
						}
						function _getLastUpdateCache() {
							var lastUpdates = localStorage.getItem(LU);
							if (!lastUpdates) {
								lastUpdates = {};
							} else {
								lastUpdates = JSON.parse(lastUpdates);
							}
							return lastUpdates;
						}
						return {
							/**
							 * Add an entry in lastUpdate manager
							 * Add now moment for id
							 * @param {String} id : the entry id
							 */
							addEntry: _addEntry,
							/**
							 * Remove entry in last update manager
							 * Means the service id is not in cache
							 * @param {String} id : the entry id
							 */
							removeEntry: _removeEntry,
							/**
							 * Return all entries date linked to cache entries
							 * @returns {Object} : { id1:String : date:number, id2:String : date:number}
							 */
							getLastUpdateCache: _getLastUpdateCache
						};
					})()
				function manageAddEvent(msgToClient) {
					var evt = document.createEvent(EVT);
					evt.initEvent(ADD, true, false);
					evt.msg = msgToClient;
					addHandlers.forEach(function (handler) {
						handler(evt);
					});
				}
				function manageRemoveEvent(compositeKey) {
					var evt = document.createEvent(EVT);
					evt.initEvent(RM, true, false);
					evt.key = compositeKey;
					removeHandlers.forEach(function (handler) {
						handler(evt);
					});
				}
				function _addEventListener(type, listener) {
					if (type === ADD) {
						addHandlers.push(listener);
					} else if (type === RM) {
						removeHandlers.push(listener);
					}
				}
				function _removeEventListener(type, listener) {
					var idx = -1;
					if (type === ADD) {
						idx = addHandlers.indexOf(listener);
						if (idx !== -1) {
							addHandlers.splice(idx, 1);
						}
					} else if (type === RM) {
						idx = removeHandlers.indexOf(listener);
						if (idx !== -1) {
							removeHandlers.splice(idx, 1);
						}
					}
				}
				function _putResultInCache(msgToClient) {
					var ids, json, obj;
					if (!msgToClient.deadline) {
						return;
					}
					lastUpdateManager.addEntry(msgToClient.id);
					manageAddEvent(msgToClient);
					ids = msgToClient.id.split("_");
					json = localStorage.getItem(ids[0]);
					obj = {};
					if (json) {
						obj = JSON.parse(json);
					}
					obj[ids[1]] = msgToClient;
					json = JSON.stringify(obj);
					localStorage.setItem(ids[0], json);
				}
				function _getResultInCache(compositeKey, ignoreCache) {
					var ids, json, msgToClient, obj, now;
					if (ignoreCache) {
						return null;
					}
					ids = compositeKey.split("_");
					msgToClient = null;
					json = localStorage.getItem(ids[0]);
					if (json) {
						obj = JSON.parse(json);
						msgToClient = obj[ids[1]];
					}
					if (msgToClient) {
						now = new Date().getTime();
						// check validity
						if (now > msgToClient.deadline) {
							this.removeEntryInCache(compositeKey);
							msgToClient = null; // invalid
						}
					}
					return msgToClient;
				}
				function _removeEntryInCache(compositeKey) {
					var ids, entry, obj;
					lastUpdateManager.removeEntry(compositeKey);
					manageRemoveEvent(compositeKey);
					ids = compositeKey.split("_");
					entry = localStorage.getItem(ids[0]);
					if (entry) {
						obj = JSON.parse(entry);
						if (ids.length === 2) {
							delete obj[ids[1]];
							localStorage.setItem(ids[0], JSON.stringify(obj));
						} else {
							localStorage.removeItem(ids[0]);
						}
					}
				}
				function _removeEntries(list) {
					list.forEach(function (key) {
						removeEntryInCache(key);
					});
				}
				function _clearCache() {
					localStorage.clear();
				}
				function _getLastUpdateCache() {
					return lastUpdateManager.getLastUpdateCache();
				}
				return {
					getLastUpdateCache: _getLastUpdateCache,
					/**
					 * Add listener for receive cache event
					 * @param {String} type event : add, remove
					 * @param {Function} listener
					 */
					addEventListener: _addEventListener,
					/**
					 * Remove listener for receive cache event
					 * @param {String} type event : add, remove
					 * @param {Function} listener
					 */
					removeEventListener: _removeEventListener,
					/**
					 * If msgToClient has deadline so we stock in cache
					 * Add result in cache storage
					 * @param {MessageToClient} msgToClient
					 */
					putResultInCache: _putResultInCache,
					/**
					 * get entry from cache
					 * @param {String} compositeKey
					 * @param {boolean} ignoreCache
					 * @returns {MessageToClient}
					 */
					getResultInCache: _getResultInCache,
					/**
					 * Remove sub entry or entry in cache
					 * @param {String} compositeKey
					 */
					removeEntryInCache: _removeEntryInCache,
					/**
					 * Remove all entries defined in list
					 * @param {array} list
					 */
					removeEntries: _removeEntries,
					/**
					 * Clear cache storage
					 */
					clearCache: _clearCache
				};
			})()
		function createEventFromPromise(type, promise, msgToClient) {
			var evt = document.createEvent(EVT);
			evt.initEvent(type, true, false);
			evt.dataservice = promise.dataservice;
			evt.operation = promise.operation;
			evt.args = promise.args;
			evt.totaltime = 0;
			evt.javatime = 0;
			evt.jstime = 0;
			evt.networktime = 0;
			if (msgToClient) {
				evt.response = msgToClient.response;
				if (opts.monitor) {
					evt.javatime = msgToClient.t; // backend timing
				}
			}
			if (opts.monitor) {
				evt.totaltime = new Date().getTime() - promise.t; // total timing
			}
			return evt;
		}
		function createMessageEventFromPromise(promise, msgToClient) {
			return createEventFromPromise(MSG, promise, msgToClient);
		}
		function createResultEventFromPromise(promise, msgToClient) {
			return createEventFromPromise(RES, promise, msgToClient);
		}
		function createConstraintEventFromPromise(promise, msgToClient) {
			return createEventFromPromise(CONSTRAINT, promise, msgToClient);
		}
		function createFaultEventFromPromise(promise, msgToClient) {
			return createEventFromPromise(FAULT, promise, msgToClient);
		}
		function stateUpdated() {
			foreachPromiseInPromisesDo(STATUS, function (promise) {
				promise.response = createMessageEventFromPromise(promise, { "response": ocelotController.status, "t": 0 });
			});
		}
		function foreachPromiseInPromisesDo(id, func) {
			foreachPromiseDo(getPromises(id), func);
		}
		function foreachPromiseDo(aPromises, func) {
			var i;
			if (aPromises) {
				for (i = 0; i < aPromises.length; i++) {
					func(aPromises[i]);
				}
			}
		}
		function addPromiseToId(promise, id) { // add promise to promise list and return if some promises exists already for id
			var exists = (promises[id] !== undefined);
			if (!exists) {
				promises[id] = [];
			}
			promises[id].push(promise);
			return exists;
		}
		function clearPromisesForId(id) {
			delete promises[id];
		}
		function getPromises(id) {
			return promises[id] || [];
		}
		function isOcelotControllerServices(promise) {
			return promise && (promise.dataservice === "ocelotController");
		}
		function isSubscription(promise) {
			return promise.dataservice === OSRV && promise.operation === SUB;
		}
		function isTopicSubscription(promise, topic) {
			return isSubscription(promise) && isTopic(promise, topic);
		}
		function isUnsubscription(promise) {
			return promise.dataservice === OSRV && promise.operation === UNSUB;
		}
		function isTopicUnsubscription(promise, topic) {
			return isUnsubscription(promise) && isTopic(promise, topic);
		}
		function isTopic(promise, topic) {
			return topic ? (promise.args[0] === topic) : true;
		}
		function extractOptions(search) {
			var params = search.split("&");
			params.forEach(function (param) {
				if (param.search(/^\??ocelot=/) === 0) {
					opts = JSON.parse(decodeURI(param.replace(/\??ocelot=/, "")));
				}
			});
		}
		function sendMfc(promise) {
			var msgToClient, mfc, xhttp;
			if (!addPromiseToId(promise, promise.id)) {
				// Subscription or unsubscription to topic, use websocket
				mfc = JSON.stringify(promise.json);
				xhttp = new XMLHttpRequest();
				xhttp.onreadystatechange = function () {
					if (xhttp.readyState === 4) {
						if (xhttp.status === 200) {
							msgToClient = JSON.parse(xhttp.responseText);
							receiveMtc(msgToClient);
						} else {
							receiveMtc({ "id": promise.id, "type": FAULT, "response": { "classname": "XMLHttpRequest", "message": "XMLHttpRequest request failed : code = " + xhttp.status, "stacktrace": [] }, "t": 0 });
						}
					}
				};
				xhttp.open("POST", "http" + path + "ocelot/endpoint", true);
				xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
				xhttp.send("mfc=" + mfc);
			}
		}
		function receiveMtc(msgToClient) {
			if (msgToClient.type === RES) { // maybe should be store result in cache
				// if msgToClient has dead line so we stock in cache
				_cacheManager.putResultInCache(msgToClient);
			}
			foreachPromiseInPromisesDo(msgToClient.id, function (promise) {
				switch (msgToClient.type) {
					case FAULT:
						promise.response = createFaultEventFromPromise(promise, msgToClient);
						break;
					case RES:
						// if msg is response of subscribe request
						if (isTopicSubscription(promise)) {
							addPromiseToId(promise, promise.args[0]);
						} else if (isTopicUnsubscription(promise)) {
							clearPromisesForId(promise.args[0]);
						}
						promise.response = createResultEventFromPromise(promise, msgToClient);
						break;
					case CONSTRAINT:
						promise.response = createConstraintEventFromPromise(promise, msgToClient);
						break;
					case MSG:
						promise.response = createMessageEventFromPromise(promise, msgToClient);
						break;
				}
			});
			// when receive result or fault, remove handlers, except for topic
			if (msgToClient.type !== MSG) {
				clearPromisesForId(msgToClient.id);
			}
		}
		function onwsmessage(evt) {
			if (opts.debug) console.debug(evt.data);
			receiveMtc(JSON.parse(evt.data));
		}
		function onwserror(evt) {
			console.info("Websocket error : " + evt.reason);
			stateUpdated();
		}
		function onwsclose(evt) {
			stateUpdated();
			if (opts.reconnect && evt.reason !== "ONUNLOAD") {
				if (opts.debug) console.debug("Websocket closed : " + evt.reason + " try reconnect each " + 1000 + "ms");
				closetimer = setInterval(function () {
					connect();
				}, 1000);
			}
		}
		function onwsopen(evt) {
			if (closetimer) clearInterval(closetimer);
			if (opts.debug) console.debug("Websocket opened");
			var ps;
			// handler, apromise, idx, promise;
			stateUpdated();
			ps = promises;
			promises = {};
			Object.keys(ps).forEach(function (id) { // we redo the subscription
				if (id !== ps[id].id) {
					foreachPromiseDo(ps[id], _addPromise);
				}
			});
		}
		function connect() {
			if (opts.debug) console.debug("Ocelotds initialization...");
			var re = /ocelot-core.js|ocelot\.js.*|ocelot\/core(\.min)?\.js/;
			for (var i = 0; i < document.scripts.length; i++) {
				var item = document.scripts[i];
				if (item.src.match(re)) {
					path = item.src.replace(/^http/, "").replace(re, "");
				}
			}
			extractOptions(document.location.search);
			// init a standard httpsession and init websocket
			return ocelotServices.initCore(opts).then(function () {
				ws = new WebSocket("ws" + path + "ocelot-endpoint");
				ws.onmessage = onwsmessage;
				ws.onopen = onwsopen;
				ws.onerror = onwserror;
				ws.onclose = onwsclose;
			});
		}
		function _close(reason) {
			setTimeout(function (w) {
				w.close(1000, reason | "Normal closure; the connection successfully completed whatever purpose for which it was created.");
			}, 10, ws);
		}
		function _addPromise(promise) {
			if (isTopicSubscription(promise, STATUS)) {
				addPromiseToId(promise, STATUS);
				stateUpdated();
				return;
			}
			if (isTopicUnsubscription(promise, STATUS)) {
				clearPromisesForId(STATUS);
				return;
			}
			// if it's internal service like ocelotController.open or ocelotController.close
			if (isOcelotControllerServices(promise)) {
				addPromiseToId(promise, promise.id);
				return;
			}
			// check entry cache
			var msgToClient = _cacheManager.getResultInCache(promise.id, promise.cacheIgnored);
			if (msgToClient) {
				// present and valid, return response without call
				promise.response = createResultEventFromPromise(promise, { "response": msgToClient.response, "t": 0 });
				return;
			}
			// else call
			sendMfc(promise);
		}
		function init() {
			// init a standard httpsession and init websocket
			connect().then(function () {
				// Controller subscribe to ocelot-cleancache topic
				ocelotServices.subscribe(CLEANCACHE).message(function (id) {
					if (id === ALL) {
						ocelotController.cacheManager.clearCache();
					} else {
						ocelotController.cacheManager.removeEntryInCache(id);
					}
				}).then(function () {
					// Get Locale from server or cache and re-set it in session, this launch a message in ocelot-cleancache
					ocelotServices.getLocale().then(function (locale) {
						if (locale) {
							ocelotServices.setLocale(locale);
						}
					});
				});
				// send states or current objects in cache with lastupdate
				ocelotServices.getOutDatedCache(ocelotController.cacheManager.getLastUpdateCache()).then(function (entries) {
					ocelotController.cacheManager.removeEntries(entries);
				});
			});
		}
		init();
		return {
			get options() {
				return opts;
			},
			get status() {
				return ws ? stateLabels[ws.readyState] : "CLOSED";
			},
			close: _close,
			addPromise: _addPromise,
			cacheManager: _cacheManager
		};
	})();
} else {
	alert("Sorry, but your browser doesn't support websocket");
}
