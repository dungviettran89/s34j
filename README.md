# S3 for Java
S34J, or S3 for Java is an attempt to implement the S3 REST API in Java. S34J is inspired by minio and will support all minio 's features in the future.
At the current stage, it can be used to serve files from any NIO supported path, which includes: local disk, WebDAV, zip file. It can be used embedded in any web app as a Filter or a Servlet or as a Standalone server.

### Links:
 - Documentation: https://s34j.cuatoi.us
 - UI Demo: https://s34j-demo.appspot.com/_admin/

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

