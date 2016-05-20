(function () {
	'use strict';
	angular.module('app').directive('wsBadge', wsBadge);

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
	function WsBadgeCtrl($scope) {
		var vm = this;
		vm.color = "#e05d44";
		vm.status = "closed";
		new Subscriber("ocelot-status").message(function (msg) {
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
