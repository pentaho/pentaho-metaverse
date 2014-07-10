package com.pentaho.metaverse.locator;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.pentaho.platform.api.locale.IPentahoLocale;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.VersionSummary;

public class TestDiUnifiedRepository implements IUnifiedRepository {

  @Override
  public boolean canUnlockFile( Serializable arg0 ) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void copyFile( Serializable arg0, String arg1, String arg2 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public RepositoryFile createFile( Serializable arg0, RepositoryFile arg1, IRepositoryFileData arg2, String arg3 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile createFile( Serializable arg0, RepositoryFile arg1, IRepositoryFileData arg2,
      RepositoryFileAcl arg3, String arg4 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile createFolder( Serializable arg0, RepositoryFile arg1, String arg2 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile createFolder( Serializable arg0, RepositoryFile arg1, RepositoryFileAcl arg2, String arg3 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteFile( Serializable arg0, String arg1 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteFile( Serializable arg0, boolean arg1, String arg2 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteFileAtVersion( Serializable arg0, Serializable arg1 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteLocalePropertiesForFile( RepositoryFile arg0, String arg1 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public RepositoryFileAcl getAcl( Serializable arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Locale> getAvailableLocalesForFile( RepositoryFile arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Locale> getAvailableLocalesForFileById( Serializable arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Locale> getAvailableLocalesForFileByPath( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFile> getChildren( Serializable arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFile> getChildren( Serializable arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> T getDataAtVersionForExecute( Serializable arg0, Serializable arg1,
      Class<T> arg2 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> T
    getDataAtVersionForRead( Serializable arg0, Serializable arg1, Class<T> arg2 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> T getDataForExecute( Serializable arg0, Class<T> arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> List<T> getDataForExecuteInBatch( List<RepositoryFile> arg0, Class<T> arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> T getDataForRead( Serializable arg0, Class<T> arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends IRepositoryFileData> List<T> getDataForReadInBatch( List<RepositoryFile> arg0, Class<T> arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFile> getDeletedFiles() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFile> getDeletedFiles( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFile> getDeletedFiles( String arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFileAce> getEffectiveAces( Serializable arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFileAce> getEffectiveAces( Serializable arg0, boolean arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile getFile( String arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile getFile( String arg0, boolean arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile getFile( String arg0, IPentahoLocale arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile getFile( String arg0, boolean arg1, IPentahoLocale arg2 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile getFileAtVersion( Serializable arg0, Serializable arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile getFileById( Serializable arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile getFileById( Serializable arg0, boolean arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile getFileById( Serializable arg0, IPentahoLocale arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile getFileById( Serializable arg0, boolean arg1, IPentahoLocale arg2 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Serializable> getFileMetadata( Serializable arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Properties getLocalePropertiesForFile( RepositoryFile arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Properties getLocalePropertiesForFileById( Serializable arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Properties getLocalePropertiesForFileByPath( String arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFile> getReferrers( Serializable arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Character> getReservedChars() {
    // TODO Auto-generated method stub
    return null;
  }

  private RepositoryFileTree createFileTree( File root ) {

    RepositoryFile repFile = new RepositoryFile( root.getPath(), root.getName(), 
        root.isDirectory(), false,
        false, null, root.getAbsolutePath(), new Date(root.lastModified()), 
        new Date(root.lastModified()), false, null, null, null, null, root.getName(), 
        null, null, null, 20, "Admin", null );
    List<RepositoryFileTree> children = new ArrayList<RepositoryFileTree>();

    File[] files = root.listFiles();
    for ( File file : files ) {
      if ( file.isHidden() ) {
        continue;
      }
      
      if ( file.isDirectory() ) {
        RepositoryFileTree kid = createFileTree( file );
        children.add( kid );
      } else if ( file.isFile() ) {
        RepositoryFile kid = new RepositoryFile( file.getPath(), file.getName(), 
            file.isDirectory(), false,
            false, null, file.getPath(), new Date(file.lastModified()), 
            new Date(file.lastModified()), false, null, null, null, null, file.getName(), 
            null, null, null, 20, "Admin", null );
        RepositoryFileTree kidTree = new RepositoryFileTree(kid, null);
        children.add( kidTree );
      }
    }
    RepositoryFileTree fileTree = new RepositoryFileTree(repFile, children);
    return fileTree;
  }
  
  @Override
  public RepositoryFileTree getTree( String arg0, int arg1, String arg2, boolean arg3 ) {

    File root = new File("src/test/resources/solution");
    RepositoryFileTree rft = createFileTree(root );

    return rft;
  }

  @Override
  public List<VersionSummary> getVersionSummaries( Serializable arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public VersionSummary getVersionSummary( Serializable arg0, Serializable arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<VersionSummary> getVersionSummaryInBatch( List<RepositoryFile> arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasAccess( String arg0, EnumSet<RepositoryFilePermission> arg1 ) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void lockFile( Serializable arg0, String arg1 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void moveFile( Serializable arg0, String arg1, String arg2 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void restoreFileAtVersion( Serializable arg0, Serializable arg1, String arg2 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setFileMetadata( Serializable arg0, Map<String, Serializable> arg1 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setLocalePropertiesForFile( RepositoryFile arg0, String arg1, Properties arg2 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setLocalePropertiesForFileById( Serializable arg0, String arg1, Properties arg2 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setLocalePropertiesForFileByPath( String arg0, String arg1, Properties arg2 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void undeleteFile( Serializable arg0, String arg1 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void unlockFile( Serializable arg0 ) {
    // TODO Auto-generated method stub

  }

  @Override
  public RepositoryFileAcl updateAcl( RepositoryFileAcl arg0 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile updateFile( RepositoryFile arg0, IRepositoryFileData arg1, String arg2 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFile updateFolder( RepositoryFile arg0, String arg1 ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryFileTree getTree( RepositoryRequest repositoryRequest ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFile> getChildren( Serializable folderId, String filter, Boolean showHiddenFiles ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryFile> getChildren( RepositoryRequest repositoryRequest ) {
    // TODO Auto-generated method stub
    return null;
  }

}
