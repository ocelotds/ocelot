function TestService() {
	this.factory = "ejb";
	this.dataservice = "demo.TestService";
	this.getEvent = function(op, args) {
		var evt = document.createEvent("Event");
		evt.initEvent("call", true, false);
		evt.factory = this.factory;
		evt.dataservice = this.dataservice;
		evt.operation = op;
		evt.args = args;
		evt.onResult = function(msg) {};
		evt.onFault = function(fault) {};
		setTimeout(function() {document.dispatchEvent(evt);}, 1);
		return evt;
	};
	this.getMessage = function (i) {
		return this.getEvent("getMessage", [i]);
	};
	this.getFault = function (i) {
		return this.getEvent("getFault", []);
	};
}
