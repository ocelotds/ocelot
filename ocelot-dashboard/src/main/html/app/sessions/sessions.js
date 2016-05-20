(function () {
	'use strict';
	angular.module('sessions.module', []).config(config);

	/* @ngInject */
	function config($stateProvider) {
		$stateProvider.state('sessions', {
			parent: 'template',
			url: '/sessions',
			views: {
				"content@": {
					templateUrl: "app/sessions/sessions.html",
					controller: SessionsCtrl,
					controllerAs: "ctrl"
				}
			}
		});
	}
	function SessionsCtrl() {
		var ctrl = this;
	}
})();


