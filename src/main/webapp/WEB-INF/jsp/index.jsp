<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>TATU - L&auml;ht&ouml;tiedostot</title>
</head>
<body>
	<h2>Valitse l&auml;ht&ouml;tiedoston muoto</h2>
	<form:form method="POST" action="../tatu/upload.html">
		<table>
			<tr>
			<td><input type="submit" name="kuv" value="KUV + DAT" /></td>
			<td><input type="submit" name="csv" value="CSV" /></td>
			<td><input type="submit" name="dbf" value="DBF" /></td>
			</tr>
		</table>  
	</form:form>
	
	<br>
	<br>
	<div style="padding:30px;  background-color:LightYellow;">
	<h4>M&auml;&auml;r&auml;muotoisen taulun k&auml;sitelt&auml;v&auml;t kent&auml;t (csv/dbf)</h4>

	<ul>
		<li>TIE (/ TIENRO) - Tienumero - Pakollinen</li>
		<li>AJR (/ AJORATA) - Ajoratanumero - Pakollinen</li>
		<li>AOSA (/ OSA / ALKUOSA) - Alkupisteen tieosanumero - Pakollinen</li>
		<li>AET (/ ALKUET / ET / ETAISYYS) - Alkupisteen et&auml;isyys</li>
		<li>LOSA (/ LOPPUOSA) - Loppupisteen tieosanumero</li>
		<li>LET (/ LOPPUET) - Loppupisteen et&auml;isyys</li>
	</ul>
	
	<h4>Huomaa my&ouml;s</h4>
	<ul>
		<li>Tiedoston nimess&auml; ei voi k&auml;ytt&auml;&auml; skandinaavisia merkkej&auml; (&aring;&auml;&ouml;...)</li>
		<li>Sama kent&auml;n nimi ei saa esiinty&auml; tiedostossa useampaan kertaan.</li>
		<li>Kenttien nimiss&auml; ei voi k&auml;ytt&auml;&auml; skandinaavisia merkkej&auml; (&aring;&auml;&ouml;...) DBF-tiedostoissa. Muissa tiedostomuodoissa kelpaavat, jos tiedoston merkist&ouml; on UTF-8.</li>
		<li>Tatu lyhent&auml;&auml; yli 10 merkki&auml; pitk&auml;t kenttien nimet 10 merkkiin.</li>
		<li>Muunnos nopeutuu olennaisesti, jos l&auml;ht&ouml;tiedoston rivit on lajiteltu tienumerokent&auml;n mukaan joko nousevaan tai laskevaan j&auml;rjestykseen.</li>
		<li>Kenttien j&auml;rjestyksell&auml; ei ole merkityst&auml;.</li>
		<li>Ei ole v&auml;li&auml;, onko kenttien nimet kirjoitettu isolla vai pienell&auml;.</li>
		<li>Yll&auml; luetellut vaihtoehtoiset kenttien nimet (sulkeissa) eiv&auml;t ole k&auml;yt&ouml;ss&auml; KUV+DAT muotoisessa aineistossa.</li>
		<li>L&auml;ht&ouml;tiedostojen merkist&ouml;koodauksen tulee olla UTF-8, jos ne sis&auml;lt&auml;v&auml;t skandimerkkej&auml; (&aring;&auml;&ouml;...). Jos tulostiedostossa skandinaaviset kirjaimet (&aring;&auml;&ouml;...) ovat korvaantuneet kysymysmerkeill&auml;, l&auml;ht&ouml;tiedosto on todenn&auml;k&ouml;isesti jossain muussa merkist&ouml;ss&auml; kuin UTF-8.</li>
	</ul>
	</div>
	
</body>
</html>
