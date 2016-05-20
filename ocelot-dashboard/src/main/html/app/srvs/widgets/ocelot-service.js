(function () {
	'use strict';
	angular.module('srvs.module').directive('ocelotService', function () {
		return {
			transclude: true,
			restrict: 'E',
			scope: {
				ds: '='
			},
			templateUrl: 'app/srvs/widgets/ocelot-service.html'
		};
	});
})();


