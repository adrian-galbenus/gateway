/**
 * Copyright (c) 2007-2014 Kaazing Corporation. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.kaazing.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * This class can be used to print out a message at the start and end of each test method in a JUnit test class
 * by including the following at the start of the class:
    @Rule
    public MethodRule testExecutionTrace = new MethodExecutionTrace();
 */
public class MethodExecutionTrace extends TestWatcher {

    /**
     * Use this constructor to set up a particular log4j configuration using a properties file,
     * or null if you do not want to load any log4j configuration properties.
     * See gateway.server src/test/resources/log4j-trace.properties for an example.
     * @param log4jConfigPropertiesFileName
     */
    public MethodExecutionTrace(String log4jPropertiesResourceName) {
        MemoryAppender.initialize();
        if (log4jPropertiesResourceName != null) {
            // Initialize log4j using a properties file available on the class path
            Properties log4j = new Properties();
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(log4jPropertiesResourceName);
            if (in == null) {
                throw new RuntimeException(String.format("Could not load resource %s", log4jPropertiesResourceName));
            }
            try {
                log4j.load(in);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Could not load resource %s", log4jPropertiesResourceName), e);
            }
            PropertyConfigurator.configure(log4j);
        }
    }

    /**
     * This constructor will configure log4j using the log4j-trace.properties file from the
     * test.util jar, which uses MemoryAppender so failed tests will print out the trace level log messages
     * to help diagnose the failure.
     */
    public MethodExecutionTrace() {
        this("log4j-trace.properties");
    }

    @Override
    public void starting(Description description) {
        System.out.println(description.getDisplayName() + " starting");
    }

    @Override
    public void failed(Throwable e, Description description) {
        if (e instanceof AssumptionViolatedException) {
            System.out.println(String.format("%s skipped programmatically with reason: %s",
                    getFullMethodName(description) , e.getMessage()));
        }
        else {
            System.out.println(getFullMethodName(description) + " FAILED with exception " + e);
            e.printStackTrace();
            System.out.println("=================== BEGIN STORED LOG MESSAGES ===========================");
            MemoryAppender.printAllMessages();
            System.out.println("=================== END STORED LOG MESSAGES ===========================");
        }
    }

    @Override
    public void succeeded(Description description) {
            System.out.println(getFullMethodName(description) + " " + "success");
    }

    private String getFullMethodName(Description description) {
        return description.getTestClass().getSimpleName() + "." + description.getMethodName();
    }
}
