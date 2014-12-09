package com.pentaho.metaverse.analyzer.kettle;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pentaho.metaverse.api.model.IInfo;
import flexjson.JSONSerializer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The ComponentDerivationRecord is a collection of information about a change to a component, including
 * named operation(s) performed to derive the component. For example, a component derivation record for a
 * transformation stream field may include a named operation such as "modified" or "calculated".
 */
@JsonTypeInfo( use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = IInfo.JSON_PROPERTY_CLASS )
public class ComponentDerivationRecord {

  protected String changedEntityName;
  protected String originalEntityName;

  protected Map<String, List<String>> operations;

  public ComponentDerivationRecord() {
    operations = new HashMap<String, List<String>>();
  }

  public ComponentDerivationRecord( String originalEntityName, String changedEntityName ) {
    this();
    this.changedEntityName = changedEntityName;
    this.originalEntityName = originalEntityName;
  }

  public ComponentDerivationRecord( String changedEntityName ) {
    this();
    this.changedEntityName = changedEntityName;
    this.originalEntityName = changedEntityName;
  }

  public String getChangedEntityName() {
    return changedEntityName;
  }

  public void setChangedEntityName( String changedEntityName ) {
    this.changedEntityName = changedEntityName;
  }

  public String getOriginalEntityName() {
    return originalEntityName;
  }

  public void setOriginalEntityName( String originalEntityName ) {
    this.originalEntityName = originalEntityName;
  }

  /**
   * returns a named list (i.e. map) of operations and their operands
   *
   * @return a Map from the operation name to a list of operands
   */
  public Map<String, List<String>> getOperations() {
    if ( operations == null ) {
      operations = new HashMap<String, List<String>>();
    }

    return operations;
  }

  /**
   * Adds or sets the operand list for an operation with the specified name
   *
   * @param operationName the name of the operation
   * @param operandList   a list of operands
   */
  public void putOperation( String operationName, List<String> operandList ) {
    if ( operations == null ) {
      operations = new HashMap<String, List<String>>();
    }
    operations.put( operationName, operandList );
  }

  public void addOperand( String operationName, String operand ) {
    if ( operand != null ) {
      if ( operations == null ) {
        operations = new HashMap<String, List<String>>();
      }
      List<String> operands = operations.get( operationName );
      if ( operands == null ) {
        operands = new LinkedList<String>();
        operations.put( operationName, operands );
      }
      operands.add( operand );
    }
  }

  public boolean hasDelta() {
    return operations != null && !operations.isEmpty();
  }

  @Override
  public String toString() {
    return new JSONSerializer().include( "*" ).serialize( operations );
  }
}
