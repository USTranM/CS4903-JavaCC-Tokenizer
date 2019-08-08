<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"
    import = "java.lang.System,query.*"
    
    %>
<!DOCTYPE html>
<html lang="en">

<head>
	<title>Mew Search</title>
	<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
	<link rel="stylesheet" href="style.css">
</head>

<body> 

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  <a class="navbar-brand" href="index.jsp">Mew Search</a>
</nav>

<%

	if (request.getParameter("search") != null) {
		
	
		String query = (String)request.getParameter("search");
		out.print(query);
		UAQuery uaquery = new UAQuery();
		String[] split = query.split(" ");
		String[] topTenPreSplit = uaquery.runQuery(split);
		
		String[][] topTenSplit = new String[10][1];
		
		for (int i = 0; i < topTenPreSplit.length; i++) {
			topTenSplit[i] = topTenPreSplit[i].split(",");
		}
		out.println();
		if (topTenSplit != null) {
			for (int i = 0; i < topTenSplit.length; i++) {
				if (topTenSplit[i] == null) {
					out.println("End of query. If no results are shown, then there are no documents related to query search.");
		%>
					<br /> 
		<%
					break;
				}
		%>
	
	
	<div class="card" style="width: 18rem;">
	  <div class="card-body">
	    <h5 class="card-title"><%= topTenSplit[i][1]%></h5>
	    <h6 class="card-subtitle mb-2 text-muted"><%= topTenSplit[i][0] + ", " + topTenSplit[i][2]%></h6>
	  </div>
	</div>
	
	
	
	<%
			}
			
			
		} else {
			response.sendRedirect("index.jsp");
		}
	}

	%>

<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js" integrity="sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1" crossorigin="anonymous"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js" integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM" crossorigin="anonymous"></script>

</body>

</html>