package com.pentaho.metaverse.api.model;

/**
 * IVersionInfo represents a version of some artifact, including the version string, name, and description
 */
public interface IVersionInfo extends IInfo {

  /**
   * Returns the version string for this artifact
   *
   * @return a string representation of the version
   */
  public String getVersion();

  /**
   * Sets the version string for this artifact
   *
   * @param version the version string to set for the artifact
   */
  public void setVersion( String version );
}
