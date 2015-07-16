var nbMsgToBroadcast = 500;
document.getElementById("nbMsgToBroadcast").innerHTML = nbMsgToBroadcast;
OcelotCacheManager.clearCache();
ocelotController.addOpenEventListener(function (event) {
	var srv = new TestServices();
	QUnit.module("TestServices");
	QUnit.test(".getVoid()", function (assert) {
		var done = assert.async();
		var token = srv.getVoid();
		token.success = function (msg) {
			assert.equal(msg, undefined);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getString()", function (assert) {
		var done = assert.async();
		var token = srv.getString();
		token.success = function (msg) {
			assert.equal(msg, "FOO");
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getNum()", function (assert) {
		var done = assert.async();
		var token = srv.getNum();
		token.success = function (msg) {
			assert.equal(msg, 1);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getNumber()", function (assert) {
		var done = assert.async();
		var token = srv.getNumber();
		token.success = function (msg) {
			assert.equal(msg, 2);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getBool()", function (assert) {
		var done = assert.async();
		var token = srv.getBool();
		token.success = function (msg) {
			assert.equal(msg, true);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getBoolean()", function (assert) {
		var done = assert.async();
		var token = srv.getBoolean();
		token.success = function (msg) {
			assert.equal(msg, false);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getDateBefore()", function (assert) {
		var done = assert.async();
		var before = new Date();
		setTimeout(function() {
			var token = srv.getDate();
			token.success = function (msg) {
				assert.ok(msg > before.getTime());
				done();
			};
			token.fail = token.success;
		}, 500);
	});
	QUnit.test(".getDateAfter()", function (assert) {
		var done = assert.async();
		var token = srv.getDate();
		token.success = function (msg) {
			var after = new Date();
			assert.ok(msg < after.getTime());
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getResult()", function (assert) {
		var done = assert.async();
		var token = srv.getResult();
		token.success = function (msg) {
			assert.deepEqual(msg, {"integer": 5});
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getCollectionInteger()", function (assert) {
		var i, expected = [], done = assert.async();
		for (i = 1; i < 5; i++) {
			expected.push(i);
		}
		var token = srv.getCollectionInteger();
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getCollectionResult()", function (assert) {
		var i, expected = [], done = assert.async();
		for (i = 0; i < 4; i++) {
			expected.push({"integer": 5});
		}
		var token = srv.getCollectionResult();
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
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
		var token = srv.getCollectionOfCollectionResult();
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".getMapResult()", function (assert) {
		var i, expected = {}, done = assert.async();
		for (i = 1; i < 5; i++) {
			expected["" + i] = {"integer": 5};
		}
		var token = srv.getMapResult();
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithNum(i)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithNum_1";
		var token = srv.methodWithNum(1);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithNumber(i)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithNumber_1";
		var token = srv.methodWithNumber(1);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithBool(true)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithBool_true";
		var token = srv.methodWithBool(true);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithBool(false)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithBool_false";
		var token = srv.methodWithBool(false);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithBoolean(false)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithBoolean_false";
		var token = srv.methodWithBoolean(false);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithBoolean(true)", function (assert) {
		var expected, done = assert.async();
		expected = "methodWithBoolean_true";
		var token = srv.methodWithBoolean(true);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithDate(d)", function (assert) {
		var expected, d, done = assert.async();
		d = new Date();
		expected = "methodWithDate_" + d.getTime();
		var token = srv.methodWithDate(d.getTime());
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithResult(r)", function (assert) {
		var expected, r, done = assert.async();
		r = {"integer": 5};
		expected = "methodWithResult_" + r.integer;
		var token = srv.methodWithResult(r);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithArrayInteger(a)", function (assert) {
		var expected, r, done = assert.async();
		r = [1, 2, 3, 4, 5];
		expected = "methodWithArrayInteger_" + r.length;
		var token = srv.methodWithArrayInteger(r);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithCollectionInteger(c)", function (assert) {
		var expected, r, done = assert.async();
		r = [1, 2, 3, 4, 5];
		expected = "methodWithCollectionInteger_" + r.length;
		var token = srv.methodWithCollectionInteger(r);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithArrayResult(c)", function (assert) {
		var i, expected, r = [], done = assert.async();
		for (i = 0; i < 4; i++) {
			r.push({"integer": 5});
		}
		expected = "methodWithArrayResult_" + r.length;
		var token = srv.methodWithArrayResult(r);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithCollectionResult(c)", function (assert) {
		var i, expected, r = [], done = assert.async();
		for (i = 0; i < 4; i++) {
			r.push({"integer": 5});
		}
		expected = "methodWithCollectionResult_" + r.length;
		var token = srv.methodWithCollectionResult(r);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithMapResult(m)", function (assert) {
		var i, expected, r = {}, done = assert.async();
		for (i = 1; i < 5; i++) {
			r["" + i] = {"integer": 5};
		}
		expected = "methodWithMapResult_4";
		var token = srv.methodWithMapResult(r);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithCollectionOfCollectionResult(c)", function (assert) {
		var i, expected, r = [], done = assert.async();
		for (i = 0; i < 4; i++) {
			var result = [];
			r.push(result);
			for (j = 0; j < 4; j++) {
				result.push({"integer": 5});
			}
		}
		expected = "methodWithCollectionOfCollectionResult_" + r.length;
		var token = srv.methodWithCollectionOfCollectionResult(r);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithManyParameters(a, b, c, d)", function (assert) {
		var a, b, c, d, expected, done = assert.async();
		a = "text", b = 5, c = {"integer": 5}, d = ["a", "b"];
		expected = "methodWithManyParameters a=" + a + " - b=" + b + " - c=" + c.integer + " - d:" + d.length;
		var token = srv.methodWithManyParameters(a, b, c, d);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithAlmostSameSignature(s)", function (assert) {
		var a, expected, done = assert.async();
		a = "text";
		expected = "String";
		var token = srv.methodWithAlmostSameSignature(a);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodWithAlmostSameSignature(i)", function (assert) {
		var a, expected, done = assert.async();
		a = 5;
		expected = "Integer";
		var token = srv.methodWithAlmostSameSignature(a);
		token.success = function (msg) {
			assert.deepEqual(msg, expected);
			done();
		};
		token.fail = token.success;
	});
	QUnit.test(".methodThatThrowException()", function (assert) {
		var done = assert.async();
		var token = srv.methodThatThrowException(a);
		token.fail = function (msg) {
			assert.equal(msg.classname, "fr.hhdev.ocelot.test.MethodException");
			done();
		};
		token.success = token.fail;
	});
	QUnit.test(".onMessage()", function (assert) {
		var done = assert.async(),
				  mdb = new TopicConsumer("mytopic");
		mdb.onMessage = function (msg) {
			assert.equal(msg, "Message From server 1");
			done();
			mdb.unsubscribe();
		};
		mdb.subscribe();
		srv.publish("mytopic", 1);
	});
	QUnit.test(".onMessages()", function (assert) {
		var result = 0, j, expected = nbMsgToBroadcast, timer, done, mdb, params, i, query;
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
		mdb = new TopicConsumer("mytopic");
		mdb.onMessage = function (msg) {
			result++;
			assert.ok(true, "" + msg + " : (" + result + ")");
			if (result === expected) {
				window.clearTimeout(timer);
				assert.equal(result, expected, "receive " + expected + " messages");
				mdb.unsubscribe();
				done();
			}
		};
		mdb.subscribe();
		timer = setTimeout(function () {
			assert.equal(result, expected, "receive " + expected + " messages");
			mdb.unsubscribe();
			done();
		}, 5 * expected);
		srv.publish("mytopic", expected);
	});
	QUnit.test(".methodCached()", function (assert) {
		var expected, done = assert.async();
		var token = srv.methodCached();
		token.success = function (msg) {
			expected = msg.length;
			var token = srv.methodCached();
			token.success = function (msg) {
				assert.equal(msg.length, expected);
				done();
			};
			token.fail = token.success;
		};
		token.fail = token.success;
	});
	QUnit.test(".methodRemoveCache()", function (assert) {
		OcelotCacheManager.clearCache();
		var expected, done = assert.async();
		var token = srv.methodCached();
		token.success = function (msg) {
			expected = msg.length;
			var token = srv.methodCached();
			token.success = function (msg) {
				assert.equal(msg.length, expected);
				var token = srv.methodRemoveCache();
				token.success = function (msg) {
					var token = srv.methodCached();
					token.success = function (msg) {
						assert.notEqual(msg.length, expected);
						done();
					};
					token.fail = token.success;
				};
				token.fail = token.success;
			};
			token.fail = token.success;
		};
		token.fail = token.success;
	});
	QUnit.test(".methodRemoveAllCache()", function (assert) {
		OcelotCacheManager.clearCache();
		var expected, done = assert.async();
		var token = srv.methodCached();
		token.success = function (msg) {
			expected = msg.length;
			var token = srv.methodCached();
			token.success = function (msg) {
				assert.equal(msg.length, expected);
				var token = srv.methodRemoveAllCache();
				token.success = function (msg) {
					var token = srv.methodCached();
					token.success = function (msg) {
						assert.notEqual(msg.length, expected);
						done();
					};
					token.fail = token.success;
				};
				token.fail = token.success;
			};
			token.fail = token.success;
		};
		token.fail = token.success;
	});
});
