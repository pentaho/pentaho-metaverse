package com.pentaho.metaverse.locator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.memory.MemoryMetaStore;

public class TestDiRepository implements Repository {

  protected MemoryMetaStore metaStore;
  public long delay = 0;
  
  @Override
  public void clearSharedObjectCache() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void connect( String arg0, String arg1 ) throws KettleException, KettleSecurityException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public int countNrJobEntryAttributes( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int countNrStepAttributes( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public RepositoryDirectoryInterface createRepositoryDirectory( RepositoryDirectoryInterface arg0, String arg1 )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteClusterSchema( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteDatabaseMeta( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteJob( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deletePartitionSchema( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteRepositoryDirectory( RepositoryDirectoryInterface arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteSlave( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteTransformation( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void disconnect() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean exists( String arg0, RepositoryDirectoryInterface arg1, RepositoryObjectType arg2 )
    throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId getClusterID( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId[] getClusterIDs( boolean arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getClusterNames( boolean arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getConnectMessage() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId getDatabaseID( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId[] getDatabaseIDs( boolean arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getDatabaseNames( boolean arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryDirectoryInterface getDefaultSaveDirectory( RepositoryElementInterface arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getDirectoryNames( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IRepositoryExporter getExporter() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IRepositoryImporter getImporter() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobAndTransformationObjects( ObjectId arg0, boolean arg1 )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId arg0, int arg1, String arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId arg0, String arg1, boolean arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public long getJobEntryAttributeInteger( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getJobEntryAttributeInteger( ObjectId arg0, int arg1, String arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getJobEntryAttributeString( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getJobEntryAttributeString( ObjectId arg0, int arg1, String arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId getJobId( String arg0, RepositoryDirectoryInterface arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getJobNames( ObjectId arg0, boolean arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobObjects( ObjectId arg0, boolean arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getJobsUsingDatabase( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LogChannelInterface getLog() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMetaStore getMetaStore() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryObject getObjectInformation( ObjectId arg0, RepositoryObjectType arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId getPartitionSchemaID( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId[] getPartitionSchemaIDs( boolean arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getPartitionSchemaNames( boolean arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryMeta getRepositoryMeta() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositorySecurityManager getSecurityManager() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositorySecurityProvider getSecurityProvider() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IRepositoryService getService( Class<? extends IRepositoryService> arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Class<? extends IRepositoryService>> getServiceInterfaces() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId getSlaveID( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId[] getSlaveIDs( boolean arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getSlaveNames( boolean arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<SlaveServer> getSlaveServers() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId arg0, int arg1, String arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId arg0, int arg1, String arg2, boolean arg3 ) throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public long getStepAttributeInteger( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getStepAttributeInteger( ObjectId arg0, int arg1, String arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getStepAttributeString( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getStepAttributeString( ObjectId arg0, int arg1, String arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId getTransformationID( String arg0, RepositoryDirectoryInterface arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getTransformationNames( ObjectId arg0, boolean arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RepositoryElementMetaInterface> getTransformationObjects( ObjectId arg0, boolean arg1 )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getTransformationsUsingDatabase( ObjectId arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryDirectoryInterface getUserHomeDirectory() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IUser getUserInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getVersion() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasService( Class<? extends IRepositoryService> arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void init( RepositoryMeta arg0 ) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void insertJobEntryDatabase( ObjectId arg0, ObjectId arg1, ObjectId arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public ObjectId insertLogEntry( String arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void insertStepDatabase( ObjectId arg0, ObjectId arg1, ObjectId arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isConnected() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public ClusterSchema loadClusterSchema( ObjectId arg0, List<SlaveServer> arg1, String arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Condition loadConditionFromStepAttribute( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DatabaseMeta loadDatabaseMeta( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId arg0, String arg1, String arg2,
      List<DatabaseMeta> arg3 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId arg0, String arg1, int arg2, String arg3,
      List<DatabaseMeta> arg4 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromStepAttribute( ObjectId arg0, String arg1, List<DatabaseMeta> arg2 )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JobMeta loadJob( ObjectId arg0, String arg1 ) throws KettleException {
    try {
      Thread.sleep( delay );
    } catch ( InterruptedException e1 ) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    File file = new File(arg0.getId());
    String content = "";
    try {

      InputStream in = new FileInputStream( file );
      byte[] buffer = new byte[2048];
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int n = 0;
      try {
        while ( n != -1 ) {
          n = in.read( buffer );
          if ( n != -1 ) {
            out.write( buffer, 0, n );
          }
        }
      } finally {
        in.close();
      }
      content = new String( out.toByteArray() );
      ByteArrayInputStream xmlStream = new ByteArrayInputStream( content.getBytes() );
      return new JobMeta( xmlStream, null, null );
      
    } catch ( Throwable e ) {
      e.printStackTrace();
    }
    
    return null;  
  }

  @Override
  public JobMeta loadJob( String arg0, RepositoryDirectoryInterface arg1, ProgressMonitorListener arg2, String arg3 )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PartitionSchema loadPartitionSchema( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RepositoryDirectoryInterface loadRepositoryDirectoryTree() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SlaveServer loadSlaveServer( ObjectId arg0, String arg1 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TransMeta loadTransformation( ObjectId arg0, String arg1 ) throws KettleException {
    File file = new File(arg0.getId());
    String content = "";
    try {

      InputStream in = new FileInputStream( file );
      byte[] buffer = new byte[2048];
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int n = 0;
      try {
        while ( n != -1 ) {
          n = in.read( buffer );
          if ( n != -1 ) {
            out.write( buffer, 0, n );
          }
        }
      } finally {
        in.close();
      }
      content = new String( out.toByteArray() );
      ByteArrayInputStream xmlStream = new ByteArrayInputStream( content.getBytes() );
      return new TransMeta( xmlStream, null, false, null, null );
      
    } catch ( Throwable e ) {
    }
    
    return null;
  }

  @Override
  public TransMeta loadTransformation( String arg0, RepositoryDirectoryInterface arg1, ProgressMonitorListener arg2,
      boolean arg3, String arg4 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<DatabaseMeta> readDatabases() throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SharedObjects readJobMetaSharedObjects( JobMeta arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SharedObjects readTransSharedObjects( TransMeta arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId renameJob( ObjectId arg0, RepositoryDirectoryInterface arg1, String arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId renameRepositoryDirectory( ObjectId arg0, RepositoryDirectoryInterface arg1, String arg2 )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ObjectId renameTransformation( ObjectId arg0, RepositoryDirectoryInterface arg1, String arg2 )
    throws KettleException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void save( RepositoryElementInterface arg0, String arg1, ProgressMonitorListener arg2 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void save( RepositoryElementInterface arg0, String arg1, ProgressMonitorListener arg2, boolean arg3 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void save( RepositoryElementInterface arg0, String arg1, Calendar arg2, ProgressMonitorListener arg3,
      boolean arg4 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveConditionStepAttribute( ObjectId arg0, ObjectId arg1, String arg2, Condition arg3 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveDatabaseMetaJobEntryAttribute( ObjectId arg0, ObjectId arg1, String arg2, String arg3,
      DatabaseMeta arg4 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveDatabaseMetaJobEntryAttribute( ObjectId arg0, ObjectId arg1, int arg2, String arg3, String arg4,
      DatabaseMeta arg5 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveDatabaseMetaStepAttribute( ObjectId arg0, ObjectId arg1, String arg2, DatabaseMeta arg3 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveJobEntryAttribute( ObjectId arg0, ObjectId arg1, String arg2, String arg3 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveJobEntryAttribute( ObjectId arg0, ObjectId arg1, String arg2, boolean arg3 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveJobEntryAttribute( ObjectId arg0, ObjectId arg1, String arg2, long arg3 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveJobEntryAttribute( ObjectId arg0, ObjectId arg1, int arg2, String arg3, String arg4 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveJobEntryAttribute( ObjectId arg0, ObjectId arg1, int arg2, String arg3, boolean arg4 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveJobEntryAttribute( ObjectId arg0, ObjectId arg1, int arg2, String arg3, long arg4 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveRepositoryDirectory( RepositoryDirectoryInterface arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveStepAttribute( ObjectId arg0, ObjectId arg1, String arg2, String arg3 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveStepAttribute( ObjectId arg0, ObjectId arg1, String arg2, boolean arg3 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveStepAttribute( ObjectId arg0, ObjectId arg1, String arg2, long arg3 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveStepAttribute( ObjectId arg0, ObjectId arg1, String arg2, double arg3 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveStepAttribute( ObjectId arg0, ObjectId arg1, int arg2, String arg3, String arg4 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveStepAttribute( ObjectId arg0, ObjectId arg1, int arg2, String arg3, boolean arg4 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveStepAttribute( ObjectId arg0, ObjectId arg1, int arg2, String arg3, long arg4 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveStepAttribute( ObjectId arg0, ObjectId arg1, int arg2, String arg3, double arg4 )
    throws KettleException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void undeleteObject( RepositoryElementMetaInterface arg0 ) throws KettleException {
    // TODO Auto-generated method stub
    
  }

}
