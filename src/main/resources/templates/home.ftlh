<head>
<title>Badminton</title>
<meta name="viewport" content="width=100, initial-scale=1">
</head>
<form href="/" method="POST">
<p>
<textarea name="names" cols=15 rows=12>
<#list players as p>
${p}
</#list>
</textarea>
<p>
<input type="submit" name="action" value="Next"/>
<p>
<textarea name="polygon" cols=15 rows=12 hidden="true">
<#list polygon as p>
${p}
</#list>
</textarea>
<p>
<b>
<#list matches as m>
${m}
<br>
</#list>
${unpaired}
</b>
