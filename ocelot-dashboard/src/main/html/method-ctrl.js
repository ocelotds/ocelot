angular.module('service-app').component('ocelotMethod', {
	templateUrl: 'method.html',
	controller: MethodController,
	controllerAs: 'methodCtrl',
	bindings: {
		srvname: '=',
		method: '='
	}
});
function MethodController($scope) {
	var inst = this;
	inst.args = [];
	inst.help = null;
	inst.method.argtemplates.forEach(function (argtemplate, idx, array) {
		inst.args.push(argtemplate);
	});
	inst.chart = {
		labels : ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"],
		series : ['Total times', 'Java times'],
	   data : [
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
			[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
		]
	};
	inst.buildhelp = function () {
		if(!inst.help) {
			inst.help = this.srvname + "." + this.method.name + "(";
			inst.method.argtemplates.forEach(function (argtemplate, idx, array) {
				inst.help += argtemplate;
				inst.help += (idx < array.length - 1) ? ", " : "";
			});
			inst.help += ")";
			inst.help += "\n\t.event(function(evt:OcelotEvent) {\n\t\tif(evt.type===\"RESULT\") {\n\t\t\t// OK\n\t\t} else {\n\t\t\t// FAIL\n\t\t}\n\t})";
			inst.help += "\n\t.then(function(result:Object) {\n\t\t// OK\n\t})";
			inst.help += "\n\t.catch(function(fault:Fault) {\n\t\t// FAIL\n\t});";
		}
	};
	inst.buildhelp();
	inst.result = inst.help;
	inst.prevent = function (event) {
		event.preventDefault();
		event.stopPropagation();
	};
	inst.clear = function (event) {
		inst.result = inst.help;
		inst.chart.data = [[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]];
	};
	inst.send = function (event) {
		var methodcall = this.srvname + "." + this.method.name + "(";
		inst.args.forEach(function (arg, idx, array) {
			methodcall += arg;
			methodcall += (idx < array.length - 1) ? ", " : "";
		});
		methodcall += ")";
		var processtime = ".event(function(evt) {inst.chart.data[0].shift();inst.chart.data[0].push(evt.totaltime);inst.chart.data[1].shift();inst.chart.data[1].push(evt.javatime);$scope.$apply();})";
		var processresult = ".then(function(result) {inst.result = JSON.stringify(result, null, 3);$scope.$apply();})";
		var processerror = ".catch(function(fault) {inst.result = JSON.stringify(fault, null, 3);$scope.$apply();})";
//		inst.chart.data[0].shift();
//		inst.chart.data[0].push(Math.random());
//		inst.chart.data[1].shift();
//		inst.chart.data[1].push(Math.random());
		eval(methodcall+processtime+processresult+processerror+";");
	};
}
