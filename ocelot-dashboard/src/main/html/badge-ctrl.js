angular.module('service-app').component('ocelotBadge', {
	templateUrl: 'badge.html',
	controller: BadgeController,
	controllerAs: 'badgeCtrl'

});
function BadgeController($scope) {
	var inst = this;
	inst.statusColor = "#e05d44";
	inst.status = "closed";
	new Subscriber("ocelot-status").message(function (msg) {
		inst.color = "#97CA00";
		inst.status = "opened";
		if (msg !== "OPEN") {
			inst.status = "closed";
			inst.color = "#e05d44";
		}
		$scope.$apply();
	});
}
