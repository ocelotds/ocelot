(function () {
	'use strict';
	angular.module('spy.module', []).config(config);

	/* @ngInject */
	function config($stateProvider) {
		$stateProvider.state('spy', {
			parent: 'template',
			url: '/spy',
			views: {
				"content@": {
					templateUrl: "app/spy/spy.html",
					controller: SpyCtrl,
					controllerAs: "ctrl",
					resolve: {
						sessions: initSessions,
						addHandler: initAddHandler,
						removeHandler: initRemoveHandler,
						updateHandler: initUpdateHandler
					}
				}
			}
		});
	}
	/* @ngInject */
	function SpyCtrl($scope, sessions, addHandler, removeHandler, updateHandler) {
		var MSG = "// Enabled a client to monitor it !!!";
		var ctrl = this;
		ctrl.requestSubscriber = null;
		ctrl.sessions = sessions;
		ctrl.requests = [];
		ctrl.filterInput = "";
		ctrl.request = MSG;
		ctrl.monitored = null;
		ctrl.showOnlyWarning = false;
		ctrl.triggerDelay = 20;
		ctrl.refresh = refresh;
		ctrl.switchMonitor = switchMonitor;
		ctrl.selectRequest = selectRequest;
		addHandler(add);
		removeHandler(remove);
		updateHandler(update);

		function add(session) {
			ctrl.sessions.push(session);
			$scope.$apply();
		}
		function update(session) {
			ctrl.sessions.forEach(function (s, idx, arr) {
				if (s.id === session.id) {
					arr.splice(idx, 1, session);
					$scope.$apply();
					return;
				}
			});
		}
		function remove(session) {
			ctrl.sessions.forEach(function (s, idx, arr) {
				if (s.id === session.id) {
					arr.splice(idx, 1);
					$scope.$apply();
					return;
				}
			});
		}
		
		function selectRequest(request) {
			ctrl.request = "// Request\n" + JSON.stringify(request.mfc, null, 3) + "\n// Response\n" + JSON.stringify(request.mtc, null, 3);
		}
		function switchMonitor(id) {
			ctrl.requests = [];
			if (ctrl.monitored) {
				if (ctrl.requestSubscriber) {
					ctrl.requestSubscriber.unsubscribe().event(function (event) {
						sessionServices.unmonitorSession(ctrl.monitored);
					});
				}
			}
			ctrl.request = MSG;
			ctrl.requestSubscriber = null;
			if (this.monitored === id) {
				ctrl.monitored = null;
			} else {
				ctrl.monitored = id;
				ctrl.requestSubscriber = new Subscriber("request-event-" + id).message(function (result) {
					ctrl.requests.splice(0, 0, result);
					$scope.$apply();
				});
				sessionServices.monitorSession(id).then(function () {
					ctrl.request = "// Monitoring "+id+" enabled"
					$scope.$apply();
				}).catch(function (fault) {
					ctrl.request = "// Monitoring "+id+" failed\n"+JSON.stringify(fault, null, 3)
					ctrl.monitored = null;
					$scope.$apply();
					ctrl.requestSubscriber.unsubscribe();
				});
			}
		}
		function refresh() {
			sessionServices.getSessionInfos().then(function (sessions) {
				ctrl.sessions = sessions;
				$scope.$apply();
			});
		}
	}
	/* @ngInject */
	function initSessions($q) {
		var deferred = $q.defer();
		sessionServices.getSessionInfos().then(function (sessions) {
			deferred.resolve(sessions);
		});
		return deferred.promise;
	}
	/* @ngInject */
	function initAddHandler($q) {
		return new Subscriber("sessioninfo-add").message;
	}
	/* @ngInject */
	function initRemoveHandler($q) {
		return new Subscriber("sessioninfo-remove").message;
	}
	/* @ngInject */
	function initUpdateHandler($q) {
		return new Subscriber("sessioninfo-update").message;
	}
})();


