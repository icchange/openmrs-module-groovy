openmrs-module-groovy
=====================

Fork of openmrs-module-groovy with added features to use Groovy scripts as a reporting tool, including restrictions based on user location/clinic.

Changes:

- Add/remove "Groovy Users" who are assigned a location for which to run reports
- Gutter button "Groovy Reporting" that links to a page where the user runs an existing Groovy script as a report. User can select a script/form and a date range.
- The Groovy script must be written to accept the following parameters: "location", "fromDate", "toDate".
- User must have "Run Groovy Reports" and "SQL Level Access" priviliges.
- User must have been added as a "Groovy User" though the Administrative page and assigned a location. 

Sample Groovy script that lists all encounters within the given date range at the location assigned to the Groovy User:

```
// Do not redefine variables "location","fromDate","toDate".
// Their definitions will be prepended based on user input. 

import org.codehaus.groovy.runtime.TimeCategory
import groovy.xml.MarkupBuilder
use(TimeCategory)
{
def referenceDate = new Date().parse("yyyy-M-d", fromDate)

def query="""
SELECT
encounter.encounter_datetime,
person_name.given_name,
person_name.middle_name,
person_name.family_name
FROM
encounter, person, person_name, location
WHERE
location.name='${location}'
and encounter.location_id = location.location_id
and encounter.voided = false
and person.person_id = encounter.patient_id
and encounter.encounter_datetime >= DATE('${fromDate}')
and encounter.encounter_datetime <= DATE('${toDate}') 
and person.person_id = person_name.person_id
ORDER BY
encounter.encounter_id ASC
"""
def results = admin.executeSQL(query,false)

print "Encounters between "+fromDate+" and "+toDate+" at "+location+"<br><br>"
for(i=0;i < results.size();i++)
{
    def result=results[i]
    print result[0].toString()+", "+result[1]+", "+result[2]+", "+result[3]+"<br>"
}

}

```
