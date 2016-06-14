(function () {
	window.subscriberFactory = (function () {
		'use strict';
		return {
			createSubscriber:function(topic) {
				return new Subscriber(topic);
			}
		};
	})();
	window.Subscriber = (function (topic) {
		var promise = ocelotServices.subscribe(topic);
		Object.defineProperty(promise, "topic", {value: topic, writable: false}); // add topic property, readonly
		delete promise.constraint;
		promise.unsubscribe = function () {
			var p = ocelotServices.unsubscribe(topic);
			p.uid = this.uid;
			return p;
		};
		return promise;
	});
})();

