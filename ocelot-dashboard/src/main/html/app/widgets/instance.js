(function () {
	'use strict';
	angular.module('app.dashboard').filter('instance', function () {
		return function (ds) {
			var instance = ds.substring(ds.lastIndexOf(".")+1);
			return instance.substr(0, 1).toLowerCase()+instance.substring(1);
		};
	});
})();

