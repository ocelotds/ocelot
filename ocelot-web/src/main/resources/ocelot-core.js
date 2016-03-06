var ocelotController, OcelotPromiseFactory, MD5Tools;
var Subscriber = (function(topic) {
   var promise = ocelotServices.subscribe(topic);
   Object.defineProperty(promise, "topic", { get: function () { return topic; } });
   promise.unsubscribe = function() {
     return ocelotServices.unsubscribe(topic);
   };
   return promise;
});
if ("WebSocket" in window) {
   ocelotController = (function () {
      var options = {"monitor":false,"debug":false}, MSG = "MESSAGE", RES = "RESULT", FAULT = "FAULT", ALL = "ALL", EVT = "Event", ADD = "add", RM = "remove",
         CLEANCACHE = "ocelot-cleancache", STATUS = "ocelot-status", OSRV = "org.ocelotds.OcelotServices", SUB = "subscribe", UNSUB = "unsubscribe",
         stateLabels = ['CONNECTING', 'OPEN', 'CLOSING', 'CLOSED'], promises = {}, openHandlers = [], closeHandlers = [], errorHandlers = [], ws;
      function createEventFromPromise(type, promise, msgToClient) {
         var evt = document.createEvent(EVT);
         evt.initEvent(type, true, false);
         evt.dataservice = promise.dataservice;
         evt.operation = promise.operation;
         evt.args = promise.args;
         evt.totaltime = 0;
         evt.javatime = 0;
         evt.jstime = 0;
         evt.networktime = 0;
         if(msgToClient) {
            evt.response = msgToClient.response;
            if(options.monitor) {
               evt.javatime = msgToClient.t; // backend timing
            } 
         }
         if(options.monitor) {
            evt.totaltime = new Date().getTime() - promise.t; // total timing
         }
         return evt;
      }
      function createMessageEventFromPromise(promise, msgToClient) {
         return createEventFromPromise(MSG, promise, msgToClient);
      }
      function createResultEventFromPromise(promise, msgToClient) {
         return createEventFromPromise(RES, promise, msgToClient);
      }
      function createFaultEventFromPromise(promise, msgToClient) {
         return createEventFromPromise(FAULT, promise, msgToClient);
      }
      function stateUpdated() {
         foreachPromiseInPromisesDo(STATUS, function(promise) {
            var response = createMessageEventFromPromise(promise, {"response":ocelotController.status,"t":0});
            promise.response = response;
         }); 
      }
      function foreachPromiseInPromisesDo(id, func) {
         foreachPromiseDo(getPromises(id), func);
      }
      function foreachPromiseDo(aPromises, func) {
         var i;
         if(aPromises) {
            for(i = 0; i < aPromises.length; i++) {
               func(aPromises[i]);
            }
         }
      }
      function addPromiseToId(promise, id) { // add promise to promise list and return if some promises exists already for id
         var exists = (promises[id] !== undefined);
         if(!exists) {
            promises[id] = [];
         }
         promises[id].push(promise);
         return exists;
      }
      function clearPromisesForId(id) {
         delete promises[id];
      }
      function getPromises(id) {
         return promises[id] || [];
      }
      function isOcelotControllerServices(promise) {
         if (promise.dataservice === "ocelotController") {
            return true;
         }
         return false;
      }
      function isSubscription(promise) {
         return promise.dataservice === OSRV && promise.operation === SUB;
      }
      function isTopicSubscription(promise, topic) {
         return isSubscription(promise) && isTopic(promise, topic)
      }
      function isUnsubscription(promise) {
         return promise.dataservice === OSRV && promise.operation === UNSUB;
      }
      function isTopicUnsubscription(promise, topic) {
         return isUnsubscription(promise) && isTopic(promise, topic)
      }
      function isTopic(promise, topic) {
         return topic ? (promise.args[0] === topic) : true;
      }
      function extractOptions(search) {
         var params = search.split("&");
         params.forEach(function (param) {
           if(param.search(/^\??ocelot=/)===0) {
             var opts = decodeURI(param.replace(/\??ocelot=/, ""));
             options = JSON.parse(opts);
           }
         });         
      }
      function sendMfc(promise) {
         var msgToClient, xhttp, mfc = JSON.stringify(promise.json);
         if(!addPromiseToId(promise, promise.id)) {
            // Subscription or unsubscription to topic, use websocket
            if (isSubscription(promise) || isUnsubscription(promise)) {
               if(ws.readyState === 1) {
                  ws.send(mfc);
               } else {
						// TODO A VOIR car le subscriber n'est pas remove
                  promise.response = createFaultEventFromPromise(promise, {"response":{"classname": "Websocket", "message": "Websocket is not ready : status = " + status, "stacktrace": []},"t":0});
               }
            // Commons calls, use http request
            } else {
               xhttp = new XMLHttpRequest();
               xhttp.onreadystatechange = function() {
                  if (xhttp.readyState === 4) {
                     if(xhttp.status === 200) {
                        msgToClient = JSON.parse(xhttp.responseText);
	                     receiveMtc(msgToClient);
                     } else {
		                  promise.response = createFaultEventFromPromise(promise, {"response":{"classname": "XMLHttpRequest", "message": "XMLHttpRequest request failed : code = " + xhttp.status, "stacktrace": []},"t":0});
                     }
                  }
               };
               xhttp.open("POST", "%CTXPATH%/ocelot/endpoint?monitor="+options.monitor, true);
               xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
               xhttp.send("mfc="+mfc);
            }
         }
      }
      function receiveMtc(msgToClient) {
         var idx, response, msgToClient, promise, apromise = getPromises(msgToClient.id);
         if (msgToClient.type === RES) { // maybe should be store result in cache
            // if msgToClient has dead line so we stock in cache
            ocelotController.cacheManager.putResultInCache(msgToClient);
         }
         for (idx = 0; idx < apromise.length; idx++) {
            promise = apromise[idx];
            if (msgToClient.type === FAULT) {
               response = createFaultEventFromPromise(promise, msgToClient);
            } else if (msgToClient.type === RES) {
               // if msg is response of subscribe request
               if (isTopicSubscription(promise)) {
                  addPromiseToId(promise, promise.args[0]);
               }
               else if (isTopicUnsubscription(promise)) {
                  clearPromisesForId(promise.args[0]);
               }
               response = createResultEventFromPromise(promise, msgToClient);
            } else if (msgToClient.type === MSG) {
               response = createMessageEventFromPromise(promise, msgToClient);
            }
            promise.response = response;
         }
         // when receive result or fault, remove handlers, except for topic
         if(msgToClient.type !== MSG) {
            clearPromisesForId(msgToClient.id);
         }
      }
      function init() {
         console.info("Websocket initialization...");
         var host = document.location.hostname;
         if (document.location.port && document.location.port !== "") {
            host = host + ":" + document.location.port;
         }
         extractOptions(document.location.search);
         ws = new WebSocket("%WSS%://" + host + "%CTXPATH%/ocelot-endpoint");
         ws.onmessage = function (evt) {
//            console.debug(evt.data);
            receiveMtc(JSON.parse(evt.data));
         };
         ws.onopen = function (evt) {
            console.info("Websocket opened");
            var handler, apromise, idx, ps, promise;
            stateUpdated();
            apromise = getPromises("ocelotController.open");
            if (apromise.length) { // its not open, but re-open
               for (idx = 0; idx < apromise.length; idx++) { // call handlers attached to open method 
                  promise = apromise[idx];
                  promise.response = createResultEventFromPromise(promise, {"response":null,"t":0});
               }
               ps = promises;
               promises = {};
               Object.keys(ps).forEach(function (id) { // we redo the previous subscription
                  if (id !== ps[id].id) {
                     foreachPromiseDo(ps[id], ocelotController.addPromise);
                  }
               });
               clearPromisesForId("ocelotController.open");
               return;
            }
            // Controller subscribe to ocelot-cleancache topic
            ocelotServices.subscribe(CLEANCACHE).message(function (id) {
               if (id === ALL) {
                  ocelotController.cacheManager.clearCache();
               }
               else {
                  ocelotController.cacheManager.removeEntryInCache(id);
               }
            });
            // Get Locale from server or cache and re-set it
            ocelotServices.getLocale().then(function (locale) {
               if (locale) {
                  ocelotServices.setLocale(locale);
               }
            });
            // send states or current objects in cache with lastupdate
            ocelotServices.getOutDatedCache(ocelotController.cacheManager.getLastUpdateCache()).then(function (entries) {
               ocelotController.cacheManager.removeEntries(entries);
            });
            while (handler = openHandlers.shift()) { // launch open handlers
               handler();
            }
         };
         ws.onerror = function (evt) {
            console.info("Websocket error : "+evt.reason);
            errorHandlers.forEach(function (handler) {
               handler(evt);
            });
            stateUpdated();
         };
         ws.onclose = function (evt) {
            var promise, idx, apromise = getPromises("ocelotController.close");
            if (apromise.length) { // its not open, but re-open
               for (idx = 0; idx < apromise.length; idx++) { // call handlers attached to close method 
                  promise = apromise[idx];
                  promise.response = createResultEventFromPromise(promise, {"response":evt.reason,"t":0});
               }
               clearPromisesForId("ocelotController.close");
            }
            closeHandlers.forEach(function (handler) {
               handler(evt);
            });
            stateUpdated();
            console.info("Websocket closed : "+evt.reason);
         };
      }
      init();
      return {
         get options() {
            return options;
         },
         get status() {
            return stateLabels[ws.readyState];
         },
         addOpenListener: function (listener) {
            if (ws.readyState === 1) { // OPEN
               listener();
            } else {
               openHandlers.push(listener);
            }
         },
         addCloseListener: function (listener) {
            if (ws.readyState === 3) { // CLOSED
               listener();
            } else {
               closeHandlers.push(listener);
            }
         },
         addErrorListener: function (listener) {
            errorHandlers.push(listener);
         },
         open: function () {
            var promise =  OcelotPromiseFactory.createPromise("ocelotController", "ocelotController.open", "open", [], []);
            init();
            return promise;
         },
         close: function (reason) {
            var promise =  OcelotPromiseFactory.createPromise("ocelotController", "ocelotController.close", "close", [], []);
            setTimeout(function() {
               ws.close(1000, reason|"Normal closure; the connection successfully completed whatever purpose for which it was created.");
            }, 10);
            return promise;
         },
         addPromise: function (promise) {
            if (isTopicSubscription(promise, STATUS)) {
               addPromiseToId(promise, STATUS);
               stateUpdated();
               return;
            }
            if (isTopicUnsubscription(promise, STATUS)) {
               clearPromisesForId(STATUS);
               return;
            }
            // if it's internal service like ocelotController.open or ocelotController.close
            if(isOcelotControllerServices(promise)){
               addPromiseToId(promise, promise.id);
               return;
            }
            // check entry cache
            var msgToClient = ocelotController.cacheManager.getResultInCache(promise.id, promise.cacheIgnored);
            if (msgToClient) {
               // present and valid, return response without call
               promise.response = createResultEventFromPromise(promise, {"response":msgToClient.response,"t":0});
               return;
            }
            // else call
            sendMfc(promise);
         },
         cacheManager: (function () {
            var LU = "ocelot-lastupdate", addHandlers = [], removeHandlers = [], lastUpdateManager;
            lastUpdateManager = {
               addEntry: function (id) {
                  var lastUpdates = this.getLastUpdateCache();
                  lastUpdates[id] = Date.now();
                  localStorage.setItem(LU, JSON.stringify(lastUpdates));
               },
               removeEntry: function (id) {
                  var lastUpdates = this.getLastUpdateCache();
                  delete lastUpdates[id];
                  localStorage.setItem(LU, JSON.stringify(lastUpdates));
               },
               getLastUpdateCache: function () {
                  var lastUpdates = localStorage.getItem(LU);
                  if (!lastUpdates) {
                     lastUpdates = {};
                  } else {
                     lastUpdates = JSON.parse(lastUpdates);
                  }
                  return lastUpdates;
               }
            };
            function manageAddEvent(msgToClient) {
               var evt = document.createEvent(EVT);
               evt.initEvent(ADD, true, false);
               evt.msg = msgToClient;
               addHandlers.forEach(function (handler) {
                  handler(evt);
               });
            }
            function manageRemoveEvent(compositeKey) {
               var evt = document.createEvent(EVT);
               evt.initEvent(RM, true, false);
               evt.key = compositeKey;
               removeHandlers.forEach(function (handler) {
                  handler(evt);
               });
            }
            return {
               getLastUpdateCache: function () {
                  return lastUpdateManager.getLastUpdateCache();
               },
               /**
                * Add listener for receive cache event
                * @param {String} type event : add, remove
                * @param {Function} listener
                */
               addEventListener: function (type, listener) {
                  if (type === ADD) {
                     addHandlers.push(listener);
                  } else if (type === RM) {
                     removeHandlers.push(listener);
                  }
               },
               /**
                * Remove listener for receive cache event
                * @param {String} type event : add, remove
                * @param {Function} listener
                */
               removeEventListener: function (type, listener) {
                  var idx = -1;
                  if (type === ADD) {
                     idx = addHandlers.indexOf(listener);
                     if(idx !== -1) {
                        addHandlers.splice(idx, 1);
                     }
                  } else if (type === RM) {
                     idx = removeHandlers.indexOf(listener);
                     if(idx !== -1) {
                        removeHandlers.splice(idx, 1);
                     }
                  }
               },
               /**
                * If msgToClient has deadline so we stock in cache
                * Add result in cache storage
                * @param {MessageToClient} msgToClient
                */
               putResultInCache: function (msgToClient) {
                  if(!msgToClient.deadline) return;
                  var ids, json, obj;
                  lastUpdateManager.addEntry(msgToClient.id);
                  manageAddEvent(msgToClient);
                  ids = msgToClient.id.split("_");
                  json = localStorage.getItem(ids[0]);
                  obj = {};
                  if (json) {
                     obj = JSON.parse(json);
                  }
                  obj[ids[1]] = msgToClient;
                  json = JSON.stringify(obj);
                  localStorage.setItem(ids[0], json);
               },
               /**
                * get entry from cache
                * @param {String} compositeKey
                * @param {boolean} ignoreCache
                * @returns {MessageToClient}
                */
               getResultInCache: function (compositeKey, ignoreCache) {
                  var ids, json, msgToClient, obj, now;
                  if (ignoreCache) {
                     return null;
                  }
                  ids = compositeKey.split("_");
                  msgToClient = null;
                  json = localStorage.getItem(ids[0]);
                  if (json) {
                     obj = JSON.parse(json);
                     msgToClient = obj[ids[1]];
                  }
                  if (msgToClient) {
                     now = new Date().getTime();
                     // check validity
                     if (now > msgToClient.deadline) {
                        this.removeEntryInCache(compositeKey);
                        msgToClient = null; // invalid
                     }
                  }
                  return msgToClient;
               },
               /**
                * Remove sub entry or entry in cache
                * @param {String} compositeKey
                */
               removeEntryInCache: function (compositeKey) {
                  var ids, entry, obj;
                  lastUpdateManager.removeEntry(compositeKey);
                  manageRemoveEvent(compositeKey);
                  ids = compositeKey.split("_");
                  entry = localStorage.getItem(ids[0]);
                  if (entry) {
                     obj = JSON.parse(entry);
                     if (ids.length === 2) {
                        delete obj[ids[1]];
                        localStorage.setItem(ids[0], JSON.stringify(obj));
                     } else {
                        localStorage.removeItem(ids[0]);
                     }
                  }
               },
               /**
                * Remove all entries defined in list
                * @param {array} list
                */
               removeEntries: function (list) {
                  list.forEach(function (key) {
                     removeEntryInCache(key);
                  });
               },
               /**
                * Clear cache storage
                */
               clearCache: function () {
                  localStorage.clear();
               }
            };
         })()
      };
   })();

   OcelotPromiseFactory = function () {
      var MSG = "MESSAGE", RES = "RESULT", FAULT = "FAULT";
      return {
         createPromise: function (ds, id, op, argNames, args) {
            return (function (ds, id, op, argNames, args) {
               var fault, handler, evt = null, thenHandlers = [], catchHandlers = [], eventHandlers = [], messageHandlers = [];
               function process() {
                  if (!evt) {
                     return;
                  }
                  if (evt.type !== MSG) {
                     while (handler = eventHandlers.shift()) {
                        handler(evt);
                     }
                     if (evt.type === RES) {
                        while (handler = thenHandlers.shift()) {
                           handler(evt.response);
                        }
                     } else if (evt.type === FAULT) {
                        fault = evt.response;
                        console.error(fault.classname+"("+fault.message+")");
                        console.table(fault.stacktrace);
                        while (handler = catchHandlers.shift()) {
                           handler(fault);
                        }
                     }
                  } else {
                     messageHandlers.forEach(function (messageHandler) {
                        messageHandler(evt.response);
                     });
                  }
               }
               var promise = {
                  id: id, key: id, dataservice: ds, operation: op, args: args, argNames: argNames, cacheIgnored: false, t: new Date().getTime(),
                  set response(e) {
                     evt = e;
                     process();
                  },
                  ignoreCache: function (ignore) {
                     this.cacheIgnored = ignore;
                     return this;
                  },
                  then: function (onFulfilled, onRejected) {
                     if (onFulfilled) {
                        thenHandlers.push(onFulfilled);
                     }
                     if (onRejected) {
                        catchHandlers.push(onRejected);
                     }
                     process();// event already receive ?
                     return this;
                  },
                  catch : function (onRejected) {
                     if (onRejected) {
                        catchHandlers.push(onRejected);
                     }
                     process();// event already receive ?
                     return this;
                  },
                  event: function (onEvented) {
                     if (onEvented) {
                        eventHandlers.push(onEvented);
                     }
                     process();// event already receive ?
                     return this;
                  },
                  message: function (onMessaged) {
                     if (onMessaged) {
                        messageHandlers.push(onMessaged);
                     }
                     process();// event already receive ?
                     return this;
                  },
                  get json() {
                     return {"id": this.id, "ds": this.dataservice, "op": this.operation, "argNames": this.argNames, "args": this.args};
                  }
               };
               var e = document.createEvent("Event");
               e.initEvent("call", true, false);
               e.promise = promise;
               setTimeout(function () {
                  document.dispatchEvent(e);
               }, 1);
               return promise;
            })(ds, id, op, argNames, args);
         }
      };
   }();
   /**
    * Add ocelotController events to document
    * @param {Event} event
    */
   document.addEventListener("call", function (event) {
      ocelotController.addPromise(event.promise);
   });
   window.addEventListener("beforeunload", function (e) {
      if (ocelotController) {
         ocelotController.close();
      }
   });
   /**
    * MD5Tools
    */
   MD5Tools = function () {
      return {
         hex_chr: function (num) {
            return "0123456789abcdef".charAt(num);
         },
         /*
          * Convert a 32-bit number to a hex string with ls-byte first
          */
         rhex: function (num) {
            var j, str = "";
            for (j = 0; j <= 3; j++) {
               str += this.hex_chr((num >> (j * 8 + 4)) & 0x0F) + this.hex_chr((num >> (j * 8)) & 0x0F);
            }
            return str;
         },
         /*
          * Convert a string to a sequence of 16-word blocks, stored as an array.
          * Append padding bits and the length, as described in the MD5 standard.
          */
         str2blks_MD5: function (str) {
            var blks, i, nblk = ((str.length + 8) >> 6) + 1;
            blks = new Array(nblk * 16);
            for (i = 0; i < nblk * 16; i++) {
               blks[i] = 0;
            }
            for (i = 0; i < str.length; i++) {
               blks[i >> 2] |= str.charCodeAt(i) << ((i % 4) * 8);
            }
            blks[i >> 2] |= 0x80 << ((i % 4) * 8);
            blks[nblk * 16 - 2] = str.length * 8;
            return blks;
         },
         /*
          * Add integers, wrapping at 2^32. This uses 16-bit operations internally 
          * to work around bugs in some JS interpreters.
          */
         add: function (x, y) {
            var lsw, msw;
            lsw = (x & 0xFFFF) + (y & 0xFFFF);
            msw = (x >> 16) + (y >> 16) + (lsw >> 16);
            return (msw << 16) | (lsw & 0xFFFF);
         },
         /*
          * Bitwise rotate a 32-bit number to the left
          */
         rol: function (num, cnt) {
            return (num << cnt) | (num >>> (32 - cnt));
         },
         /*
          * These functions implement the basic operation for each round of the
          * algorithm.
          */
         cmn: function (q, a, b, x, s, t) {
            return this.add(this.rol(this.add(this.add(a, q), this.add(x, t)), s), b);
         },
         ff: function (a, b, c, d, x, s, t) {
            return this.cmn((b & c) | ((~b) & d), a, b, x, s, t);
         },
         gg: function (a, b, c, d, x, s, t) {
            return this.cmn((b & d) | (c & (~d)), a, b, x, s, t);
         },
         hh: function (a, b, c, d, x, s, t) {
            return this.cmn(b ^ c ^ d, a, b, x, s, t);
         },
         ii: function (a, b, c, d, x, s, t) {
            return this.cmn(c ^ (b | (~d)), a, b, x, s, t);
         }
      };
   }();

   /*
    * Take a string and return the hex representation of its MD5.
    * 10637920c62fe58f57cbdb1afaa7ad3e
    * 
    */
   String.prototype.md5 = function () {
      var x, a, b, c, d, olda, oldb, oldc, oldd, i;
      x = MD5Tools.str2blks_MD5(this);
      a = 1732584193;
      b = -271733879;
      c = -1732584194;
      d = 271733878;
      for (i = 0; i < x.length; i += 16) {
         olda = a;
         oldb = b;
         oldc = c;
         oldd = d;
         a = MD5Tools.ff(a, b, c, d, x[i + 0], 7, -680876936);
         d = MD5Tools.ff(d, a, b, c, x[i + 1], 12, -389564586);
         c = MD5Tools.ff(c, d, a, b, x[i + 2], 17, 606105819);
         b = MD5Tools.ff(b, c, d, a, x[i + 3], 22, -1044525330);
         a = MD5Tools.ff(a, b, c, d, x[i + 4], 7, -176418897);
         d = MD5Tools.ff(d, a, b, c, x[i + 5], 12, 1200080426);
         c = MD5Tools.ff(c, d, a, b, x[i + 6], 17, -1473231341);
         b = MD5Tools.ff(b, c, d, a, x[i + 7], 22, -45705983);
         a = MD5Tools.ff(a, b, c, d, x[i + 8], 7, 1770035416);
         d = MD5Tools.ff(d, a, b, c, x[i + 9], 12, -1958414417);
         c = MD5Tools.ff(c, d, a, b, x[i + 10], 17, -42063);
         b = MD5Tools.ff(b, c, d, a, x[i + 11], 22, -1990404162);
         a = MD5Tools.ff(a, b, c, d, x[i + 12], 7, 1804603682);
         d = MD5Tools.ff(d, a, b, c, x[i + 13], 12, -40341101);
         c = MD5Tools.ff(c, d, a, b, x[i + 14], 17, -1502002290);
         b = MD5Tools.ff(b, c, d, a, x[i + 15], 22, 1236535329);
         a = MD5Tools.gg(a, b, c, d, x[i + 1], 5, -165796510);
         d = MD5Tools.gg(d, a, b, c, x[i + 6], 9, -1069501632);
         c = MD5Tools.gg(c, d, a, b, x[i + 11], 14, 643717713);
         b = MD5Tools.gg(b, c, d, a, x[i + 0], 20, -373897302);
         a = MD5Tools.gg(a, b, c, d, x[i + 5], 5, -701558691);
         d = MD5Tools.gg(d, a, b, c, x[i + 10], 9, 38016083);
         c = MD5Tools.gg(c, d, a, b, x[i + 15], 14, -660478335);
         b = MD5Tools.gg(b, c, d, a, x[i + 4], 20, -405537848);
         a = MD5Tools.gg(a, b, c, d, x[i + 9], 5, 568446438);
         d = MD5Tools.gg(d, a, b, c, x[i + 14], 9, -1019803690);
         c = MD5Tools.gg(c, d, a, b, x[i + 3], 14, -187363961);
         b = MD5Tools.gg(b, c, d, a, x[i + 8], 20, 1163531501);
         a = MD5Tools.gg(a, b, c, d, x[i + 13], 5, -1444681467);
         d = MD5Tools.gg(d, a, b, c, x[i + 2], 9, -51403784);
         c = MD5Tools.gg(c, d, a, b, x[i + 7], 14, 1735328473);
         b = MD5Tools.gg(b, c, d, a, x[i + 12], 20, -1926607734);
         a = MD5Tools.hh(a, b, c, d, x[i + 5], 4, -378558);
         d = MD5Tools.hh(d, a, b, c, x[i + 8], 11, -2022574463);
         c = MD5Tools.hh(c, d, a, b, x[i + 11], 16, 1839030562);
         b = MD5Tools.hh(b, c, d, a, x[i + 14], 23, -35309556);
         a = MD5Tools.hh(a, b, c, d, x[i + 1], 4, -1530992060);
         d = MD5Tools.hh(d, a, b, c, x[i + 4], 11, 1272893353);
         c = MD5Tools.hh(c, d, a, b, x[i + 7], 16, -155497632);
         b = MD5Tools.hh(b, c, d, a, x[i + 10], 23, -1094730640);
         a = MD5Tools.hh(a, b, c, d, x[i + 13], 4, 681279174);
         d = MD5Tools.hh(d, a, b, c, x[i + 0], 11, -358537222);
         c = MD5Tools.hh(c, d, a, b, x[i + 3], 16, -722521979);
         b = MD5Tools.hh(b, c, d, a, x[i + 6], 23, 76029189);
         a = MD5Tools.hh(a, b, c, d, x[i + 9], 4, -640364487);
         d = MD5Tools.hh(d, a, b, c, x[i + 12], 11, -421815835);
         c = MD5Tools.hh(c, d, a, b, x[i + 15], 16, 530742520);
         b = MD5Tools.hh(b, c, d, a, x[i + 2], 23, -995338651);
         a = MD5Tools.ii(a, b, c, d, x[i + 0], 6, -198630844);
         d = MD5Tools.ii(d, a, b, c, x[i + 7], 10, 1126891415);
         c = MD5Tools.ii(c, d, a, b, x[i + 14], 15, -1416354905);
         b = MD5Tools.ii(b, c, d, a, x[i + 5], 21, -57434055);
         a = MD5Tools.ii(a, b, c, d, x[i + 12], 6, 1700485571);
         d = MD5Tools.ii(d, a, b, c, x[i + 3], 10, -1894986606);
         c = MD5Tools.ii(c, d, a, b, x[i + 10], 15, -1051523);
         b = MD5Tools.ii(b, c, d, a, x[i + 1], 21, -2054922799);
         a = MD5Tools.ii(a, b, c, d, x[i + 8], 6, 1873313359);
         d = MD5Tools.ii(d, a, b, c, x[i + 15], 10, -30611744);
         c = MD5Tools.ii(c, d, a, b, x[i + 6], 15, -1560198380);
         b = MD5Tools.ii(b, c, d, a, x[i + 13], 21, 1309151649);
         a = MD5Tools.ii(a, b, c, d, x[i + 4], 6, -145523070);
         d = MD5Tools.ii(d, a, b, c, x[i + 11], 10, -1120210379);
         c = MD5Tools.ii(c, d, a, b, x[i + 2], 15, 718787259);
         b = MD5Tools.ii(b, c, d, a, x[i + 9], 21, -343485551);
         a = MD5Tools.add(a, olda);
         b = MD5Tools.add(b, oldb);
         c = MD5Tools.add(c, oldc);
         d = MD5Tools.add(d, oldd);
      }
      return MD5Tools.rhex(a) + MD5Tools.rhex(b) + MD5Tools.rhex(c) + MD5Tools.rhex(d);
   };
} else {
   alert("Sorry, but your browser doesn't support websocket");
}
