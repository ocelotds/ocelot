(function () {
	'use strict';
	angular.module('app.dashboard').directive('wsBadge', wsBadge);

	function wsBadge() {
		var directive = {
			restrict: 'E',
			templateUrl: 'app/widgets/ws-badge.html',
			controller: WsBadgeCtrl,
			controllerAs: 'bctrl'
		};
		return directive;
	}
	/* @ngInject */
	function WsBadgeCtrl($scope, subscriberFactory) {
		var vm = this;
		vm.color = "#e05d44";
		vm.status = "closed";
		subscriberFactory.createSubscriber("ocelot-status").message(function (msg) {
			vm.color = "#97CA00";
			vm.status = "opened";
			if (msg !== "OPEN") {
				vm.status = "closed";
				vm.color = "#e05d44";
			}
			$scope.$apply();
		});
	}
})();
