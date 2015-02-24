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
<openmrs:require privilege="Run Groovy Reports" otherwise="/login.htm" redirect="/module/groovy/report.form"/>
<%@ include file="/WEB-INF/template/header.jsp" %>
<!--<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/moduleResources/groovy/css/main.css"/>-->
<p>
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
						ONLY public scripts will be displayed -->
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
    </table>
	<input type="submit" value="<spring:message code="groovy.report.run"/>"/>
</form>
<p>
<c:choose>
	<c:when test="${ output!=null }">
		<div style="">
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
<%@ include file="/WEB-INF/template/footer.jsp" %>
