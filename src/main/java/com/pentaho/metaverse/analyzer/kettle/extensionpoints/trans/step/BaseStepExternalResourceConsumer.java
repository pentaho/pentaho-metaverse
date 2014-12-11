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
package com.pentaho.metaverse.analyzer.kettle.extensionpoints.trans.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.analyzer.kettle.extensionpoints.IStepExternalResourceConsumer;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import com.pentaho.metaverse.impl.model.BaseResourceInfo;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;

import java.util.Collection;
import java.util.Collections;

/**
 * This class is a do-nothing reference implementation for StepExternalConsumer plugins. Subclasses should override
 * the various methods with business logic that can handle the external resources used by the given step.
 */
public abstract class BaseStepExternalResourceConsumer<T extends BaseStepMeta>
  implements IStepExternalResourceConsumer<T> {

  @Override
  public boolean isDataDriven( T meta ) {
    return false;
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromMeta( T meta ) {
    return Collections.emptyList();
  }

  @Override
  public Collection<IExternalResourceInfo> getResourcesFromRow( T meta, RowMetaInterface rowMeta, Object[] row ) {
    return Collections.emptyList();
  }

  protected IExternalResourceInfo getFileResource( FileObject fileObject, boolean isInput ) {
    BaseResourceInfo resource = null;
    if ( fileObject != null ) {
      resource = new BaseResourceInfo();
      resource.setName( fileObject.getName().getPath() );
      resource.setInput( isInput );
      resource.setType( DictionaryConst.NODE_TYPE_FILE );
    }
    return resource;
  }


}
