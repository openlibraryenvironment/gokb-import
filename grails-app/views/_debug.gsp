<pre>
<% /*println Environment.current */%>
<% /*println Environment.current.documentUploadFolder */%>
<% println request.properties.collect{it}.join('\n') %>
</pre>