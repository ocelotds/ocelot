(function () {
	'use strict';
	angular.module('spy.module').filter('filtersession', function () {
		return function (input, showDisabled) {
			if(showDisabled) return input;
			if(input) {
				return input.filter(function(session) {
					return session.open;
				});
			}
			return input;
		};
	});
})();
