declare class OcelotEvent extends Event {
	promise: IOcelotPromise;
}
declare class OcelotResultEvent extends Event {
    dataservice: string;
    operation: string;
    args: any[];
    totaltime: number;
    javatime: number;
    jstime: number;
    networktime: number;
    msgToClient: MessageToCLient;
    response: any;
}
declare class OcelotMsgEvent extends Event {
    msg: MessageToCLient;
}
declare class OcelotKeyEvent extends Event {
    key: string;
}
declare interface PromisesMap {
    [key: string]: IOcelotPromise[];
}
declare interface LastUpdatedMap {
    [key: string]: any;
}
declare class IOcelotOptions {
    monitor: boolean;
    debug: boolean;
    reconnect: boolean;
}
declare class Fault {
    message: string;
    classname: string;
    stacktrace: string[];
}
declare class Locale {
    country: string;
    language: string;
}
declare interface MessageFromClient {
    id: string;
    ds: string;
    op: string; 
    argNames: string[];
    args: any[];
}
declare interface MessageToCLient {
    t: number;
    response: any;
    id: string;
    type: string;
    deadline: number;
}
declare interface IOcelotPromise {
    dataservice:string;
    id:string;
    operation:string;
    argNames:string[];
    args:any[];
    response: OcelotResultEvent;
    json: MessageFromClient;
    cacheIgnored:boolean;
    t:number;
    then(onFulfilled: Function, onRejected?: Function): IOcelotPromise;
    catch(onRejected: Function): IOcelotPromise;
    event(onEvented: Function): IOcelotPromise;
}
declare module ocelotServices {
    export function initCore(option: IOcelotOptions): IOcelotPromise;
    export function setLocale(locale: Locale): IOcelotPromise;
    export function getLocale(): IOcelotPromise;
    export function getOutDatedCache(map: LastUpdatedMap): IOcelotPromise;
    export function subscribe(topic: string): IOcelotPromise;
    export function unsubscribe(topic: string): IOcelotPromise;
}