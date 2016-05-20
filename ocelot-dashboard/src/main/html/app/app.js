(function () {
	'use strict';
	angular.module('app', [
		'ui.codemirror', 
		'chart.js',
		'ui.router',
		'ui.bootstrap',
		'srvs.module', 
		'sessions.module'
	]).config(config);

	/* @ngInject */
	function config($stateProvider, $urlRouterProvider) {
		$urlRouterProvider.otherwise('');
		$stateProvider.state('template', {
			abstract: true,
			views: {
				"content": {
					template: ''
				},
				"topmenu": {
					templateUrl: "app/topmenu.html",
					controller: TopMenuCtrl,
					controllerAs: "ctrl"
				}
			}
		});
	}
	function TopMenuCtrl() {
		var ctrl = this;
	}
})();
