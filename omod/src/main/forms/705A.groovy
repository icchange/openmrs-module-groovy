/* FORM MH 717 *******************************************************
* 
* Author :  Others + Alejandro Ramirez-Sanabria
* Dates  :  24/04/2015
* 
* Description:
* Form 705A-B
*/


/*********************************************************************
*	Changelog
*	Who?	When?		What?
*--------------------------------------------------------------------
*	ARS		22/03/15	Fixed the gregorian calendar
*					problem (KMRI-593)
*	ARS 		24/04/15	RE-Formatted the codee and 
					visual aspect. 
*********************************************************************/


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
	b.table(border: 1) {
		for(row in dataTable) {
			def colour = "#FFFFFF"
			def fontStyle = ""
			if (row[numColumns]==1) { 
				colour = "#1AAC9B"
			}
			if (row[numColumns]==2) {
				fontStyle = "font-weight:bold"; 
				colour = "#F0F0F5" 
				//colour = "#D4D4CA" 
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


def printHeader(title, location, fromDate, toDate) {
	print "<h1><center>"+title+"</center></h1><br>"
	print "<center><table border=1>"
	print "<tr><td width=\"200px\"><b>Clinic</b></td><td width=\"200px\">" + location + "</td><td width=\"200px\"><b>Date Range</b></td><td width=\"200px\">"+ fromDate +" to " + toDate +"</td></tr>"
	print "</center></table><br><hr>"
}

def getMonth(month) {
	months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"  ];
	return months[month];
	
}



debug = true

//End of helper functions 

def over5YearsOld=false

//////////////////////////////////////////////////////////////////////////////
import org.codehaus.groovy.runtime.TimeCategory
import groovy.xml.MarkupBuilder
use(TimeCategory)
{
	//use only for mock data and debuggin purposes
	
	if (debug) {
		fromDate = '2015-3-1';
		toDate = '2015-3-31';
		location = 'AMREF'; 
	}

	def strDateFormat = "yyyy-M-d";
	def fromDateGC = getGregorianCalendar(strDateFormat, fromDate);
	def referenceDate = new GregorianCalendar(); 
	referenceDate.setTime(fromDateGC.getTime()-5.years);
	def birthDateRestriction = referenceDate.format('yyyy-M-d')
	def toDateGC = getGregorianCalendar(strDateFormat, toDate);
	def toDateTemp = new GregorianCalendar(); 
	toDateTemp.setTime(toDateTemp.getTime()+1.days);
	def toDateAdjusted= toDateTemp.getTime().format(strDateFormat)

	def ageComparisonOp=">"
	if(over5YearsOld) ageComparisonOp="<="

	printHeader("MOH 705 A", location, fromDate, toDate);


	def diagnosisQuery="""
	SELECT
	obs.person_id,
	obs.encounter_id,
	obs.value_coded,
	encounter.encounter_datetime,
	person_name.given_name,
	person_name.middle_name,
	person_name.family_name,
	concept_name.name
	FROM
	encounter, person, obs, person_name, concept_name, location
	WHERE
	location.name='${location}'
	and encounter.location_id = location.location_id
	and encounter.voided = false
	and person.person_id = encounter.patient_id
	and encounter.encounter_datetime >= DATE('${fromDate}')
	and encounter.encounter_datetime <= DATE('${toDateAdjusted}') 
	and person.birthdate ${ageComparisonOp} DATE('${birthDateRestriction}')
	and obs.voided = false
	and obs.encounter_id = encounter.encounter_id
	and person.person_id = person_name.person_id
	and concept_name.concept_id = obs.value_coded
	and obs.concept_id = 1284
	and concept_name.name LIKE '%(Reportable)%'
	GROUP BY
	obs.obs_id,
	obs.person_id,
	obs.value_coded,
	encounter.encounter_datetime
	ORDER BY
	encounter.encounter_id ASC
	"""
	diagnosis = admin.executeSQL(diagnosisQuery,false)

	def referralsInQuery="""
	SELECT
	obs.person_id,
	obs.encounter_id,
	obs.value_coded,
	encounter.encounter_datetime,
	person_name.given_name,
	person_name.middle_name,
	person_name.family_name,
	concept_name.name,
	concept.class_id
	FROM
	encounter, person, obs, person_name, concept_name, location, concept
	WHERE
	location.name='${location}'
	and encounter.location_id = location.location_id
	and encounter.voided = false
	and person.person_id = encounter.patient_id
	and encounter.encounter_datetime >= DATE('${fromDate}')
	and encounter.encounter_datetime <= DATE('${toDateAdjusted}') 
	and person.birthdate ${ageComparisonOp} DATE('${birthDateRestriction}')
	and obs.voided = false
	and obs.encounter_id = encounter.encounter_id
	and person.person_id = person_name.person_id
	and concept_name.concept_id = obs.value_coded
	and obs.concept_id = 161257
	and concept_name.concept_id=concept.concept_id
	and concept.class_id = 30
	GROUP BY
	obs.person_id,
	obs.value_coded,
	encounter.encounter_datetime,
	obs.obs_id
	ORDER BY
	encounter.encounter_id ASC
	"""
	referralsIn = admin.executeSQL(referralsInQuery,false)

	def referralsOutQuery="""
	SELECT
	obs.person_id,
	obs.encounter_id,
	obs.value_coded,
	encounter.encounter_datetime,
	person_name.given_name,
	person_name.middle_name,
	person_name.family_name,
	concept_name.name,
	concept.class_id
	FROM
	encounter, person, obs, person_name, concept_name, location, concept
	WHERE
	location.name='${location}'
	and encounter.location_id = location.location_id
	and encounter.voided = false
	and person.person_id = encounter.patient_id
	and encounter.encounter_datetime >= DATE('${fromDate}')
	and encounter.encounter_datetime <= DATE('${toDateAdjusted}') 
	and person.birthdate ${ageComparisonOp} DATE('${birthDateRestriction}')
	and obs.voided = false
	and obs.encounter_id = encounter.encounter_id
	and person.person_id = person_name.person_id
	and concept_name.concept_id = obs.value_coded
	and obs.concept_id = 1272
	and concept_name.concept_id=concept.concept_id
	and concept.class_id = 30
	GROUP BY
	obs.person_id,
	obs.value_coded,
	encounter.encounter_datetime,
	obs.obs_id
	ORDER BY
	encounter.encounter_id ASC
	"""
	referralsOut = admin.executeSQL(referralsOutQuery,false)

	def attendancesQuery="""
	SELECT
	encounter.patient_id,
	encounter.encounter_id,
	encounter.encounter_type,
	encounter.encounter_datetime,
	person_name.given_name,
	person_name.middle_name,
	person_name.family_name,
	encounter_type.name,
	encounter.form_id,
	form.name
	FROM
	encounter, person, location, person_name, encounter_type, form
	WHERE
	location.name='${location}'
	and encounter.location_id = location.location_id
	and encounter.encounter_type = encounter_type.encounter_type_id
	and encounter.voided = false
	and person.person_id = encounter.patient_id
	and encounter.encounter_datetime >= DATE('${fromDate}')
	and encounter.encounter_datetime <= DATE('${toDateAdjusted}') 
	and person.birthdate ${ageComparisonOp} DATE('${birthDateRestriction}')
	and person.person_id = person_name.person_id
	and form.form_id=encounter.form_id
	"""
	attendances = admin.executeSQL(attendancesQuery,false)



	/////////////////////////////////////////////////////////////////////////////////
	writer = new StringWriter()
	b = new MarkupBuilder(writer)
	def diagnosisRows=[
	[161749,"Diarrhoea"],
	[161775,"Tuberculosis"],
	[161753,"Dysentery"],
	[161745,"Cholera"],
	[161763,"Meningococcal Meningitis"],
	[161774,"Tetanus"],
	[161770,"Poliomyelitis (AFP)"],
	[161744,"Chicken Pox"],
	[161762,"Measles"],
	[161758,"Infectious Hepatitis"],
	[162007,"Clinical Malaria"],
	[161746,"Confirmed Malaria"],
	[161760,"Malaria in Pregnancy"],
	[161776,"Typhoid fever"],
	[161773,"Sexually transmitted infections"],
	[161777,"Urinary Tract Infections"],
	[161740,"Bilharzia"],
	[161761,"Malnutrition"],
	[161739,"Aneamia"],
	[161756,"Eye infections"],
	[161754,"Ear infections"],
	[161766,"Other dis. of repiratory system"],
	[161768,"Pneumonia"],
	[161723,"Abortion"],
	[161750,"Dis. of puerperium and childbirth"],
	[161757,"Hypertension"],
	[161764,"Mental disorders"],
	[161747,"Dental disorders"],
	[161751,"Dis. of the skin (incl. wounds)"],
	[161771,"Rheumatism, Joint Pains, etc."],
	[161769,"Poisoning"],
	[161724,"Accidents - fractures, injuries, etc."],
	[161772,"Sexual assault"],
	[161743,"Burns"],
	[161741,"Bites - animals, snakes, etc"],
	[161748,"Diabetes"],
	[161755,"Epilepsy"],
	[161752,"Dracunculosis"],
	[161779,"Yellow Fever"],
	[161778,"Viral Haemorrhagic Fever"],
	[161765,"New AIDS Cases"],
	[161767,"Plague"],
	[161742,"Brucellosis"],
	[161780,"All other diagnoses"],
	]

	//Split the data by months 
	//First see how many months we have
	def numOfMonths = toDateGC.get(Calendar.MONTH) - fromDateGC.get(Calendar.MONTH) + 1;
	def numOfYears = toDateGC.get(Calendar.YEAR) - fromDateGC.get(Calendar.YEAR);
	numOfMonths += numOfYears * 12;

	def startDate = fromDateGC.clone();  
	def endingDateGC = toDateGC.clone();
	def lastEndingDateGC = startDate.clone();
	println "<table border=0>"
	def endingDate = new Date().parse("yyyy-M-d", toDate); 
	for (a = 1; a<= numOfMonths; a++) {
		
		if (a%2 == 0) { // different bgcolour
			bgcolour = "#E8F7F5";
		} else {
			bgcolour = "#FFFFF5";
		}
		// header row
		def tableData=[]
		def headerRow=[""]
		def numDays = 0;
		
		startDate = lastEndingDateGC.clone(); 
		def currentMonth = startDate.get(Calendar.MONTH);
	
		for(currentDate = startDate.clone(); (currentDate<=endingDateGC && currentDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH)); currentDate.add(Calendar.DAY_OF_MONTH,1)) {
			headerRow.add(currentDate.getTime().format('dd'));
			numDays++;
			lastEndingDateGC  = currentDate.clone(); 
		}
		
		lastEndingDateGC.add(Calendar.DAY_OF_MONTH, 1); 
		
		headerRow.add("Total")
		headerRow.add(1); //Flag information for formatting 
		tableData.add(headerRow)

		// diagnosis
		for(i=0;i < diagnosisRows.size()+1;i++)
		{
			if(i==diagnosisRows.size())
			{
				def tempRow = ["TOTALS"];
				for (j =0; j!= numDays+1; ++j) {
					tempRow.add("");
				}
				tempRow.add(1); 
				tableData.add(tempRow)
				break;
			}
			def conceptId=diagnosisRows[i][0]
			def conceptName=diagnosisRows[i][1]
			def row=[]
			def rowTotal=0
			row.add(conceptName)    
			def iterations=0;
			for(currentDate = startDate.clone(); (currentDate<=endingDateGC && currentDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH)); currentDate.add(Calendar.DAY_OF_MONTH, 1))
			{
				def counter=0
				for(k in diagnosis)
				{
					if(k[3].getYear()==currentDate.getTime().getYear() && k[3].getMonth()==currentDate.getTime().getMonth() && k[3].getDate()==currentDate.getTime().getDate() && conceptId==k[2])
					counter++
					
				}
				row.add(counter)
				rowTotal=rowTotal+counter
				iterations++
			}
			row.add(rowTotal)
			row.add(0);
			tableData.add(row);
		}

		// total diagnosis
		def totalNewCasesRow=["TOTAL NEW CASES"]
		def totalNewCasesRowTotal=0    
		for(currentDate = startDate.clone(); (currentDate<=endingDateGC && currentDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH)); currentDate.add(Calendar.DAY_OF_MONTH, 1))
		{
			def counter=0
			for(k in diagnosis)
			if(k[3].getYear()==currentDate.getTime().getYear() && k[3].getMonth()==currentDate.getTime().getMonth() && k[3].getDate()==currentDate.getTime().getDate())
			counter++
			totalNewCasesRow.add(counter)
			totalNewCasesRowTotal=totalNewCasesRowTotal+counter
		}
		totalNewCasesRow.add(totalNewCasesRowTotal)
		totalNewCasesRow.add(2);
		tableData.add(totalNewCasesRow);


		// first attendances
		def firstAttendancesRow=[]
		def firstAttendancesRowTotal=0
		firstAttendancesRow.add("NO. OF FIRST ATTENDANCES")
		def reAttendancesRow=[]
		def reAttendancesRowTotal=0
		reAttendancesRow.add("RE-ATTENDANCES")
		def formTypes=[
		4:"Adult Intake Form",
		5:"Pediatric Intake Form",
		7:"Maternity Intake Form",
		8:"Adult Follow Up Form",
		11:"Vitals Form",
		12:"Pediatric Followup Form",
		13:"Maternity Followup Form",
		15:"Lab Result Form",
		16:"Drug Order",
		17:"Nutrition Consult",
		18:"Lab Order Form",
		19:"Labour and Delivery Form 2 Summary of Labour",
		20:"Pregnancy Listing",
		21:"HIV Form",
		22:"Labour and Delivery Form 3 Newborn and HIV status",
		23:"Labour and Delivery Form 1 Partograph and Vitals",
		26:"Newborn Form"
		]
		def firstAttendanceForms=[4,5,7,26]
		def reAttendanceForms=[8,11,12,13,17,19,20,21,22,23]
		def miscForms=[15,16,18]
		for(currentDate = startDate.clone(); (currentDate<=endingDateGC && currentDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH));  currentDate.add(Calendar.DAY_OF_MONTH, 1))
		{
			def firstAttendanceCounter=0
			def reAttendanceCounter=0
			for(k in attendances)      
			{
				if(k[3].getYear()==currentDate.getTime().getYear() && k[3].getMonth()==currentDate.getTime().getMonth() && k[3].getDate()==currentDate.getTime().getDate())
				{
					if(firstAttendanceForms.contains(k[8]))
					firstAttendanceCounter++
					if(reAttendanceForms.contains(k[8]))
					reAttendanceCounter++
				}
			}
			firstAttendancesRow.add(firstAttendanceCounter)
			firstAttendancesRowTotal = firstAttendancesRowTotal + firstAttendanceCounter
			reAttendancesRow.add(reAttendanceCounter)
			reAttendancesRowTotal = reAttendancesRowTotal + reAttendanceCounter
		}
		firstAttendancesRow.add(firstAttendancesRowTotal)
		firstAttendancesRow.add(2); //Flag 
		tableData.add(firstAttendancesRow);
		reAttendancesRow.add(reAttendancesRowTotal)
		reAttendancesRow.add(2); //Flag 
		tableData.add(reAttendancesRow);

		// referrals in
		def referralsInRow=[]
		def referralsInRowTotal=0
		referralsInRow.add("REFERRALS IN")
		for(currentDate = startDate.clone(); (currentDate<=endingDateGC && currentDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH));  currentDate.add(Calendar.DAY_OF_MONTH, 1))
		{
			def counter=0
			for(k in referralsIn)
			if(k[3].getYear()==currentDate.getTime().getYear() && k[3].getMonth()==currentDate.getTime().getMonth() && k[3].getDate()==currentDate.getTime().getDate())
			counter++
			referralsInRow.add(counter)
			referralsInRowTotal=referralsInRowTotal+counter
		}
		referralsInRow.add(referralsInRowTotal)
		referralsInRow.add(2)
		tableData.add(referralsInRow);

		// referrals out
		def referralsOutRow=[]
		def referralsOutRowTotal=0
		referralsOutRow.add("REFERRALS OUT")
		for(currentDate = startDate.clone(); (currentDate<=endingDateGC && currentDate.get(Calendar.MONTH) == startDate.get(Calendar.MONTH));  currentDate.add(Calendar.DAY_OF_MONTH, 1))
		{
			def counter=0
			for(k in referralsOut)
			if(k[3].getYear()==currentDate.getTime().getYear() && k[3].getMonth()==currentDate.getTime().getMonth() && k[3].getDate()==currentDate.getTime().getDate())
			counter++
			referralsOutRow.add(counter)
			referralsOutRowTotal=referralsOutRowTotal+counter
		}
		referralsOutRow.add(referralsOutRowTotal)
		referralsOutRow.add(2) //flag
		tableData.add(referralsOutRow);
		
		//Print Report
		println "<tr valign=\"top\"><td width = \"80%\" bgcolor=\""+ bgcolour+"\"><center>"
		println "<h3>Month : "+ getMonth(currentMonth) + " (" + a + " of " + numOfMonths + ")</h3>"
		

		
		printTable(tableData, numDays+2);

		println "<br /></center></td></tr>"
		}
		println "</table>"

}


