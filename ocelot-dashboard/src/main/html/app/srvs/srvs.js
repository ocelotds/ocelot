(function () {
	'use strict';
	angular.module('srvs.module', []).config(config);

	/* @ngInject */
	function config($stateProvider) {
		$stateProvider.state('root', {
			parent: 'template',
			url: '',
			views: {
				"content@": {
					templateUrl: "app/srvs/srvs.html",
					controller: TestCtrl,
					controllerAs: "ctrl",
					resolve: {
						services: getServices
					}
				}
			}
		}).state('home', {
			parent: 'root',
			url: '/'
		}).state('srvs', {
			parent: 'root',
			url: '/srvs'
		});
	}
	function TestCtrl(services) {
		var ctrl = this;
		ctrl.services = services;
	}
	/* @ngInject */
	function getServices($q) {
		var deferred = $q.defer();
		serviceServices.getServices().then(function (services) {
			deferred.resolve(services);
		});
		return deferred.promise;
	}
})();


