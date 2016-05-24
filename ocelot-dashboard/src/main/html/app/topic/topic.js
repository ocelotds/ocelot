(function () {
	'use strict';
	angular.module('topic.module', []).config(config);

	/* @ngInject */
	function config($stateProvider) {
		$stateProvider.state('topic', {
			parent: 'template',
			url: '/topic',
			views: {
				"content@": {
					templateUrl: "app/topic/topic.html",
					controller: TopicCtrl,
					controllerAs: "ctrl",
					resolve: {
//						sessionsBytopic: initSessionsTopic,
//						addHandler: initAddHandler,
//						removeHandler: initRemoveHandler
					}
				}
			}
		});
	}
	/* @ngInject */
	function TopicCtrl($scope/*, sessionsBytopic, addHandler, removeHandler*/) {
		var ctrl = this;
		ctrl.topics = []; // Object.keys(sessionsBytopic);
		ctrl.sessionsBytopic = {}; //sessionsBytopic; // {"topicname", [{"id":"sessionid","username":"principalname"}, {"id":"sessionid","username":"principalname"}]}
		ctrl.topic = null; // topic selected
		ctrl.subscription = null; // topic subscribed
		ctrl.subscriber = null; // subscriber to ctrl.subscription
		ctrl.session = null; // session selected
		ctrl.messages = ""; // messages zone
		ctrl.payload = "// Enter message to send";
		ctrl.selectTopic = selectTopic;
		ctrl.selectSession = selectSession;
		ctrl.subscribe = subscribe;
		ctrl.sendPayload = sendPayload;
//		removeHandler(remove);
//		addHandler(add);
		ctrl.subAdd = null;
		ctrl.subRemove = null;
		init();



		function init() {
			ctrl.subAdd = new Subscriber("session-topic-add").event(function (evt) {
				ctrl.subRemove = new Subscriber("session-topic-remove").event(function (evt) {
					topicServices.getSessionIdsByTopic().then(function (sessionsBytopic) {
						ctrl.topics = Object.keys(sessionsBytopic);
						ctrl.sessionsBytopic = sessionsBytopic; // {"topicname", [{"id":"sessionid","username":"principalname"}, {"id":"sessionid","username":"principalname"}]}
						$scope.$apply();
					});
				}).message(remove);
				$scope.$apply();
			}).message(add);
		}
		function sendPayload(payload, topic, session) {
			if (payload && topic) {
				if (session) {
					topicServices.sendJsonToTopicForSession(payload, topic, session.id);
				} else {
					topicServices.sendJsonToTopic(payload, topic);
				}
			}
		}
		/**
		 * subscribe to to
		 * @param {string} to
		 */
		function subscribe(to) {
			ctrl.messages = "";
			if (ctrl.subscription && ctrl.subscriber) {
				ctrl.subscriber.unsubscribe();
				ctrl.messages = "// Unsubscription to " + ctrl.subscriber.topic;
				ctrl.subscriber = null;
			}
			if (ctrl.subscription === to) {
				ctrl.subscription = null;
			} else {
				ctrl.subscription = to;
				ctrl.subscriber = new Subscriber(to).catch(function (fault) {
					ctrl.subscriber = null;
					ctrl.subscription = null;
					ctrl.messages = "// Subscription to " + ctrl.subscription + " failed\n" + JSON.stringify(fault, null, 3);
					$scope.$apply();
				}).then(function (res) {
					ctrl.messages = "// Subscription to " + ctrl.subscription + " done";
					$scope.$apply();
				}).message(function (msg) {
					ctrl.messages += "// Receive message to " + ctrl.subscription + "\n" + JSON.stringify(msg, null, 3) + "\n";
					$scope.$apply();
				});
			}
		}
		function selectTopic(topic) {
			ctrl.topic = topic;
			ctrl.session = null;
		}
		function selectSession(session) {
			ctrl.session = session;
		}
		function add(topic_sessionInfo) {
			var topic = topic_sessionInfo.topic;
			if (ctrl.topics.indexOf(topic) === -1) {
				ctrl.topics.push(topic);
				ctrl.sessionsBytopic[topic] = [];
			}
			ctrl.sessionsBytopic[topic].push(topic_sessionInfo.sessionInfo);
			$scope.$apply();
		}
		function remove(topic_sessionInfo) {
			var topic = topic_sessionInfo.topic;
			if (ctrl.sessionsBytopic[topic]) {
				var sessionInfo = topic_sessionInfo.sessionInfo;
				ctrl.sessionsBytopic[topic].every(function (s, idx, arr) {
					if (s.id === sessionInfo.id) {
						arr.splice(idx, 1);
						if (!arr.length) {
							var n = ctrl.topics.indexOf(topic);
							ctrl.topics.splice(n, 1);
						}
						$scope.$apply();
						return false;
					}
					return true;
				});
			}
		}
	}
	/* @ngInject */
	function initSessionsTopic($q) {
		var deferred = $q.defer();
		topicServices.getSessionIdsByTopic().then(function (sessionsBytopic) {
			deferred.resolve(sessionsBytopic);
		});
		return deferred.promise;
	}
	/* @ngInject */
	function initAddHandler($q) {
		return new Subscriber("session-topic-add").message;
	}
	/* @ngInject */
	function initRemoveHandler($q) {
		return new Subscriber("session-topic-remove").message;
	}
})();


