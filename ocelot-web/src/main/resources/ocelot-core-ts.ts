/// <reference path="ocelot-services.d.ts" />
/**
 * MD5Tools
 */
class MD5Tools {
	static hex_chr(num: number): string {
		return "0123456789abcdef".charAt(num);
	}
	/*
	 * Convert a 32-bit number to a hex string with ls-byte first
	 */
	static rhex(num: number): string {
		let j: number, str: string = "";
		for (j = 0; j <= 3; j++) {
			str += this.hex_chr((num >> (j * 8 + 4)) & 0x0F) + this.hex_chr((num >> (j * 8)) & 0x0F);
		}
		return str;
	}
	/*
 	 * Convert a string to a sequence of 16-word blocks, stored as an array.
 	 * Append padding bits and the length, as described in the MD5 standard.
	 */
	static str2blks_MD5(str: string): number[] {
		let blks: number[], i: number, nblk: number = ((str.length + 8) >> 6) + 1;
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
	}
	/*
	 * Add integers, wrapping at 2^32. This uses 16-bit operations internally
	 * to work around bugs in some JS interpreters.
	 */
	static add(x: number, y: number): number {
		let lsw: number, msw: number;
		lsw = (x & 0xFFFF) + (y & 0xFFFF);
		msw = (x >> 16) + (y >> 16) + (lsw >> 16);
		return (msw << 16) | (lsw & 0xFFFF);
	}
	/*
	 * Bitwise rotate a 32-bit number to the left
	 */
	static rol(num: number, cnt: number): number {
		return (num << cnt) | (num >>> (32 - cnt));
	}
	/*
	 * These functions implement the basic operation for each round of the
	 * algorithm.
	 */
	static cmn(q: number, a: number, b: number, x: number, s: number, t: number): number {
		return this.add(this.rol(this.add(this.add(a, q), this.add(x, t)), s), b);
	}
	static ff(a: number, b: number, c: number, d: number, x: number, s: number, t: number): number {
		return this.cmn((b & c) | ((~b) & d), a, b, x, s, t);
	}
	static gg(a: number, b: number, c: number, d: number, x: number, s: number, t: number): number {
		return this.cmn((b & d) | (c & (~d)), a, b, x, s, t);
	}
	static hh(a: number, b: number, c: number, d: number, x: number, s: number, t: number): number {
		return this.cmn(b ^ c ^ d, a, b, x, s, t);
	}
	static ii(a: number, b: number, c: number, d: number, x: number, s: number, t: number): number {
		return this.cmn(c ^ (b | (~d)), a, b, x, s, t);
	}
};
/*
 * Take a string and return the hex representation of its MD5.
 * 10637920c62fe58f57cbdb1afaa7ad3e
 *
 */
interface String {
    md5(): string;
}
(<any>String.prototype).md5 = function () {
	let x: number[], a: number, b: number, c: number, d: number, olda: number, oldb: number, oldc: number, oldd: number, i: number;
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
class LastUpdateManager {
	public addEntry(id: string): void {
		let lastUpdates: LastUpdatedMap = this.getLastUpdateCache();
		lastUpdates[id] = Date.now();
		localStorage.setItem(OcelotConstants.LU, JSON.stringify(lastUpdates));
	}
	public removeEntry(id: string): void {
		let lastUpdates: LastUpdatedMap = this.getLastUpdateCache();
		delete lastUpdates[id];
		localStorage.setItem(OcelotConstants.LU, JSON.stringify(lastUpdates));
	}
	public getLastUpdateCache(): LastUpdatedMap {
		let lastUpdates: string = localStorage.getItem(OcelotConstants.LU);
		if (!lastUpdates) {
			return {};
		} else {
			return JSON.parse(lastUpdates);
		}
	}
}
class OcelotCacheManager implements IOcelotCacheManager {
	private lastUpdateManager: LastUpdateManager = new LastUpdateManager();
	private addHandlers: Function[] = [];
	private removeHandlers: Function[] = [];
	private manageAddEvent(msgToClient: MessageToCLient): void {
		let evt: OcelotMsgEvent = <OcelotMsgEvent>document.createEvent(OcelotConstants.EVT);
		evt.initEvent(OcelotConstants.ADD, true, false);
		evt.msg = msgToClient;
		this.addHandlers.forEach(function (handler: Function) {
			handler(evt);
		});
	}
	private manageRemoveEvent(compositeKey: string) {
		let evt: OcelotKeyEvent = <OcelotKeyEvent>document.createEvent(OcelotConstants.EVT);
		evt.initEvent(OcelotConstants.RM, true, false);
		evt.key = compositeKey;
		this.removeHandlers.forEach(function (handler: Function) {
			handler(evt);
		});
	}
	public getLastUpdateCache(): LastUpdatedMap {
		return this.lastUpdateManager.getLastUpdateCache();
	}
	public addEventListener(type: string, listener: Function): void {
		if (type === OcelotConstants.ADD) {
			this.addHandlers.push(listener);
		} else if (type === OcelotConstants.RM) {
			this.removeHandlers.push(listener);
		}
	}
	public removeEventListener(type: string, listener: Function): void {
		let idx = -1;
		if (type === OcelotConstants.ADD) {
			idx = this.addHandlers.indexOf(listener);
			if (idx !== -1) {
				this.addHandlers.splice(idx, 1);
			}
		} else if (type === OcelotConstants.RM) {
			idx = this.removeHandlers.indexOf(listener);
			if (idx !== -1) {
				this.removeHandlers.splice(idx, 1);
			}
		}
	}
	public putResultInCache(msgToClient: MessageToCLient): void {
		let ids: string[], json: string, obj: any;
		if (!msgToClient.deadline) {
			return;
		}
		this.lastUpdateManager.addEntry(msgToClient.id);
		this.manageAddEvent(msgToClient);
		ids = msgToClient.id.split("_");
		json = localStorage.getItem(ids[0]);
		obj = {};
		if (json) {
			obj = JSON.parse(json);
		}
		obj[ids[1]] = msgToClient;
		json = JSON.stringify(obj);
		localStorage.setItem(ids[0], json);
	}
	public getResultInCache(compositeKey: string, ignoreCache: boolean): MessageToCLient {
		let ids: string[], json: string, msgToClient: MessageToCLient, obj: any, now: number;
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
	}
	public removeEntryInCache(compositeKey: string): void {
		this.lastUpdateManager.removeEntry(compositeKey);
		this.manageRemoveEvent(compositeKey);
		let ids: string[] = compositeKey.split("_");
		let entry: string = localStorage.getItem(ids[0]);
		if (entry) {
			let obj: any = JSON.parse(entry);
			if (ids.length === 2) {
				delete obj[ids[1]];
				localStorage.setItem(ids[0], JSON.stringify(obj));
			} else {
				localStorage.removeItem(ids[0]);
			}
		}
	}
	public removeEntries(list: string[]): void {
		list.forEach(function (key) {
			this.removeEntryInCache(key);
		});
	}
	public clearCache(): void {
		localStorage.clear();
	}
}
class OcelotController {
	private opts: IOcelotOptions = { "monitor": false, "debug": false, "reconnect": true };
	private ws: WebSocket;
	private promisesMap: PromisesMap = {}
	private path: string;
	private closetimer: number;
	private ocelotCacheManager: IOcelotCacheManager = new OcelotCacheManager();
	public constructor() {
		// init a standard httpsession and init websocket
		this.connect().then(function () {
			// Controller subscribe to ocelot-cleancache topic
			new Subscriber(OcelotConstants.CLEANCACHE).message(function (id: string) {
				if (id === OcelotConstants.ALL) {
					this.ocelotCacheManager.clearCache();
				} else {
					this.ocelotCacheManager.removeEntryInCache(id);
				}
			}).then(function () {
				// Get Locale from server or cache and re-set it in session, this launch a message in ocelot-cleancache
				ocelotServices.getLocale().then(function (locale: Locale) {
					if (locale) {
						ocelotServices.setLocale(locale);
					}
				});
			});
			// send states or current objects in cache with lastupdate
			ocelotServices.getOutDatedCache(this.ocelotCacheManager.getLastUpdateCache()).then(function (entries: string[]) {
				this.ocelotCacheManager.removeEntries(entries);
			});
		});
	}
	public get cacheManager(): IOcelotCacheManager {
		return this.ocelotCacheManager;
	}
	public get options(): IOcelotOptions {
		return this.opts;
	}
	public get status(): string {
		return this.ws ? OcelotConstants.stateLabels[this.ws.readyState] : "CLOSED";
	}
	public close(reason: string): void {
		setTimeout(function () {
			this.ws.close(1000, reason || "Normal closure; the connection successfully completed whatever purpose for which it was created.");
		}, 10);
	}
	public addPromise(promise: IOcelotPromise): void {
		if (this.isTopicSubscription(promise, OcelotConstants.STATUS)) {
			this.addPromiseToId(promise, OcelotConstants.STATUS);
			this.stateUpdated();
			return;
		}
		if (this.isTopicUnsubscription(promise, OcelotConstants.STATUS)) {
			this.clearPromisesForId(OcelotConstants.STATUS);
			return;
		}
		// if it's internal service like ocelotController.open or ocelotController.close
		if (this.isOcelotControllerServices(promise)) {
			this.addPromiseToId(promise, promise.id);
			return;
		}
		// check entry cache
		let msgToClient: MessageToCLient = this.ocelotCacheManager.getResultInCache(promise.id, promise.cacheIgnored);
		if (msgToClient) {
			// present and valid, return response without call
			promise.response = this.createResultEventFromPromise(promise, <MessageToCLient>{ "response": msgToClient.response, "t": 0 });
			return;
		}
		// else call
		this.sendMfc(promise);
	}
	private createEventFromPromise(type: string, promise: IOcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
		let evt: OcelotResultEvent = <OcelotResultEvent>document.createEvent(OcelotConstants.EVT);
		evt.initEvent(type, true, false);
		evt.dataservice = promise.dataservice;
		evt.operation = promise.operation;
		evt.args = promise.args;
		evt.totaltime = 0;
		evt.javatime = 0;
		evt.jstime = 0;
		evt.networktime = 0;
		if (msgToClient) {
			evt.response = msgToClient.response;
			if (this.options.monitor) {
				evt.javatime = msgToClient.t; // backend timing
			}
		}
		if (this.options.monitor) {
			evt.totaltime = new Date().getTime() - promise.t; // total timing
		}
		return evt;
	}
	private createMessageEventFromPromise(promise: OcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
		return this.createEventFromPromise(OcelotConstants.MSG, promise, msgToClient);
	}
	private createResultEventFromPromise(promise: IOcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
		return this.createEventFromPromise(OcelotConstants.RES, promise, msgToClient);
	}
	private createConstraintEventFromPromise(promise: OcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
		return this.createEventFromPromise(OcelotConstants.CONSTRAINT, promise, msgToClient);
	}
	private createFaultEventFromPromise(promise: OcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
		return this.createEventFromPromise(OcelotConstants.FAULT, promise, msgToClient);
	}
	private stateUpdated(): void {
		this.foreachPromiseInPromisesDo(OcelotConstants.STATUS, function (promise: OcelotPromise) {
			let response = this.createMessageEventFromPromise(promise, { "response": status, "t": 0, "id": "", "type": "", deadline: 0 });
			promise.response = response;
		});
	}
	private foreachPromiseInPromisesDo(id: string, func: Function): void {
		this.foreachPromiseDo(this.getPromises(id), func);
	}
	private foreachPromiseDo(aPromises: IOcelotPromise[], func: Function): void {
		if (aPromises) {
			for (let i: number = 0; i < aPromises.length; i++) {
				func(aPromises[i]);
			}
		}
	}
	private addPromiseToId(promise: IOcelotPromise, id: string): boolean { // add promise to promise list and return if some promises exists already for id
		let exists = (this.promisesMap[id] !== undefined);
		if (!exists) {
			this.promisesMap[id] = [];
		}
		this.promisesMap[id].push(promise);
		return exists;
	}
	private clearPromisesForId(id: string): void {
		delete this.promisesMap[id];
	}
	private getPromises(id: string): IOcelotPromise[] {
		return this.promisesMap[id] || [];
	}
	private isOcelotControllerServices(promise: IOcelotPromise): boolean {
		return promise && (promise.dataservice === "ocelotController");
	}
	private isSubscription(promise: IOcelotPromise): boolean {
		return promise.dataservice === OcelotConstants.OSRV && promise.operation === OcelotConstants.SUB;
	}
	private isTopicSubscription(promise: IOcelotPromise, topic: string): boolean {
		return this.isSubscription(promise) && this.isTopic(promise, topic);
	}
	private isUnsubscription(promise: IOcelotPromise): boolean {
		return promise.dataservice === OcelotConstants.OSRV && promise.operation === OcelotConstants.UNSUB;
	}
	private isTopicUnsubscription(promise: IOcelotPromise, topic: string): boolean {
		return this.isUnsubscription(promise) && this.isTopic(promise, topic);
	}
	private isTopic(promise: IOcelotPromise, topic: string): boolean {
		return topic ? (promise.args[0] === topic) : true;
	}
	private extractOptions(search: string): void {
		let params = search.split("&");
		params.forEach(function (param) {
			if (param.search(/^\??ocelot=/) === 0) {
				this.options = JSON.parse(decodeURI(param.replace(/\??ocelot=/, "")));
			}
		});
	}
	private sendMfc(promise: IOcelotPromise): void {
		if (!this.addPromiseToId(promise, promise.id)) {
			// Subscription or unsubscription to topic, use websocket
			let mfc: string = JSON.stringify(promise.json);
			let xhttp: XMLHttpRequest = new XMLHttpRequest();
			let oc: OcelotController = this;
			xhttp.onreadystatechange = function () {
				if (xhttp.readyState === 4) {
					if (xhttp.status === 200) {
						let msgToClient: MessageToCLient = JSON.parse(xhttp.responseText);
						oc.receiveMtc(msgToClient);
					} else {
						oc.receiveMtc(<MessageToCLient>{ "id": promise.id, "type": OcelotConstants.FAULT, "response": { "classname": "XMLHttpRequest", "message": "XMLHttpRequest request failed : code = " + xhttp.status, "stacktrace": [] }, "t": 0 });
					}
				}
			};
			xhttp.open("POST", "http" + this.path + "ocelot/endpoint", true);
			xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
			xhttp.send("mfc=" + mfc);
		}
	}
	private receiveMtc(msgToClient: MessageToCLient): void {
		if (msgToClient.type === OcelotConstants.RES) { // maybe should be store result in cache
			// if msgToClient has dead line so we stock in cache
			this.ocelotCacheManager.putResultInCache(msgToClient);
		}
		this.foreachPromiseInPromisesDo(msgToClient.id, function (promise: OcelotPromise) {
			switch (msgToClient.type) {
				case OcelotConstants.FAULT:
					promise.response = this.createFaultEventFromPromise(promise, msgToClient);
					break;
				case OcelotConstants.RES:
					// if msg is response of subscribe request
					if (this.isTopicSubscription(promise)) {
						this.addPromiseToId(promise, promise.args[0]);
					} else if (this.isTopicUnsubscription(promise)) {
						this.clearPromisesForId(promise.args[0]);
					}
					promise.response = this.createResultEventFromPromise(promise, msgToClient);
					break;
				case OcelotConstants.CONSTRAINT:
					promise.response = this.createConstraintEventFromPromise(promise, msgToClient);
					break;
				case OcelotConstants.MSG:
					promise.response = this.createMessageEventFromPromise(promise, msgToClient);
					break;
			}
		});
		// when receive result or fault, remove handlers, except for topic
		if (msgToClient.type !== OcelotConstants.MSG) {
			this.clearPromisesForId(msgToClient.id);
		}
	}
	private onwsmessage(evt: MessageEvent): void {
		if (this.options.debug) console.debug(evt.data);
		this.receiveMtc(JSON.parse(evt.data));
	}
	private onwserror(evt: ErrorEvent): void {
		console.info("Websocket error : " + evt.error);
		this.stateUpdated();
	}
	private onwsclose(evt: CloseEvent): void {
		this.stateUpdated();
		if (evt.reason !== "ONUNLOAD") {
			if (this.options.debug) console.debug("Websocket closed : " + evt.reason + " try reconnect each " + 1000 + "ms");
			this.closetimer = setInterval(function () {
				this.connect();
			}, 1000);
		}
	}
	private onwsopen(evt: Event): void {
		clearInterval(this.closetimer);
		if (this.options.debug) console.debug("Websocket opened");
		let ps: PromisesMap;
		// handler, apromise, idx, promise;
		this.stateUpdated();
		ps = this.promisesMap;
		this.promisesMap = {};
		Object.keys(ps).forEach(function (id: string) { // we redo the subscription
			if (ps[id].length && id !== ps[id][0].id) { // ps[id][0].id : topic name
				this.foreachPromiseDo(ps[id], this.addPromise);
			}
		});
	}
	private connect(): IOcelotPromise {
		if (this.options.debug) console.debug("Ocelotds initialization...");
		let re = /ocelot-core.js|ocelot\.js.*|ocelot\/core(\.min)?\.js/;
		for (let i = 0; i < document.scripts.length; i++) {
			let item: HTMLScriptElement = <HTMLScriptElement>document.scripts[i];
			if (item.src.match(re)) {
				this.path = item.src.replace(/^http/, "").replace(re, "");
			}
		}
		this.extractOptions(document.location.search);
		// init a standard httpsession and init websocket
		return ocelotServices.initCore(this.options).then(function () {
			this.ws = new WebSocket("ws" + this.path + "ocelot-endpoint");
			this.ws.onmessage = this.onwsmessage;
			this.ws.onopen = this.onwsopen;
			this.ws.onerror = this.onwserror;
			this.ws.onclose = this.onwsclose;
		});
	}

}
let ocelotController: OcelotController = new OcelotController();
let OcelotPromiseFactory:any = {
	createPromise: function(ds: string, id: string, op: string, argNames: string[], args: string[]): IOcelotPromise {
		return new OcelotPromiseSrv(ds, id, op, argNames, args);
	}
}
interface IOcelotCacheManager {
	/**
	 * Get map of last update cache
	 */
	getLastUpdateCache(): LastUpdatedMap
	/**
	 * Add listener for receive cache event
	 * @param {String} type event : add, remove
	 * @param {Function} listener
	 */
	addEventListener(type: string, listener: Function): void
	/**
	 * Remove listener for receive cache event
	 * @param {String} type event : add, remove
	 * @param {Function} listener
	 */
	removeEventListener(type: string, listener: Function): void
	/**
	 * If msgToClient has deadline so we stock in cache
	 * Add result in cache storage
	 * @param {MessageToClient} msgToClient
	 */
	putResultInCache(msgToClient: MessageToCLient): void
	/**
	 * get entry from cache
	 * @param {String} compositeKey
	 * @param {boolean} ignoreCache
	 * @returns {MessageToClient}
	 */
	getResultInCache(compositeKey: string, ignoreCache: boolean): MessageToCLient
	/**
	 * Remove sub entry or entry in cache
	 * @param {String} compositeKey
	 */
	removeEntryInCache(compositeKey: string): void
	/**
	 * Remove all entries defined in list
	 * @param {array} list
	 */
	removeEntries(list: string[]): void
	/**
	 * Clear cache storage
	 */
	clearCache(): void
}
let OcelotConstants = {
	get LU(): string { return "ocelot-lastupdate" },
	get RES(): string { return "RESULT" },
	get FAULT(): string { return "FAULT" },
	get CONSTRAINT(): string { return "CONSTRAINT" },
	get MSG(): string { return "MESSAGE" },
	get ALL(): string { return "ALL" },
	get EVT(): string { return "Event" },
	get ADD(): string { return "add" },
	get RM(): string { return "remove" },
	get CLEANCACHE(): string { return "ocelot-cleancache" },
	get STATUS(): string { return "ocelot-status" },
	get OSRV(): string { return "org.ocelotds.OcelotServices" },
	get SUB(): string { return "subscribe" },
	get UNSUB(): string { return "unsubscribe" },
	get stateLabels(): string[] { return ['CONNECTING', 'OPEN', 'CLOSING', 'CLOSED'] }
};
/**
 * Add ocelotController events to document
 * @param {Event} event
 */
document.addEventListener("call", function (event: OcelotEvent) {
	ocelotController.addPromise(event.promise);
});
window.addEventListener("beforeunload", function (e: Event) {
	if (ocelotController) {
		ocelotController.close("ONUNLOAD");
	}
});
/*class OcelotOptions implements IOcelotOptions {
	public constructor(public monitor: boolean = false, public debug: boolean = false) {}
}*/
abstract class OcelotPromise implements IOcelotPromise {
	public constructor(public dataservice: string, public id: string, public operation: string, public argNames: string[], public args: any[]) {
		this.key = id;
		this.t = new Date().getTime();
		this.launch();
	}
	protected key: string;
	public t: number;
	get cacheIgnored(): boolean {
		return false;
	}
	protected fault: Fault;
	protected thenHandlers: Function[] = [];
	protected catchHandlers: Function[] = [];
	protected constraintHandlers: Function[] = [];
	protected eventHandlers: Function[] = []
	protected evt: OcelotResultEvent;
	private launch(): void {
		let e: Event = document.createEvent("Event");
		e.initEvent("call", true, false);
		let oe: OcelotEvent = <OcelotEvent>e;
		oe.promise = this;
		setTimeout(function () {
			document.dispatchEvent(oe);
		}, 1);
	}
	set response(e: OcelotResultEvent) {
		this.evt = e;
		this.process();
	}
	get json(): MessageFromClient {
		return { "id": this.id, "ds": this.dataservice, "op": this.operation, "argNames": this.argNames, "args": this.args };
	}
	public then(onFulfilled: Function, onRejected?: Function): OcelotPromise {
		this.thenHandlers.push(onFulfilled);
		if (onRejected) {
			this.catchHandlers.push(onRejected);
		}
		this.process();// event already receive ?
		return this;
	}
	public catch(onRejected: Function): OcelotPromise {
		if (onRejected) {
			this.catchHandlers.push(onRejected);
		}
		this.process();// event already receive ?
		return this;
	}
	public event(onEvented: Function): OcelotPromise {
		if (onEvented) {
			this.eventHandlers.push(onEvented);
		}
		this.process();// event already receive ?
		return this;
	}
	protected process(): void {
		let handler: Function;
		if (!this.evt) {
			return;
		}
		while (handler = this.eventHandlers.shift()) {
			handler(this.evt);
		}
		switch (this.evt.type) {
			case OcelotConstants.RES:
				while (handler = this.thenHandlers.shift()) {
					handler(this.evt.response);
				}
				break;
			case OcelotConstants.CONSTRAINT:
				while (handler = this.constraintHandlers.shift()) {
					handler(this.evt.response);
				}
				break;
			case OcelotConstants.FAULT:
				let fault: Fault = <Fault>this.evt.response;
				console.error(fault.classname + "(" + fault.message + ")");
				while (handler = this.catchHandlers.shift()) {
					handler(fault);
				}
				break;
		}
	}
}
class OcelotPromiseSrv extends OcelotPromise {
	private cignored: boolean = false;
	public constraint(onConstraint: Function): OcelotPromiseSrv {
		if (onConstraint) {
			this.constraintHandlers.push(onConstraint);
		}
		this.process();// event already receive ?
		return this;
	}
	public ignoreCache(ignore: boolean): OcelotPromiseSrv {
		this.cignored = ignore;
		return this;
	}
	get cacheIgnored(): boolean {
		return this.cignored;
	}
}
class OcelotPromiseTopic extends OcelotPromise {
	public constructor(p: IOcelotPromise) {
		super(p.dataservice, p.id, p.operation, p.argNames, p.args);
	}
	get topic(): string {
		return this.args[0];
	}
	protected messageHandlers: Function[] = [];
	protected process(): void {
		if (!this.evt) {
			return;
		}
		if (this.evt.type !== OcelotConstants.MSG) {
			super.process();
		} else {
			this.messageHandlers.forEach(function (messageHandler: Function) {
				messageHandler(this.evt.response);
			});
		}
	}
	public message(onMessaged: Function): OcelotPromiseTopic {
		if (onMessaged) {
			this.messageHandlers.push(onMessaged);
		}
		this.process();// event already receive ?
		return this;
	}
	public unsubscribe(): IOcelotPromise {
		return ocelotServices.unsubscribe(this.topic);
	}
}
class Subscriber extends OcelotPromiseTopic {
	public constructor(public topic: string) {
		super(<IOcelotPromise>ocelotServices.subscribe(topic));
	}
}
if ("WebSocket" in window) {
	/*	(function (): IOcelotController {
			function init() {
				// init a standard httpsession and init websocket
				connect().then(function () {
					// Controller subscribe to ocelot-cleancache topic
					new Subscriber(OcelotConstants.CLEANCACHE).message(function (id: string) {
						if (id === OcelotConstants.ALL) {
							ocelotCacheManager.clearCache();
						} else {
							ocelotCacheManager.removeEntryInCache(id);
						}
					}).then(function () {
						// Get Locale from server or cache and re-set it in session, this launch a message in ocelot-cleancache
						ocelotServices.getLocale().then(function (locale: Locale) {
							if (locale) {
								ocelotServices.setLocale(locale);
							}
						});
					});
					// send states or current objects in cache with lastupdate
					ocelotServices.getOutDatedCache(ocelotCacheManager.getLastUpdateCache()).then(function (entries: string[]) {
						ocelotCacheManager.removeEntries(entries);
					});
				});
			}
			let options: IOcelotOptions = { "monitor": false, "debug": false };
			let ws: WebSocket;
			let promisesMap: PromisesMap = {}
			let path: string;
			let closetimer: number;
			let ocelotCacheManager = (function (): IOcelotCacheManager {
				let addHandlers: Function[] = [];
				let removeHandlers: Function[] = [];
				let lastUpdateManager: ILastUpdateManager = (function (): ILastUpdateManager {
					return <ILastUpdateManager>{
						addEntry(id: string): void {
							let lastUpdates: LastUpdatedMap = this.getLastUpdateCache();
							lastUpdates[id] = Date.now();
							localStorage.setItem(OcelotConstants.LU, JSON.stringify(lastUpdates));
						},
						removeEntry(id: string): void {
							let lastUpdates: LastUpdatedMap = this.getLastUpdateCache();
							delete lastUpdates[id];
							localStorage.setItem(OcelotConstants.LU, JSON.stringify(lastUpdates));
						},
						getLastUpdateCache(): LastUpdatedMap {
							let lastUpdates: string = localStorage.getItem(OcelotConstants.LU);
							if (!lastUpdates) {
								return {};
							} else {
								return JSON.parse(lastUpdates);
							}
						}
					}
				})()
				function manageAddEvent(msgToClient: MessageToCLient): void {
					let evt: OcelotMsgEvent = <OcelotMsgEvent>document.createEvent(OcelotConstants.EVT);
					evt.initEvent(OcelotConstants.ADD, true, false);
					evt.msg = msgToClient;
					this.addHandlers.forEach(function (handler: Function) {
						handler(evt);
					});
				}
				function manageRemoveEvent(compositeKey: string) {
					let evt: OcelotKeyEvent = <OcelotKeyEvent>document.createEvent(OcelotConstants.EVT);
					evt.initEvent(OcelotConstants.RM, true, false);
					evt.key = compositeKey;
					this.removeHandlers.forEach(function (handler: Function) {
						handler(evt);
					});
				}
				return <IOcelotCacheManager>{
					getLastUpdateCache(): LastUpdatedMap {
						return this.lastUpdateManager.getLastUpdateCache();
					},
					addEventListener(type: string, listener: Function): void {
						if (type === OcelotConstants.ADD) {
							this.addHandlers.push(listener);
						} else if (type === OcelotConstants.RM) {
							this.removeHandlers.push(listener);
						}
					},
					removeEventListener(type: string, listener: Function): void {
						let idx = -1;
						if (type === OcelotConstants.ADD) {
							idx = this.addHandlers.indexOf(listener);
							if (idx !== -1) {
								this.addHandlers.splice(idx, 1);
							}
						} else if (type === OcelotConstants.RM) {
							idx = this.removeHandlers.indexOf(listener);
							if (idx !== -1) {
								this.removeHandlers.splice(idx, 1);
							}
						}
					},
					putResultInCache(msgToClient: MessageToCLient): void {
						let ids: string[], json: string, obj: any;
						if (!msgToClient.deadline) {
							return;
						}
						this.lastUpdateManager.addEntry(msgToClient.id);
						this.manageAddEvent(msgToClient);
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
					getResultInCache(compositeKey: string, ignoreCache: boolean): MessageToCLient {
						let ids: string[], json: string, msgToClient: MessageToCLient, obj: any, now: number;
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
					removeEntryInCache(compositeKey: string): void {
						this.lastUpdateManager.removeEntry(compositeKey);
						this.manageRemoveEvent(compositeKey);
						let ids: string[] = compositeKey.split("_");
						let entry: string = localStorage.getItem(ids[0]);
						if (entry) {
							let obj: any = JSON.parse(entry);
							if (ids.length === 2) {
								delete obj[ids[1]];
								localStorage.setItem(ids[0], JSON.stringify(obj));
							} else {
								localStorage.removeItem(ids[0]);
							}
						}
					},
					removeEntries(list: string[]): void {
						list.forEach(function (key) {
							this.removeEntryInCache(key);
						});
					},
					clearCache(): void {
						localStorage.clear();
					}
				}
			})();
			function createEventFromPromise(type: string, promise: OcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
				let evt: OcelotResultEvent = <OcelotResultEvent>document.createEvent(OcelotConstants.EVT);
				evt.initEvent(type, true, false);
				evt.dataservice = promise.dataservice;
				evt.operation = promise.operation;
				evt.args = promise.args;
				evt.totaltime = 0;
				evt.javatime = 0;
				evt.jstime = 0;
				evt.networktime = 0;
				if (msgToClient) {
					evt.response = msgToClient.response;
					if (options.monitor) {
						evt.javatime = msgToClient.t; // backend timing
					}
				}
				if (options.monitor) {
					evt.totaltime = new Date().getTime() - promise.t; // total timing
				}
				return evt;
			}
			function createMessageEventFromPromise(promise: OcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
				return createEventFromPromise(OcelotConstants.MSG, promise, msgToClient);
			}
			function createResultEventFromPromise(promise: OcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
				return createEventFromPromise(OcelotConstants.RES, promise, msgToClient);
			}
			function createConstraintEventFromPromise(promise: OcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
				return createEventFromPromise(OcelotConstants.CONSTRAINT, promise, msgToClient);
			}
			function createFaultEventFromPromise(promise: OcelotPromise, msgToClient: MessageToCLient): OcelotResultEvent {
				return createEventFromPromise(OcelotConstants.FAULT, promise, msgToClient);
			}
			function stateUpdated(): void {
				foreachPromiseInPromisesDo(OcelotConstants.STATUS, function (promise: OcelotPromise) {
					let response = createMessageEventFromPromise(promise, { "response": status, "t": 0, "id": "", "type": "", deadline: 0 });
					promise.response = response;
				});
			}
			function foreachPromiseInPromisesDo(id: string, func: Function): void {
				foreachPromiseDo(getPromises(id), func);
			}
			function foreachPromiseDo(aPromises: IOcelotPromise[], func: Function): void {
				if (aPromises) {
					for (let i: number = 0; i < aPromises.length; i++) {
						func(aPromises[i]);
					}
				}
			}
			function addPromiseToId(promise: OcelotPromise, id: string): boolean { // add promise to promise list and return if some promises exists already for id
				let exists = (promisesMap[id] !== undefined);
				if (!exists) {
					promisesMap[id] = [];
				}
				promisesMap[id].push(promise);
				return exists;
			}
			function clearPromisesForId(id: string): void {
				delete promisesMap[id];
			}
			function getPromises(id: string): IOcelotPromise[] {
				return promisesMap[id] || [];
			}
			function isOcelotControllerServices(promise: OcelotPromise): boolean {
				return promise && (promise.dataservice === "ocelotController");
			}
			function isSubscription(promise: OcelotPromise): boolean {
				return promise.dataservice === OcelotConstants.OSRV && promise.operation === OcelotConstants.SUB;
			}
			function isTopicSubscription(promise: OcelotPromise, topic: string): boolean {
				return isSubscription(promise) && isTopic(promise, topic);
			}
			function isUnsubscription(promise: OcelotPromise): boolean {
				return promise.dataservice === OcelotConstants.OSRV && promise.operation === OcelotConstants.UNSUB;
			}
			function isTopicUnsubscription(promise: OcelotPromise, topic: string): boolean {
				return isUnsubscription(promise) && isTopic(promise, topic);
			}
			function isTopic(promise: OcelotPromise, topic: string): boolean {
				return topic ? (promise.args[0] === topic) : true;
			}
			function extractOptions(search: string): void {
				let params = search.split("&");
				params.forEach(function (param) {
					if (param.search(/^\??ocelot=/) === 0) {
						options = JSON.parse(decodeURI(param.replace(/\??ocelot=/, "")));
					}
				});
			}
			function sendMfc(promise: OcelotPromise): void {
				if (!addPromiseToId(promise, promise.id)) {
					// Subscription or unsubscription to topic, use websocket
					let mfc: string = JSON.stringify(promise.json);
					let xhttp: XMLHttpRequest = new XMLHttpRequest();
					xhttp.onreadystatechange = function () {
						if (xhttp.readyState === 4) {
							if (xhttp.status === 200) {
								let msgToClient: MessageToCLient = JSON.parse(xhttp.responseText);
								receiveMtc(msgToClient);
							} else {
								receiveMtc(<MessageToCLient>{ "id": promise.id, "type": OcelotConstants.FAULT, "response": { "classname": "XMLHttpRequest", "message": "XMLHttpRequest request failed : code = " + xhttp.status, "stacktrace": [] }, "t": 0 });
							}
						}
					};
					xhttp.open("POST", "http" + path + "ocelot/endpoint", true);
					xhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
					xhttp.send("mfc=" + mfc);
				}
			}
			function receiveMtc(msgToClient: MessageToCLient): void {
				if (msgToClient.type === OcelotConstants.RES) { // maybe should be store result in cache
					// if msgToClient has dead line so we stock in cache
					ocelotCacheManager.putResultInCache(msgToClient);
				}
				foreachPromiseInPromisesDo(msgToClient.id, function (promise: OcelotPromise) {
					switch (msgToClient.type) {
						case OcelotConstants.FAULT:
							promise.response = createFaultEventFromPromise(promise, msgToClient);
							break;
						case OcelotConstants.RES:
							// if msg is response of subscribe request
							if (this.isTopicSubscription(promise)) {
								addPromiseToId(promise, promise.args[0]);
							} else if (this.isTopicUnsubscription(promise)) {
								clearPromisesForId(promise.args[0]);
							}
							promise.response = createResultEventFromPromise(promise, msgToClient);
							break;
						case OcelotConstants.CONSTRAINT:
							promise.response = createConstraintEventFromPromise(promise, msgToClient);
							break;
						case OcelotConstants.MSG:
							promise.response = createMessageEventFromPromise(promise, msgToClient);
							break;
					}
				});
				// when receive result or fault, remove handlers, except for topic
				if (msgToClient.type !== OcelotConstants.MSG) {
					clearPromisesForId(msgToClient.id);
				}
			}
			function onwsmessage(evt: MessageEvent): void {
				if (options.debug) console.debug(evt.data);
				receiveMtc(JSON.parse(evt.data));
			}
			function onwserror(evt: ErrorEvent): void {
				console.info("Websocket error : " + evt.error);
				stateUpdated();
			}
			function onwsclose(evt: CloseEvent): void {
				stateUpdated();
				if (evt.reason !== "ONUNLOAD") {
					if (options.debug) console.debug("Websocket closed : " + evt.reason + " try reconnect each " + 1000 + "ms");
					closetimer = setInterval(function () {
						connect();
					}, 1000);
				}
			}
			function onwsopen(evt: Event): void {
				clearInterval(closetimer);
				if (options.debug) console.debug("Websocket opened");
				let ps: PromisesMap;
				// handler, apromise, idx, promise;
				stateUpdated();
				ps = promisesMap;
				promisesMap = {};
				Object.keys(ps).forEach(function (id: string) { // we redo the subscription
					if (ps[id].length && id !== ps[id][0].id) { // ps[id][0].id : topic name
						foreachPromiseDo(ps[id], this.addPromise);
					}
				});
			}
			function connect(): IOcelotPromise {
				if (options.debug) console.debug("Ocelotds initialization...");
				let re = /ocelot-core.js|ocelot\.js.*|ocelot\/core(\.min)?\.js/;
				for (let i = 0; i < document.scripts.length; i++) {
					let item: HTMLScriptElement = <HTMLScriptElement>document.scripts[i];
					if (item.src.match(re)) {
						path = item.src.replace(/^http/, "").replace(re, "");
					}
				}
				extractOptions(document.location.search);
				// init a standard httpsession and init websocket
				return ocelotServices.initCore(options).then(function () {
					ws = new WebSocket("ws" + path + "ocelot-endpoint");
					ws.onmessage = onwsmessage;
					ws.onopen = onwsopen;
					ws.onerror = onwserror;
					ws.onclose = onwsclose;
				});
			}
			init()
			return {
				get cacheManager(): IOcelotCacheManager {
					return ocelotCacheManager;
				},
				get options(): IOcelotOptions {
					return this.opts;
				},
				get status(): string {
					return ws ? OcelotConstants.stateLabels[ws.readyState] : "CLOSED";
				},
				close(reason: string): void {
					setTimeout(function () {
						ws.close(1000, reason || "Normal closure; the connection successfully completed whatever purpose for which it was created.");
					}, 10);
				},
				addPromise(promise: OcelotPromiseSrv): void {
					if (isTopicSubscription(promise, OcelotConstants.STATUS)) {
						addPromiseToId(promise, OcelotConstants.STATUS);
						stateUpdated();
						return;
					}
					if (isTopicUnsubscription(promise, OcelotConstants.STATUS)) {
						clearPromisesForId(OcelotConstants.STATUS);
						return;
					}
					// if it's internal service like ocelotController.open or ocelotController.close
					if (isOcelotControllerServices(promise)) {
						addPromiseToId(promise, promise.id);
						return;
					}
					// check entry cache
					let msgToClient: MessageToCLient = ocelotCacheManager.getResultInCache(promise.id, promise.cacheIgnored);
					if (msgToClient) {
						// present and valid, return response without call
						promise.response = createResultEventFromPromise(promise, <MessageToCLient>{ "response": msgToClient.response, "t": 0 });
						return;
					}
					// else call
					sendMfc(promise);
				}
			}
		})(); */
} else {
	alert("Sorry, but your browser doesn't support websocket");
}
