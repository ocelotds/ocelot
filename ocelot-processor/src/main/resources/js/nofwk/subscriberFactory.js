var promiseFactory = (function () {
	'use strict';
	return {
		createSubscriber:function(topic) {
			return new Subscriber(topic);
		}
	};
})();
var Subscriber = (function (topic) {
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

