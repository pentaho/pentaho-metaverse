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


package org.pentaho.metaverse.api.messages;

import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.messages.MessageUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
  private static final String BUNDLE_NAME = "org.pentaho.metaverse.api.messages.messages";

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

  public static String getString( final String key, final String param1, final String param2, final String param3,
                                  final String param4, final String param5 ) {
    return MessageUtil.getString( Messages.getBundle(), key, param1, param2, param3, param4, param5 );
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
