<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>  
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<table border="1px">
		<tr>	
			<td>id</td>
			<td>书名</td>
			<td>数量</td>
			<td>操作</td>
		</tr>
		<c:forEach items="${list}" var="l">
			<tr>
				<td>${l.bookId }</td>
				<td>${l.name}</td>
				<td>${l.number}</td>
				<td>
					<a href="<c:url value="/book/editBook?bookId=${l.bookId}"/>">修改</a>
					<a href="<c:url value="/book/delBook/${l.bookId}"/>">删除</a>
				</td>
			</tr>
		</c:forEach>
	</table>
	<br/><br/>
	
	
	<span>这里用来测试@ModelAttribute</span>
	<!-- 这里并未修改书的所有信息，价格是没有修改的，但是又不能传递null过去 -->
	<form action="<c:url value="/book/testModelAttribute"/>">
		编号：<input name="bookId" value="1000">
		书名：<input name="name" value="激荡十年">
		数量：<input name="number" value="12">
		<button type="submit" value="submit">提交</button>
	</form>
	
	<span>新增</span>
	
	<div>
		<c:set var="ctx" value="${pageContext.request.contextPath}"/>
	</div>
	
	<form:form action="${ctx}/book/newBook" method="post"  commandName="book">
		编号：<form:input path="bookId" />
		书名：<form:input path="name" />
		数量：<form:input path="number" />
		价格：<form:input path="price" />
		<button type="submit" value="submit">提交</button>
	</form:form>
	
	<%-- <form action="${ctx}/book/newBook" method="post">
		编号：<input name="bookId" />
		书名：<input name="name" />
		数量：<input name="number" />
		价格：<input name="price" />
		<button type="submit" value="submit">提交</button>
	
	</form> --%>
	
	<br/>

	<!-- method必须为post 及enctype属性-->
	<form action="${ctx}/book/bookUpload" method="post" enctype="multipart/form-data">
		<input type="file" name="file">
		<input type="submit" value="上传">
	</form>

	<div>
		<table border="1px">
			<tr>
				<td>姓名</td>
				<td>年龄</td>
				<td>生日</td>
			</tr>
			<c:forEach items="${persions}" var="p">
				<tr>
					<td>${p.name}</td>
					<td>${p.age}</td>
					<td>${p.birthday}</td>
					<td>
						<a href="<c:url value="/book/editBook?bookId=${l.bookId}"/>">修改</a>
						<a href="<c:url value="/book/delBook/${l.bookId}"/>">删除</a>
					</td>
				</tr>
			</c:forEach>
		</table>


	</div>

	<div>
		<form:form action="${ctx}/book/newPersion" method="post" commandName="persion">
			姓名：<form:input path="name" />
			年龄：<form:input path="age" />
			生日：<form:input path="birthday" />
			<button type="submit" value="submit">提交</button>
		</form:form>
	</div>
	
</body>
</html>