/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2014 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 */
package com.pentaho.metaverse.api;

import com.google.common.collect.Multimap;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.metaverse.MetaverseException;

import java.util.List;

/**
 * Created by mburgess on 1/21/15.
 */
public interface ILineageClient {

  /**
   * Finds the step(s) in the given transformation that created the given field
   *
   * @param transMeta
   * @param targetStepName
   * @param fieldName
   * @return a list of step names, where each step has created a field with the given name
   * @throws MetaverseException
   */
  public List<String> getCreatorSteps( TransMeta transMeta, String targetStepName, String fieldName )
    throws MetaverseException;

  /**
   * Finds the step(s) in the given transformation that created the given field, with respect to the given target step.
   * This means if a field has been renamed or derived from another field from another step, then the lineage graph
   * is traversed back from the target step to determine which steps contributed to the field in the target step.
   * This differs from getCreatorSteps() as the lineage graph traversal will not stop with a "creates" relationship;
   * rather, this method will traverse other relationships ("uses", "derives", e.g.) to find the actual origin fields
   * that comprise the final field in the target step.
   *
   * @param transMeta
   * @param targetStepName
   * @param fieldName
   * @return a map of step names to field names, where each step has created a field with the returned name, and that
   * field has contributed in some way to the specified field in the target step.
   * @throws MetaverseException
   */
  public Multimap<String, String> getOriginSteps( TransMeta transMeta, String targetStepName, String fieldName )
    throws MetaverseException;


}
