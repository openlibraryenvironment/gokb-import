<!DOCTYPE html>
<html>
<head>
    <title>Ygor - <% println session.id %></title>
    <meta name="layout" content="enrichment">
    <g:if env="development"><link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}"
                                  type="text/css"></g:if>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/css/select2.min.css" rel="stylesheet"/>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/select2/4.0.6-rc.0/js/select2.min.js"></script>
</head>

<body>
    <g:render template="processInfo"/>

    <g:render template="uploadFile"/>

    <g:render template="listDocuments"/>

</body>
</html>
