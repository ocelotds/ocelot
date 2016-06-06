(function() {
	'use strict';
	try {
		angular.module("ocelot.ds");
	} catch (e) {
		angular.module("ocelot.ds", []);
	}
	angular.module("ocelot.ds").factory('subscriberFactory', factory);
	factory.$inject = ['ocelotServices'];
	/* @ngInject */
	function factory(ocelotServices) {
		return {
			createSubscriber: function (topic) {
				var promise = ocelotServices.subscribe(topic);
				Object.defineProperty(promise, "topic", {value: topic, writable: false}); // add topic property, readonly
				delete promise.constraint;
				promise.unsubscribe = function () {
					var p = ocelotServices.unsubscribe(topic);
					p.uid = this.uid;
					return p;
				};
				return promise;
			}
		};
	}
})();