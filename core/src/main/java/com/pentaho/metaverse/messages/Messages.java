/*
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

package com.pentaho.metaverse.messages;

import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.messages.MessageUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
  private static final String BUNDLE_NAME = "com.pentaho.metaverse.messages.messages";

  private static final Map<Locale, ResourceBundle>
  locales = Collections.synchronizedMap( new HashMap<Locale, ResourceBundle>() );

  protected static ResourceBundle getBundle() {
    Locale locale = LocaleHelper.getLocale();
    ResourceBundle bundle = Messages.locales.get( locale );
    if ( bundle == null ) {
      bundle = ResourceBundle.getBundle( Messages.BUNDLE_NAME, locale );
      Messages.locales.put( locale, bundle );
    }
    return bundle;
  }

  public static String getString( final String key ) {
    try {
      return Messages.getBundle().getString( key );
    } catch ( MissingResourceException e ) {
      return '!' + key + '!';
    }
  }

  public static String getString( final String key, final String param1 ) {
    return MessageUtil.getString( Messages.getBundle(), key, param1 );
  }

  public static String getString( final String key, final String param1, final String param2 ) {
    return MessageUtil.getString( Messages.getBundle(), key, param1, param2 );
  }

  public static String getString( final String key, final String param1, final String param2, final String param3 ) {
    return MessageUtil.getString( Messages.getBundle(), key, param1, param2, param3 );
  }

  public static String getString( final String key, final String param1, final String param2, final String param3,
                                  final String param4 ) {
    return MessageUtil.getString( Messages.getBundle(), key, param1, param2, param3, param4 );
  }

  public static String getErrorString( final String key ) {
    return MessageUtil.formatErrorMessage( key, Messages.getString( key ) );
  }

  public static String getErrorString( final String key, final String param1 ) {
    return MessageUtil.getErrorString( Messages.getBundle(), key, param1 );
  }

  public static String getErrorString( final String key, final String param1, final String param2 ) {
    return MessageUtil.getErrorString( Messages.getBundle(), key, param1, param2 );
  }

  public static String getErrorString( final String key, final String param1, final String param2,
                                       final String param3 ) {
    return MessageUtil.getErrorString( Messages.getBundle(), key, param1, param2, param3 );
  }

}
