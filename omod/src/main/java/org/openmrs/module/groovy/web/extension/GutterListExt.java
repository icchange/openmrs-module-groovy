package org.openmrs.module.groovy.web.extension;

import org.openmrs.module.web.extension.LinkExt;


public class GutterListExt extends LinkExt {

	public String getLabel() {
		return "groovy.title.gutter";
	}

	public String getUrl() {
		return "module/groovy/report.form";
	}

	public String getRequiredPrivilege() {
		return "Run Groovy Reports";
	}

}
