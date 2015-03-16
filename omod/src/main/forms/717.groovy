def fromDate = "2015-03-1"
def toDate = "2015-03-30"
location = "AMREF"

/* FORM MH 717 *************************************
 * 
 * Author :  Alejandro Ramirez
 * Dates  :  3/3/2015
 * 
 * Description:
 * This groovyscript creates a HTML report based on the MOH 717 form. 
*/


def monthNames = [ "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December" ];
print "<h1><center>Form MH 717</center></h1><br>"
  
  
//////////////////////////////////////////////////////////////////////////////
import org.codehaus.groovy.runtime.TimeCategory
import groovy.xml.MarkupBuilder
use(TimeCategory)
{
  def referenceDate = new Date().parse("yyyy-M-d", fromDate)
  referenceDate=referenceDate-5.years
  def birthDateRestriction = referenceDate.format('yyyy-M-d')
  def toDateTemp = new Date().parse("yyyy-M-d", toDate)
  toDateTemp=toDateTemp+1.days;
  def toDateAdjusted=toDateTemp.format("yyyy-M-d")
  
  //TODO: Must invalidate the form if multiple months are selected? discovered bugs
  
  def month = referenceDate.getMonth();
  def yearRef = referenceDate.getYear();
  
def c= new GregorianCalendar()
c.time = referenceDate
// TODO: BUG FIXING
  print "<center><table border=1>"
  print "<tr><td width=\"150px\"><b>Clinic</b></td><td width=\"150px\">" + location + "</td><td width=\"150px\"><b>Month</b></td><td width=\"150px\">"+monthNames[month]+ " " +  c.get(Calendar.YEAR)  +"</td></tr>"
  //print "<tr><td><b>Total Reports Received at District Level</b></td><td></td></tr>"
  print "</center></table><br><hr>"
 
def diagnosisQuery="""
  SELECT
   person.gender,
   person.birthdate, 
   obs.person_id,
   obs.encounter_id,
   obs.value_coded,
   encounter.encounter_datetime,
   person_name.given_name,
   person_name.middle_name,
   person_name.family_name,
   concept_name.name,
   visit_type.name
  FROM
   encounter, person, obs, person_name, concept_name, location, visit, visit_type
  WHERE
   location.name='${location}'
   and encounter.location_id = location.location_id
   and encounter.voided = false
   and person.person_id = encounter.patient_id
   and encounter.encounter_datetime >= DATE('${fromDate}') 
   and encounter.encounter_datetime <= DATE('${toDateAdjusted}') 
   and obs.voided = false
   and obs.encounter_id = encounter.encounter_id
   and person.person_id = person_name.person_id
   and concept_name.concept_id = obs.value_coded
   and obs.concept_id = 1284
   and concept_name.name LIKE '%(Reportable)%'
   and encounter.visit_id = visit.visit_id
   and visit.visit_type_id = visit_type.visit_type_id
  GROUP BY
   obs.obs_id,
   obs.person_id,
   obs.value_coded,
   encounter.encounter_datetime
  ORDER BY
   encounter.encounter_id ASC
  """
  diagnosis = admin.executeSQL(diagnosisQuery,false)


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
  form.name,
  visit_type.name,
  person.gender as Gender,
  person.birthdate as Bday
  FROM
  encounter, person, location, person_name, encounter_type, form,  visit, visit_type
  WHERE
  location.name='${location}'
  and encounter.location_id = location.location_id
  and encounter.encounter_type = encounter_type.encounter_type_id
  and encounter.voided = false
  and person.person_id = encounter.patient_id
  and encounter.encounter_datetime >= DATE('${fromDate}')
  and encounter.encounter_datetime <= DATE('${toDateAdjusted}') 
  and person.person_id = person_name.person_id
  and form.form_id=encounter.form_id
  and encounter.visit_id = visit.visit_id
  and visit.visit_type_id = visit_type.visit_type_id
  """
  attendances = admin.executeSQL(attendancesQuery,false)

  ////////////////////////////////////////////////////////////////////////////
  //Process Data
  //General table skeleton
    dataTableOutpatients = [ // format : col1 , col2, col3, col4, colour_flag 
	["A. GENERAL OUTPATIENT (FILTER CLINICS)", "NEW PATIENTS", "RE-ATTENDANCES", "TOTAL",1],
	["A.1.1 Over 5 - Male ", 0, 0, 0,0],
	["A.1.2 Over 5 - Female ", 0, 0, 0,0],
	["A.1.3 Children Under 5 - Male ", 0, 0, 0,0],
	["A.1.4 Children Under 5 - Female ", 0, 0, 0,0],
	["A.1.5 TOTAL GENERAL OUTPATIENTS ", 0, 0, 0,1],
	["A.2 CASUALTY", "", "", "",1],
	["A.3 SPECIAL CLINIC", "", "", "",0],
	["A.3.1 E.N.T. Clinic", 0, 0, 0,0],
	["A.3.2 Eye Clinic", 0, 0, 0,0],
	["A.3.3 TB and Leprosy ", 0, 0, 0,0],
	["A.3.4 Sexually Transmitted Infections", 0, 0, 0,0],
	["A.3.5 Psychiatry ", 0, 0, 0,0],
	["A.3.6 Orthopaedic Clnic", 0, 0, 0,0],
	["A.3.7 All other Special Clinics", 0, 0, 0,0],
	["A.3.8 TOTAL SPECIAL CLINICS ", 0, 0, 0,1],
	["A.4 MCH/FP CLIENTS", "", "", "",0],
	["A.4.1 CWC Attendances", 0, 0, 0,0],
	["A.4.2 ANC Attendances", 0, 0, 0,0],
	["A.4.3 PNC Attendances", 0, 0, 0,0],
	["A.4.4 FP Attendances", 0, 0, 0,0],
	["A.4.5 TOTAL MCH/FP", 0, 0, 0,1],
	["A.5 DENTAL CLINIC ", "", "", "",0],
	["A.5.1 Attendances (excluding fillings and extractions)", 0, 0, 0,0],
	["A.5.2 Fillings", 0, 0, 0,0],
	["A.5.3 Extractions", 0, 0, 0,0],
	["A.5.4 TOTAL DENTAL SERVICES", 0, 0, 0,1],
	["A.6 TOTAL OUTPATIENT SERVICES", 0, 0, 0,1],
	["A.7 MEDICAL EXAMINATIONS", 0, "A.10 INJECTIONS", 0,1],
	["A.8 MEDICAL REPORTS", 0, "A.11 STICHING",0,1],
	["A.9 DRESSINGS", 0, "P.O.P.", 0,1],
    ]; //End of dataTableOutpatients
	
	dataTableInpatients = [ // format : col1 , col2, col3, col4, col5, col5, colour_flag 
	["B.1 INPATIENTS", "GENERAL ADULTS", "GENERAL PEDIATRICS", "MATERNITY MOTHERS","AMENITY","TOTAL",1],
	["B.1.1 Discharges ", 0, 0, 0, 0, 0, 0],
	["B.1.2 Deaths ", 0, 0, 0, 0, 0, 0],
	["B.1.3 Abscondees ", 0, 0, 0, 0, 0, 0],
	["B.1.4 TOTAL DISCHARGES, DEATHS, ETC", 0, 0, 0, 0, 0, 1],
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
	["B.3.1 Vaginal Delivery", 0,0],
	["B.3.2 Caesarian Sections", 0,0],
	["B.3.3 Fresh Still Birth", 0,0],
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
	

   // total attendances and reattendances
	def firstAttendanceForms=[4,5,7,26]
	def reAttendanceForms=[8,11,12,13,17,19,20,21,22,23]
	def miscForms=[15,16,18]
	def totalNewCasesRowTotal=0    
	def counter=0
	def indexReAtt=1 //alternates depending on the type
	//Get the data. We are only interested in outpatients and separate by
	//	age and gender (cols 12 and 11 respectively). 
	for(row in attendances) { //NEW
		if(firstAttendanceForms.contains(row[8]) && row[10].toString().toUpperCase()=="OUTPATIENT") 
		{
			indexReAtt = 1 // New patient
		}
		else if(reAttendanceForms.contains(row[8]) && row[10].toString().toUpperCase()=="OUTPATIENT") 
		{
			indexReAtt = 2 // old patient
		} 
		else 
		{
			break //we are not going to count it otherwise
		}
		
		if (row[11].toUpperCase() == "M" && row[12] < referenceDate) {    
			dataTableOutpatients[1][indexReAtt]++;
			dataTableOutpatients[1][3]++;
		} else if (row[11].toUpperCase() == "M" && row[12] >= referenceDate) {    
			dataTableOutpatients[3][indexReAtt]++;
			dataTableOutpatients[3][3]++;
		} else if (row[11].toUpperCase() == "F" && row[12] < referenceDate) {    
			dataTableOutpatients[2][indexReAtt]++;
			dataTableOutpatients[2][3]++;
		} else if (row[11].toUpperCase() == "F" && row[12] >= referenceDate) {    
			dataTableOutpatients[4][indexReAtt]++;
			dataTableOutpatients[4][3]++;
		}
	}
	//Total new
    dataTableOutpatients[5][1] = dataTableOutpatients[1][1] + dataTableOutpatients[3][1] + dataTableOutpatients[2][1] + dataTableOutpatients[4][1];
    //Re-att total  
	dataTableOutpatients[5][2] = dataTableOutpatients[1][2] + dataTableOutpatients[3][2] + dataTableOutpatients[2][2] + dataTableOutpatients[4][2];
    //Total column  
	dataTableOutpatients[5][3] = dataTableOutpatients[1][3] + dataTableOutpatients[3][3] + dataTableOutpatients[2][3] + dataTableOutpatients[4][3];

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
	
	println "</center></td><td><center>"
	
	printTable(dataTableMortuaryMedRec,2)
	
	println "</center></td></tr></table>"
	
	println "</td></tr><tr><td><center>"
	
	println "<h3>Section C. SPECIAL SERVICES</h3>"
	
	printTable(dataTableSpecial, 7)
	
	println "</center></td></tr></table>"
	
	println "</center></td></tr></table>"
  
}

def printTable(dataTable, numColumns){
    writer = new StringWriter()  
    b = new MarkupBuilder(writer)
	b.table(border: 1, width: "100%") {
		for(row in dataTable) {
			def colour = "#FFFFFF"
			if (row[numColumns]==1) colour = "#B5BCBD"
			tr(bgcolor: colour) {
			for (i = 0; i< numColumns; i++) {
				td(row[i]);
			}
		} 
	}
	}
	println writer

}  


    

​
