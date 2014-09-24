/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.groovy.web.controller;

import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.groovy.GroovyScript;
import org.openmrs.User;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.groovy.GroovyUser;
import org.openmrs.module.groovy.GroovyUtil;
import org.openmrs.module.groovy.api.service.GroovyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


import org.openmrs.module.groovy.web.dwr.DWRGroovyService;

/**
 * This controller backs and saves the Groovy module settings
 */
@Controller
@RequestMapping("/module/groovy/report.form")
public class GroovyReportFormController {

    /**
     * Logger for this class
     */
    protected final Log log = LogFactory.getLog(getClass());
    private String view="/module/groovy/groovyReportForm";

    public GroovyReportFormController()
    {

    }

    @RequestMapping(method = RequestMethod.GET)    
    public String listReports(ModelMap map)
    {
		map.addAttribute("scripts",GroovyUtil.getService().getAllScripts());
		map.addAttribute("output",null);
    	map.addAttribute("error",null);

    	log.info("get groovy reports");
        return view;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String runReport(
    	@RequestParam(value="id",required=true) Integer id,
    	@RequestParam(value="fromDate",required=true) String fromDate,
    	@RequestParam(value="toDate",required=true) String toDate,
    	ModelMap map)
    {
    	map.addAttribute("scripts",GroovyUtil.getService().getAllScripts());
    	if(id==-1)
    		return view;
    	GroovyScript script = GroovyUtil.getService().getScriptById(id);
    	if(script==null)
    		return view;
    	log.info("run groovy report");
    	
    	
    	User currentUser = Context.getAuthenticatedUser();
    	String location=null;
    	List<GroovyUser> groovyUsers = GroovyUtil.getService().getAllUsers();
		for (int i=0; i<groovyUsers.size(); i++)
    	{
    		GroovyUser tempUser=groovyUsers.get(i);
    		if(tempUser.getUser().getId()==currentUser.getId())
    		{
    			location=tempUser.getLocation().getName();
    			break;
    		}
    	}
    	String scriptString="def fromDate='"+fromDate+"'\n"+
			"def toDate='"+toDate+"'\n"+
			"def location='"+location+"'\n"+
			script.getScript();
    	try
    	{
			DWRGroovyService svc=new DWRGroovyService();
    		String[] result=svc.eval(scriptString);
    		map.addAttribute("output",cleanResponse(result[1]));
    		map.addAttribute("error",cleanResponse(result[2]));
    	}
    	catch(Exception ex)
    	{
    		map.addAttribute("output",null);
    		map.addAttribute("error",ex.toString());	
    	}
        return view;
    }

    private String cleanResponse(String str)
    {
    	if(str==null || str.trim().length()==0)
    		return null;
    	return str.trim();
    }
}
