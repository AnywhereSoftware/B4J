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

package org.apache.commons.net.io;

import java.io.IOException;

/**
 * The CopyStreamException class is thrown by the org.apache.commons.io.Util
 * copyStream() methods.  It stores the number of bytes confirmed to
 * have been transferred before an I/O error as well as the IOException
 * responsible for the failure of a copy operation.
 * @see Util
 * @version $Id: CopyStreamException.java 1088720 2011-04-04 18:52:00Z dfs $
 */
@anywheresoftware.b4a.BA.Hide
public class CopyStreamException extends IOException
{
    private static final long serialVersionUID = -2602899129433221532L;

    private final long totalBytesTransferred;
    private final IOException ioException;

    /**
     * Creates a new CopyStreamException instance.
     * @param message  A message describing the error.
     * @param bytesTransferred  The total number of bytes transferred before
     *        an exception was thrown in a copy operation.
     * @param exception  The IOException thrown during a copy operation.
     */
    public CopyStreamException(String message,
                               long bytesTransferred,
                               IOException exception)
    {
        super(message);
        totalBytesTransferred = bytesTransferred;
        ioException = exception;
    }

    /**
     * Returns the total number of bytes confirmed to have
     * been transferred by a failed copy operation.
     * @return The total number of bytes confirmed to have
     * been transferred by a failed copy operation.
     */
    public long getTotalBytesTransferred()
    {
        return totalBytesTransferred;
    }

    /**
     * Returns the IOException responsible for the failure of a copy operation.
     * @return The IOException responsible for the failure of a copy operation.
     */
    public IOException getIOException()
    {
        return ioException;
    }
}
