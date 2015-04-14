//TODO: Implement permutations
//TODO: Implement time slider (accumulative - new events add to old & as it happens - only events that happened on that day are displayed)
//TODO: Display events at a certain radius around the click

def showDebug = true
/*
def fromDate="2014-12-01"
def toDate="2015-04-13"
def location="AMREF"
def minAge="1"
def maxAge="10"
def diagnosisRows =[[161759, "Malaria (Reportable)", 4, null, null]]
*/
if (showDebug)
{
	print "Debug: " +diagnosisRows
}

def selectItem = ""
def fromItem = ""
def whereItem = ""
def finalQuery = ""
/*print conId1+"<br/>"*/
print '<div style="" class=".class_printable_region"  id=".class_printable_region">'
print "Custom Reports Form <br>"
print fromDate+" to "+toDate+" at "+location+"<br><br>";
print minAge+" to "+maxAge+" yrs old<br><br>";

//Taking care of legend colors
def colors = [[0, 0, 255, 0.85],[0, 255, 0, 0.85],[255, 0, 0, 0.85],[255,0,255,0.85],[0,255,255,0.85],[192,192,192,0.85],[102,51,0,0.85],[102,0,0,0.85],[255, 255, 0, 0.85]]
def c = 0
println "<style>"
for (color in colors)
{
	println "#p${c} {background-color:rgba(${color[0]},${color[1]},${color[2]},${color[3]});}";
	c++;
}
println "</style>"

//Creating map area
print """
  <br/>
  <br/>
  <h3>People, who have opted out of the program, will not be seen on map</h3>
  <br/>
  <style>
       .map {
        height: 600px;
        width: 100%;
       }
       
       @media print
       {
        .noprint {display:none;}
       }  
  </style>
  
    <label id="projection" style="display:none">EPSG:3857</label>
    <div id="map" class="map"></div>
  """

//Creating legend table
	println """
	<script type="text/javascript">
		var tt = document.getElementById('legend');
		tt.border = '1px;';
		var tr0 = tt.insertRow(0);
		var tc0 = tr0.insertCell(0);
		tc0.colSpan = '2';
		tc0.style.textAlign = 'center';
		tc0.innerHTML = '<h4>Legend</h4>';
		
		//And initializing the map
		var mousePositionControl = new ol.control.MousePosition({
			coordinateFormat: ol.coordinate.createStringXY(4),
			projection: 'EPSG:3857',
			undefinedHTML: '&nbsp;'
		});

		format = 'image/png';
		var map = new ol.Map({
			controls: ol.control.defaults({
				attributionOptions: /** @type {olx.control.AttributionOptions} */ ({
					collapsible: false
				})
			}).extend([mousePositionControl]),
			layers: 
			[
				new ol.layer.Tile({
					source: new ol.source.OSM()
				})
			],
			target: 'map',
			view: new ol.View({
				center: [4095101.1653739787, -146044.79830809683],
				zoom: 16,
				maxZoom: 18,
				minZoom: 16
			}),
		});
			 
		var image = new ol.style.Circle({
			radius: 5,
			fill: new ol.style.Fill({color: 'red', 
									opacity: 0.50}),
			stroke: new ol.style.Stroke({color: 'red', width: 0})
		});
	</script>
	"""

//////////////////////////////////////////////////////////////////////////////
import org.codehaus.groovy.runtime.TimeCategory
import groovy.xml.MarkupBuilder
use(TimeCategory)
{  
	def referenceDate = new Date().parse("yyyy-M-d", fromDate)
	referenceDate=referenceDate-5.years
	def minDate = new Date().parse("yyyy-M-d", fromDate) - minAge.toInteger().years
	def maxDate = new Date().parse("yyyy-M-d", fromDate) - maxAge.toInteger().years

	//print "Birth dates between: " + minDate.toString() + " - " + maxDate.toString()

	def birthDateRestriction = referenceDate.format('yyyy-M-d')
	def toDateTemp = new Date().parse("yyyy-M-d", toDate)
	toDateTemp=toDateTemp+1.days;
	def toDateAdjusted=toDateTemp.format("yyyy-M-d")

	/////////////////////////////////////////////////////////////////
	//Setup for table output
	writer = new StringWriter()
	b = new MarkupBuilder(writer)

	//header row
	def tableData=[]
	def endingDate = new Date().parse("yyyy-M-d", toDate)
	def headerRow=[""]
	for(currentDate = new Date().parse("yyyy-M-d", fromDate); currentDate<=endingDate; currentDate=currentDate+1.days)
	{
		headerRow.add(currentDate.format('dd'))
	}
	headerRow.add("Total")
	tableData.add(headerRow)
	/////////////////////////////////////////////////////////////////
	//Here the grand loop starts - for diagnosis in diagnosisRows iteration
	
	for(i=0;i < diagnosisRows.size();i++)
	{
		def conId = diagnosisRows[i][0]
		def name = diagnosisRows[i][1]
		def typeCode = diagnosisRows[i][2]
		def op = "="
		def val = conId
		def group = "1284"
		
		if (diagnosisRows[i][3] != null && diagnosisRows[i][3] != "null" && diagnosisRows[i][4] != null && diagnosisRows[i][4] != "null")
		{
			op = diagnosisRows[i][3]
			val = diagnosisRows[i][4]
			group = conId
		}
		else
		{
			op = "="
			val = conId
			group = "1284"
		}
		def type = ""
		
		switch (typeCode)
		{
			case '1':
				type = "value_numeric"
				break
			case '2':
				type = "value_coded"
				break
			case '3':
				type = "value_text"
				break
			case '4':
				type = "value_coded" //placeholder
				break
			case '5':
				type = "value_coded" //placeholder
				break
			case '6':
				type = "value_datetime"
				break
			case '7':
				type = "value_datetime"
				break
			case '8':
				type = "value_datetime"
				break
			case '10':
				type = "value_boolean"
				break
			case '11':
				type = "value_coded" //placeholder
				break
			case '12':
				type = "value_coded" //placeholder
				break
			case '13':
				type = "value_complex"
				break			
			default:
				type = "value_coded" //placeholder
				if (showDebug)
				{
					print "Debug Warning: typeCode fell through the cracks"
				}
				break
		}
		
		def diagnosisQuery="""
		SELECT
		obs.person_id,
		obs.encounter_id,
		obs.${type},
		encounter.encounter_datetime,
		person_name.given_name,
		person_name.middle_name,
		person_name.family_name,
		concept_name.name,
		encounter.latitude,
		encounter.longitude
		FROM
		encounter, person, obs, person_name, concept_name, location
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
		-- and person.birthdate =< DATE('${minDate}') and person.birthdate => DATE('${maxDate}')		
		and concept_name.concept_id = obs.${type}
		and obs.concept_id = '${group}'
		and obs.${type} ${op} '${val}' 
		and concept_name.name LIKE '%(Reportable)%'
		GROUP BY
		obs.obs_id,
		obs.person_id,
		obs.${type},
		encounter.encounter_datetime
		ORDER BY
		encounter.encounter_id ASC
		"""
		
		//Query execution
		diagnosis = admin.executeSQL(diagnosisQuery,false)
		
		/*Inserts padding at the end of the table. Not used at the moment.
		if(i==diagnosisRows.size()-1)
		{
			def row=[" "]
			row.add(" ")
			for(currentDate = new Date().parse("yyyy-M-d", fromDate); currentDate<=endingDate; currentDate=currentDate+1.days)
			{
				row.add(" ")
			}
			tableData.add(row)
			tableData.add(row)
		}*/
		
		// diagnosis
		def row=[]
		def rowTotal=0
		row.add(name)    
		for(currentDate = new Date().parse("yyyy-M-d", fromDate); currentDate<=endingDate; currentDate=currentDate+1.days)
		{
			def counter=0
			for(k in diagnosis)
			{
				if(k[3].getYear()==currentDate.getYear() && k[3].getMonth()==currentDate.getMonth() && k[3].getDate()==currentDate.getDate())
				{counter++}
			}
			row.add(counter)
			rowTotal=rowTotal+counter
		}
		row.add(rowTotal)
		tableData.add(row);

		///////////////////////////////////////////////////////
		// This section is responsible for:
		//1. Creating different point styles, separate for each diagnosis
		//-- Add a legend record
		//2. Storing point data inside page.
		//3. Creating a vector layer with the created points data and style.
		println """
		<script type="text/javascript">
			var tt = document.getElementById('legend');
			var tr2 = tt.insertRow(${i+1});
			var tc1 = tr2.insertCell(0);
			var tc2 = tr2.insertCell(1);
			tc2.innerHTML = '- ${diagnosisRows[i][1]}';
			tc1.id = 'p${i}';
			tc1.style.width = '40px';
		
			var style${i} = {
				'Point': [new ol.style.Style({
					image: new ol.style.Circle({
						radius: 10,
						fill: new ol.style.Fill({color: 'rgba(${colors[i][0]},${colors[i][1]},${colors[i][2]},${colors[i][3]})'}),
						stroke: new ol.style.Stroke({color: 'rgba(${colors[i][0]},${colors[i][1]},${colors[i][2]},${colors[i][3]})', width: 0})
					})
				})]
			}
			
			
			var styleFunction${i} = function(feature, resolution) {
				return style${i}[feature.getGeometry().getType()];
			};
			
			var diseaseVectorSource${i} = new ol.source.GeoJSON(
			/** @type {olx.source.GeoJSONOptions} */ ({
				format: new ol.format.GeoJSON({
					defaultProjection: 'EPSG:4326'
				}),
				projection: 'EPSG:4326',
				object: 
				{
					"type": "FeatureCollection",
					"generator": "na",
					"copyright": "na",
					"timestamp": "2015-01-27T22:54:02Z",
					"features": ["""

					def lat = 0
					def lon = 0
					for (a in diagnosis)
					{
						lat = a[8]
						lon = a[9]
						if (lat!=null)
						{
							print '{"type": "Feature","geometry": {"type": "Point","coordinates": [' 
							print "${lat}" 
							print ', ' 
							print "${lon}"
							println ']}},'
						}
					}

					print '{"type": "Feature","geometry": {"type": "Point","coordinates": [0,0]}}'
					println """] 
				}
			}));
			
			var diseaseVectorLayer${i} = new ol.layer.Vector({
			source: diseaseVectorSource${i},
			style: styleFunction${i}
			});

			map.addLayer(diseaseVectorLayer${i});
		</script>
		"""
	}
  
	/////////////////////////////////////////////////////////////////
	/*An outdated diagnosis query
	def diagnosisQuery="""
	SELECT
	obs.person_id,
	obs.encounter_id,
	obs.value_coded,
	encounter.encounter_datetime,
	person_name.given_name,
	person_name.middle_name,
	person_name.family_name,
	concept_name.name,
	encounter.latitude,
	encounter.longitude
	FROM
	encounter, person, obs, person_name, concept_name, location
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
	-- and person.birthdate =< DATE('${minDate}') and person.birthdate => DATE('${maxDate}')
	and concept_name.concept_id = obs.value_coded
	and obs.concept_id = 1284
	and obs.value_coded = ${conId}
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
	*/




	/////////////////////////////////////////////////////////////////////////////////



	/**
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
	*/
  

  

	/** total diagnosis
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
	tableData.add(totalNewCasesRow);**/


	/** first attendances
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
	tableData.add(reAttendancesRow);*/

	/** referrals in
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
	tableData.add(referralsInRow);*/

	/** referrals out
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
	tableData.add(referralsOutRow);*/

	b.table(border: 1)
	{  
		for(tableRow in tableData)
		{
			tr
			{
				for(cell in tableRow)
				{
					td(cell)
				}
			}
		}
	}

  println writer
  println '</div>'
  


  
  
  
  
  
  
  
  
  
  
  
 
  
  
  
  
  
  
  
  
  
  
  
 
  
  
  
  
  
  
  
  
  
  println """<script type="text/javascript">
	///////////////////////////////////////
	//Disease style & its function used to be here
  
         
   var alliedStyles = {   'Point': [new ol.style.Style({
          image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
                                        anchor: [0.5, 10],
                                        anchorXUnits: 'fraction',
                                        anchorYUnits: 'pixels',
                                        opacity: 1.00,
          scale: 1.50,
          src: '../../images/openmrs_logo_tiny.png'                                        
                                      }))
           })]
         }  
         var alliedStyleFunction = function(feature, resolution) {
           return alliedStyles[feature.getGeometry().getType()];
         };
         
   var alliedVectorSource = new ol.source.GeoJSON(
          /** @type {olx.source.GeoJSONOptions} */ ({
          format: new ol.format.GeoJSON({
           defaultProjection: 'EPSG:4326'
          }),
          projection: 'EPSG:3857',
          object: 
          {
  "type": "FeatureCollection",
  "generator": "overpass-turbo",
  "copyright": "The data included in this document is from www.openstreetmap.org. The data is made available under ODbL.",
  "timestamp": "2015-01-27T22:54:02Z",
  "features": [
    {
      "type": "Feature",
      "id": "node/2016217939",
      "properties": {
        "@id": "node/2016217939",
        "amenity": "clinic",
        "health_facility:referals": "AMREF",
        "health_facility:type": "health_center",
        "medical_service:general_medical_services": "yes",
        "medical_service:outpatient": "yes",
        "medical_service:pregnancy_test": "yes",
        "name": "Vostrum Clinic,Kibera Branch",
        "opening_hours": "24/7",
        "operational_status": "operational",
        "operator:type": "N/A"
      },
      "geometry": {
        "type": "Point",
        "coordinates": [
          36.781119,
          -1.3077519
        ]
      }
    },
  {
      "type": "Feature",
      "id": "node/701794660",
      "properties": {
        "@id": "node/701794660",
        "amenity": "hospital",
        "health_facility:bed": "14",
        "health_facility:exam_room": "yes",
        "health_facility:patients_per_day": "many",
        "health_facility:referals": "AMREF,Mbagathi,Kenyatta National Hospital",
        "health_facility:type": "health_center",
        "medical_service:antenatal_care": "yes",
        "medical_service:antiretroviral_therapy": "yes",
        "medical_service:basic_emergency_obsteric_care": "yes",
        "medical_service:circumcision": "yes",
        "medical_service:condom_distribution": "yes",
        "medical_service:disease_support_groups": "Blood Sugar",
        "medical_service:family_planning": "yes",
        "medical_service:gender_based_violence_services": "yes",
        "medical_service:general_medical_services": "yes",
        "medical_service:growth_and_nutritional_support": "yes",
        "medical_service:home_based_care": "yes",
        "medical_service:immunizations": "yes",
        "medical_service:inpatient": "yes",
        "medical_service:integrated_management_of_childhood_illness": "yes",
        "medical_service:malaria": "yes",
        "medical_service:minor_surgery": "yes",
        "medical_service:outpatient": "yes",
        "medical_service:pmtct": "yes",
        "medical_service:pregnancy_test": "yes",
        "medical_service:psychosocial_support_counselling": "yes",
        "medical_service:sexually_transmitted_infections_management": "yes",
        "medical_service:tb_diagnosis": "yes",
        "medical_service:tb_labs": "yes",
        "medical_service:tb_treatment": "yes",
        "medical_service:vct_hiv_counselling_test": "yes",
        "medical_service:youth_friendly_services": "yes",
        "medical_staff:clinical_officer": "2",
        "medical_staff:community_pharmacist": "2",
        "medical_staff:counsellor": "2",
        "medical_staff:general_practitioner": "1",
        "medical_staff:lab_tech": "2",
        "medical_staff:medical_officer": "1",
        "medical_staff:midwife": "2",
        "medical_staff:nurse": "2",
        "medical_staff:pharm_tech": "2",
        "name": "Ushirika medical centre",
        "opening_hours": "24/7",
        "operational_status": "operational",
        "operational_status:electricity": "always",
        "operator:type": "ngo_cbo"
      },
      "geometry": {
        "type": "Point",
        "coordinates": [
          36.7727571,
          -1.3077798
        ]
      }
    },
    {
      "type": "Feature",
      "id": "node/671166105",
      "properties": {
        "@id": "node/671166105",
        "amenity": "hospital",
        "health_facility:bed": "5",
        "health_facility:cot": "14",
        "health_facility:exam_room": "yes",
        "health_facility:patients_per_day": "100",
        "health_facility:referals": "IDH,KNH",
        "health_facility:type": "health_center",
        "medical_service:antenatal_care": "yes",
        "medical_service:antiretroviral_therapy": "yes",
        "medical_service:circumcision": "yes",
        "medical_service:comprehensive_essential_obsteric_care": "yes",
        "medical_service:condom_distribution": "yes",
        "medical_service:family_planning": "yes",
        "medical_service:gender_based_violence_services": "yes",
        "medical_service:general_medical_services": "yes",
        "medical_service:growth_and_nutritional_support": "yes",
        "medical_service:health_insurance": "no",
        "medical_service:home_based_care": "yes",
        "medical_service:immunizations": "yes",
        "medical_service:inpatient": "yes",
        "medical_service:integrated_management_of_childhood_illness": "yes",
        "medical_service:malaria": "yes",
        "medical_service:minor_surgery": "yes",
        "medical_service:outpatient": "yes",
        "medical_service:palliative_care": "yes",
        "medical_service:pmtct": "yes",
        "medical_service:pregnancy_test": "yes",
        "medical_service:prevention": "HIV",
        "medical_service:psychosocial_support_counselling": "yes",
        "medical_service:sexually_transmitted_infections_management": "yes",
        "medical_service:tb_diagnosis": "yes",
        "medical_service:tb_labs": "yes",
        "medical_service:tb_treatment": "yes",
        "medical_service:vct_hiv_counselling_test": "yes",
        "medical_service:youth_friendly_services": "yes",
        "medical_staff:clinical_officer": "5",
        "medical_staff:counsellor": "2",
        "medical_staff:general_practitioner": "1",
        "medical_staff:lab_tech": "2",
        "medical_staff:medical_officer": "5",
        "medical_staff:midwife": "7",
        "medical_staff:nurse": "6",
        "medical_staff:pharmacist": "4",
        "name": "Amref-Kibera",
        "opening_hours": "Mo-Fr 08:00-16:30",
        "operational_status": "operational",
        "operational_status:electricity": "always",
        "operator:type": "ngo_international"
      },
      "geometry": {
        "type": "Point",
        "coordinates": [
          36.7956417,
          -1.311196
        ]
      }
    }
]
}
})
);
  
   var alliedVectorLayer = new ol.layer.Vector({
            source: alliedVectorSource,
            style: alliedStyleFunction
          });
          
          map.addLayer(alliedVectorLayer);
  """
  
println "</script>"

}

​