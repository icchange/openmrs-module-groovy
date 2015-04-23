/* FORM MOH 717 *************************************
* 
* Author :  Alejandro Ramirez
* Dates  :  3/3/2015
* 
* Description:
* This groovyscript creates a HTML report based on the MOH 717 form. 
* Original code taken from 705A/B
*/


/*** TODO ***
	1) Fix the query for prescriptions -- the location is not accurate
	2) When available, expand Section A based on visit types. 
	3) When available, visits would also have extra info for inpatients. 
*/

//ARS: Helper functions -- these functions should be located in a common library.  


/**** getEncounterNumberbyLocationAndDateRange 
	* Author: ARS
	* Description: This function returns simply the number of valid encounters
	*				per date range
	*
	*/

def getEncounterNumberbyLocationAndDateRange(location, fromDate, toDate) {
	def numEncountersQuery = """
	SELECT COUNT(*)
	FROM
		encounter, location
	WHERE	
		encounter.location_id = location.location_id AND
		location.name = '${location}' AND
		encounter.date_created >= DATE('${fromDate}') AND
		encounter.date_created <= DATE('${toDate}') 
	"""
	numEncountersList = admin.executeSQL(numEncountersQuery,false)	
	return numEncountersList[0][0]
}
	
/**** getMaternityOutcomeByLocationAndDateRange 
	* Author: ARS
	* Description: This function returns all the maternity information based
	*				on two concepts: pregnancy outcome and method of delivery
	*
	*/

def getMaternityOutcomeByLocationAndDateRange(location, fromDate, toDate) {
	def maternityQuery = """
	SELECT obs.value_coded
	FROM 
		visit, encounter, obs, location
	WHERE
		location.name = '${location}' and	
		location.location_id = visit.location_id and
		visit.visit_id = encounter.visit_id and
		encounter.encounter_id = obs.encounter_id and
		obs.date_created >= DATE('${fromDate}') and
		obs.date_created <= DATE('${toDate}') and
		obs.voided = false and	
		encounter.voided = false and
		visit.voided = false and
		obs.voided = false and
		obs.concept_id IN (161033, 5630)
	GROUP BY
		visit.visit_id
	"""
	maternityQueryList = admin.executeSQL(maternityQuery,false)	
	return maternityQueryList
}

/** *** getPrescriptionsByLocationDateRange
	* Authors: ARS
	* Description: This function returns the prescriptions
	*				during a given date range and location
	*
*/
def getPrescriptionsByLocationDateRange(location, fromDate, toDate) {
	def prescriptionsQuery = """
	SELECT
		p.birthdate, cs.concept_set
	FROM
		orders o
		INNER JOIN drug_order do ON (o.order_id = do.order_id)
		INNER JOIN drug d ON (do.drug_inventory_id = d.drug_id)
		INNER JOIN concept c ON (d.concept_id = c.concept_id)
		INNER JOIN person p ON (p.person_id = o.patient_id)
		INNER JOIN user_property up ON (o.creator = up.user_id)
		INNER JOIN location l ON (up.property_value = l.location_id) 
		LEFT JOIN concept_set cs ON (c.concept_id = cs.concept_id)
	WHERE 
		o.date_created >= DATE('${fromDate}') AND
		o.date_created <= DATE('${toDate}') AND 
		o.voided = false AND
		up.property = 'defaultLocation' AND 
		l.name = '${location}'
	GROUP BY	
		o.order_id
	ORDER BY
		d.name ASC
	""" 
	prescriptionsList = admin.executeSQL(prescriptionsQuery,false)	
	return prescriptionsList
}


/*
* *** getAttendancesByLocationAndDate
* Author: ARS
* Description: 	For reporting purposes, this function returns a table
* 				with all the information regarding a patient visit.
* Arguments: 
*				Location - clinic
*				fromDate - YYYY/MM/DD
*				toDate -- YYYY/MM/DD
*				
*/

def getAttendancesByLocationAndDate(location, fromDate, toDate) {
	def attendancesQuery="""
	SELECT
		visit_type.name,
		person.gender as Gender,
		person.birthdate as Bday,
		1 as newAttendanceFlag
	FROM
		person, location, person_name,  visit, visit_type
	WHERE
		location.name='${location}'
		and visit.location_id = location.location_id
		and visit.voided = false
		and person.person_id = visit.patient_id
		and visit.date_started >= DATE('${fromDate}')
		and visit.date_started <= DATE('${toDate}') 
		and person.person_id = person_name.person_id
		and visit.visit_type_id = visit_type.visit_type_id
		AND visit.patient_id NOT IN 
		(SELECT 
			v1.patient_id
		FROM
			visit v1
		WHERE 
			v1.patient_id = visit.patient_id AND visit.date_started < v1.date_started)
	UNION ALL
	SELECT
		visit_type.name,
		person.gender as Gender,
		person.birthdate as Bday,
		0 as newAttendanceFlag
	FROM
		person, location, person_name,  visit, visit_type
	WHERE
		location.name='${location}'
		and visit.location_id = location.location_id
		and visit.voided = false
		and person.person_id = visit.patient_id
		and visit.date_started >= DATE('${fromDate}')
		and visit.date_started <= DATE('${toDate}') 
		and person.person_id = person_name.person_id
		and visit.visit_type_id = visit_type.visit_type_id
		AND visit.patient_id IN 
		(SELECT 
			v1.patient_id
		FROM
			visit v1
		WHERE 
			v1.patient_id = visit.patient_id AND visit.date_started < v1.date_started)
	"""
	attendances = admin.executeSQL(attendancesQuery,false)
	return attendances; 
}

/*
* *** printTable
* Author: ARS
* Description: Prints a HTML table. Each row is an HTML row. 
* Each row is of length numColumns. The extra column has a flag
* to determine if the row is highlighted or not. 
*/

def printTable(dataTable, numColumns){
	writer = new StringWriter()  
	b = new MarkupBuilder(writer)
	b.table(border: 1, width: "100%") {
		for(row in dataTable) {
			def colour = "#FFFFFF"
			def fontStyle = ""
			if (row[numColumns]==1) { 
				colour = "#1AAC9B"
			}
			if (row[numColumns]==2) {
				fontStyle = "font-weight:bold"; 
				colour = "#F0F0F5" 
			}
			tr(bgcolor: colour, style: fontStyle) {
				for (i = 0; i< numColumns; i++) {
					td(row[i]);
				}
			} 
		}
	}
	println writer

}  

/* *** getGregorianCalendar 
*  Author: ARS
*  Description: Gets a gregorian calendar object to handle dates from a string. 
*  Note that Date() is deprecated and can produce funny results, especially
*  when using .getYear()
*/

def getGregorianCalendar(strDateFormat, strDate) {
	def calendar = new GregorianCalendar()
	def referenceDate = new Date().parse(strDateFormat, strDate)
	calendar.time = referenceDate
	return calendar; 
}


debug = true

def monthNames = [ "January", "February", "March", "April", "May", "June",
"July", "August", "September", "October", "November", "December" ];
print "<h1><center>Form MOH 717</center></h1><br>"

//ARS: END OF Helper functions -- these functions should be located in a common library.  ^^^^ 



//////////////////////////////////////////////////////////////////////////////
import org.codehaus.groovy.runtime.TimeCategory
import groovy.xml.MarkupBuilder

use(TimeCategory)
{
	//We use these variables to fiddle with the code/data
	//	when in debug mode. For production, debug must be
	//	set to 0. 
	if (debug) {
		fromDate = "2015-03-1"
		toDate = "2015-03-31"
		location = "AMREF"
	}
	//
	def strDateFormat = "yyyy-M-d";
	def fromDateGC = getGregorianCalendar(strDateFormat, fromDate);
	fromDateGC.set(Calendar.DAY_OF_MONTH, 1); //First day of the month
	def referenceDate = new GregorianCalendar();
	def referenceDate14yo = new GregorianCalendar();
	referenceDate14yo.setTime(fromDateGC.getTime()-14.years);
	def toDateTempGC = getGregorianCalendar(strDateFormat, toDate);
	toDateTempGC.set(Calendar.MONTH, toDateTempGC.get(Calendar.MONTH)+1); 
	toDateTempGC.set(Calendar.DAY_OF_MONTH, 0); 
	def toDateAdjusted= toDateTempGC.getTime().format(strDateFormat)
	def fromDateAdjusted= fromDateGC.getTime().format(strDateFormat)
	
	//We need to check if the range is only one month since
	//	MOH 717 is a monthly report. 
	
	if ((fromDateGC.get(Calendar.YEAR)!=toDateTempGC.get(Calendar.YEAR) ||
	    (fromDateGC.get(Calendar.MONTH)!=toDateTempGC.get(Calendar.MONTH)))) {
		println "<h3><center><b>Error:</b> MOH 717 requires you to select only one month!</center></h3>"
		return;
	}

	print "<center><table border=1>"
	print "<tr><td width=\"150px\"><b>Clinic</b></td><td width=\"150px\">" + location + "</td><td width=\"150px\"><b>Month</b></td><td width=\"150px\">"+monthNames[toDateTempGC.get(Calendar.MONTH)]+ " " +  toDateTempGC.get(Calendar.YEAR)  +"</td></tr>"
	print "</center></table><br><hr>"
	print "<center>Note: values shown below are for the entire month.</center>"

	
	//For the prescriptions query, there is a number of predefined "sets"
	//in the database. These concepts/sets are the following: 
	/*
	Antibiotic Class - 162145
	Anti-malarial Class - 162146
	Anti-fungalClass - 162147
	Anti-TB class - 162148
	PJP Medication Class - 162149
	Anti-Parasitic Class - 162150
	Anti-viral (Not ARV) Class - 162151
	Antiretroviral Drugs -1085 
	
	TODO: Clarify which ones are antibiotics
	AOTM these are the broader categories: 
	** Antibiotics: anti-fungal, anti-TB, anti-malarial, antibiotics. 
	** Special: PJP, anti-parasistic, anti-viral, and anti-retroviral. 
	** Common: All other drugs not in the above categories administed to 14+ y.o. 
	** Children: All other drugs administered to children.
		* Type 0: Common 
		* Type 1: Antibiotics
		* Type 2: Special
	*/
	
	def conceptIDAntibioticsDrugs = [162145, 162146, 162147, 162148];
	def conceptIDSpecialDrugs = [162149, 162150, 162151, 1085];
	
	prescriptionsList = getPrescriptionsByLocationDateRange(location, fromDateAdjusted, toDateAdjusted);
	
	attendances = getAttendancesByLocationAndDate(location, fromDateAdjusted, toDateAdjusted); 
	
	maternityList = getMaternityOutcomeByLocationAndDateRange(location, fromDateAdjusted, toDateAdjusted);
	
	numExaminations = getEncounterNumberbyLocationAndDateRange(location, fromDateAdjusted, toDateAdjusted);
	////////////////////////////////////////////////////////////////////////////
	//Process Data
	//General table skeleton
	dataTableOutpatients = [ // format : col1 , col2, col3, col4, colour_flag 
	["A. GENERAL OUTPATIENT (FILTER CLINICS)", "NEW PATIENTS", "RE-ATTENDANCES", "TOTAL",1],
	["A.1.1 Over 5 - Male ", 0, 0, 0,0],
	["A.1.2 Over 5 - Female ", 0, 0, 0,0],
	["A.1.3 Children Under 5 - Male ", 0, 0, 0,0],
	["A.1.4 Children Under 5 - Female ", 0, 0, 0,0],
	["A.1.5 TOTAL GENERAL OUTPATIENTS ", 0, 0, 0,2],
	["A.2 TOTAL CASUALTY", 0, 0, 0,2],
	["A.3 SPECIAL CLINIC", "", "", "",1],
	["A.3.1 E.N.T. Clinic", 0, 0, 0,0],
	["A.3.2 Eye Clinic", 0, 0, 0,0],
	["A.3.3 TB and Leprosy ", 0, 0, 0,0],
	["A.3.4 Sexually Transmitted Infections", 0, 0, 0,0],
	["A.3.5 Psychiatry ", 0, 0, 0,0],
	["A.3.6 Orthopaedic Clinic", 0, 0, 0,0],
	["A.3.7 All other Special Clinics", 0, 0, 0,0],
	["A.3.8 TOTAL SPECIAL CLINICS ", 0, 0, 0,2],
	["A.4 MCH/FP CLIENTS", "", "", "",1],
	["A.4.1 CWC Attendances", 0, 0, 0,0],
	["A.4.2 ANC Attendances", 0, 0, 0,0],
	["A.4.3 PNC Attendances", 0, 0, 0,0],
	["A.4.4 FP Attendances", 0, 0, 0,0],
	["A.4.5 TOTAL MCH/FP", 0, 0, 0,2],
	["A.5 DENTAL CLINIC ", "", "", "",1],
	["A.5.1 Attendances (excluding fillings and extractions)", 0, 0, 0,0],
	["A.5.2 Fillings", 0, 0, 0,0],
	["A.5.3 Extractions", 0, 0, 0,0],
	["A.5.4 TOTAL DENTAL SERVICES", 0, 0, 0,2],
	["A.6 TOTAL OUTPATIENT SERVICES", 0, 0, 0,2],
	["A.7 MEDICAL EXAMINATIONS", 0, "A.10 INJECTIONS", 0,1],
	["A.8 MEDICAL REPORTS", 0, "A.11 STICHING",0,1],
	["A.9 DRESSINGS", 0, "P.O.P.", 0,1],
	]; //End of dataTableOutpatients

	dataTableInpatients = [ // format : col1 , col2, col3, col4, col5, col5, colour_flag 
	["B.1 INPATIENTS", "GENERAL ADULTS", "GENERAL PEDIATRICS", "MATERNITY MOTHERS","AMENITY","TOTAL",1],
	["B.1.1 Discharges ", 0, 0, 0, 0, 0, 0],
	["B.1.2 Deaths ", 0, 0, 0, 0, 0, 0],
	["B.1.3 Abscondees ", 0, 0, 0, 0, 0, 0],
	["B.1.4 TOTAL DISCHARGES, DEATHS, ETC", 0, 0, 0, 0, 0, 2],
	["B.1.9 Admissions ", 0, 0, 0, 0, 0, 0],
	["B.1.10 Paroles ", 0, 0, 0, 0, 0, 0],
	["B.1.11 Occupied Bed Days - NHIF", 0, 0, 0, 0, 0, 0],
	["B.1.11a Occupied Bed Days - Non NHIF ", 0, 0, 0, 0, 0, 0],
	["B.1.12 Well Person Days", 0, 0, 0, 0, 0, 0],
	["B.1.5 Beds - Authorized", 0, 0, 0, 0, 0, 0],
	["B.1.6 Beds - Actual Physical", 0, 0, 0, 0, 0, 0],
	["B.1.7 Cots - Authorized", 0, 0, 0, 0, 0, 0],
	["B.1.8 Cots - Actual Physical", 0, 0, 0, 0, 0, 0],
	]; //end of dataTableOutpatients

	dataTableMaternity = [
	["B.2 MATERNITY SERVICES", "TOTAL",1],
	["B.2.1 Vaginal Delivery", 0,0],
	["B.2.2 Caesarian Sections", 0,0],
	["B.2.3 Fresh Still Birth", 0,0],
	["B.2.4 Macerate Still Birth", 0,0],
	]; //dataTableMaternity

	dataTableOperations = [
	["B.3 OPERATIONS", "TOTAL",1],
	["B.3.1 Minor Surgery", 0,0],
	["B.3.2 Circumcision", 0,0],
	["B.3.3 Major Surgery", 0,0],
	]; // dataTableOperations


	dataTablePharmacy = [
	["D. PHARMARCY", "No OF PRESCRIPTIONS",1],
	["D.1 Common Drugs", 0,0],
	["D.2 Antibiotics", 0,0],
	["D.3 Special Drugs", 0,0],
	["D.4 For Children", 0,0],
	]; // dataTablePharmacy

	dataTableMortuaryMedRec = [
	["E. MORTUARY", "TOTAL",1],
	["E.1 Body Days", 0,0],
	["E.2 Embalment", 0,0],
	["E.3 Post-mortem", 0,0],
	["E.4 Unclaimed Body Days", 0,0],
	["F. MEDICAL RECORDS ISSUED", "TOTAL",1],
	["F.1 New Files", 0,0],
	["F.2 Outpatient Records", 0,0],
	]; //dataTableMortuaryMedRec

	dataTableSpecial = [ //format col1 ...col7 colour_flag
	["C.1 Laboratory - No of Tests", "Routine", 0, "Special", 0, "Total", 0, 0],
	["C.2 X-Ray - No of Examinations", "Plain", 0, "Enhancement", 0, "Ultrasound", 0, 0],
	["", "Special (MRI/CT)", 0, "", 0, "Total", 0,0],
	["C.3 Physiotherapy - No of Treatments", "", 0, "Non-private", 0, "", 0, 0],
	["C.4", "Private", 0, "Non-private", 0, "Total", 0, 0],
	["C.5 Orthopaedic Technology", "Private", 0, "Non-private", 0, "Total", 0, 0],
	]; //dataTableSpecial



	// Total attendances and reattendances***********************
	def totalNewCasesRowTotal=0    
	def counter=0
	def indexReAtt=1 //alternates depending on the type
	//Get the data. We are only interested in outpatients and separate by
	//  age and gender (cols 12 and 11 respectively). 
	for(row in attendances) {
		//First, determine if the patient is old or new 
		if(row[3] == 1) 
		{
			indexReAtt = 1 // New patient
		}
		else if(row[3] == 0) 
		{
			indexReAtt = 2 // old patient
		} 
		//now, lets look at the type of patient
		switch (row[0].toString().toUpperCase()) {
			case 'OUTPATIENT': //Sort by gender and age < 5
				if (row[1].toUpperCase() == "M" && row[2] < referenceDate.getTime()) {    
					dataTableOutpatients[1][indexReAtt]++;
					dataTableOutpatients[1][3]++;
				} else if (row[1].toUpperCase() == "M" && row[2] >= referenceDate.getTime()) {    
					dataTableOutpatients[3][indexReAtt]++;
					dataTableOutpatients[3][3]++;
				} else if (row[1].toUpperCase() == "F" && row[2] < referenceDate.getTime()) {    
					dataTableOutpatients[2][indexReAtt]++;
					dataTableOutpatients[2][3]++;
				} else if (row[1].toUpperCase() == "F" && row[2] >= referenceDate.getTime()) {    
					dataTableOutpatients[4][indexReAtt]++;
					dataTableOutpatients[4][3]++;
				}
				break;
			case 'CCC-TB':
				dataTableOutpatients[10][indexReAtt]++;
				dataTableOutpatients[10][3]++;
				break;			
			case 'CCC-HIV':
				dataTableOutpatients[11][indexReAtt]++;
				dataTableOutpatients[11][3]++;
			case 'MATERNITY':
			case 'PEDIATRICS':
			case 'NUTRITION':
				dataTableOutpatients[14][indexReAtt]++;
				dataTableOutpatients[14][3]++;
				break; 
			case 'CWC':
				dataTableOutpatients[17][indexReAtt]++;
				dataTableOutpatients[17][3]++;
				break;
			case 'ANC':
				dataTableOutpatients[18][indexReAtt]++;
				dataTableOutpatients[18][3]++;
				break;
			case 'PNC':
				dataTableOutpatients[19][indexReAtt]++;
				dataTableOutpatients[19][3]++;
				break;
			case 'FAMILY PLANNING':
				dataTableOutpatients[20][indexReAtt]++;
				dataTableOutpatients[20][3]++;
				break;
			default: //Ignore other types
				break;
		}
	}

	// General outpatients total
	//Total new
	dataTableOutpatients[5][1] = dataTableOutpatients[1][1] + dataTableOutpatients[3][1] + dataTableOutpatients[2][1] + dataTableOutpatients[4][1];
	//Re-att total  
	dataTableOutpatients[5][2] = dataTableOutpatients[1][2] + dataTableOutpatients[3][2] + dataTableOutpatients[2][2] + dataTableOutpatients[4][2];
	//Total column  
	dataTableOutpatients[5][3] = dataTableOutpatients[1][3] + dataTableOutpatients[3][3] + dataTableOutpatients[2][3] + dataTableOutpatients[4][3];


	//A.3 Total
	dataTableOutpatients[15][1] = dataTableOutpatients[14][1] + dataTableOutpatients[11][1] + dataTableOutpatients[10][1] ;
	//Re-att total  
	dataTableOutpatients[15][2] = dataTableOutpatients[14][2] + dataTableOutpatients[11][2] + dataTableOutpatients[10][2] ;
	//Total column  
	dataTableOutpatients[15][3] = dataTableOutpatients[14][3] + dataTableOutpatients[11][3] + dataTableOutpatients[10][3] ;

	//A.4 Total
	dataTableOutpatients[21][1] = dataTableOutpatients[20][1] + dataTableOutpatients[19][1] + dataTableOutpatients[18][1] + dataTableOutpatients[17][1];
	//Re-att total  
	dataTableOutpatients[21][2] = dataTableOutpatients[20][2] + dataTableOutpatients[19][2] + dataTableOutpatients[18][2] + dataTableOutpatients[17][2];
	//Total column  
	dataTableOutpatients[21][3] = dataTableOutpatients[20][3] + dataTableOutpatients[19][3] + dataTableOutpatients[18][3] + dataTableOutpatients[17][3];		

	//Grand total outpatient for Section A
	for(i = 1; i <= 3; ++i)
	{
	dataTableOutpatients[27][i] = dataTableOutpatients[26][i] +  
								  dataTableOutpatients[21][i] + 
								  dataTableOutpatients[15][i] + 
								  dataTableOutpatients[5][i];
	}
	
	// Examinations ****************************************************************
	dataTableOutpatients[28][1] = numExaminations;
	
	
	
	// Prescription information*****************************************************
	// 
	for (row in prescriptionsList) {
		if(row[1]==null) { // The drug does not belong to a set
			if (row[0] < referenceDate14yo.getTime()) { 
				dataTablePharmacy[1][1]++; // Common drugs
			}
			else {
				dataTablePharmacy[4][1]++; // Children drugs
			}
		} else if (conceptIDAntibioticsDrugs.contains(row[1])) {
			dataTablePharmacy[2][1]++; //Antibiotics
		} else if (conceptIDSpecialDrugs.contains(row[1])) {
			dataTablePharmacy[3][1]++; //Special
		}
	}
	
	// Maternity information *******************************************************
	for (row in maternityList) {
		if(row[0]==159916) //Fresh Stillbirth
		{
			dataTableMaternity[2][1]++; 
		} else if (row[0]==135436) //Macerated stillbirth 
		{
			dataTableMaternity[3][1]++;
		} else if (row[0]==1170 || row[0]==118159) //Vaginal and assisted birth
		{
			dataTableMaternity[1][1]++;
		} else if (row[0]==1171) // C-section 
		{
			dataTableMaternity[2][1]++;
		}	
	}
	

	////////////////////////////////////////////////////////////////////////////
	//Print Report
	println "<table border=0><tr valign=\"top\"><td width = \"40%\"><center>"
	println "<h3>Section A. Outpatient Services</h3>"

	printTable(dataTableOutpatients, 4)

	println "</center></td><td width=\"60%\"><center>"
	println "<h3>Section B. Inpatient Services</h3><table boder=0><tr><td><center>"
	printTable(dataTableInpatients, 6)
	println "</center></td></tr><tr><td><table width=\"100%\" border=0><tr valign=\"top\"><td width=\"50%\"><center>"
	printTable(dataTableMaternity,2)
	println "</center></td><td width = \"50%\"><center>"
	printTable(dataTableOperations,2)

	println "</center></td></tr><tr valign=\"top\"><td><center>"
	printTable(dataTablePharmacy,2)
	println "<br />Note: all adult prescriptions are categorized as 'common'."
	println "</center></td><td><center>"

	printTable(dataTableMortuaryMedRec,2)

	println "</center></td></tr></table>"
	println "</td></tr><tr><td><center>"
	println "<h3>Section C. SPECIAL SERVICES</h3>"

	printTable(dataTableSpecial, 7)

	println "</center></td></tr></table>"
	println "</center></td></tr></table>"

}


