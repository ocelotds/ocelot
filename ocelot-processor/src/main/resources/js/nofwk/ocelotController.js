var ocelotController;
if ("WebSocket" in window) {
	ocelotController = (function () {
		'use strict';
		var opts = {"monitor": false, "debug": false}, MSG = "MESSAGE", CONSTRAINT = "CONSTRAINT", RES = "RESULT";
		var FAULT = "FAULT", ALL = "ALL", EVT = "Event", ADD = "add", RM = "remove", CLEANCACHE = "ocelot-cleancache", ALERT = "ocelot-alert";
		var STATUS = "ocelot-status", OSRV = "org.ocelotds.OcelotServices", SUB = "subscribe", UNSUB = "unsubscribe", initialized = false;
		var uid = 0, stateLabels = ['CONNECTING', 'OPEN', 'CLOSING', 'CLOSED'], closetimer, promises = {}, path, ws = null, _listenerSetted = false, readyFunction;
		var _cacheManager = (function () {
			var LU = "ocelot-lastupdate", addHandlers = [], removeHandlers = [];
			var lastUpdateManager = (function () {
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
			})();
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
						_removeEntryInCache(compositeKey);
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
		})();
		function getUid() {
			return ++uid;
		}
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
				evt.javatime = msgToClient.t; // backend timing
			}
			if (evt.javatime) {
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
				promise.response = createMessageEventFromPromise(promise, {"response": _status(), "t": 0});
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
		function removePromiseForTopic(promise, topic) {
			if (promises.hasOwnProperty(topic)) {
				var promiseForTopic = promises[topic];
				promiseForTopic.every(function(p, idx, arr) {
					if(promise.uid === p.uid) {
						promiseForTopic.splice(idx, 1);
						if (!promiseForTopic.length) {
							clearPromisesForId(topic);
						}
						return false;
					}
					return true;
				});
			}
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
			params.every(function (param) {
				if (param.search(/^\??ocelot=/) === 0) {
					opts = JSON.parse(decodeURI(param.replace(/\??ocelot=/, "")));
					return false;
				}
				return true;
			});
		}
		function sendMfc(promise) {
			var msgToClient, mfc, xhttp;
			if (!addPromiseToId(promise, promise.id)) {
				mfc = JSON.stringify(promise.json);
				if (promise.ws) {
					if (ws.readyState === WebSocket.OPEN) {
						ws.send(mfc);
					} else {
						if(opts.debug) 
							console.debug("warning : Websocket is not ready, defer "+promise.dataservice+"."+promise.operation+"("+promise.args+");")
					}
				} else {
					xhttp = new XMLHttpRequest();
					xhttp.timeout = promise.maxtime;
					xhttp.ontimeout = function() {
						receiveMtc({"id": promise.id, "type": FAULT, "response": {"classname": "XMLHttpRequest", "message": xhttp.statusText, "stacktrace": []}, "t": 0});
					};
					xhttp.onerror = function () {
						receiveMtc({"id": promise.id, "type": FAULT, "response": {"classname": "XMLHttpRequest", "message": xhttp.statusText, "stacktrace": []}, "t": 0});
					};
					xhttp.onload = function () {
						if (xhttp.readyState === XMLHttpRequest.DONE) {
							if (xhttp.status === 200) {
								msgToClient = JSON.parse(xhttp.responseText);
								receiveMtc(msgToClient);
							} else {
								receiveMtc({"id": promise.id, "type": FAULT, "response": {"classname": "XMLHttpRequest", "message": xhttp.statusText, "stacktrace": []}, "t": 0});
							}
						}
					};
					xhttp.open("POST", "http" + path + "ocelot/endpoint", true);
					xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
					xhttp.send("mfc=" + mfc);
				}
			}
		}
		function receiveMtc(msgToClient) {
			if (msgToClient.type === RES) { // maybe should be store result in cache
				// if msgToClient has dead line so we stock in cache
				_cacheManager.putResultInCache(msgToClient);
			}
			foreachPromiseInPromisesDo(msgToClient.id, function (promise) {
				if (opts.debug)
					console.debug(promise.dataservice+"."+promise.operation+" : ", msgToClient);
				switch (msgToClient.type) {
					case FAULT:
						promise.response = createFaultEventFromPromise(promise, msgToClient);
						break;
					case RES:
						// if msg is response of subscribe request
						if (isTopicSubscription(promise)) {
							promise.uid = getUid(); // useful for found promise
							addPromiseToId(promise, promise.args[0]);
						} else if (isTopicUnsubscription(promise)) {
							removePromiseForTopic(promise, promise.args[0]);
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
			receiveMtc(JSON.parse(evt.data));
		}
		function onwserror(evt) {
			console.info("Websocket error : " + evt.reason);
			stateUpdated();
		}
		function onwsclose(evt) {
			stateUpdated();
			// TODO think about autoreconnect, disabled for the moment
			if (false && evt.reason !== "ONUNLOAD") {
				if (opts.debug)
					console.debug("Websocket closed : " + evt.reason + " try reconnect each " + 1000 + "ms");
				closetimer = setInterval(function () {
					connect();
				}, 1000);
			}
		}
		function connect() {
			if (opts.debug)
				console.debug("Ocelotds initialization...");
			var re = /ocelot\/core.*/;
			for (var i = 0; i < document.scripts.length; i++) {
				var item = document.scripts[i];
				if (item.src.match(re)) {
					path = item.src.replace(/^http/, "").replace(re, "");
				}
			}
			extractOptions(document.location.search);
			// init a standard httpsession and init websocket
			return ocelotServices.initCore(opts, null).then(function () {
				ws = new WebSocket("ws" + path + "ocelot-endpoint");
				ws.onmessage = onwsmessage;
				ws.onopen = onwsopen;
				ws.onerror = onwserror;
				ws.onclose = onwsclose;
			});
		}
		function addPromiseEvent(event) {
			_addPromise(event.promise);
		}
		function _close(reason) {
			setTimeout(function (w) {
				if(w) {
					w.close(1000, reason | "Normal closure; the connection successfully completed whatever purpose for which it was created.");
				}
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
				promise.response = createResultEventFromPromise(promise, {"response": msgToClient.response, "t": 0});
				return;
			}
			// else call
			// if debug mode add handler for catch and log error 
			if (opts.debug) {
				promise.catch(function (fault) {
					console.table(fault.stacktrace);
				});
			}
			sendMfc(promise);
		}
		function _status() {
			return ws ? stateLabels[ws.readyState] : "CLOSED";
		}
		function _onready(func) {
			if(_listenerSetted) {
				if(func!==null) {
					func();
				}
			} else {
				readyFunction = func;
			}
		}
		function onwsopen(evt) {
			if (closetimer)
				clearInterval(closetimer);
			if (opts.debug)
				console.debug("Websocket opened");
			var pss;
			// handler, apromise, idx, promise;
			stateUpdated();
			pss = promises; // {"md5_1" : [p1, p2], "md5_2" : [p3, p4]... }
			Object.keys(pss).forEach(function (id) { // we redo the subscription
				var ps = pss[id];
				if(ps && ps.length && ps[0].id !== id) { // if ps[0].id !== id then topic. we can test the md5 of ocelotServices.subscribe(...)
					ps.forEach(function(p) {
			         if(opts.debug) 
				         console.debug("Send defered ws-calls : "+p.dataservice+"."+p.operation+"("+p.args+")");
						foreachPromiseDo(p, _addPromise);
					});
				}
			});
         if(!initialized) {
            if(opts.debug) 
               console.debug("Initialisation des subscribers...");
            initSubscribers();
            initialized = true;
			}
		}
		function initSubscribers() {
			// Controller subscribe to ocelot-cleancache topic
			subscriberFactory.createSubscriber(CLEANCACHE).message(function (id) {
				if (id === ALL) {
					_cacheManager.clearCache();
				} else {
					_cacheManager.removeEntryInCache(id);
				}
			}).then(function () {
				// Get Locale from server or cache and re-set it in session, this launch a message in ocelot-cleancache
				ocelotServices.getLocale().then(function (locale) {
					if (locale) {
						ocelotServices.setLocale(locale);
					}
				});
			});
			subscriberFactory.createSubscriber(ALERT).message(function (message) {
				alert(message);
			});
		}
		function init() {
			/**
			 * Add ocelotController events to document
			 * @param {Event} event
			 */
			document.addEventListener("call", addPromiseEvent);
			_listenerSetted = true;
			if(readyFunction) {
				readyFunction();
			}
			window.addEventListener("beforeunload", function (e) {
				_close("ONUNLOAD");
			});
			// init a standard httpsession and init websocket
			connect().then(function () {
				// send states or current objects in cache with lastupdate
				ocelotServices.getOutDatedCache(_cacheManager.getLastUpdateCache()).then(function (entries) {
					_cacheManager.removeEntries(entries);
				});
			});
		}
		init();
		return {
			get options() {
				return opts;
			},
			get status() {
				return _status();
			},
			close: _close,
			cacheManager: _cacheManager,
			onready: _onready
		};
	})();
} else {
	alert("Sorry, but your browser doesn't support websocket");
}
