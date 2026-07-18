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



package org.pentaho.dictionary;

import org.junit.Before;
import org.junit.Test;

public class DictionaryConstTest {

  @Before
  public void setUp() throws Exception {

  }

  @Test( expected = UnsupportedOperationException.class )
  public void testEnsureNonPublicConstructor() {
    DictionaryConst dc = new DictionaryConst();
  }
}
