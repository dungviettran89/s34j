/*
 * Copyright (C) 2018 dungviettran89@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package us.cuatoi.s34j.spring;

public class SpringStorageConstants {
    public static final String STREAMING_PAYLOAD = "STREAMING-AWS4-HMAC-SHA256-PAYLOAD";
    public static final String UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD";
    public static final String BLANK_PAYLOAD = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    public static final String SCHEME = "AWS4";
    public static final String ALGORITHM = "HMAC-SHA256";
    public static final String SERVICE = "s3";
    public static final String TERMINATOR = "aws4_request";

    public static final String EXPIRATION_DATE_FORMAT = "yyyy-MM-dd'T'HH':'mm':'ss'.'SSS'Z'";
    public static final String X_AMZ_DATE_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
    public static final String HTTP_HEADER_DATE_FORMAT = "EEE',' dd MMM yyyy HH':'mm':'ss 'GMT'";
    public static final String SCOPE_DATE_FORMAT = "yyyyMMdd";

    public static final String CONTENT_TYPE = "application/xml; charset=utf-8";
    public static final String STORAGE_CLASS = "STANDARD";
}
