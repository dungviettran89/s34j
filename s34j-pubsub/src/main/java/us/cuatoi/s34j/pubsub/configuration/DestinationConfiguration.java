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

package us.cuatoi.s34j.pubsub.configuration;

public class DestinationConfiguration {
    private String project;
    private byte[] key;

    public byte[] getKey() {
        return key.clone();
    }

    public void setKey(byte[] key) {
        this.key = key.clone();
    }

    public DestinationConfiguration withKey(byte[] key) {
        this.key = key.clone();
        return this;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public DestinationConfiguration withProject(String project) {
        this.project = project;
        return this;
    }
}
