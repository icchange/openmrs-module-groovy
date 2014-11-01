def over5YearsOld=true
def showDebug=false
print "Form 705B<br>"
print fromDate+" to "+toDate+" at "+location+"<br><br>";

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

  def ageComparisonOp=">"
  if(over5YearsOld) ageComparisonOp="<="

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
  [161780,"ALL OTHER DISEASES"],
  ]

  // header row
  def tableData=[]
  def endingDate = new Date().parse("yyyy-M-d", toDate)
  def headerRow=[""]
  for(currentDate = new Date().parse("yyyy-M-d", fromDate); currentDate<=endingDate; currentDate=currentDate+1.days)
  headerRow.add(currentDate.format('dd'))
  headerRow.add("Total")
  tableData.add(headerRow)

  // diagnosis
  for(i=0;i < diagnosisRows.size();i++)
  {
  if(i==diagnosisRows.size()-1)
  {
   tableData.add([" "])
   tableData.add([" "])
  }
  def conceptId=diagnosisRows[i][0]
  def conceptName=diagnosisRows[i][1]
  def row=[]
  def rowTotal=0
  row.add(conceptName)    
  def iterations=0;
  for(currentDate = new Date().parse("yyyy-M-d", fromDate); currentDate<=endingDate; currentDate=currentDate+1.days)
  {
   def counter=0
   for(k in diagnosis)
   {
    if(k[3].getYear()==currentDate.getYear() && k[3].getMonth()==currentDate.getMonth() && k[3].getDate()==currentDate.getDate() && conceptId==k[2])
     counter++
   }
   row.add(counter)
   rowTotal=rowTotal+counter
   iterations++
  }
  row.add(rowTotal)
  tableData.add(row);
  }

  // total diagnosis
  def totalNewCasesRow=["TOTAL NEW CASES"]
  def totalNewCasesRowTotal=0    
  for(currentDate = new Date().parse("yyyy-M-d", fromDate); currentDate<=endingDate; currentDate=currentDate+1.days)
  {
  def counter=0
  for(k in diagnosis)
   if(k[3].getYear()==currentDate.getYear() && k[3].getMonth()==currentDate.getMonth() && k[3].getDate()==currentDate.getDate())
    counter++
  totalNewCasesRow.add(counter)
  totalNewCasesRowTotal=totalNewCasesRowTotal+counter
  }
  totalNewCasesRow.add(totalNewCasesRowTotal)
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
  for(currentDate = new Date().parse("yyyy-M-d", fromDate); currentDate<=endingDate; currentDate=currentDate+1.days)
  {
  def firstAttendanceCounter=0
  def reAttendanceCounter=0
  for(k in attendances)      
  {
   if(k[3].getYear()==currentDate.getYear() && k[3].getMonth()==currentDate.getMonth() && k[3].getDate()==currentDate.getDate())
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
  tableData.add(firstAttendancesRow);
  reAttendancesRow.add(reAttendancesRowTotal)
  tableData.add(reAttendancesRow);

  // referrals in
  def referralsInRow=[]
  def referralsInRowTotal=0
  referralsInRow.add("REFERRALS IN")
  for(currentDate = new Date().parse("yyyy-M-d", fromDate); currentDate<=endingDate; currentDate=currentDate+1.days)
  {
  def counter=0
  for(k in referralsIn)
   if(k[3].getYear()==currentDate.getYear() && k[3].getMonth()==currentDate.getMonth() && k[3].getDate()==currentDate.getDate())
    counter++
  referralsInRow.add(counter)
  referralsInRowTotal=referralsInRowTotal+counter
  }
  referralsInRow.add(referralsInRowTotal)
  tableData.add(referralsInRow);

  // referrals out
  def referralsOutRow=[]
  def referralsOutRowTotal=0
  referralsOutRow.add("REFERRALS OUT")
  for(currentDate = new Date().parse("yyyy-M-d", fromDate); currentDate<=endingDate; currentDate=currentDate+1.days)
  {
  def counter=0
  for(k in referralsOut)
   if(k[3].getYear()==currentDate.getYear() && k[3].getMonth()==currentDate.getMonth() && k[3].getDate()==currentDate.getDate())
    counter++
  referralsOutRow.add(counter)
  referralsOutRowTotal=referralsOutRowTotal+counter
  }
  referralsOutRow.add(referralsOutRowTotal)
  tableData.add(referralsOutRow);

  b.table(border: 1)
  {  
  for(tableRow in tableData)
  {
   tr
   {
    for(cell in tableRow)
     td(cell)
   }
  }
  }

  println writer

  if(showDebug)
  {
  println "<br><br>"
  println "encounters<br>"
  for (encounter in diagnosis)
  {
   println "${encounter}"
   print "<br>"
  }
  println "<br><br>"
  println "referral in<br>"
  for (encounter in referralsIn)
  {
   println "${encounter}"
   print "<br>"
  }
  println "<br><br>"
  println "referral out<br>"
  for (encounter in referralsOut)
  {
   println "${encounter}"
   print "<br>"
  }
  println "<br><br>"
  println "attendances<br>"
  for (a in attendances)
  {
   println "${a}"
   print "<br>"
  }
  }
}

​