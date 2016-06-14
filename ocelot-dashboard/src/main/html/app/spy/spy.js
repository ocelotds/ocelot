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
						sessions: initSessions
					}
				}
			}
		});
	}
	/* @ngInject */
	function SpyCtrl($scope, sessionServices, subscriberFactory, sessions) {
		var MSG = "// Enabled a client to monitor it !!!";
		var ctrl = this;
		ctrl.requestSubscriber = null;
		ctrl.addSubscriber = null;
		ctrl.removeSubscriber = null;
		ctrl.updateSubscriber = null;
		ctrl.sessions = sessions;
		ctrl.requests = [];
		ctrl.filterInput = "";
		ctrl.request = MSG;
		ctrl.monitored = null;
		ctrl.showDisabled = false;
		ctrl.showOnlyWarning = false;
		ctrl.triggerDelay = 20;
		ctrl.switchShowDisabled = switchShowDisabled;
		ctrl.switchMonitor = switchMonitor;
		ctrl.selectRequest = selectRequest;
		activate();
		$scope.$on('$destroy', desactivate);

		function desactivate(){
			unmonitor();
			if(ctrl.addSubscriber) {
				ctrl.addSubscriber.unsubscribe();
				ctrl.addSubscriber = null;
			}
			if(ctrl.removeSubscriber) {
				ctrl.removeSubscriber.unsubscribe();
				ctrl.removeSubscriber = null;
			}
			if(ctrl.updateSubscriber) {
				ctrl.updateSubscriber.unsubscribe();
				ctrl.updateSubscriber = null;
			}
		}

		function activate() {
			ctrl.addSubscriber = subscriberFactory.createSubscriber("sessioninfo-add").message(add);
			ctrl.removeSubscriber = subscriberFactory.createSubscriber("sessioninfo-remove").message(remove);
			ctrl.updateSubscriber = subscriberFactory.createSubscriber("sessioninfo-update").message(update);
		}

		function animateSession(session, animate, duration) {
			session.new = animate;
			setTimeout(function(s) {s.new = false;$scope.$apply();}, duration, session);
		}
		function unmonitor() {
			ctrl.monitored = null;
			if(ctrl.requestSubscriber) {
				ctrl.requestSubscriber.unsubscribe();
				ctrl.requestSubscriber = null;
			}
		}
		function add(session) {
			ctrl.sessions.push(session);
			animateSession(session, true, 5000);
			$scope.$apply();
		}
		function update(session) {
			ctrl.sessions.every(function (s, idx, arr) {
				if (s.id === session.id) {
					arr.splice(idx, 1, session);
					animateSession(session, session.open, 5000);
					$scope.$apply();
					return false;
				}
				return true;
			});
		}
		function remove(session) {
			ctrl.sessions.every(function (s, idx, arr) {
				if (s.id === session.id) {
					arr.splice(idx, 1);
					$scope.$apply();
					return false;
				}
				return true;
			});
		}
		
		function selectRequest(request) {
			ctrl.request = "// Request\n" + JSON.stringify(request.mfc, null, 3) + "\n// Response\n" + JSON.stringify(request.mtc, null, 3);
		}
		function switchShowDisabled() {
			ctrl.showDisabled = !ctrl.showDisabled;
		}
		function switchMonitor(id) {
			ctrl.requests = [];
			if (ctrl.monitored) {
				if (ctrl.requestSubscriber) {
					var m = ctrl.monitored;
					ctrl.requestSubscriber.unsubscribe().event(function (event) {
						sessionServices.unmonitorSession(m);
					});
				}
			}
			ctrl.request = MSG;
			ctrl.requestSubscriber = null;
			if (this.monitored === id) {
				ctrl.monitored = null;
			} else {
				ctrl.monitored = id;
				ctrl.requestSubscriber = subscriberFactory.createSubscriber("request-event-" + id).message(function (result) {
					ctrl.requests.splice(0, 0, result);
					$scope.$apply();
				});
				sessionServices.monitorSession(id).then(function () {
					ctrl.request = "// Monitoring "+id+" enabled";
					$scope.$apply();
				}).catch(function (fault) {
					ctrl.request = "// Monitoring "+id+" failed\n"+JSON.stringify(fault, null, 3);
					ctrl.monitored = null;
					$scope.$apply();
					ctrl.requestSubscriber.unsubscribe();
				});
			}
		}
	}
	/* @ngInject */
	function initSessions($q, sessionServices) {
		var deferred = $q.defer();
		sessionServices.getSessionInfos().then(function (sessions) {
			deferred.resolve(sessions);
		});
		return deferred.promise;
	}
})();


