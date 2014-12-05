package com.pentaho.metaverse.analyzer.kettle.plugin;

import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExternalResourceConsumerPluginTypeTest {

  ExternalResourceConsumerPluginType pluginType;
  ExternalResourceConsumer mockAnnotation;

  @Before
  public void setUp() throws Exception {
    pluginType = ExternalResourceConsumerPluginType.getInstance();
    mockAnnotation = mock( ExternalResourceConsumer.class );
  }

  @Test
  public void testGetInstance() throws Exception {
    assertNotNull( pluginType );
  }

  @Test
  public void testRegisterNatives() throws Exception {
    // Noop method, call for coverage
    pluginType.registerNatives();
  }

  @Test
  public void testExtractCategory() throws Exception {
    when( mockAnnotation.categoryDescription() ).thenReturn( "Test" );
    assertEquals( "Test", pluginType.extractCategory( mockAnnotation ) );
  }

  @Test
  public void testExtractDesc() throws Exception {
    when( mockAnnotation.description() ).thenReturn( "Test" );
    assertEquals( "Test", pluginType.extractDesc( mockAnnotation ) );
  }

  @Test
  public void testExtractID() throws Exception {
    when( mockAnnotation.id() ).thenReturn( "Test" );
    assertEquals( "Test", pluginType.extractID( mockAnnotation ) );
  }

  @Test
  public void testExtractName() throws Exception {
    when( mockAnnotation.name() ).thenReturn( "Test" );
    assertEquals( "Test", pluginType.extractName( mockAnnotation ) );
  }

  @Test
  public void testExtractImageFile() throws Exception {
    assertNull( pluginType.extractImageFile( mockAnnotation ) );
  }

  @Test
  public void testExtractI18nPackageName() throws Exception {
    when( mockAnnotation.i18nPackageName() ).thenReturn( "Test" );
    assertEquals( "Test", pluginType.extractI18nPackageName( mockAnnotation ) );
  }

  @Test
  public void testExtractDocumentationUrl() throws Exception {
    assertNull( pluginType.extractDocumentationUrl( mockAnnotation ) );
  }

  @Test
  public void testExtractCasesUrl() throws Exception {
    assertNull( pluginType.extractCasesUrl( mockAnnotation ) );
  }

  @Test
  public void testExtractForumUrl() throws Exception {
    assertNull( pluginType.extractForumUrl( mockAnnotation ) );
  }

  @Test
  public void testAddExtraClasses() throws Exception {
    // Noop method, call for coverage
    pluginType.addExtraClasses( null, null, null );
  }

  @Test
  public void testExtractSeparateClassLoader() throws Exception {
    assertFalse( pluginType.extractSeparateClassLoader( null ) );
  }

  @Test
  public void testRegisterXmlPlugins() throws Exception {
    // Noop method, call for coverage
    pluginType.registerXmlPlugins();
  }
}
