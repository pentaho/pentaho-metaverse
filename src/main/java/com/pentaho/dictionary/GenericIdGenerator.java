package com.pentaho.dictionary;

/**
 * This is a temporary/test class for generating ids
 * @author jdixon
 *
 */
public class GenericIdGenerator implements IIdGenerator {

  private static final String SEPARATOR = "~";

  private String type;

  /**
   * Creates a generic id generator of a given type
   * @param type The type of the id, this will be prepended to every id created
   */
  public GenericIdGenerator( String type ) {
    this.type = type;
  }

  @Override
  public String[] getTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getId( String... tokens ) {
    StringBuilder id = new StringBuilder();
    id.append( type );
    for ( String token : tokens ) {
      id.append( SEPARATOR );
      id.append( token );
    }
    return id.toString();
  }

}
