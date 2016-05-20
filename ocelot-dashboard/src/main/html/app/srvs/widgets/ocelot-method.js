(function () {
	'use strict';
	angular.module('srvs.module').directive('ocelotMethod', function () {
		return {
			restrict: 'E',
			transclude: true,
			templateUrl: 'app/srvs/widgets/ocelot-method.html',
			controller: OcelotMethodCtrl,
			controllerAs: 'omc',
			scope: {
				srvname: '=',
				method: '='
			}
		};
	});
	/* @ngInject */
	function OcelotMethodCtrl($scope) {
		var vm = this;
		vm.srvname = $scope.srvname;
		vm.method = $scope.method;
		vm.args = [];
		vm.help = null;
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
		vm.buildhelp = function () {
			if (!vm.help) {
				vm.help = this.srvname + "." + this.method.name + "(";
				vm.method.argtemplates.forEach(function (argtemplate, idx, array) {
					vm.help += argtemplate;
					vm.help += (idx < array.length - 1) ? ", " : "";
				});
				vm.help += ")";
				vm.help += "\n\t.event(function(evt:OcelotEvent) {\n\t\tif(evt.type===\"RESULT\") {\n\t\t\t// OK\n\t\t} else {\n\t\t\t// FAIL\n\t\t}\n\t})";
				vm.help += "\n\t.then(function(result:Object) {\n\t\t// OK\n\t})";
				vm.help += "\n\t.catch(function(fault:Fault) {\n\t\t// FAIL\n\t});";
			}
		};
		vm.buildhelp();
		vm.result = vm.help;
		vm.prevent = function (event) {
			event.preventDefault();
			event.stopPropagation();
		};
		vm.clear = function (event) {
			vm.result = vm.help;
			vm.chart.data = [[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]];
		};
		vm.send = function (event) {
			var methodcall = this.srvname + "." + this.method.name + "(";
			vm.args.forEach(function (arg, idx, array) {
				methodcall += arg;
				methodcall += (idx < array.length - 1) ? ", " : "";
			});
			methodcall += ")";
			var processtime = ".event(function(evt) {vm.chart.data[0].shift();vm.chart.data[0].push(evt.totaltime);vm.chart.data[1].shift();vm.chart.data[1].push(evt.javatime);$scope.$apply();})";
			var processresult = ".then(function(result) {vm.result = JSON.stringify(result, null, 3);$scope.$apply();})";
			var processerror = ".catch(function(fault) {vm.result = JSON.stringify(fault, null, 3);$scope.$apply();})";
//		vm.chart.data[0].shift();
//		vm.chart.data[0].push(Math.random());
//		vm.chart.data[1].shift();
//		vm.chart.data[1].push(Math.random());
			eval(methodcall + processtime + processresult + processerror + ";");
		};
	}
})();



