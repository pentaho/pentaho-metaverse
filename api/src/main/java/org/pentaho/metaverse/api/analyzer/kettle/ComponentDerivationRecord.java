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


package org.pentaho.metaverse.api.analyzer.kettle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.pentaho.metaverse.api.ChangeType;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.model.IInfo;
import org.pentaho.metaverse.api.model.IOperation;
import org.pentaho.metaverse.api.model.Operation;
import org.pentaho.metaverse.api.model.Operations;

import java.util.List;

/**
 * The ComponentDerivationRecord is a collection of information about a change to a component, including
 * named operation(s) performed to derive the component. For example, a component derivation record for a
 * transformation stream field may include a named operation such as "modified" or "calculated".
 */
@JsonTypeInfo( use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = IInfo.JSON_PROPERTY_CLASS )
public class ComponentDerivationRecord {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected StepField originalField;
  protected StepField changedField;

  protected ChangeType changeType;

  protected Operations operations;

  public ComponentDerivationRecord() {
    changeType = ChangeType.METADATA;
    operations = new Operations();
    originalField = new StepField();
    changedField = new StepField();
  }

  public ComponentDerivationRecord( StepField originalField, StepField changedField ) {
    this();
    this.originalField = originalField;
    this.changedField = changedField;
  }

  public ComponentDerivationRecord( StepField originalField, StepField changedField, ChangeType changeType ) {
    this();
    this.originalField = originalField;
    this.changedField = changedField;
    this.changeType = changeType;
  }

  public ComponentDerivationRecord( String originalEntityName, String changedEntityName ) {
    this( originalEntityName, changedEntityName, ChangeType.METADATA );
  }

  public ComponentDerivationRecord( String originalEntityName, String changedEntityName, ChangeType changeType ) {
    this();
    this.originalField = new StepField( "", originalEntityName );
    this.changedField = new StepField( "", changedEntityName );
    this.changeType = changeType;
  }

  public ComponentDerivationRecord( String changedEntityName, ChangeType changeType ) {
    this();
    this.originalField = new StepField( "", changedEntityName );
    this.changedField = new StepField( "", changedEntityName );
    this.changeType = changeType;
  }

  public ComponentDerivationRecord( String changedEntityName ) {
    this( changedEntityName, ChangeType.METADATA );
  }

  public ChangeType getChangeType() {
    return changeType;
  }

  public void setChangeType( ChangeType changeType ) {
    this.changeType = changeType;
  }

  public String getChangedEntityName() {
    return changedField.getFieldName();
  }

  public void setChangedEntityName( String changedEntityName ) {
    changedField.setFieldName( changedEntityName );
  }

  public String getOriginalEntityName() {
    return originalField.getFieldName();
  }

  public void setOriginalEntityName( String originalEntityName ) {
    originalField.setFieldName( originalEntityName );
  }

  public String getOriginalEntityStepName() {
    return originalField.getStepName();
  }

  public void setOriginalEntityStepName( String stepName ) {
    originalField.setStepName( stepName );
  }

  public String getChangedEntityStepName() {
    return changedField.getStepName();
  }

  public void setChangedEntityStepName( String stepName ) {
    changedField.setStepName( stepName );
  }

  public StepField getOriginalField() {
    return originalField;
  }

  public void setOriginalField( StepField originalField ) {
    this.originalField = originalField;
  }

  public StepField getChangedField() {
    return changedField;
  }

  public void setChangedField( StepField changedField ) {
    this.changedField = changedField;
  }

  /**
   * returns a named list (i.e. map) of operations and their operands
   *
   * @return a Map from the operation name to a list of operands
   */
  public List<IOperation> getOperations( ChangeType type ) {
    return getOperations().get( type );
  }

  /**
   * Returns the Operations associated with this record
   *
   * @return an Operations object containing this record's associated data/metadata operations
   */
  public Operations getOperations() {
    if ( operations == null ) {
      operations = new Operations();
    }
    return operations;
  }

  /**
   * Adds or sets the operand list for an operation with the specified name
   *
   * @param operation the operation to add
   */
  public void addOperation( Operation operation ) {
    if ( operation != null ) {
      getOperations().addOperation( operation.getType(), operation );
    }
  }

  /**
   * Determines whether this record represents a change in metadata or data, by inspecting how many operations have
   * been applied
   *
   * @return true if this record has changed the associated field, false otherwise
   */
  public boolean hasDelta() {
    return operations != null && !operations.isEmpty();
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    ComponentDerivationRecord that = (ComponentDerivationRecord) o;

    if ( originalField != null ? !originalField.equals( that.originalField ) : that.originalField != null ) {
      return false;
    }
    if ( changedField != null ? !changedField.equals( that.changedField ) : that.changedField != null ) {
      return false;
    }
    if ( changeType != that.changeType ) {
      return false;
    }
    return !( operations != null ? !operations.equals( that.operations ) : that.operations != null );

  }

  @Override public int hashCode() {
    int result = originalField != null ? originalField.hashCode() : 0;
    result = 31 * result + ( changedField != null ? changedField.hashCode() : 0 );
    result = 31 * result + ( changeType != null ? changeType.hashCode() : 0 );
    result = 31 * result + ( operations != null ? operations.hashCode() : 0 );
    return result;
  }

  @Override
  public String toString() {
    ObjectNode root = OBJECT_MAPPER.createObjectNode();

    if ( operations != null ) {
      for ( ChangeType ct : ChangeType.values() ) {
        List<IOperation> operationsForType = operations.get( ct );
        if ( operationsForType == null ) {
          continue;
        }

        ArrayNode operationArray = root.putArray( ct.toString() );
        for ( IOperation operation : operationsForType ) {
          ObjectNode operationNode = operationArray.addObject();
          operationNode.put( "category", operation.getCategory() );
          operationNode.put( "class", operation.getClass().getName() );
          operationNode.put( "description", operation.getDescription() );
          operationNode.put( "name", operation.getName() );
          operationNode.put( "type", operation.getType() == null ? null : operation.getType().name() );
        }
      }
    }

    try {
      return OBJECT_MAPPER.writeValueAsString( root );
    } catch ( JsonProcessingException e ) {
      // Return a safe fallback string instead of throwing, to avoid surprising failures
      // in logging/debugging code paths that invoke toString()
      return getClass().getSimpleName() + "@" + Integer.toHexString( hashCode() );
    }
  }
}
