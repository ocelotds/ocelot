(function () {
	'use strict';
	angular.module('app.dashboard').filter('arrjson', function () {
		return function (input) {
			var result = [];
			if(input) {
				input.forEach(function (a, idx, arr) {
					result.push(JSON.stringify(a));
				});
			}
			return result.join(',');
		};
	});
})();
