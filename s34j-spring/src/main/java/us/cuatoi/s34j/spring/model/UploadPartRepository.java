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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UploadPartRepository extends CrudRepository<UploadPartModel, String> {

    void deleteByBucketName(String bucketName);

    UploadPartModel findOneByUploadPartOrderAndUploadId(long partNumber, String uploadId);

    Page<UploadPartModel> findAllByUploadIdAndUploadPartOrderGreaterThanOrderByUploadPartOrder(String uploadId, long partNumberMarker, Pageable page);

    List<UploadPartModel> findAllByUploadId(String uploadId);
}
