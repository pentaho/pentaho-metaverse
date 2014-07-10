package com.pentaho.metaverse.api;

/**
 * An IIdGenerator generates unique ids for a given type of artifact. Artifact types include
 * transformations, jobs, database tables and columns, file system text files etc. Every metaverse 
 * @author jdixon
 *
 */
public interface IIdGenerator {

  /**
   * Returns the types of artifacts that this id generator supports
   * @return
   */
  public String[] getTypes();
  
  /**
   * Returns an id given a collection of tokens that represent an artifact. The number of tokens
   * is variable because some artifacts only need one e.g. a file system path where as others
   * such as a database column need more.
   * @param tokens
   * @return
   */
  public String getId( String... tokens );
  
}
