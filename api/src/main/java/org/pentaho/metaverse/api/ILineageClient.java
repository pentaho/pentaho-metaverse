/*!
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
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
package org.pentaho.metaverse.api;

import org.pentaho.di.trans.TransMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The ILineageClient interface specifies methods to be used by consumers of the lineage capabilities. These are
 * meant to be domain-driven in order to abstract the underlying graph traversals and to answer the actual
 * business question(s).
 */
public interface ILineageClient {

  /**
   * Finds the step(s) in the given transformation that created the given field, with respect to the given target step.
   * This means if a field has been renamed or derived from another field from another step, then the lineage graph
   * is traversed back from the target step to determine which steps contributed to the field in the target step.
   * This differs from getCreatorSteps() as the lineage graph traversal will not stop with a "creates" relationship;
   * rather, this method will traverse other relationships ("uses", "derives", e.g.) to find the actual origin fields
   * that comprise the final field in the target step.
   *
   * @param transMeta      a reference to a transformation's metadata
   * @param targetStepName the target step name associated with the given field names
   * @param fieldNames     a collection of field names associated with the target step, for which to find the step(s)
   *                       and field(s) that contributed to those fields
   * @return a map from target field name to step-field objects, where each step has created a field with
   * the returned name, and that field has contributed in some way to the specified target field.
   * @throws MetaverseException if an error occurred while finding the origin steps
   */
  public Map<String, Set<StepField>> getOriginSteps(
    TransMeta transMeta, String targetStepName, Collection<String> fieldNames ) throws MetaverseException;

  /**
   * Returns the paths between the origin field(s) and target field(s). A path in this context is an ordered list of
   * StepFieldOperations objects, each of which corresponds to a field at a certain step where operation(s) are
   * applied. The order of the list corresponds to the order of the steps from the origin step (see getOriginSteps())
   * to the target step. This method can be used to trace a target field back to its origin and discovering what
   * operations were performed upon it during it's lifetime. Inversely the path could be used to re-apply the operations
   * to the origin field, resulting in the field's "value" at each point in the path.
   *
   * @param transMeta      a reference to a transformation's metadata
   * @param targetStepName the target step name associated with the given field names
   * @param fieldNames     a collection of field names associated with the target step, for which to find the step(s)
   *                       and field(s) and operation(s) that contributed to those fields
   * @return a map of target field name to a set of paths. Each path is an ordered list of StepFieldOperations objects,
   * describing the path from the origin step field to the target step field, including the operations performed.
   * @throws MetaverseException if an error occurred while finding the origin steps
   */
   Map<String, Set<List<StepFieldOperations>>> getOperationPaths(
    TransMeta transMeta, String targetStepName, Collection<String> fieldNames ) throws MetaverseException;
}
