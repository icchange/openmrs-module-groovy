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
	<table>
		<tr>
			<td>
				<table border="1px; " >
			        <tr>
			        	<td>Report Name: </td>
			            <td>
			            	<select name="id">
								<option value="-1"></option>
								<c:forEach var="script" items="${scripts}">
									<option value="${script.id}" <c:if test="${selectId == script.id}">selected</c:if>>${script.name}</option>
									<!-- Here for each script we will separate public scripts from personal ones.
									ONLY private scripts will be displayed -->
								</c:forEach>
							</select>
						</td>
			        </tr>
			        <tr>
			        	<td>Date Range: </td>
			            <td>
			            	<input id="fromDate" name="fromDate" type="date" value="${fromDate}"/>
							-
							<input id="toDate" name="toDate" type="date" value="${toDate}"/>
						</td>
			        </tr>
			        <tr>
			        	<td>Age Range: </td>
			            <td>
			            	<input id="minAge" name="minAge" type="number" value="${minAge}"/>
							-
							<input id="maxAge" name="maxAge" type="number" value="${maxAge}"/>
						</td>
			        </tr>
			        <c:forEach var="i" begin="1" end="9">
						<c:if test="${parameters[i-1][0] != null && parameters[i-1][0] != ''}">
						   	<tr id="row${i}">
								<td colspan="2">
									<!-- Hidden value - conceptId -->
									<input id="conId${i}" name="conId${i}" style="display:none" value="${parameters[i-1][0]}"/>
									
									<!-- Displaying the concept name -->
									${parameters[i-1][1]}
									
									<!-- Keeping concept name as hidden input -->
									<input id="name${i}" name="name${i}" style="display:none" value="${parameters[i-1][1]}"/>
									
									<!-- Keeping concept type as a hidden input -->
									<input id="type${i}" name="type${i}" style="display:none" value="${parameters[i-1][2]}"/>
									
									<!-- Displaying operators based on concept type -->
									<c:if test="${parameters[i-1][2] == '1'}"><!-- Numeric -->
										<select name="op${i}">							
											<option value="=" <c:if test="${'=' == parameters[i-1][3]}">selected</c:if>>Equals to</option>							
											<option value="<" <c:if test="${'<' == parameters[i-1][3]}">selected</c:if>>Less Than</option>						
											<option value=">" <c:if test="${'>' == parameters[i-1][3]}">selected</c:if>>More Than</option>						
											<option value="=<" <c:if test="${'=<' == parameters[i-1][3]}">selected</c:if>>Less Than or Equals to</option>			
											<option value=">=" <c:if test="${'>=' == parameters[i-1][3]}">selected</c:if>>More Than or Equals to</option>	
										</select>
										<!-- Here we will enter the provided comparison values -->
										<input id="val${i}" name="val${i}" value="${parameters[i-1][4]}"/>
									</c:if>
									<c:if test="${parameters[i-1][2] == '2'}"><!-- Coded -->
									</c:if>
									<c:if test="${parameters[i-1][2] == '3'}"><!-- Text -->
										<select name="op${i}">			
											<option value="="  <c:if test="${'=' == parameters[i-1][3]}">selected</c:if>>Equals</option>
											<option value="LIKE"  <c:if test="${'LIKE' == parameters[i-1][3]}">selected</c:if>>Like</option>
										</select>
										
										<!-- Here we will enter the provided comparison values -->
										<input id="val${i}" name="val${i}" value="${parameters[i-1][4]}"/>
									</c:if>
									<c:if test="${parameters[i-1][2] == '4'}"><!-- Type N/A -->
									</c:if>
									<c:if test="${parameters[i-1][2] == '5'}"><!-- Document -->
									</c:if>
									<c:if test="${parameters[i-1][2] == '6'}"><!-- Date -->
										<select name="op${i}">							
											<option value="=" <c:if test="${'=' == parameters[i-1][3]}">selected</c:if>>Equals to</option>							
											<option value="<" <c:if test="${'<' == parameters[i-1][3]}">selected</c:if>>Less Than</option>						
											<option value=">" <c:if test="${'>' == parameters[i-1][3]}">selected</c:if>>More Than</option>						
											<option value="=<" <c:if test="${'=<' == parameters[i-1][3]}">selected</c:if>>Less Than or Equals to</option>			
											<option value=">=" <c:if test="${'>=' == parameters[i-1][3]}">selected</c:if>>More Than or Equals to</option>	
										</select>
										
										<!-- Here we will enter the provided comparison values -->
										<input id="val${i}" name="val${i}" value="${parameters[i-1][4]}"/>
									</c:if>
									<c:if test="${parameters[i-1][2] == '7'}"><!-- Time -->
										<select name="op${i}">							
											<option value="=" <c:if test="${'=' == parameters[i-1][3]}">selected</c:if>>Equals to</option>							
											<option value="<" <c:if test="${'<' == parameters[i-1][3]}">selected</c:if>>Less Than</option>						
											<option value=">" <c:if test="${'>' == parameters[i-1][3]}">selected</c:if>>More Than</option>						
											<option value="=<" <c:if test="${'=<' == parameters[i-1][3]}">selected</c:if>>Less Than or Equals to</option>			
											<option value=">=" <c:if test="${'>=' == parameters[i-1][3]}">selected</c:if>>More Than or Equals to</option>	
										</select>
										
										<!-- Here we will enter the provided comparison values -->
										<input id="val${i}" name="val${i}" value="${parameters[i-1][4]}"/>
									</c:if>
									<c:if test="${parameters[i-1][2] == '8'}"><!-- Datetime -->
										<select name="op${i}">							
											<option value="=" <c:if test="${'=' == parameters[i-1][3]}">selected</c:if>>Equals to</option>							
											<option value="<" <c:if test="${'<' == parameters[i-1][3]}">selected</c:if>>Less Than</option>						
											<option value=">" <c:if test="${'>' == parameters[i-1][3]}">selected</c:if>>More Than</option>						
											<option value="=<" <c:if test="${'=<' == parameters[i-1][3]}">selected</c:if>>Less Than or Equals to</option>			
											<option value=">=" <c:if test="${'>=' == parameters[i-1][3]}">selected</c:if>>More Than or Equals to</option>	
										</select>
										
										<!-- Here we will enter the provided comparison values -->
										<input id="val${i}" name="val${i}" value="${parameters[i-1][4]}"/>
									</c:if>
									<!-- There is not type #9 -->
									<c:if test="${parameters[i-1][2] == '10'}"><!-- Boolean -->
										<select name="op${i}">							
											<option value="=">Is</option>											
											<option value="!=">Is Not</option>	
										</select>	
										
										<!-- Here we will enter the provided comparison values -->
										<!-- Boolean type should only have 2 values to compare to -->
										<select name="val${i}">							
											<option value="1">TRUE</option>											
											<option value="0">FALSE</option>	
										</select>	
									</c:if>
									<c:if test="${parameters[i-1][2] == '11'}"><!-- Rule -->
									</c:if>
									<c:if test="${parameters[i-1][2] == '12'}"><!-- Structured Numeric -->
									</c:if>
									<c:if test="${parameters[i-1][2] == '13'}"><!-- Complex -->
									</c:if>	
									
									<input type='button' name="remove${i}" value="Remove Criteria" align="right" onclick="deleteRow(this)"/>
								</td>
							</tr>
						</c:if>
					</c:forEach>
					<!-- Hide addition of new criteria upon reaching the maximum of 9 -->
					<c:if test="${true}">
				        <tr>
							<td colspan="2">
								<openmrs:fieldGen type="org.openmrs.Concept" formFieldName="conceptField" val="" parameters="includeVoided=false|noBind=true|optionHeader=[blank]" />
								<div id="conceptField_error" class="error" style="display:none"></div>
								<input type='submit' name="add" value="Add Concept to Criteria set"/>
							</td>
						</tr>
					</c:if>
			    </table>
		    </td>
		    <td>
		    	<table id="legend">		    	
		    	</table>
		    </tr>
	    </td>
    </table>
    
   	<script type="text/javascript">
   	function deleteRow(el) {

   	  // while there are parents, keep going until reach TR 
   	  while (el.parentNode && el.tagName.toLowerCase() != 'tr') {
   	    el = el.parentNode;
   	  }

   	  // If el has a parentNode it must be a TR, so delete it
   	  // Don't delte if only 3 rows left in table
   	  if (el.parentNode && el.parentNode.rows.length > 3) {
   	    el.parentNode.removeChild(el);
   	  }
   	}
   	</script>					
    
		
	<input type="submit"  name="execute" value="<spring:message code="groovy.report.run"/>"/>
	<!-- <button onclick='showMap()'>Show on Map</button> 
	
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
    <div id="map" class="map"></div> -->
    
	<link rel="stylesheet" href="http://openlayers.org/en/v3.0.0/css/ol.css" type="text/css">
    <script src="http://openlayers.org/en/v3.0.0/build/ol.js" type="text/javascript"></script>
   
	
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
			