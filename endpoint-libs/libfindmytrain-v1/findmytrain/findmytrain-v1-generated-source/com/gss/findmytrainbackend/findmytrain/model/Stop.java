/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2014-07-22 21:53:01 UTC)
 * on 2014-08-21 at 08:29:21 UTC 
 * Modify at your own risk.
 */

package com.gss.findmytrainbackend.findmytrain.model;

/**
 * Model definition for Stop.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the findmytrain. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class Stop extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Key id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String station;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String time;

  /**
   * @return value or {@code null} for none
   */
  public Key getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public Stop setId(Key id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getStation() {
    return station;
  }

  /**
   * @param station station or {@code null} for none
   */
  public Stop setStation(java.lang.String station) {
    this.station = station;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getTime() {
    return time;
  }

  /**
   * @param time time or {@code null} for none
   */
  public Stop setTime(java.lang.String time) {
    this.time = time;
    return this;
  }

  @Override
  public Stop set(String fieldName, Object value) {
    return (Stop) super.set(fieldName, value);
  }

  @Override
  public Stop clone() {
    return (Stop) super.clone();
  }

}
