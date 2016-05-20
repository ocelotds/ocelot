(function () {
	'use strict';
	angular.module('app').provider('serviceServices2', serviceServices);

	function serviceServices() {
		this.$get = ServiceServices;
		this._ds = "org.ocelotds.dashboard.services.ServiceServices";
		function ServiceServices() {
			var service = {
				getServices: getServices
			};
			return service;
		}
		function getServices() {
			if (typeof OcelotPromiseFactory !== "undefined"){
				return OcelotPromiseFactory.createPromise(_ds,"dc7975911684dbacf5970cd5b73ee786_"+JSON.stringify([]).md5(),"getServices",[],[]);
			}
			return [];
		}

	}}
)();

