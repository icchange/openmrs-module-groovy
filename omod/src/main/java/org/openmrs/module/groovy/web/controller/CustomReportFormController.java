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
import org.openmrs.Concept;
import org.openmrs.util.OpenmrsUtil;

/**
 * This controller backs and saves the Groovy module settings
 */
@Controller
@RequestMapping("/module/groovy/CustomReport.form")
public class CustomReportFormController {

    /**
     * Logger for this class
     */
    protected final Log log = LogFactory.getLog(getClass());
    private String view="/module/groovy/customReportForm";

    public CustomReportFormController()
    {

    }

    @RequestMapping(value = "/module/groovy/customReportForm", method = RequestMethod.GET)    
    public String listReports(
    		ModelMap map,
    		@RequestParam(required = false, value = "selectId") String selectId,
    		@RequestParam(required = false, value = "afromDate") String fromDate,
    		@RequestParam(required = false, value = "atoDate") String toDate,
    		@RequestParam(required = false, value = "aminAge") String minAge,
    		@RequestParam(required = false, value = "amaxAge") String maxAge,
    		@RequestParam(value="aconId1", required = false) String conId1,
    		@RequestParam(value="aname1", required = false) String name1,
    		@RequestParam(value="atype1", required = false) String type1,
    		@RequestParam(value="aop1", required = false) String op1,
    		@RequestParam(value="aval1", required = false) String val1,
    		@RequestParam(value="aconId2", required = false) String conId2,
    		@RequestParam(value="aname2", required = false) String name2,
    		@RequestParam(value="atype2", required = false) String type2,
    		@RequestParam(value="aop2", required = false) String op2,
    		@RequestParam(value="aval2", required = false) String val2,
    		@RequestParam(value="aconId3", required = false) String conId3,
    		@RequestParam(value="aname3", required = false) String name3,
    		@RequestParam(value="atype3", required = false) String type3,
    		@RequestParam(value="aop3", required = false) String op3,
    		@RequestParam(value="aval3", required = false) String val3,
    		@RequestParam(value="aconId4", required = false) String conId4,
    		@RequestParam(value="aname4", required = false) String name4,
    		@RequestParam(value="atype4", required = false) String type4,
    		@RequestParam(value="aop4", required = false) String op4,
    		@RequestParam(value="aval4", required = false) String val4,
    		@RequestParam(value="aconId5", required = false) String conId5,
    		@RequestParam(value="aname5", required = false) String name5,
    		@RequestParam(value="atype5", required = false) String type5,
    		@RequestParam(value="aop5", required = false) String op5,
    		@RequestParam(value="aval5", required = false) String val5,
    		@RequestParam(value="aconId6", required = false) String conId6,
    		@RequestParam(value="aname6", required = false) String name6,
    		@RequestParam(value="atype6", required = false) String type6,
    		@RequestParam(value="aop6", required = false) String op6,
    		@RequestParam(value="aval6", required = false) String val6,
    		@RequestParam(value="aconId7", required = false) String conId7,
    		@RequestParam(value="aname7", required = false) String name7,
    		@RequestParam(value="atype7", required = false) String type7,
    		@RequestParam(value="aop7", required = false) String op7,
    		@RequestParam(value="aval7", required = false) String val7,
    		@RequestParam(value="aconId8", required = false) String conId8,
    		@RequestParam(value="aname8", required = false) String name8,
    		@RequestParam(value="atype8", required = false) String type8,
    		@RequestParam(value="aop8", required = false) String op8,
    		@RequestParam(value="aval8", required = false) String val8,
    		@RequestParam(value="aconId9", required = false) String conId9,
    		@RequestParam(value="aname9", required = false) String name9,
    		@RequestParam(value="atype9", required = false) String type9,
    		@RequestParam(value="aop9", required = false) String op9,
    		@RequestParam(value="aval9", required = false) String val9
    	)
    {
    	Integer reportId = -1;
    	if (selectId != null){
    		reportId = Integer.parseInt(selectId);
    	}
    	map.addAttribute("selectId", reportId);
    	map.addAttribute("fromDate", fromDate);
    	map.addAttribute("toDate", toDate);
    	map.addAttribute("minAge", minAge);
    	map.addAttribute("maxAge", maxAge);    	
    	String[][]parameters = new String[9][5];
    	
    	//Populating data store with the changes
    	if (conId1 != null && conId1 != ""){    		
    		//populating the provided details
    		parameters[0][0] = conId1;
    		parameters[0][1] = name1;
    		parameters[0][2] = type1;
    		parameters[0][3] = op1;
    		parameters[0][4] = val1;
        	//map.addAttribute("testNameValue", name1);
    	}
    	if (conId2 != null && conId2 != ""){
    		//populating the provided details		
    		parameters[1][0] = conId2;
    		parameters[1][1] = name2;
    		parameters[1][2] = type2;
    		parameters[1][3] = op2;
    		parameters[1][4] = val2;
    	}
    	if (conId3 != null && conId3 != ""){
    		//populating the provided details		
    		parameters[2][0] = conId3;
    		parameters[2][1] = name3;
    		parameters[2][2] = type3;
    		parameters[2][3] = op3;
    		parameters[2][4] = val3;
    	}
    	if (conId4 != null && conId4 != ""){
    		//populating the provided details		
    		parameters[3][0] = conId4;
    		parameters[3][1] = name4;
    		parameters[3][2] = type4;
    		parameters[3][3] = op4;
    		parameters[3][4] = val4;
    	}
    	if (conId5 != null && conId5 != ""){
    		//populating the provided details		
    		parameters[4][0] = conId5;
    		parameters[4][1] = name5;
    		parameters[4][2] = type5;
    		parameters[4][3] = op5;
    		parameters[4][4] = val5;
    	}
    	if (conId6 != null && conId6 != ""){
    		//populating the provided details		
    		parameters[5][0] = conId6;
    		parameters[5][1] = name6;
    		parameters[5][2] = type6;
    		parameters[5][3] = op6;
    		parameters[5][4] = val6;
    	}
    	if (conId7 != null && conId7 != ""){
    		//populating the provided details		
    		parameters[6][0] = conId7;
    		parameters[6][1] = name7;
    		parameters[6][2] = type7;
    		parameters[6][3] = op7;
    		parameters[6][4] = val7;
    	}
    	if (conId8 != null && conId8 != ""){
    		//populating the provided details		
    		parameters[7][0] = conId8;
    		parameters[7][1] = name8;
    		parameters[7][2] = type8;
    		parameters[7][3] = op8;
    		parameters[7][4] = val8;
    	}
    	if (conId9 != null && conId9 != ""){
    		//populating the provided details		
    		parameters[8][0] = conId9;
    		parameters[8][1] = name9;
    		parameters[8][2] = type9;
    		parameters[8][3] = op9;
    		parameters[8][4] = val9;
    	}
    	

    	map.addAttribute("parameters", parameters);
		map.addAttribute("scripts",GroovyUtil.getService().getAllScripts());
		map.addAttribute("output",null);
    	map.addAttribute("error",null);
    	log.info("got groovy reports");
    	
        return view;
    }
    
    @RequestMapping(params = "add", value = "/module/groovy/customReportForm", method = RequestMethod.POST)
    public String addConcept(
    		ModelMap map,
        	@RequestParam(value="id",required=true) Integer id,
        	@RequestParam(value="conceptField",required=false) String conceptFieldString,
    		@RequestParam(value="fromDate",required = false) String fromDate,
    		@RequestParam(value="toDate",required = false) String toDate,
    		@RequestParam(value="minAge",required = false) String minAge,
    		@RequestParam(value="maxAge",required = false) String maxAge,
    		@RequestParam(value="conId1", required = false) String conId1,
    		@RequestParam(value="name1", required = false) String name1,
    		@RequestParam(value="type1", required = false) String type1,
    		@RequestParam(value="op1", required = false) String op1,
    		@RequestParam(value="val1", required = false) String val1,
    		@RequestParam(value="conId2", required = false) String conId2,
    		@RequestParam(value="name2", required = false) String name2,
    		@RequestParam(value="type2", required = false) String type2,
    		@RequestParam(value="op2", required = false) String op2,
    		@RequestParam(value="val2", required = false) String val2,
    		@RequestParam(value="conId3", required = false) String conId3,
    		@RequestParam(value="name3", required = false) String name3,
    		@RequestParam(value="type3", required = false) String type3,
    		@RequestParam(value="op3", required = false) String op3,
    		@RequestParam(value="val3", required = false) String val3,
    		@RequestParam(value="conId4", required = false) String conId4,
    		@RequestParam(value="name4", required = false) String name4,
    		@RequestParam(value="type4", required = false) String type4,
    		@RequestParam(value="op4", required = false) String op4,
    		@RequestParam(value="val4", required = false) String val4,
    		@RequestParam(value="conId5", required = false) String conId5,
    		@RequestParam(value="name5", required = false) String name5,
    		@RequestParam(value="type5", required = false) String type5,
    		@RequestParam(value="op5", required = false) String op5,
    		@RequestParam(value="val5", required = false) String val5,
    		@RequestParam(value="conId6", required = false) String conId6,
    		@RequestParam(value="name6", required = false) String name6,
    		@RequestParam(value="type6", required = false) String type6,
    		@RequestParam(value="op6", required = false) String op6,
    		@RequestParam(value="val6", required = false) String val6,
    		@RequestParam(value="conId7", required = false) String conId7,
    		@RequestParam(value="name7", required = false) String name7,
    		@RequestParam(value="type7", required = false) String type7,
    		@RequestParam(value="op7", required = false) String op7,
    		@RequestParam(value="val7", required = false) String val7,
    		@RequestParam(value="conId8", required = false) String conId8,
    		@RequestParam(value="name8", required = false) String name8,
    		@RequestParam(value="type8", required = false) String type8,
    		@RequestParam(value="op8", required = false) String op8,
    		@RequestParam(value="val8", required = false) String val8,
    		@RequestParam(value="conId9", required = false) String conId9,
    		@RequestParam(value="name9", required = false) String name9,
    		@RequestParam(value="type9", required = false) String type9,
    		@RequestParam(value="op9", required = false) String op9,
    		@RequestParam(value="val9", required = false) String val9
    	)
    {

    	map.addAttribute("selectId", id);
    	String[][]parameters = new String[9][5];    	
    	

    	//Populating data store with the changes
    	if (conId1 != null && conId1 != ""){    		
    		//populating the provided details
    		parameters[0][0] = conId1;
    		parameters[0][1] = name1;
    		parameters[0][2] = type1;
    		parameters[0][3] = op1;
    		parameters[0][4] = val1;
        	//map.addAttribute("testNameValue", name1);
    	}
    	if (conId2 != null && conId2 != ""){
    		//populating the provided details		
    		parameters[1][0] = conId2;
    		parameters[1][1] = name2;
    		parameters[1][2] = type2;
    		parameters[1][3] = op2;
    		parameters[1][4] = val2;
    	}
    	if (conId3 != null && conId3 != ""){
    		//populating the provided details		
    		parameters[2][0] = conId3;
    		parameters[2][1] = name3;
    		parameters[2][2] = type3;
    		parameters[2][3] = op3;
    		parameters[2][4] = val3;
    	}
    	if (conId4 != null && conId4 != ""){
    		//populating the provided details		
    		parameters[3][0] = conId4;
    		parameters[3][1] = name4;
    		parameters[3][2] = type4;
    		parameters[3][3] = op4;
    		parameters[3][4] = val4;
    	}
    	if (conId5 != null && conId5 != ""){
    		//populating the provided details		
    		parameters[4][0] = conId5;
    		parameters[4][1] = name5;
    		parameters[4][2] = type5;
    		parameters[4][3] = op5;
    		parameters[4][4] = val5;
    	}
    	if (conId6 != null && conId6 != ""){
    		//populating the provided details		
    		parameters[5][0] = conId6;
    		parameters[5][1] = name6;
    		parameters[5][2] = type6;
    		parameters[5][3] = op6;
    		parameters[5][4] = val6;
    	}
    	if (conId7 != null && conId7 != ""){
    		//populating the provided details		
    		parameters[6][0] = conId7;
    		parameters[6][1] = name7;
    		parameters[6][2] = type7;
    		parameters[6][3] = op7;
    		parameters[6][4] = val7;
    	}
    	if (conId8 != null && conId8 != ""){
    		//populating the provided details		
    		parameters[7][0] = conId8;
    		parameters[7][1] = name8;
    		parameters[7][2] = type8;
    		parameters[7][3] = op8;
    		parameters[7][4] = val8;
    	}
    	if (conId9 != null && conId9 != ""){
    		//populating the provided details		
    		parameters[8][0] = conId9;
    		parameters[8][1] = name9;
    		parameters[8][2] = type9;
    		parameters[8][3] = op9;
    		parameters[8][4] = val9;
    	}
    	
    	
    	//Converting String into integer
    	//tracking concept field acquisition and insertion into the dataset
    	String conceptField = "";
    	boolean popped = true; //dual-purpose boolean flag
    	if (conceptFieldString != null && conceptFieldString != ""){
    		popped = false; 
    		conceptField = conceptFieldString;
    	}
    	String addOn = "?afromDate="+fromDate+"&atoDate="+toDate+"&aminAge="+minAge+"&amaxAge="+maxAge+"&";
    	//addOn contains address line parameters, hence initiating with the question mark
    	Integer max = 9;//Defines maximum amount of elements to iterate through. Cannot be more than 9.
    	
    	//iterating through the dataset elements and adding the extra element if there is room left
    	for (int i = 1; i <= max; i++){
    		if(parameters[i-1][0] != null && parameters[i-1][0] != ""){
    			addOn += "aconId" + Integer.toString(i) + "=" + parameters[i-1][0];
	    		addOn += "&aname" + Integer.toString(i) + "=" + parameters[i-1][1];
	    		addOn += "&atype" + Integer.toString(i) + "=" + parameters[i-1][2];
	    		addOn += "&aop" + Integer.toString(i) + "=" + parameters[i-1][3];
	    		addOn += "&aval" + Integer.toString(i) + "=" + parameters[i-1][4];
	    		if (i != max){
	    			addOn += "&";//ensuring that ampersand is not appended at the end of the address line
	    		}
    		}else if(!popped){//if conceptField is not null and not added then add concept to the dataset
    			Concept temp = OpenmrsUtil.getConceptByIdOrName(conceptField);
    			
    			addOn += "aconId" + Integer.toString(i) + "=" + conceptField;
	    		addOn += "&aname" + Integer.toString(i) + "=" + temp.getName().getName();
	    		addOn += "&atype" + Integer.toString(i) + "=" + temp.getDatatype().getConceptDatatypeId();
	    		//addOn += "&op" + Integer.toString(i) + "=" + "";
	    		//addOn += "&val" + Integer.toString(i) + "=" + "";
	    		popped = true;
	    		if (i != max){
	    			addOn += "&";//ensuring that ampersand is not appended at the end of the address line
	    		}
    		}
    	}
    	
    	map.addAttribute("parameters", parameters);
		map.addAttribute("scripts",GroovyUtil.getService().getAllScripts());
		map.addAttribute("output",null);
    	map.addAttribute("error",null);

    	log.info("got groovy reports");
    	    	
    	return "redirect:" + "/module/groovy/CustomReport.form" + addOn;
    }

    
    
    
    @RequestMapping(params = "execute", method = RequestMethod.POST)
    public String runReport(
	    	@RequestParam(value="id",required=true) Integer id,
	    	@RequestParam(value="fromDate",required=true) String fromDate,
	    	@RequestParam(value="toDate",required=true) String toDate,
	    	@RequestParam(value="minAge",required=false) String minAge,
	    	@RequestParam(value="maxAge",required=false) String maxAge,
	    	ModelMap map,
			@RequestParam(value="conId1", required = false) String conId1,
			@RequestParam(value="name1", required = false) String name1,
			@RequestParam(value="type1", required = false) String type1,
			@RequestParam(value="op1", required = false) String op1,
			@RequestParam(value="val1", required = false) String val1,
			@RequestParam(value="conId2", required = false) String conId2,
			@RequestParam(value="name2", required = false) String name2,
			@RequestParam(value="type2", required = false) String type2,
			@RequestParam(value="op2", required = false) String op2,
			@RequestParam(value="val2", required = false) String val2,
			@RequestParam(value="conId3", required = false) String conId3,
			@RequestParam(value="name3", required = false) String name3,
			@RequestParam(value="type3", required = false) String type3,
			@RequestParam(value="op3", required = false) String op3,
			@RequestParam(value="val3", required = false) String val3,
			@RequestParam(value="conId4", required = false) String conId4,
			@RequestParam(value="name4", required = false) String name4,
			@RequestParam(value="type4", required = false) String type4,
			@RequestParam(value="op4", required = false) String op4,
			@RequestParam(value="val4", required = false) String val4,
			@RequestParam(value="conId5", required = false) String conId5,
			@RequestParam(value="name5", required = false) String name5,
			@RequestParam(value="type5", required = false) String type5,
			@RequestParam(value="op5", required = false) String op5,
			@RequestParam(value="val5", required = false) String val5,
			@RequestParam(value="conId6", required = false) String conId6,
			@RequestParam(value="name6", required = false) String name6,
			@RequestParam(value="type6", required = false) String type6,
			@RequestParam(value="op6", required = false) String op6,
			@RequestParam(value="val6", required = false) String val6,
			@RequestParam(value="conId7", required = false) String conId7,
			@RequestParam(value="name7", required = false) String name7,
			@RequestParam(value="type7", required = false) String type7,
			@RequestParam(value="op7", required = false) String op7,
			@RequestParam(value="val7", required = false) String val7,
			@RequestParam(value="conId8", required = false) String conId8,
			@RequestParam(value="name8", required = false) String name8,
			@RequestParam(value="type8", required = false) String type8,
			@RequestParam(value="op8", required = false) String op8,
			@RequestParam(value="val8", required = false) String val8,
			@RequestParam(value="conId9", required = false) String conId9,
			@RequestParam(value="name9", required = false) String name9,
			@RequestParam(value="type9", required = false) String type9,
			@RequestParam(value="op9", required = false) String op9,
			@RequestParam(value="val9", required = false) String val9
		)
    {

    	map.addAttribute("selectId", id);
    	map.addAttribute("fromDate", fromDate);
    	map.addAttribute("toDate", toDate);
    	map.addAttribute("minAge", minAge);
    	map.addAttribute("maxAge", maxAge); 
    	
    	String[][]parameters = new String[9][5];    	
    	//Populating data store with the changes
    	if (conId1 != null && conId1 != ""){    		
    		//populating the provided details
    		parameters[0][0] = conId1;
    		parameters[0][1] = name1;
    		parameters[0][2] = type1;
    		parameters[0][3] = op1;
    		parameters[0][4] = val1;
        	//map.addAttribute("testNameValue", name1);
    	}
    	if (conId2 != null && conId2 != ""){
    		//populating the provided details		
    		parameters[1][0] = conId2;
    		parameters[1][1] = name2;
    		parameters[1][2] = type2;
    		parameters[1][3] = op2;
    		parameters[1][4] = val2;
    	}
    	if (conId3 != null && conId3 != ""){
    		//populating the provided details		
    		parameters[2][0] = conId3;
    		parameters[2][1] = name3;
    		parameters[2][2] = type3;
    		parameters[2][3] = op3;
    		parameters[2][4] = val3;
    	}
    	if (conId4 != null && conId4 != ""){
    		//populating the provided details		
    		parameters[3][0] = conId4;
    		parameters[3][1] = name4;
    		parameters[3][2] = type4;
    		parameters[3][3] = op4;
    		parameters[3][4] = val4;
    	}
    	if (conId5 != null && conId5 != ""){
    		//populating the provided details		
    		parameters[4][0] = conId5;
    		parameters[4][1] = name5;
    		parameters[4][2] = type5;
    		parameters[4][3] = op5;
    		parameters[4][4] = val5;
    	}
    	if (conId6 != null && conId6 != ""){
    		//populating the provided details		
    		parameters[5][0] = conId6;
    		parameters[5][1] = name6;
    		parameters[5][2] = type6;
    		parameters[5][3] = op6;
    		parameters[5][4] = val6;
    	}
    	if (conId7 != null && conId7 != ""){
    		//populating the provided details		
    		parameters[6][0] = conId7;
    		parameters[6][1] = name7;
    		parameters[6][2] = type7;
    		parameters[6][3] = op7;
    		parameters[6][4] = val7;
    	}
    	if (conId8 != null && conId8 != ""){
    		//populating the provided details		
    		parameters[7][0] = conId8;
    		parameters[7][1] = name8;
    		parameters[7][2] = type8;
    		parameters[7][3] = op8;
    		parameters[7][4] = val8;
    	}
    	if (conId9 != null && conId9 != ""){
    		//populating the provided details		
    		parameters[8][0] = conId9;
    		parameters[8][1] = name9;
    		parameters[8][2] = type9;
    		parameters[8][3] = op9;
    		parameters[8][4] = val9;
    	}
    	
    	//Providing the dataset to jsp file
    	map.addAttribute("parameters", parameters);
    	
    	if(minAge==null || minAge=="")
    	{
    		log.info("Setting minAge to 0");
    		minAge="0";
    	}

    	if(maxAge==null || maxAge=="")
    	{
    		log.info("Setting maxAge to 999");
    		maxAge="999";
    	}
    	
    	log.info("Getting groovy script");
    	map.addAttribute("scripts",GroovyUtil.getService().getAllScripts());
    	if(id==-1)
    		return view;
    	GroovyScript script = GroovyUtil.getService().getScriptById(id);
    	if(script==null)
    		return view;
    	

    	log.info("Verifying appropriate rights and location");
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
		
    	log.info("Building groovy script");
    	String scriptString="def fromDate='"+fromDate+"'\n"+
			"def toDate='"+toDate+"'\n"+
			"def location='"+location+"'\n"+
			"def minAge='"+minAge+"'\n"+
			"def maxAge='"+maxAge+"'\n"+
			script.getScript();
    	
    	String addIn = "";
    	for (Integer i=1; i<=9; i++){
    		if(parameters[i-1][0] != null && parameters[i-1][0] != ""){
    			addIn += "def conId"+i.toString()+"='"+parameters[i-1][0]+"'\n" +
    				"def name"+i.toString()+"='"+parameters[i-1][1]+"'\n" +
    				"def type"+i.toString()+"='"+parameters[i-1][2]+"'\n" +
    				"def op"+i.toString()+"='"+parameters[i-1][3]+"'\n" +
    				"def val"+i.toString()+"='"+parameters[i-1][4]+"'\n";
    		}
    	}
    	
    	scriptString = addIn + scriptString;
    	
    	try
    	{
			DWRGroovyService svc=new DWRGroovyService();
	    	log.info("Executing groovy script");
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
