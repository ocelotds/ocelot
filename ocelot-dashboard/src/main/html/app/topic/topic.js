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
					resolve : {
						sessionsBytopic : initSessionsBytopic
					}
				}
			}
		});
	}
	/* @ngInject */
	function TopicCtrl($scope, topicServices, subscriberFactory, sessionsBytopic) {
		var ctrl = this;
		ctrl.topics = Object.keys(sessionsBytopic).filter(filter);
		ctrl.sessionsBytopic = sessionsBytopic; // {"topicname", [{"id":"sessionid","username":"principalname"}, {"id":"sessionid","username":"principalname"}]}
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
		ctrl.refresh = refresh;
		ctrl.subAdd = null;
		ctrl.subRemove = null;
		activate();
		$scope.$on('$destroy', desactivate);

		function desactivate() {
			if (ctrl.subAdd) {
				ctrl.subAdd.unsubscribe();
				ctrl.subAdd = null;
			}
			if (ctrl.subRemove) {
				ctrl.subRemove.unsubscribe();
				ctrl.subRemove = null;
			}
			if (ctrl.subscriber) {
				ctrl.subscriber.unsubscribe();
				ctrl.subscriber = null;
			}
		}
		function activate() {
			ctrl.subRemove = subscriberFactory.createSubscriber("session-topic-remove").message(remove);
			ctrl.subAdd = subscriberFactory.createSubscriber("session-topic-add").message(add);
		}
		function refresh() {
			topicServices.getSessionIdsByTopic().then(function (sessionsBytopic) {
				ctrl.topics = Object.keys(sessionsBytopic).filter(filter);
				ctrl.sessionsBytopic = sessionsBytopic; // {"topicname", [{"id":"sessionid","username":"principalname"}, {"id":"sessionid","username":"principalname"}]}
				$scope.$apply();
			});
		}
		function filter(elt) {
			return elt !== "session-topic-add" && elt !== "session-topic-remove" && 
					  elt !== "sessioninfo-update" && elt !== "sessioninfo-add" && elt !== "sessioninfo-remove";
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
				ctrl.messages = "// Unsubscription to " + ctrl.subscriber.topic+"\n";
				ctrl.subscriber = null;
			}
			if (ctrl.subscription === to) {
				ctrl.subscription = null;
			} else {
				ctrl.subscription = to;
				ctrl.subscriber = subscriberFactory.createSubscriber(to).catch(function (fault) {
					ctrl.subscriber = null;
					ctrl.subscription = null;
					ctrl.messages = "// Subscription to " + ctrl.subscription + " failed\n" + JSON.stringify(fault, null, 3);
					$scope.$apply();
				}).then(function (res) {
					ctrl.messages = "// Subscription to " + ctrl.subscription + " done\n";
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
	function initSessionsBytopic($q, topicServices) {
		var deferred = $q.defer();
		topicServices.getSessionIdsByTopic().then(function (sessionsBytopic) {
			deferred.resolve(sessionsBytopic);
		});
		return deferred.promise;
	}
})();


