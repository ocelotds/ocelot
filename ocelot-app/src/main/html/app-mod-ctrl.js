angular.module('service-app', [
	'ui.codemirror', 'chart.js'
]).directive('ocelotService', function () {
	return {
		restrict: 'E',
		transclude: true,
		scope: {
			ds: '=ds'
		},
		templateUrl: 'service.html'
	};
}).controller('ServiceController', ['$scope',
	function ($scope) {
		var inst = this;
		inst.services = [];
		if (ocelotServices) {
			ocelotServices.getServices().then(function (result) {
				inst.services = result;
				$scope.$apply();
			});
		}
	}
]);
