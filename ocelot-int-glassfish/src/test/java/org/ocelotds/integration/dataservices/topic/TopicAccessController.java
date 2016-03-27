/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ocelotds.integration.dataservices.topic;

import org.ocelotds.security.JsTopicAccessController;

/**
 *
 * @author hhfrancois
 */
public interface TopicAccessController extends JsTopicAccessController {
	
	boolean isAccess();
	void setAccess(boolean access);
	
}
