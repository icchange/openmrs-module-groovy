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
<openmrs:require privilege="List Groovy Users" otherwise="/login.htm" redirect="/module/groovy/user.list"/>
<%@ include file="/WEB-INF/template/header.jsp" %>
<!--<link rel="stylesheet" type="text/css"
      href="${pageContext.request.contextPath}/moduleResources/groovy/css/main.css"/>-->

<%@ include file="localHeader.jsp" %>

<p>
    <spring:message code="groovy.user.info"/>
</p>

<c:choose>
    <c:when test="${ fn:length(users) > 0}">
        <table style="border: dashed 1px #000;" border="1px; " >
            <tr>
            	<td>User</td>                
                <td>Name</td>                
                <td>Location</td>
                <td>Action</td>
            </tr>
            <c:forEach var="user" items="${users}" varStatus="status">
                <tr>
                	<form method="post">
                    <td>${user.user.systemId}</td>
                    <td>${user.user}</td>
                    <td>${user.location}</td>
                    <td>
                        <input type="submit" value="<spring:message code="groovy.delete"/>"/>
                    </td>
                	<input type="hidden" value="${user.id}" name="id"/>
                	<input type="hidden" value="user.delete" name="type"/>
                	<input type="hidden" value="-1" name="locationid"/>
                	</form>
                </tr>
            </c:forEach>          
        </table>
    </c:when>
    <c:otherwise>
        <h5><spring:message code="groovy.user.none"/></h5>
    </c:otherwise>
</c:choose>
<p>
<h4>Add Groovy User</h4>
<form method="post">
	<table style="border: dashed 1px #000;" border="1px; " >
		<tr>
			<td>User</td>
			<td>
				<select name="id">
					<option value="-1"></option>
					<c:forEach var="systemuser" items="${systemusers}" varStatus="status">
						<option value="${systemuser.id}">${systemuser}</option>
					</c:forEach>
				</select>
			</td>
		</tr>
		<tr>
			<td>Location</td>
			<td>
				<select name="locationid">
					<option value="-1"></option>
					<openmrs:forEachRecord name="location">
						<option value="${record.id}">${record}</option>
					</openmrs:forEachRecord>
				</select>
			</td>
		</tr>
	</table>
	<input type="hidden" value="user.add" name="type"/>
	<input type="submit" value="<spring:message code="groovy.user.add"/>"/>	
</form>

<%@ include file="/WEB-INF/template/footer.jsp" %>
