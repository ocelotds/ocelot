(function () {
	'use strict';
	angular.module('app').directive('btnSwitch', btnSwitch);

	function btnSwitch() {
		var directive = {
			restrict: 'E',
			templateUrl: 'app/widgets/btn-switch.html',
			scope : {
				onswitch : "&onswitch",
				value : '=',
				disabled: '=',
				size: '=',
				options: '='
			},
			controller : BtnSwitchCtrl,
			controllerAs : 'bsctrl'
		};
		return directive;
	}
	/* @ngInject */
	function BtnSwitchCtrl($scope) {
		var ctrl = this;
		ctrl.switchFn = switchFn;
		function switchFn($event) {
			$event.stopPropagation();
			if($scope.onswitch) $scope.onswitch();
		}
	}
})();

