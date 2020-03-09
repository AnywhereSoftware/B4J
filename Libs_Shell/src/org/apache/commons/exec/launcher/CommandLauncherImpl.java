/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.commons.exec.launcher;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.environment.EnvironmentUtils;

/**
 * A command launcher for a particular JVM/OS platform. This class is a general
 * purpose command launcher which can only launch commands in the current
 * working directory.
 *
 * @version $Id: CommandLauncherImpl.java 1557338 2014-01-11 10:34:22Z sebb $
 */
public abstract class CommandLauncherImpl implements CommandLauncher {

    public Process exec(final CommandLine cmd, final Map<String, String> env)
            throws IOException {
        final String[] envVar = EnvironmentUtils.toStrings(env);
        return Runtime.getRuntime().exec(cmd.toStrings(), envVar);
    }

    public abstract Process exec(final CommandLine cmd, final Map<String, String> env,
            final File workingDir) throws IOException;

    /** @see org.apache.commons.exec.launcher.CommandLauncher#isFailure(int) */    
    public boolean isFailure(final int exitValue)
    {
        // non zero exit value signals failure
        return exitValue != 0;
    }
}
