<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>明细</title>

</head>
<body>
	
	<table border="1px">
		<tr>
			<td>id</td>
			<td>书名</td>
			<td>数量</td>
		</tr>
		<tr>
			<td>${book.bookId}</td>
			<td>${book.name}</td>
			<td>${book.number}</td>
		</tr>
	</table>

	<br/><br/><br/>
	<%-- commandName="book" --%>
	<div>
		<c:set var="ctx" value="${pageContext.request.contextPath}"/>
	</div>
	
	<!-- form 默认需要一个域对象，其名为command -->
	<form:form action="${ctx}/book/updateBook" commandName="book">
		<form:hidden path="bookId" />
	书名：<form:input path="name" />
	数量：<form:input path="number" />
	价格：<form:input path="price" />
		<button type="submit" value="submit">提交</button>
	</form:form>
</body>
</html>