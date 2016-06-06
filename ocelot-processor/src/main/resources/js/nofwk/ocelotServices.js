var ocelotServices = (function () {
	'use strict';
	var _ds="org.ocelotds.OcelotServices";
	return{
		getLocale: function () {
			return promiseFactory.create(_ds, "781c53ed36c8e09e518531b803fba95f", "getLocale", false, [], []);
		},
		getNumberSubscribers: function (topic) {
			return promiseFactory.create(_ds, "cfa53ab38ca43a942121b4f570dfe95e", "getNumberSubscribers", false, ["topic"], [topic]);
		},
		getOutDatedCache: function (states) {
			return promiseFactory.create(_ds, "5404853cfea31cbe84ca88246988a1fa", "getOutDatedCache", false, ["states"], [states]);
		},
		getUsername: function () {
			return promiseFactory.create(_ds, "f84864d6fe75d993429e068b3ff5ae15", "getUsername", false, [], []);
		},
		initCore: function (options) {
			return promiseFactory.create(_ds, "f99f49fe74bbf490d0f0b2480b150ffc", "initCore", false, ["options", "httpSession"], [options, null]);
		},
		setLocale: function (locale) {
			return promiseFactory.create(_ds, "f266133b09e8b50cce062c3e2a9771c3", "setLocale", false, ["locale"], [locale]);
		},
		subscribe: function (topic) {
			return promiseFactory.create(_ds, "01427e6f30f92a84bec0882479ed265d", "subscribe", true, ["topic", "session"], [topic, null]);
		},
		unsubscribe: function (topic) {
			return promiseFactory.create(_ds, "1b8a918a6d81be3aa8c9bb87cec3d361", "unsubscribe", true, ["topic", "session"], [topic, null]);
		}
	};	
})();

