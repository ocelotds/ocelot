(function () {
	'use strict';
	angular.module('srv.module').directive('ocelotMethod', function () {
		return {
			restrict: 'E',
			transclude: true,
			templateUrl: 'app/srv/widgets/ocelot-method.html',
			controller: OcelotMethodCtrl,
			controllerAs: 'omc',
			scope: {
				srvname: '=',
				method: '='
			}
		};
	});
	/* @ngInject */
	function OcelotMethodCtrl($scope, $injector) {
		var vm = this;
		vm.srvname = $scope.srvname;
		vm.method = $scope.method;
		vm.args = [];
		vm.help = buildhelp();
		vm.method.argtemplates.forEach(function (argtemplate, idx, array) {
			vm.args.push(argtemplate);
		});
		vm.chart = {
			labels: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"],
			series: ['Total times', 'Java times'],
			data: [
				[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
				[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
			]
		};
		vm.result = vm.help;
		vm.prevent = prevent;
		vm.clear = clear;
		vm.send = send;
		
		
		// =============================================
		function buildhelp() {
			var help = vm.srvname + "." + vm.method.name + "(";
			vm.method.argtemplates.forEach(function (argtemplate, idx, array) {
				help += argtemplate;
				help += (idx < array.length - 1) ? ", " : "";
			});
			help += ")";
			help += "\n\t.event(function(evt:OcelotEvent) {\n\t\tif(evt.type===\"RESULT\") {\n\t\t\t// OK\n\t\t} else {\n\t\t\t// FAIL\n\t\t}\n\t})";
			help += "\n\t.then(function(result:"+vm.method.returntype+") {\n\t\t// OK\n\t})";
			help += "\n\t.catch(function(fault:Fault) {\n\t\t// FAIL\n\t});";
			return help;
		}
		function prevent(event) {
			event.preventDefault();
			event.stopPropagation();
		}
		function clear() {
			vm.result = vm.help;
			vm.chart.data = [[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]];
		}
		function send() {
			var srv;
			try {
				srv = $injector.get(vm.srvname);
			} catch(e) {
				srv = eval(vm.srvname);
			}
			var methodcall = "srv." + vm.method.name + "(";
			vm.args.forEach(function (arg, idx, array) {
				methodcall += arg;
				methodcall += (idx < array.length - 1) ? ", " : "";
			});
			methodcall += ")";
			var processtime = ".event(function(evt) {vm.chart.data[0].shift();vm.chart.data[0].push(evt.totaltime);vm.chart.data[1].shift();vm.chart.data[1].push(evt.javatime);$scope.$apply();})";
			var processresult = ".then(function(result) {vm.result = JSON.stringify(result, null, 3);$scope.$apply();})";
			var processerror = ".catch(function(fault) {vm.result = JSON.stringify(fault, null, 3);$scope.$apply();})";
			eval(methodcall + processtime + processresult + processerror + ";");
		}
	}
})();



