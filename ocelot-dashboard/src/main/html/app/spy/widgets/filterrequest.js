(function () {
	'use strict';
	angular.module('spy.module').filter('filterrequest', function () {
		return function (input, filterInput, triggerDelay, showOnlyWarning) {
			var result = [];
			if(input) {
				input.forEach(function (request, idx, arr) {
					if((request.mfc.ds.search(filterInput) !== -1 || request.mfc.op.search(filterInput) !== -1) && 
						(!showOnlyWarning || request.mtc.type === 'FAULT' || request.t > triggerDelay)) {
						result.push(request);
					}
				});
			}
			return result;
		};
	});
})();

