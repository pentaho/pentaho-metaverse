/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
