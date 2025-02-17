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

package org.apache.log4j;

import junit.framework.TestCase;

import java.util.Vector;

import org.apache.log4j.spi.LoggingEvent;

/**
 * A superficial but general test of log4j.
 */
public class AsyncAppenderTestCase extends TestCase {

    public AsyncAppenderTestCase(String name) {
        super(name);
    }

    public void setUp() {
    }

    public void tearDown() {
        LogManager.shutdown();
    }

    // this test checks whether it is possible to write to a closed AsyncAppender
    public void closeTest() throws Exception {
        Logger root = Logger.getRootLogger();
        VectorAppender vectorAppender = new VectorAppender();
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setName("async-CloseTest");
        asyncAppender.addAppender(vectorAppender);
        root.addAppender(asyncAppender);

        root.debug("m1");
        asyncAppender.close();
        root.debug("m2");

        Vector v = vectorAppender.getVector();
        assertEquals(v.size(), 1);
    }

    // this test checks whether appenders embedded within an AsyncAppender are also
    // closed
    public void test2() {
        Logger root = Logger.getRootLogger();
        VectorAppender vectorAppender = new VectorAppender();
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setName("async-test2");
        asyncAppender.addAppender(vectorAppender);
        root.addAppender(asyncAppender);

        root.debug("m1");
        asyncAppender.close();
        root.debug("m2");

        Vector v = vectorAppender.getVector();
        assertEquals(v.size(), 1);
        assertTrue(vectorAppender.isClosed());
    }

    // this test checks whether appenders embedded within an AsyncAppender are also
    // closed
    public void test3() {
        int LEN = 200;
        Logger root = Logger.getRootLogger();
        VectorAppender vectorAppender = new VectorAppender();
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setName("async-test3");
        asyncAppender.addAppender(vectorAppender);
        root.addAppender(asyncAppender);

        for (int i = 0; i < LEN; i++) {
            root.debug("message" + i);
        }

        System.out.println("Done loop.");
        System.out.flush();
        asyncAppender.close();
        root.debug("m2");

        Vector v = vectorAppender.getVector();
        assertEquals(v.size(), LEN);
        assertTrue(vectorAppender.isClosed());
    }

    /**
     * Tests that a bad appender will switch async back to sync. See bug 23021
     *
     * @throws Exception thrown if Thread.sleep is interrupted
     * @since 1.2.12
     */
    public void testBadAppender() throws Exception {
        Appender nullPointerAppender = new NullPointerAppender();
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.addAppender(nullPointerAppender);
        asyncAppender.setBufferSize(5);
        asyncAppender.activateOptions();
        Logger root = Logger.getRootLogger();
        root.addAppender(nullPointerAppender);
        try {
            root.info("Message");
            Thread.sleep(10);
            root.info("Message");
            fail("Should have thrown exception");
        } catch (NullPointerException ex) {

        }
    }

    /**
     * Tests location processing when buffer is full and locationInfo=true. See bug 41186.
     */
    public void testLocationInfoTrue() {
        BlockableVectorAppender blockableAppender = new BlockableVectorAppender();
        AsyncAppender async = new AsyncAppender();
        async.addAppender(blockableAppender);
        async.setBufferSize(5);
        async.setLocationInfo(true);
        async.setBlocking(false);
        async.activateOptions();
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.addAppender(async);
        Greeter greeter = new Greeter(rootLogger, 100);
        synchronized (blockableAppender.getMonitor()) {
            greeter.run();
            rootLogger.error("That's all folks.");
        }
        async.close();
        Vector events = blockableAppender.getVector();
        LoggingEvent initialEvent = (LoggingEvent) events.get(0);
        LoggingEvent discardEvent = (LoggingEvent) events.get(events.size() - 1);
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("%C:%L %m%n");
        layout.activateOptions();
        String initialStr = layout.format(initialEvent);
        assertEquals(Greeter.class.getName(),
                initialStr.substring(0, Greeter.class.getName().length()));
        String discardStr = layout.format(discardEvent);
        assertEquals("?:? ", discardStr.substring(0, 4));
    }

    /**
     * Tests location processing when buffer is full and locationInfo=false. See bug 41186.
     */
    public void testLocationInfoFalse() {
        BlockableVectorAppender blockableAppender = new BlockableVectorAppender();
        AsyncAppender async = new AsyncAppender();
        async.addAppender(blockableAppender);
        async.setBufferSize(5);
        async.setLocationInfo(false);
        async.setBlocking(false);
        async.activateOptions();
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.addAppender(async);
        Greeter greeter = new Greeter(rootLogger, 100);
        synchronized (blockableAppender.getMonitor()) {
            greeter.run();
            rootLogger.error("That's all folks.");
        }
        async.close();
        Vector events = blockableAppender.getVector();
        LoggingEvent initialEvent = (LoggingEvent) events.get(0);
        LoggingEvent discardEvent = (LoggingEvent) events.get(events.size() - 1);
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("%C:%L %m%n");
        layout.activateOptions();
        String initialStr = layout.format(initialEvent);
        assertEquals("?:? ", initialStr.substring(0, 4));
        String discardStr = layout.format(discardEvent);
        assertEquals("?:? ", discardStr.substring(0, 4));
    }




    /**
     * Test that a mutable message object is evaluated before being placed in the async queue. See bug 43559.
     */
    public void testMutableMessage() {
        BlockableVectorAppender blockableAppender = new BlockableVectorAppender();
        AsyncAppender async = new AsyncAppender();
        async.addAppender(blockableAppender);
        async.setBufferSize(5);
        async.setLocationInfo(false);
        async.activateOptions();
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.addAppender(async);
        StringBuffer buf = new StringBuffer("Hello");
        synchronized (blockableAppender.getMonitor()) {
            rootLogger.info(buf);
            buf.append(", World.");
        }
        async.close();
        Vector events = blockableAppender.getVector();
        LoggingEvent event = (LoggingEvent) events.get(0);
        PatternLayout layout = new PatternLayout();
        layout.setConversionPattern("%m");
        layout.activateOptions();
        String msg = layout.format(event);
        assertEquals("Hello", msg);
    }

}
