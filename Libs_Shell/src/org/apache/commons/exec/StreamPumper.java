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

package org.apache.commons.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.exec.util.DebugUtils;

/**
 * Copies all data from an input stream to an output stream.
 *
 * @version $Id: StreamPumper.java 1557263 2014-01-10 21:18:09Z ggregory $
 */
public class StreamPumper implements Runnable {

	/** the default size of the internal buffer for copying the streams */
	private static final int DEFAULT_SIZE = 1024;

	/** the input stream to pump from */
	private final InputStream is;

	/** the output stream to pmp into */
	private final OutputStream os;

	/** the size of the internal buffer for copying the streams */ 
	private final int size;

	/** was the end of the stream reached */
	private boolean finished;

	/** close the output stream when exhausted */
	private final boolean closeWhenExhausted;

	/**
	 * Create a new stream pumper.
	 * 
	 * @param is input stream to read data from
	 * @param os output stream to write data to.
	 * @param closeWhenExhausted if true, the output stream will be closed when the input is exhausted.
	 */
	public StreamPumper(final InputStream is, final OutputStream os,
			final boolean closeWhenExhausted) {
		this.is = is;
		this.os = os;
		this.size = DEFAULT_SIZE;
		this.closeWhenExhausted = closeWhenExhausted;
	}

	/**
	 * Create a new stream pumper.
	 *
	 * @param is input stream to read data from
	 * @param os output stream to write data to.
	 * @param closeWhenExhausted if true, the output stream will be closed when the input is exhausted.
	 * @param size the size of the internal buffer for copying the streams
	 */
	public StreamPumper(final InputStream is, final OutputStream os,
			final boolean closeWhenExhausted, final int size) {
		this.is = is;
		this.os = os;
		this.size = size > 0 ? size : DEFAULT_SIZE;
		this.closeWhenExhausted = closeWhenExhausted;
	}

	/**
	 * Create a new stream pumper.
	 * 
	 * @param is input stream to read data from
	 * @param os output stream to write data to.
	 */
	public StreamPumper(final InputStream is, final OutputStream os) {
		this(is, os, false);
	}

	/**
	 * Copies data from the input stream to the output stream. Terminates as
	 * soon as the input stream is closed or an error occurs.
	 */
	public void run() {
		synchronized (this) {
			// Just in case this object is reused in the future
			finished = false;
		}

		final byte[] buf = new byte[this.size];

		int length;
		try {
			while ((length = is.read(buf)) > 0) {
				synchronized (os) { //b4x
					os.write(buf, 0, length);
					os.flush(); 
				}
			}
		} catch (final Exception e) {
			// nothing to do - happens quite often with watchdog
		} finally {
			if (closeWhenExhausted) {
				try {
					os.close();
				} catch (final IOException e) {
					final String msg = "Got exception while closing exhausted output stream";
					DebugUtils.handleException(msg ,e);
				}
			}
			synchronized (this) {
				finished = true;
				notifyAll();
			}
		}
	}

	/**
	 * Tells whether the end of the stream has been reached.
	 * 
	 * @return true is the stream has been exhausted.
	 */
	public synchronized boolean isFinished() {
		return finished;
	}

	/**
	 * This method blocks until the stream pumper finishes.
	 * 
	 * @exception InterruptedException
	 *                if any thread interrupted the current thread before or while the current thread was waiting for a
	 *                notification.
	 * @see #isFinished()
	 */
	public synchronized void waitFor() throws InterruptedException {
		while (!isFinished()) {
			wait();
		}
	}
}
