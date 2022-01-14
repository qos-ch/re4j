/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j.varia;

import static org.apache.log4j.TestContants.TARGET_OUTPUT_PREFIX;
import static org.apache.log4j.TestContants.TEST_INPUT_PREFIX;
import static org.apache.log4j.TestContants.TEST_WITNESS_PREFIX;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.util.Compare;
import org.apache.log4j.util.ControlFilter;
import org.apache.log4j.util.Filter;
import org.apache.log4j.util.LineNumberFilter;
import org.apache.log4j.util.Log4jAndNothingElseFilter;
import org.apache.log4j.util.Transformer;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ErrorHandlerTestCase  {

  static String TEMP = TARGET_OUTPUT_PREFIX+"fallback.out";
  static String FILTERED = TARGET_OUTPUT_PREFIX+"filtered";


  static String EXCEPTION1 = "java.lang.Exception: Just testing";
  static String EXCEPTION2 = "\\s*at .*\\(.*\\)";
  static String EXCEPTION3 = "\\s*at .*\\(Native Method\\)";

  static String TEST1_PAT =
                       "FALLBACK - (root|test) - Message \\d";


  Logger root;
  Logger logger;


  @Before
  public void setUp() {
    root = Logger.getRootLogger();
    logger = Logger.getLogger("test");
  }

  @After
  public void tearDown() {
    root.getLoggerRepository().resetConfiguration();
  }

  @Test
  public void test1() throws Exception {
    DOMConfigurator.configure(TEST_INPUT_PREFIX+"xml/fallback1.xml");
    Appender primary = root.getAppender("PRIMARY");
    ErrorHandler eh = primary.getErrorHandler();
    assertNotNull(eh);

    common();

    ControlFilter cf = new ControlFilter(new String[]{TEST1_PAT,
					       EXCEPTION1, EXCEPTION2, EXCEPTION3});

    Transformer.transform(TEMP, FILTERED, new Filter[] {cf,
                            new LineNumberFilter(),
                            new Log4jAndNothingElseFilter()});


    assertTrue(Compare.compare(FILTERED, TEST_WITNESS_PREFIX+"fallback1"));
  }

  @Test
  public void test2() throws Exception {
    PropertyConfigurator.configure(TEST_INPUT_PREFIX+"fallback1.properties");
    Appender primary = root.getAppender("PRIMARY");
    ErrorHandler eh = primary.getErrorHandler();
    assertNotNull(eh);

    common();

    ControlFilter cf = new ControlFilter(new String[]{TEST1_PAT,
					       EXCEPTION1, EXCEPTION2, EXCEPTION3});

    Transformer.transform(TEMP, FILTERED, new Filter[] {cf,
                            new LineNumberFilter(),
                            new Log4jAndNothingElseFilter()});


    assertTrue(Compare.compare(FILTERED, TEST_WITNESS_PREFIX+"fallback1"));
  }

  void common() {
    int i = -1;

    logger.debug("Message " + ++i);
    root.debug("Message " + i);

    logger.info ("Message " + ++i);
    root.info("Message " + i);

    logger.warn ("Message " + ++i);
    root.warn("Message " + i);

    logger.error("Message " + ++i);
    root.error("Message " + i);

    logger.log(Level.FATAL, "Message " + ++i);
    root.log(Level.FATAL, "Message " + i);

    Exception e = new Exception("Just testing");
    logger.debug("Message " + ++i, e);
    root.debug("Message " + i, e);

    logger.error("Message " + ++i, e);
    root.error("Message " + i, e);

  }

}
