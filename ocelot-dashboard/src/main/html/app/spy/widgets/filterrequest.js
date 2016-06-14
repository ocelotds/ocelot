(function () {
	'use strict';
	angular.module('spy.module').filter('filterrequest', function () {
		return function (input, filterInput, triggerDelay, showOnlyWarning) {
			if(input) {
				return input.filter(function(request) {
					return ((request.mfc.ds.search(filterInput) !== -1 || request.mfc.op.search(filterInput) !== -1) && 
						(!showOnlyWarning || request.mtc.type === 'FAULT' || request.t > triggerDelay));
				});
			}
			return input;
		};
	});
})();
