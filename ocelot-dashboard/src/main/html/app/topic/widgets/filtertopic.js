(function () {
	'use strict';
	angular.module('topic.module').filter('filtertopic', function () {
		return function (input) {
			var result = [];
			if(input) {
				input.forEach(function (topic, idx, arr) {
					if(topic !== "session-topic-add" && topic !== "session-topic-remove" && 
						  topic !== "sessioninfo-update" && topic !== "sessioninfo-add" && 
						  topic !== "sessioninfo-remove") {
						result.push(topic);
					}
				});
			}
			return result;
		};
	});
})();


