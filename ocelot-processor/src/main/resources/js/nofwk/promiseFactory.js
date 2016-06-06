var promiseFactory = (function () {
	'use strict';
	return {
		create: function (ds, id, op, ws, argNames, args) {
			return (function (ds, id, op, ws, argNames, args) {
				var fault, evt = null, cacheIgnored = false, start = new Date().getTime(), timeout = 10000, key = id + "_" + JSON.stringify(args).md5();
				var thenHandlers = [], catchHandlers = [], constraintHandlers = [], eventHandlers = [], messageHandlers = [];
				function process() {
					if (!evt) {
						return;
					}
					if (evt.type !== "MESSAGE") {
						while (eventHandlers.length) {
							eventHandlers.shift()(evt);
						}
						switch (evt.type) {
							case "RESULT":
								while (thenHandlers.length) {
									thenHandlers.shift()(evt.response);
								}
								break;
							case "CONSTRAINT":
								while (constraintHandlers.length) {
									constraintHandlers.shift()(evt.response);
								}
								break;
							case "FAULT":
								fault = evt.response;
								console.error(fault.classname + "(" + fault.message + ")");
								while (catchHandlers.length) {
									catchHandlers.shift()(fault);
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
					get id() {
						return key;
					},
					get dataservice() {
						return ds;
					},
					get operation() {
						return op;
					},
					get args() {
						return args;
					},
					get t() {
						return start;
					},
					get ws() {
						return ws;
					},
					set response(e) {
						evt = e;
						process();
					},
					ignoreCache: function (ignore) {
						cacheIgnored = ignore;
						return this;
					},
					then: function (onFulfilled, onRejected) {
						if (onFulfilled) {
							thenHandlers.push(onFulfilled);
						}
						if (onRejected) {
							catchHandlers.push(onRejected);
						}
						process(); // event already receive ?
						return this;
					},
					catch : function (onRejected) {
						if (onRejected) {
							catchHandlers.push(onRejected);
						}
						process(); // event already receive ?
						return this;
					},
					constraint: function (onConstraint) {
						if (onConstraint) {
							constraintHandlers.push(onConstraint);
						}
						process(); // event already receive ?
						return this;
					},
					event: function (onEvented) {
						if (onEvented) {
							eventHandlers.push(onEvented);
						}
						process(); // event already receive ?
						return this;
					},
					message: function (onMessaged) {
						if (onMessaged) {
							messageHandlers.push(onMessaged);
						}
						process(); // event already receive ?
						return this;
					},
					get json() {
						return {"id": key, "ds": ds, "op": op, "argNames": argNames, "args": args};
					}
				};
				var e = document.createEvent("Event");
				e.initEvent("call", true, false);
				e.promise = promise;
				setTimeout(function () {
					document.dispatchEvent(e);
				}, 1);
				return promise;
			})(ds, id, op, ws, argNames, args);
		}
	};
})();

