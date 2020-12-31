<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>TATU - Tiedostojen lataus</title>
</head>
<body>
	<h2>Tiedot muunnettu. Lataa tiedosto</h2>
	<br/>
	Rivej&auml; yhteens&auml;: ${totalRows}
	<br/>
	Virheit&auml; yhteens&auml;: ${errorRows}
	
	<br />
	<br />
	<form method="post" action="../tatu/downloadFile.html?id=${id}" enctype="multipart/form-data">
		<input type="submit" value="Lataa"/>
	</form>
	<br />
	<a href="../tatu/index.html">Alkuun</a>
</body>
</html>