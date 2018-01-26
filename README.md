# S3 for Java
S34J, or S3 for Java is an attempt to implement the S3 REST API in Java. S34J is inspired by minio and will support all minio 's features in the future.
At the current stage, it can be used to serve files from any NIO supported path, which includes: local disk, WebDAV, zip file. It can be used embedded in any web app as a Filter or a Servlet or as a Standalone server.

### Supported API:
- PUT / DELETE Bucket
- DELETE Multiple objects
- GET Bucket Location
- GET / PUT / DELETE Bucket Policy. Handle a subset of Bucket Policy
- GET list of multi part upload
- List Objects (both V1 and V2)
- HEAD / GET /  DELETE Objects
- PUT Objects. Support both chunked and normal upload
- PUT Objects (Copy)
- POST Object with resigned request. Post Policy is supported.
- Initialize multi part upload
- Upload Part. Upload Part (Copy) is not supported.
- Complete multi part upload
- Abort multi part upload
### Current feature:
- Standalone server
- Embedded servlet
- Embedded filter
### Planned features:
- Clustered server
- Canned ACL
- Docker release (standalone and clustered)
### Possible features:

- Object versioning
- Multiple access keys + secret keys

### Quick start guide
S34J can be used in Embedded or Standalone mode
#### Embedded
##### Installation
To be documented
##### Configuration
SimpleStorageServlet can be used to serve any NIO path with S3 API. Below is 
a sample web.xml file:
```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
		 http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
    <servlet>
        <servlet-name>SimpleStorageServlet</servlet-name>
        <servlet-class>us.cuatoi.s34jserver.core.servlet.SimpleStorageServlet</servlet-class>
        <init-param>
            <param-name>accessKey</param-name>
            <param-value>Q3AM3UQ867SPQQA43P2F</param-value>
        </init-param>
        <init-param>
            <param-name>secretKey</param-name>
            <param-value>zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG</param-value>
        </init-param>
        <init-param>
            <param-name>path</param-name>
            <param-value>/data</param-value>
        </init-param>
        <init-param>
            <param-name>region</param-name>
            <param-value>us-central-1}</param-value>
        </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>SimpleStorageServlet</servlet-name>
        <url-pattern>/*</url-pattern>
    </servlet-mapping>
</web-app>
```
Or it can be configured in SpringBoot:
```java
    @Bean
    public ServletRegistrationBean servletRegistrationBean() throws IOException {
        HashMap<String, String> parameters = Maps.newHashMap();
        parameters.put("region", region);
        parameters.put("secretKey", secretKey);
        parameters.put("accessKey", accessKey);
        parameters.put("path", path);
        String uploadDir = Files.createTempDirectory("upload")
                .toAbsolutePath().toString();
        MultipartConfigElement mce = new MultipartConfigElement(uploadDir);
        ServletRegistrationBean registration = new ServletRegistrationBean();
        registration.setName(SimpleStorageServlet.class.getSimpleName());
        registration.setServlet(new SimpleStorageServlet());
        registration.setLoadOnStartup(1);
        registration.setMultipartConfig(mce);
        registration.setUrlMappings(Lists.newArrayList("/*"));
        registration.setInitParameters(parameters);

        return registration;
    }
```
If you prefer a filter, please use SimpleStorageFilter. Sample web.xml is 
provided below:
```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
		 http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
    <filter>
        <filter-name>SimpleStorageFilter</filter-name>
        <filter-class>us.cuatoi.s34jserver.core.servlet.SimpleStorageFilter</filter-class>
        <init-param>
            <param-name>accessKey</param-name>
            <param-value>Q3AM3UQ867SPQQA43P2F</param-value>
        </init-param>
        <init-param>
            <param-name>secretKey</param-name>
            <param-value>zuf+tfteSlswRu7BJ86wekitnifILbZam1KYY3TG</param-value>
        </init-param>
        <init-param>
            <param-name>path</param-name>
            <param-value>/data</param-value>
        </init-param>
        <init-param>
            <param-name>region</param-name>
            <param-value>us-central-1}</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>SimpleStorageFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
</web-app>
```
Springboot configuration is provided below:
 ```java
 @Bean
    public FilterRegistrationBean filterRegistrationBean() throws IOException {
        HashMap<String, String> parameters = Maps.newHashMap();
        parameters.put("region", region);
        parameters.put("secretKey", secretKey);
        parameters.put("accessKey", accessKey);
        parameters.put("path", path);
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new SimpleStorageFilter());
        bean.setName(SimpleStorageFilter.class.getSimpleName());
        bean.setInitParameters(parameters);
        bean.setUrlPatterns(Lists.newArrayList("/*"));
        return bean;
    }
```
#### Standalone
##### Installation
To be documented
##### Configuration
To be documented