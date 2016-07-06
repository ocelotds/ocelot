(function () {
	'use strict';
	window.ocelotServices = (function () {
		'use strict';
		var _ds = "org.ocelotds.OcelotServices";
		return {
			/**
			 * get current user locale
			 *
			 * @return {java.util.Locale}
			 */
			getLocale : function () {
				return promiseFactory.create(_ds, "781c53ed36c8e09e518531b803fba95f_" + JSON.stringify([]).md5(), "getLocale", false, []);
			},
			/**
			 * Get number of subscriber
			 * 
			 * @param {java.lang.String} topic
			 * @return {java.lang.Integer}
			 */
			getNumberSubscribers : function (topic) {
				return promiseFactory.create(_ds, "cfa53ab38ca43a942121b4f570dfe95e_" + JSON.stringify([topic]).md5(), "getNumberSubscribers", false, [topic]);
			},
			/**
			 * GEt outdated cache among list
			 * @param {java.util.Map<java.lang.String,java.lang.Long>} states
			 * @return {java.util.Collection<java.lang.String>}
			 */
			getOutDatedCache : function (states) {
				return promiseFactory.create(_ds, "5404853cfea31cbe84ca88246988a1fa_" + JSON.stringify([states]).md5(), "getOutDatedCache", false, [states]);
			},
			/**
			 * return current username from session
			 *
			 * @return {java.lang.String}
			 */
			getUsername : function () {
				return promiseFactory.create(_ds, "f84864d6fe75d993429e068b3ff5ae15_" + JSON.stringify([]).md5(), "getUsername", false, []);
			},
			/**
			 * @return {java.lang.String}
			 */
			getVersion : function () {
				return promiseFactory.create(_ds, "ff1161ede2db4eac97fd8dd4a57bf0f3_" + JSON.stringify([]).md5(), "getVersion", false, []);
			},
			/**
			 * Init core
			 * @param {org.ocelotds.objects.Options} options
			 * @param {javax.servlet.http.HttpSession} httpSession
			 */
			initCore : function (options, httpSession) {
				return promiseFactory.create(_ds, "f99f49fe74bbf490d0f0b2480b150ffc_" + JSON.stringify([options,httpSession]).md5(), "initCore", false, [options,httpSession]);
			},
			/**
			 * define locale for current user
			 * 
			 * @param {java.util.Locale} locale
			 */
			setLocale : function (locale) {
				return promiseFactory.create(_ds, "f266133b09e8b50cce062c3e2a9771c3_" + JSON.stringify([locale]).md5(), "setLocale", false, [locale]);
			},
			/**
			 * Subscribe to topic
			 * 
			 * @param {java.lang.String} topic
			 * @param {javax.websocket.Session} session
			 * @return {java.lang.Integer}
			 */
			subscribe : function (topic, session) {
				return promiseFactory.create(_ds, "01427e6f30f92a84bec0882479ed265d_" + JSON.stringify([topic,session]).md5(), "subscribe", true, [topic,session]);
			},
			/**
			 * Unsubscribe to topic
			 * @param {java.lang.String} topic
			 * @param {javax.websocket.Session} session
			 * @return {java.lang.Integer}
			 */
			unsubscribe : function (topic, session) {
				return promiseFactory.create(_ds, "1b8a918a6d81be3aa8c9bb87cec3d361_" + JSON.stringify([topic,session]).md5(), "unsubscribe", true, [topic,session]);
			}
		};
	})();
})();
