(function () {
	'use strict';
	angular.module('app', [
		'ui.codemirror',
		'chart.js',
		'ui.router',
		'ngAnimate',
		'ui.bootstrap',
		'srv.module',
		'spy.module',
		'topic.module', 
		'ocelotds'
	]).config(config);

	/* @ngInject */
	function config($stateProvider, $urlRouterProvider) {
      $urlRouterProvider.otherwise('/srv');
		$stateProvider.state('template', {
			abstract: true,
			views: {
				"content": {
					template: ''
				},
				"topmenu": {
					templateUrl: "app/topmenu.html",
					controller: TopMenuCtrl,
					controllerAs: "ctrl",
					resolve: {
						username: initUsername
					}
				}
			}
		});
	}
	/* @ngInject */
	function TopMenuCtrl(username) {
		var ctrl = this;
		ctrl.username = username;
	}
	/* @ngInject */
	function initUsername($q) {
		var deferred = $q.defer();
		ocelotServices.getUsername().then(function (username) {
			deferred.resolve(username);
		});
		return deferred.promise;
	}
})();
