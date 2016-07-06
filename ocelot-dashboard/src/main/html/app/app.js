(function () {
	'use strict';
	angular.module('app.dashboard', [
		'hljs',
		'ui.codemirror',
		'chart.js',
		'ui.router',
		'ui.bootstrap',
		'srv.module',
		'spy.module',
		'topic.module', 
		'ocelot.ds'
	]).config(config);

	/* @ngInject */
	function config($stateProvider, $urlRouterProvider, hljsServiceProvider) {
		hljsServiceProvider.setOptions({
		  tabReplace: '  '
		});
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
						username: initUsername,
						version: initVersion
					}
				}
			}
		});
	}
	/* @ngInject */
	function TopMenuCtrl(username, version) {
		var ctrl = this;
		ctrl.username = username;
		ctrl.version = version;
	}
	/* @ngInject */
	function initUsername($q, ocelotServices) {
		var deferred = $q.defer();
		ocelotServices.getUsername().then(function (username) {
			deferred.resolve(username);
		});
		return deferred.promise;
	}
	/* @ngInject */
	function initVersion($q, ocelotServices) {
		var deferred = $q.defer();
		ocelotServices.getVersion().then(function (version) {
			deferred.resolve(version);
		});
		return deferred.promise;
	}
})();
