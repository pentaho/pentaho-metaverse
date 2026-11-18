/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.metaverse;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IntegrationTestUtilIT {
  private static final String SCRIPT_CLASS =
    "org.pentaho.di.trans.steps.scriptvalues_mod.ScriptValuesMetaMod";

  @Test
  public void missingOptionalScriptClassDoesNotBreakRegistration() throws Exception {
    PluginRegistry registry = Mockito.mock( PluginRegistry.class );
    PrintStream originalError = System.err;
    ByteArrayOutputStream warning = new ByteArrayOutputStream();
    try ( PrintStream capturedError = new PrintStream( warning, true, StandardCharsets.UTF_8 );
          MockedStatic<PluginRegistry> registryFactory = Mockito.mockStatic( PluginRegistry.class ) ) {
      System.setErr( capturedError );
      registryFactory.when( PluginRegistry::getInstance ).thenReturn( registry );

      invokeRegistration( ScriptClassMode.MISSING );

      verify( registry, never() ).registerPlugin( eq( StepPluginType.class ), any( PluginInterface.class ) );
      assertTrue( warning.toString( StandardCharsets.UTF_8 )
        .contains( "Optional ScriptValueMod plugin is unavailable" ) );
    } finally {
      System.setErr( originalError );
    }
  }

  @Test
  public void availableScriptClassIsRegistered() throws Exception {
    PluginRegistry registry = Mockito.mock( PluginRegistry.class );
    try ( MockedStatic<PluginRegistry> registryFactory = Mockito.mockStatic( PluginRegistry.class ) ) {
      registryFactory.when( PluginRegistry::getInstance ).thenReturn( registry );

      invokeRegistration( ScriptClassMode.AVAILABLE );

      ArgumentCaptor<PluginInterface> plugin = ArgumentCaptor.forClass( PluginInterface.class );
      verify( registry ).registerPlugin( eq( StepPluginType.class ), plugin.capture() );
      assertEquals( "ScriptValueMod", plugin.getValue().getIds()[ 0 ] );
      assertEquals( SCRIPT_CLASS, plugin.getValue().getClassMap().get( StepMetaInterface.class ) );
    }
  }

  @Test
  public void existingScriptPluginIsPreservedWithoutLoadingImplementation() throws Exception {
    PluginRegistry registry = Mockito.mock( PluginRegistry.class );
    PluginInterface existing = Mockito.mock( PluginInterface.class );
    try ( MockedStatic<PluginRegistry> registryFactory = Mockito.mockStatic( PluginRegistry.class ) ) {
      registryFactory.when( PluginRegistry::getInstance ).thenReturn( registry );
      when( registry.findPluginWithId( StepPluginType.class, "ScriptValueMod" ) ).thenReturn( existing );

      invokeRegistration( ScriptClassMode.MISSING );

      verify( registry, never() ).registerPlugin( eq( StepPluginType.class ), any( PluginInterface.class ) );
    }
  }

  @Test
  public void brokenScriptClassLoadingIsNotIgnored() throws Exception {
    PluginRegistry registry = Mockito.mock( PluginRegistry.class );
    try ( MockedStatic<PluginRegistry> registryFactory = Mockito.mockStatic( PluginRegistry.class ) ) {
      registryFactory.when( PluginRegistry::getInstance ).thenReturn( registry );

      InvocationTargetException failure =
        assertThrows( InvocationTargetException.class, () -> invokeRegistration( ScriptClassMode.BROKEN ) );
      assertEquals( NoClassDefFoundError.class, failure.getCause().getClass() );
      assertEquals( "missing script dependency", failure.getCause().getMessage() );
    }
  }

  private static void invokeRegistration( ScriptClassMode mode ) throws Exception {
    ClassLoader parent = IntegrationTestUtilIT.class.getClassLoader();
    String harnessName = IntegrationTestUtil.class.getName();
    // Load the real harness as a downstream test-JAR consumer with a different classpath.
    ClassLoader loader = new ClassLoader( parent ) {
      @Override
      protected synchronized Class<?> loadClass( String name, boolean resolve ) throws ClassNotFoundException {
        if ( SCRIPT_CLASS.equals( name ) ) {
          if ( mode == ScriptClassMode.MISSING ) {
            throw new ClassNotFoundException( name );
          }
          if ( mode == ScriptClassMode.BROKEN ) {
            throw new NoClassDefFoundError( "missing script dependency" );
          }
        }
        if ( harnessName.equals( name ) ) {
          Class<?> loaded = findLoadedClass( name );
          if ( loaded == null ) {
            String resource = name.replace( '.', '/' ) + ".class";
            try ( InputStream input = parent.getResourceAsStream( resource ) ) {
              if ( input == null ) {
                throw new ClassNotFoundException( name );
              }
              byte[] bytes = input.readAllBytes();
              loaded = defineClass( name, bytes, 0, bytes.length );
            } catch ( IOException e ) {
              throw new ClassNotFoundException( name, e );
            }
          }
          if ( resolve ) {
            resolveClass( loaded );
          }
          return loaded;
        }
        return super.loadClass( name, resolve );
      }
    };
    Method register = loader.loadClass( harnessName ).getDeclaredMethod( "registerKettlePlugins" );
    register.setAccessible( true );
    register.invoke( null );
  }

  private enum ScriptClassMode {
    AVAILABLE, MISSING, BROKEN
  }
}
