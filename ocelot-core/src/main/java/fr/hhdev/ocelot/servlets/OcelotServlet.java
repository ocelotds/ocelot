/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hhdev.ocelot.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author francois
 */
@WebServlet(name = "OcelotServlet", urlPatterns = {"/ocelot1.js"})
public class OcelotServlet extends HttpServlet {

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		response.setContentType("text/javascript;charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			out.println("function OcelotController() {");
			out.println("var ws = new WebSocket(\"ws://\" + document.location.hostname + \":\" + document.location.port + \""+request.getContextPath()+"/endpoint\");");
			out.println("ws.topicHandlers = {};ws.resultHandlers = {};ws.faultHandlers = {};");
			out.println("this.subscribe = function (token) {");
			out.println("var command = \"{\\\"topic\\\":\\\"\" + token.topic + \"\\\",\\\"cmd\\\":\\\"subscribe\\\"}\";");
			out.println("ws.send(command);");
			out.println("ws.topicHandlers[token.topic] = token.onMessage;};");
			out.println("this.unsubscribe = function (token) {ws.topicHandlers[token.topic] = null;};");
			out.println("this.call = function (token) {");
			out.println("var uuid = new UUID().random();");
			out.println("ws.resultHandlers[uuid] = token.onResult;");
			out.println("ws.faultHandlers[uuid] = token.onFault;");
			out.println("var msg = \"{\\\"id\\\":\\\"\" + uuid + \"\\\",\\\"ds\\\":\\\"\" + token.dataservice + \"\\\",\\\"op\\\":\\\"\" + token.operation + \"\\\", \\\"args\\\":\"+JSON.stringify(token.args)+\"}\";");
			out.println("var command = \"{\\\"topic\\\":\\\"\" + token.factory + \"\\\",\\\"cmd\\\":\\\"call\\\",\\\"msg\\\":\" + msg + \"}\";");
			out.println("ws.send(command);};");
			out.println("ws.onmessage =function (evt) {");
			out.println("var msgToClient = JSON.parse(evt.data);");
			out.println("if(msgToClient.result) {");
			out.println("if(this.topicHandlers[msgToClient.id]) {");
			out.println("this.topicHandlers[msgToClient.id](msgToClient.result);");
			out.println("} else if(this.resultHandlers[msgToClient.id]) {");
			out.println("this.resultHandlers[msgToClient.id](msgToClient.result);}");
			out.println("} else if(msgToClient.fault) {");
			out.println("this.faultHandlers[msgToClient.id](msgToClient.fault);}};");
			out.println("ws.onopen = function (evt) {};");
			out.println("ws.onerror = function (evt) {alert(evt.toString());};}");
			out.println("function UUID() {");
			out.println("this.random = function () {");
			out.println("return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {");
			out.println("var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);");
			out.println("return v.toString(16);});};}");
			out.println("var controller;");
			out.println("document.addEventListener(\"subscribe\", function (event) {controller.subscribe(event);});");
			out.println("document.addEventListener(\"unsubscribe\", function (event) {controller.unsubscribe(event);});");
			out.println("document.addEventListener(\"call\", function (event) {controller.call(event);});");
			out.println("window.addEventListener(\"load\", function (event) {controller = new OcelotController();});");
			out.println("function Mdb(topic) {");
			out.println("var topic = topic;");
			out.println("this.subscribe = function() {");
			out.println("var subEvent = document.createEvent(\"Event\");");
			out.println("subEvent.initEvent(\"subscribe\", true, false);");
			out.println("subEvent.topic = topic;");
			out.println("subEvent.onMessage = this.onMessage;");
			out.println("document.dispatchEvent(subEvent);};");
			out.println("this.unsubscribe = function() {");
			out.println("var unsubEvent = document.createEvent(\"Event\");");
			out.println("unsubEvent.initEvent(\"unsubscribe\", true, false);");
			out.println("unsubEvent.topic = topic;");
			out.println("document.dispatchEvent(unsubEvent);};");
			out.println("this.onMessage = function(msg) {};}");
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
	
}
