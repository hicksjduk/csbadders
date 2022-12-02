<head>
<title>Badminton</title>
<meta name="viewport" content="width=100, initial-scale=1">
</head>
<form href="/" method="POST">
<p>
Player names (one per line):
<br><textarea name="names" cols=15 rows=12>
<#list players as p>
${p}
</#list>
</textarea>
<br>Duplicate names and <br>blank lines are ignored.
<p>
<input type="submit" name="action" value="Next"/>
<p>
<textarea name="xxx" cols=15 rows=12 hidden="true">
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
