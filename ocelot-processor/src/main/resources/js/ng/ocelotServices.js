(function () {
	'use strict';
	try {
		angular.module("ocelot.ds");
	} catch (e) {
		angular.module("ocelot.ds", []);
	}
	angular.module("ocelot.ds").provider('ocelotServices', provider);
	provider.$inject = ['promiseFactoryProvider'];
	/* @ngInject */
	function provider(promiseFactoryProvider) {
		this.$get = function () {
			var _ds = "org.ocelotds.OcelotServices";
			return {
				getLocale: function () {
					return promiseFactoryProvider.$get().create(_ds, "781c53ed36c8e09e518531b803fba95f_", "getLocale", false, [], []);
				},
				getNumberSubscribers: function (topic) {
					return promiseFactoryProvider.$get().create(_ds, "cfa53ab38ca43a942121b4f570dfe95e_", "getNumberSubscribers", false, ["topic"], [topic]);
				},
				getOutDatedCache: function (states) {
					return promiseFactoryProvider.$get().create(_ds, "5404853cfea31cbe84ca88246988a1fa_", "getOutDatedCache", false, ["states"], [states]);
				},
				getUsername: function () {
					return promiseFactoryProvider.$get().create(_ds, "f84864d6fe75d993429e068b3ff5ae15_", "getUsername", false, [], []);
				},
				initCore: function (options) {
					return promiseFactoryProvider.$get().create(_ds, "f99f49fe74bbf490d0f0b2480b150ffc_", "initCore", false, ["options", "httpSession"], [options, null]);
				},
				setLocale: function (locale) {
					return promiseFactoryProvider.$get().create(_ds, "f266133b09e8b50cce062c3e2a9771c3_", "setLocale", false, ["locale"], [locale]);
				},
				subscribe: function (topic) {
					return promiseFactoryProvider.$get().create(_ds, "01427e6f30f92a84bec0882479ed265d_", "subscribe", true, ["topic", "session"], [topic, null]);
				},
				unsubscribe: function (topic) {
					return promiseFactoryProvider.$get().create(_ds, "1b8a918a6d81be3aa8c9bb87cec3d361_", "unsubscribe", true, ["topic", "session"], [topic, null]);
				}
			};
		};
	}
})();