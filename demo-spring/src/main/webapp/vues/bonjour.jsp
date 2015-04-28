<%-- 
    Document   : index
    Created on : 28 avr. 2015, 23:00:32
    Author     : Arnaud Laye
--%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" isELIgnored="false"
    pageEncoding="ISO-8859-1"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title><spring:message code="titre.bonjour"/> : ${personne}</title>
    </head>
    <body>
        <spring:message code="libelle.bonjour.lemonde" arguments="${personne}"/>
    </body>
</html>