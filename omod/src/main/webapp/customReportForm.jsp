<%--
  The contents of this file are subject to the OpenMRS Public License
  Version 1.0 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://license.openmrs.org

  Software distributed under the License is distributed on an "AS IS"
  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  License for the specific language governing rights and limitations
  under the License.

  Copyright (C) OpenMRS, LLC.  All Rights Reserved.

--%>
<%@ include file="/WEB-INF/template/include.jsp" %>
<openmrs:require privilege="Run Custom Reports" otherwise="/login.htm" redirect="/module/groovy/CustomReport.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css" />


<!--<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/moduleResources/groovy/css/main.css"/>-->
<p>
<div class="noprint">
<form method="post">
	<table border="1px; " >
        <tr>
        	<td>Report Name: </td>
            <td>
            	<select name="id">
					<option value="-1"></option>
					<c:forEach var="script" items="${scripts}">
						<option value="${script.id}">${script.name}</option>
						<!-- Here for each script we will separate public scripts from personal ones.
						ONLY private scripts will be displayed -->
					</c:forEach>
				</select>
			</td>
        </tr>
        <tr>
        	<td>Date Range: </td>
            <td>
            	<input id="fromDate" name="fromDate" type="date"/>
				-
				<input id="toDate" name="toDate" type="date"/>
			</td>
        </tr>
        <tr>
        	<td>Age Range: </td>
            <td>
            	<input id="minAge" name="minAge" type="number"/>
				-
				<input id="maxAge" name="maxAge" type="number"/>
			</td>
        </tr>
        <tr>
			<td colspan="2">
				<form id="conceptForm" class="conceptForm" method="GET">				
					<table id="criteriaTable" style="text-align: left; width: 600px;" border="1">
						<tr>
							<td>
								<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="conceptField" val="" parameters="includeVoided=false|noBind=true|optionHeader=[blank]" />
								<div id="conceptField_error" class="error" style="display:none"></div>
							</td>
						</tr>
					</table>
					
					<input type='button' value="Add" name="Add" id="Add"/>
				</form>
			</td>
		</tr>
		<!-- <tr>
			<td colspan="2">
	    		
			</td>
		</tr> -->
    </table>
   								
    
		
	<input type="submit"  name="execute" value="<spring:message code="groovy.report.run"/>"/>
	<!-- <button onclick='showMap()'>Show on Map</button>  -->
	
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
    
	<link rel="stylesheet" href="http://openlayers.org/en/v3.0.0/css/ol.css" type="text/css">
    <script src="http://openlayers.org/en/v3.0.0/build/ol.js" type="text/javascript"></script>
   
	
    <script type="text/javascript">
    /*
	    var BaseField = function (fname, ferror, emsg) 
		{
			var self = this;
			
			self.field = $j(fname);
			self.error = $j(ferror);
			self.emsg = emsg;
			
			self.validate =  function () {return true;};
			
			self.clear =  function() 
			{
				self.field.val("");
				self.error.text("");
				self.error.hide();
			}
		};
*/
		$("#Add").bind("click", (function () {
			 
			alert("Concept Added!");
		 
			addConcept();
		 
		}));
			
		function addConcept() {

			//var self = this;			
			//self.conceptCriteria = new BaseField("#conceptField", "#conceptField_error", "");
			
		    var table = document.getElementById("criteriaTable");
		    var rowcounter = table.getElementsByTagName("tr").length;
		    var row = table.insertRow(rowcounter);
		    var cell0 = row.insertCell(0);
		    var cell1 = row.insertCell(1);
		    //var cell2 = row.insertCell(2);
		    cell0.innerHTML = "1";//self.conceptCriteria.field.attr("conceptId");
		    cell1.innerHTML = "2";//self.conceptCriteria.field.attr("datatype");
		    //cell2.innerHTML = "<button onclick='deleteConcept(" + (rowcounter) + ")'>Delete this row</button>";
		}
		
		function deleteConceptRow(button) {
		     var tr=button.parentNode.parentNode;
	         tr.parentNode.removeChild(tr);
		}
 	</script>		
</form>
</div>
<p>
<c:choose>
	<c:when test="${ output!=null }">
		<div style="">
		<p>
		<!--  <a href="#" id="downloadAsPDF">Download as PDF</a>-->
		</p>
		${output}
		</div>
	</c:when>
</c:choose>
<br>
<c:choose>
	<c:when test="${ error!=null }">
		<div style="border: solid 1px red;" border="1px; " >
		${error}
		</div>
	</c:when>
</c:choose>
</p>
<p>
${console}
<p>
<script>
$j(function() {
	$j( "#fromDate" ).datepicker({ dateFormat: "yy-mm-dd" });
	$j( "#toDate" ).datepicker({ dateFormat: "yy-mm-dd" });
});
</script>
<div class="noprint">
<%@ include file="/WEB-INF/template/footer.jsp" %>
</div>
			