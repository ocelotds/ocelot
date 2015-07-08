ocelotController.addOpenEventListener(function(event) {
	var srv = new TestEJBService();
	QUnit.test("Test TestEJBService", function (assert) {
		var done = assert.async();
		var token = srv.getMessage(1);
		token.success = function (msg) {
			assert.equal(msg, "Message from ejb service getMessage(int 1)");
			done();
		};
		token.fail = function (fault) {
		};
	});
//	QUnit.test("TestTestEJBServ", function (assert) {
//		var done = assert.async();
//		var token = srv.getMessage(true);
//		token.success = function (msg) {
//			assert.equal(msg, "Message from ejb service getMessage(boolean true)");
//			done();
//		};
//		token.fail = function (fault) {
//			assert.equal(fault.classname, "javax.el.MethodNotFoundException");
//			done();
//		};
//	});
});
