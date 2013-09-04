/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.apache.drill.exec.record.selection;

import io.netty.buffer.ByteBuf;

import org.apache.drill.exec.exception.SchemaChangeException;

public class SelectionVector4 {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SelectionVector4.class);

  private final ByteBuf vector;
  private final int recordCount;
  private int start;
  private int length;
  
  public SelectionVector4(ByteBuf vector, int recordCount, int batchRecordCount) throws SchemaChangeException {
    if(recordCount > Integer.MAX_VALUE /4) throw new SchemaChangeException(String.format("Currently, Drill can only support allocations up to 2gb in size.  You requested an allocation of %d bytes.", recordCount * 4));
    this.recordCount = recordCount;
    this.start = 0;
    this.length = Math.min(batchRecordCount, recordCount);
    this.vector = vector;
  }
  
  public int getTotalCount(){
    return recordCount;
  }
  
  public int getCount(){
    return length;
  }
  
  public void set(int index, int compound){
    vector.setInt(index*4, compound);
  }
  public void set(int index, int recordBatch, int recordIndex){
    vector.setInt(index*4, (recordBatch << 16) | (recordIndex & 65535));
  }
  
  public int get(int index){
    return vector.getInt( (start+index)*4);
  }
  
  /**
   * Caution: This method shares the underlying buffer between this vector and the newly created one.
   * @return Newly created single batch SelectionVector4.
   * @throws SchemaChangeException 
   */
  public SelectionVector4 createNewWrapperCurrent(){
    try {
      vector.retain();
      SelectionVector4 sv4 = new SelectionVector4(vector, length, length);
      sv4.start = this.start;
      return sv4;
    } catch (SchemaChangeException e) {
      throw new IllegalStateException("This shouldn't happen.");
    }
  }
  
  public boolean next(){
//    logger.debug("Next called. Start: {}, Length: {}, recordCount: " + recordCount, start, length);
    
    if(start + length >= recordCount){
      
      start = recordCount;
      length = 0;
//      logger.debug("Setting count to zero.");
      return false;
    }
    
    start = start+length;
    int newEnd = Math.min(start+length, recordCount);
    length = newEnd - start;
//    logger.debug("New start {}, new length {}", start, length);
    return true;
  }
  
  public void clear(){
    this.vector.clear();
  }
  
  
}