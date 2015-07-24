'use strict';
var nbMsgToBroadcast = 500;
document.getElementById("nbMsgToBroadcast").innerHTML = nbMsgToBroadcast;
ocelotController.cacheManager.clearCache();
ocelotController.addOpenListener(function () {
	var srv = new TestServices();
	var ocelotsrv = new OcelotServices();
	QUnit.module("TestServices");
	QUnit.test(".getVoid()", function (assert) {
		var done = assert.async();
		srv.getVoid().event(function (evt) {
			assert.equal(evt.type, "RESULT");
			done();
		});
	});
	QUnit.test(".getString()", function (assert) {
		var done = assert.async();
		srv.getString().event(function (evt) {
			assert.equal(evt.result, "FOO");
			done();
		});
	});
	QUnit.test(".getNum()", function (assert) {
		var done = assert.async();
		srv.getNum().event(function (evt) {
			assert.equal(evt.result, 1);
			done();
		});
	});
	QUnit.test(".getNumber()", function (assert) {
		var done = assert.async();
		srv.getNumber().event(function (evt) {
			assert.equal(evt.result, 2);
			done();
		});
	});
	QUnit.test(".getBool()", function (assert) {
		var done = assert.async();
		srv.getBool().event(function (evt) {
			assert.equal(evt.result, true);
			done();
		});
	});
	QUnit.test(".getBoolean()", function (assert) {
		var done = assert.async();
		srv.getBoolean().event(function (evt) {
			assert.equal(evt.result, false);
			done();
		});
	});
	QUnit.test(".getDateBefore()", function (assert) {
		var done = assert.async();
		var before = new Date();
		setTimeout(function() {
			srv.getDate().event(function (evt) {
				assert.ok(evt.result > before.getTime());
				done();
			});
		}, 50);
	});
	QUnit.test(".getDateAfter()", function (assert) {
		var done = assert.async();
		srv.getDate().event(function (evt) {
			setTimeout(function() {
				var after = new Date();
				assert.ok(evt.result < after.getTime(), "receive "+evt.result+" - expected lesser than "+after.getTime());
				done();
			}, 50);
		});
	});
	QUnit.test(".getResult()", function (assert) {
		var done = assert.async();
		srv.getResult().event(function (evt) {
			assert.deepEqual(evt.result, {"integer": 5});
			done();
		});
	});
	QUnit.test(".getCollectionInteger()", function (assert) {
		var i, expected = [], done = assert.async();
		for (i = 1; i < 5; i++) {
			expected.push(i);
		}
		srv.getCollectionInteger().event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".getCollectionResult()", function (assert) {
		var i, expected = [], done = assert.async();
		for (i = 0; i < 4; i++) {
			expected.push({"integer": 5});
		}
		srv.getCollectionResult().event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".getCollectionOfCollectionResult()", function (assert) {
		var i, j, expected = [], done = assert.async();
		for (i = 0; i < 4; i++) {
			var result = [];
			expected.push(result);
			for (j = 0; j < 4; j++) {
				result.push({"integer": 5});
			}
		}
		srv.getCollectionOfCollectionResult().event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".getMapResult()", function (assert) {
		var i, expected = {}, done = assert.async();
		for (i = 1; i < 5; i++) {
			expected["" + i] = {"integer": 5};
		}
		srv.getMapResult().event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithNum(i)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithNum_1";
		srv.methodWithNum(1).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithNumber(i)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithNumber_1";
		srv.methodWithNumber(1).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithBool(true)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithBool_true";
		srv.methodWithBool(true).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithBool(false)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithBool_false";
		srv.methodWithBool(false).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithBoolean(false)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithBoolean_false";
		srv.methodWithBoolean(false).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithBoolean(true)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithBoolean_true";
		srv.methodWithBoolean(true).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithDate(d)", function (assert) {
		var expected, d, done = assert.async();
		d = new Date();
		expected = "methodWithDate_" + d.getTime();
		srv.methodWithDate(d.getTime()).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithResult(r)", function (assert) {
		var expected, r, done = assert.async();
		r = {"integer": 5};
		expected = "methodWithResult_" + r.integer;
		srv.methodWithResult(r).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithArrayInteger(a)", function (assert) {
		var expected, r, done = assert.async();
		r = [1, 2, 3, 4, 5];
		expected = "methodWithArrayInteger_" + r.length;
		srv.methodWithArrayInteger(r).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithCollectionInteger(c)", function (assert) {
		var expected, r, done = assert.async();
		r = [1, 2, 3, 4, 5];
		expected = "methodWithCollectionInteger_" + r.length;
		srv.methodWithCollectionInteger(r).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithArrayResult(c)", function (assert) {
		var i, expected, r = [], done = assert.async();
		for (i = 0; i < 4; i++) {
			r.push({"integer": 5});
		}
		expected = "methodWithArrayResult_" + r.length;
		srv.methodWithArrayResult(r).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithCollectionResult(c)", function (assert) {
		var i, expected, r = [], done = assert.async();
		for (i = 0; i < 4; i++) {
			r.push({"integer": 5});
		}
		expected = "methodWithCollectionResult_" + r.length;
		srv.methodWithCollectionResult(r).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithMapResult(m)", function (assert) {
		var i, expected, r = {}, done = assert.async();
		for (i = 1; i < 5; i++) {
			r["" + i] = {"integer": 5};
		}
		expected = "methodWithMapResult_4";
		srv.methodWithMapResult(r).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithCollectionOfCollectionResult(c)", function (assert) {
		var i, j, expected, r = [], done = assert.async();
		for (i = 0; i < 4; i++) {
			var result = [];
			r.push(result);
			for (j = 0; j < 4; j++) {
				result.push({"integer": 5});
			}
		}
		expected = "methodWithCollectionOfCollectionResult_" + r.length;
		srv.methodWithCollectionOfCollectionResult(r).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithManyParameters(a, b, c, d)", function (assert) {
		var a, b, c, d, expected, done = assert.async();
		a = "text", b = 5, c = {"integer": 5}, d = ["a", "b"];
		expected = "methodWithManyParameters a=" + a + " - b=" + b + " - c=" + c.integer + " - d:" + d.length;
		srv.methodWithManyParameters(a, b, c, d).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithAlmostSameSignature(s)", function (assert) {
		var expected, done = assert.async();
		expected = "String";
		srv.methodWithAlmostSameSignature("text").event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodWithAlmostSameSignature(i)", function (assert) {
		var expected, done = assert.async();
		expected = "Integer";
		srv.methodWithAlmostSameSignature(5).event(function (evt) {
			assert.deepEqual(evt.result, expected);
			done();
		});
	});
	QUnit.test(".methodThatThrowException()", function (assert) {
		var done = assert.async();
		srv.methodThatThrowException().event(function (evt) {
			assert.equal(evt.fault.classname, "fr.hhdev.ocelot.test.MethodException");
			done();
		});
	});
	QUnit.test(".methodCached()", function (assert) {
		var expected, done = assert.async();
		srv.methodCached().event(function (evt) {
			assert.equal(evt.type, "RESULT", "Receive result : "+expected+" from server and put in cache.");
			expected = evt.result.length;
			srv.methodCached().event(function (evt) {
				assert.equal(evt.result.length, expected, "Receive result from cache : "+evt.result.length);
				done();
			});
		});
	});
	QUnit.test(".methodRemoveCache()", function (assert) {
		ocelotController.cacheManager.clearCache();
		var expected, done = assert.async();
		srv.methodCached().event(function (evt) {
			expected = evt.result.length;
			assert.equal(evt.type, "RESULT", "Receive result : "+expected+" from server and put in cache.");
			srv.methodCached().event(function (evt) {
				assert.equal(evt.result.length, expected, "Receive result from cache : "+evt.result.length);
				srv.methodRemoveCache().event(function (evt) {
					assert.equal(evt.type, "RESULT", "Cache removed.");
					srv.methodCached().event(function (evt) {
						assert.notEqual(evt.result.length, expected, "Receive result : "+evt.result.length+" from server");
						done();
					});
				});
			});
		});
	});
	QUnit.test(".methodRemoveAllCache()", function (assert) {
		ocelotController.cacheManager.clearCache();
		var expected, done = assert.async();
		srv.methodCached().event(function (evt) {
			expected = evt.result.length;
			assert.equal(evt.type, "RESULT", "Receive result : "+expected+" from server and put in cache.");
			srv.methodCached().event(function (evt) {
				assert.equal(evt.result.length, expected, "Receive result from cache : "+evt.result.length);
				srv.methodRemoveAllCache().event(function (evt) {
					assert.equal(evt.type, "RESULT", "All Cache removed.");
					srv.methodCached().event(function (evt) {
						assert.notEqual(evt.result.length, expected, "Receive result "+evt.result.length+" from server");
						done();
					});
				});
			});
		});
	});
	QUnit.test(".onMessage()", function (assert) {
		var timer, done = assert.async();
		ocelotsrv.subscribe("mytopic").event(function(evt) {
			assert.equal(evt.type, "RESULT", "Subscription to 'mytopic' : ok.");
			srv.publish("mytopic", 1).event(function(evt) {
				assert.equal(evt.type, "RESULT", "Call publish method : ok.");
			});
		}).message(function(msg) {
			assert.equal(msg, "Message From server 1", "Receive message in 'mytopic' : ok.");
			ocelotsrv.unsubscribe("mytopic").event(function(evt) {
				assert.equal(evt.type, "RESULT", "Unsubscription to 'mytopic' : ok.");
				window.clearTimeout(timer);
				done();
			});
		});
		timer = setTimeout(function () {
			assert.equal(0, 1, "Receive 0 messages");
			ocelotsrv.unsubscribe("mytopic").event(function(evt) {
				assert.equal(evt.type, "RESULT", "Unsubscription to 'mytopic' : ok.");
				done();
			});
		}, 500);
	});
	QUnit.test(".onMessages()", function (assert) {
		var result = 0, expected = nbMsgToBroadcast, timer, done, params, i, query;
		query = location.search;
		params = query.split("&");
		for (i = 0; i < params.length; i++) {
			var param = params[i].replace("?", "");
			var keyval = param.split("=");
			if(keyval.length === 2) {
				if(keyval[0] === "nbmsg") {
					expected = parseInt(keyval[1]);
				}
			}
		}
		done = assert.async();
		ocelotsrv.subscribe("mytopic").event(function(evt) {
			assert.equal(evt.type, "RESULT", "Subscription to 'mytopic' : ok.");
			srv.publish("mytopic", expected).event(function(evt) {
				assert.equal(evt.type, "RESULT", "Call publish("+expected+") method : ok.");
			});
		}).message(function(msg) {
			result++;
			assert.ok(true, "" + msg + " : (" + result + ")");
			if(result===expected) {
				assert.equal(result, expected, "Receive "+result+"/"+expected+" messages");
				window.clearTimeout(timer);
				ocelotsrv.unsubscribe("mytopic").event(function(evt) {
					assert.equal(evt.type, "RESULT", "Unsubscription to 'mytopic' : ok.");
					done();
				});
			}
		});
		timer = setTimeout(function () {
			assert.equal(0, expected, "Receive 0/"+expected+" messages");
			ocelotsrv.unsubscribe("mytopic").event(function(evt) {
				assert.equal(evt.type, "RESULT", "Unsubscription to 'mytopic' : ok.");
				done();
			});
		}, 50 * expected);
	});
});
