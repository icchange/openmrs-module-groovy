package org.openmrs.module.groovy.web.controller;

import java.util.List;

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


@Controller
@RequestMapping("/module/groovy/user.list")
public class GroovyUserListController {

  /**
     * Logger for this class
     */
    protected final Log log = LogFactory.getLog(getClass());
    private String view = "/module/groovy/groovyUserList";

    public GroovyUserListController() {
    	
    }

    @RequestMapping(method = RequestMethod.GET)
    public String listUsers() {
    	log.info("get list groovy users");
        return view;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String actions(
    	@RequestParam(value="id",required=true) Integer id,
    	@RequestParam(value="locationid",required=false) Integer locationid,
		@RequestParam(value="type",required=true) String type,
    	final ModelMap map)
    {
    	if(type.equals("user.delete"))
		{
			GroovyUser user = GroovyUtil.getService().getUser(id);
	    	if(user != null) {
	        	GroovyUtil.getService().deleteUser(user);
	        }
		}
		else if(type.equals("user.add") && locationid>=0 && id>=0)
		{
			User newUser=Context.getUserService().getUser(id);
			if(newUser==null)
				throw new IllegalArgumentException("invalid id");
			Location newLocation=Context.getLocationService().getLocation(locationid);
			if(newLocation==null)
				throw new IllegalArgumentException("invalid locationid");

			List<GroovyUser> users = GroovyUtil.getService().getAllUsers();
			for (int i=0; i<users.size(); i++)
	    	{
	    		GroovyUser tempUser=users.get(i);
	    		if(tempUser.getUser().getId()==newUser.getId())
	    			throw new IllegalArgumentException("Groovy User already exists. Either choose a different user or delete existing Groovy User.");		
	    	}
	    	GroovyUser newGroovyUser=new GroovyUser();
	    	newGroovyUser.setLocation(newLocation);
	    	newGroovyUser.setUser(newUser);
	    	GroovyUtil.getService().saveUser(newGroovyUser);	
		}

		map.addAttribute("users",GroovyUtil.getService().getAllUsers());
		return view;
    }

    @ModelAttribute("users")
    List<GroovyUser> populateUsers() {
        return GroovyUtil.getService().getAllUsers();
    }

    @ModelAttribute("systemusers")
    List<User> populateSystemUsers() {
    	List<User> users=Context.getUserService().getAllUsers();
    	for (int i=0; i<users.size(); i++)
    	{
    		if(users.get(i).getUsername()==null || users.get(i).getUsername().length()==0)
    		{
    			users.remove(i);
    			i--;
    		}
    	}
        return users;
    }	
}
