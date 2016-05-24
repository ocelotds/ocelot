(function () {
	'use strict';
	angular.module('srv.module', []).config(config);

	/* @ngInject */
	function config($stateProvider) {
		$stateProvider.state('srv', {
			parent: 'template',
			url: '/srv',
			views: {
				"content@": {
					templateUrl: "app/srv/srv.html",
					controller: TestCtrl,
					controllerAs: "ctrl",
					resolve: {
						services: initServices
					}
				}
			}
		}).state('root', {
			parent: 'srv',
			url: '/'
		});
	}
	/* @ngInject */
	function TestCtrl(services) {
		var ctrl = this;
		ctrl.services = services;
		ctrl.service = services.length?services[0]:null;
	}
	/* @ngInject */
	function initServices($q) {
		var deferred = $q.defer();
		serviceServices.getServices().then(function (services) {
			deferred.resolve(services);
		});
		return deferred.promise;
	}
})();


