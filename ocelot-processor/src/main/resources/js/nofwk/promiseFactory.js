(function () {
	window.promiseFactory = (function () {
		'use strict';
		return {
			create: function (ds, id, op, ws, args) {
				return (function (ds, id, op, ws, args) {
					var fault, evt = null, _cacheIgnored = false, start = new Date().getTime(), _timeout = 10000, key = id;
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
						get maxtime() {
							return _timeout;
						},
						get cacheIgnored() {
							return _cacheIgnored;
						},
						timeout: function (timeout) {
							_timeout = timeout;
							return this;
						},
						ignoreCache: function (ignore) {
							_cacheIgnored = ignore;
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
							return {"id": key, "ds": ds, "op": op, "args": args};
						}
					};
					setTimeout(ocelotController.addPromise, 0, promise);
					return promise;
				})(ds, id, op, ws, args);
			}
		};
	})();
})();

