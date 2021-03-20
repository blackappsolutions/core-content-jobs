https://documentation.coremedia.com/cmcc-10/artifacts/2101.2/javadoc/common/com/coremedia/cap/server/importexport/base/exporter/ServerExporter.html

coremedia-io-2010.2.jar!/com/coremedia/io/URLLoader.class
```
if ("file".equals(url.getProtocol())) {
    var17 = Paths.get(url.toURI()).toFile().exists();
    return var17;
}

if (urlConnection instanceof AwsS3URLConnection) {
    var17 = ((AwsS3URLConnection)urlConnection).exists();
    return var17;
}
 
if (!(urlConnection instanceof HttpURLConnection)) {
    InputStream inputStream = urlConnection.getInputStream();
    
    [..] // https://stackoverflow.com/a/16507509
    
    // With Basic Auth
    String userInfo = url.getUserInfo();
    if (userInfo != null) {
        String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userInfo.getBytes());
        urlConnection.setRequestProperty("Authorization", basicAuth);
    }
```
