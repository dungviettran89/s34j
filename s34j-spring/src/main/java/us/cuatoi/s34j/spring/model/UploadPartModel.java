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

package us.cuatoi.s34j.spring.model;

import com.google.gson.Gson;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;

@Entity
public class UploadPartModel {
    @Id
    @javax.persistence.Id
    private String uploadPartId;
    private long uploadPartOrder;
    private long createdDate = System.currentTimeMillis();
    private String uploadId;
    private String objectName;
    private String bucketName;
    private String etag;
    private long size;

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getUploadPartId() {
        return uploadPartId;
    }

    public void setUploadPartId(String uploadPartId) {
        this.uploadPartId = uploadPartId;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public long getUploadPartOrder() {
        return uploadPartOrder;
    }

    public void setUploadPartOrder(long uploadPartOrder) {
        this.uploadPartOrder = uploadPartOrder;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + new Gson().toJson(this);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
