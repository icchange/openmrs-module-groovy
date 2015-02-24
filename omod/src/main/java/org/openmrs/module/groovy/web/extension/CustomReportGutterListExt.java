package org.openmrs.module.groovy.web.extension;

import org.openmrs.module.web.extension.LinkExt;


public class CustomReportGutterListExt extends LinkExt {

	public String getLabel() {
		return "groovy.title.gutter.custom";
	}

	public String getUrl() {
		return "module/groovy/CustomReport.form";
	}

	public String getRequiredPrivilege() {
		return "Run General Reports";
	}

}
