<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%! String type = "***"; %> 
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>TATU - Tiedostojen l&auml;hetys</title>
</head>
<body>
	<form method="post" action="../tatu/uploadDone.html" enctype="multipart/form-data">
		<input type="hidden" name="filetype" value="${filetype}" />
<%type = (String)pageContext.findAttribute("filetype");%>
	 
<% if (type == "KUV") { %>
			<input type="hidden" name ="tieosoiteverkon_tyyppi" value="viimeisin"/>
			<input type="hidden" name="marvot" value="Tulee"/>
			<input type="hidden" name="tieosoiteverkon_vuosi" value="" />
			
<% } else { %>
			
			<h2>Valitse k&auml;ytett&auml;v&auml; tieosoiteverkko</h2>
			<p><input type="radio" name="tieosoiteverkon_tyyppi" value="viimeisin" checked onClick="this.form.tieosoiteverkon_vuosi.disabled='disabled';"/>Viimeisin: 1.7.2020</p>
			<p><input type="radio" name="tieosoiteverkon_tyyppi" value="vanha" onClick="this.form.tieosoiteverkon_vuosi.disabled='';" />Vanha tai muu: 
			<select name="tieosoiteverkon_vuosi" disabled>
				<option value="20201">1.1.2020</option>
				<option value="20192">14.10.2019</option>
				<option value="20191">1.7.2019</option>
				<option value="2019">1.1.2019</option>
				<option value="2018">2.1.2018</option>
				<option value="20171">1.7.2017</option>
				<option value="2017">2.1.2017</option>
				<option value="2016">1.3.2016</option>
				<option value="2015">1.1.2015</option>
	    		<option value="2014">1.1.2014</option>
	    		<option value="2013">1.1.2013</option>
	    		<option value="2012">1.1.2012</option>
	    		<option value="2011">1.1.2011</option>
	    		<option value="2010">1.1.2010</option>
	    		<option value="2009">1.1.2009</option>
	    		<option value="9999">Lapin ELYn tieverkko</option>
	  		</select></p>
	  		<h2>Tuotetaanko m-arvot pistem&auml;isen&auml; shape-tiedostona mukaan?</h2>
			<p><input type="checkbox" name="marvot" value="Tulee"/> M-arvot mukaan</p>
			<p><input type="checkbox" name="paikannustapa" value="Ajorataspesifi"/> Ajorataspesifi paikannus tieosoitev&auml;leille</p>
			 
<% } %>
		<h2>Valitse ${filetype}-tiedosto ja l&auml;het&auml;</h2>
		<input type="file" name="file" size="100" />
		<input type="submit" value="L&auml;het&auml;"/>
	</form>
	<a href="../tatu/index.html">Alkuun</a>
</body>
</html>