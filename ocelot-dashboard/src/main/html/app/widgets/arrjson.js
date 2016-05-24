angular.module('app').filter('arrjson', function () {
	return function (input) {
		var result = [];
		input.forEach(function (a, idx, arr) {
			result.push(JSON.stringify(a));
		});
		return result.join(',');
	};
});

