
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package anywheresoftware.b4j.objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;

/**
 * Shell provides methods to start new processes and run other applications.
 *The execution is asynchronous. The ProcessCompleted event is raised after the process has exited.
 */
@Version(1.56f)
@ShortName("Shell")
@Events(values={"ProcessCompleted (Success As Boolean, ExitCode As Int, StdOut As String, StdErr As String)",
		"StdOut (Buffer() As Byte, Length As Int)",
"StdErr (Buffer() As Byte, Length As Int)"})
public class Shell {
	private DefaultExecutor exec;
	private CommandLine cl;
	private String encoding = "UTF8";
	private String eventName;
	private HashMap<String, String> envVars;
	private OutputStream outForProcessInputStream;
	private PipedInputStream processInputStream;
	private ByteArrayOutputStream tempOut, tempErr;

	/**
	 * Initializes the object.
	 *EventName - Determines the sub that will handle the events.
	 *Executable - Executable file to run.
	 *Args - List of command line arguments. Pass Null if not needed.
	 */
	public void Initialize(String EventName, String Executable, List Args) {
		init(EventName, Executable, Args, true);
	}
	public void InitializeDoNotHandleQuotes(String EventName, String Executable, List Args) {
		init(EventName, Executable, Args, false);

	}
	private void init(String EventName, String Executable, List Args, boolean handle) {
		exec = new DefaultExecutor();
		exec.setExitValues(null);
		eventName = EventName.toLowerCase(BA.cul);
		cl = new CommandLine(Executable);
		if (Args.IsInitialized()) {
			for (Object o : Args.getObject()) {
				cl.addArgument((String)o, handle);
			}
		}
	}
	public boolean IsInitialized() {
		return exec != null;
	}
	/**
	 * Gets the executable file name.
	 */
	public String getExecutable() {
		return cl.getExecutable();
	}
	/**
	 * Gets the command line arguments.
	 */
	public String[] getArguments() {
		return cl.getArguments();
	}
	/**
	 * Set the system environment variables that will be passed to the child process.
	 *Example:<code>
	 *shl.SetEnvironmentVariables(CreateMap("ZZZ": "abc", "YYYY": "213"))</code>
	 */
	@SuppressWarnings("unchecked")
	public void SetEnvironmentVariables(Map Vars) {
		if (Vars.IsInitialized() == false)
			envVars = null;
		else {
			envVars = new HashMap<String, String>();
			for (Entry<Object, Object> e : ((java.util.Map<Object, Object>)Vars.getObject()).entrySet())
				envVars.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
		}
	}
	/**
	 * Gets or sets whether the process input stream will be open for communication.
	 *You can use WriteToInputStream to write data while the process is running.
	 */
	public void setInputStreamEnabled(boolean b) throws IOException {
		if (b) {
			outForProcessInputStream = new PipedOutputStream();
			processInputStream = new PipedInputStream((PipedOutputStream) outForProcessInputStream);

		}
		else {
			outForProcessInputStream = null;
			processInputStream = null;
		}
	}
	public boolean getInputStreamEnabled() {
		return processInputStream != null;
	}
	/**
	 * Writes data to the process input stream.
	 */
	public void WriteToInputStream(byte[] Data) throws IOException {
		outForProcessInputStream.write(Data);
	}
	/**
	 * Gets or sets the process working directory.
	 */
	public String getWorkingDirectory() {
		return exec.getWorkingDirectory().toString();
	}
	public void setWorkingDirectory(String s) {
		exec.setWorkingDirectory(new File(s));
	}
	/**
	 * Sets the encoding used. This encoding should match the process output encoding.
	 *The default value is UTF8.
	 */
	public void setEncoding(String s) {
		encoding = s;
	}
	/**
	 * Kills the process if it is currently running.
	 */
	public void KillProcess() {
		exec.getWatchdog().destroyProcess();
	}
	/**
	 * Starts the process.
	 *TimeoutMs - Timeout in milliseconds. Pass -1 to disable the timeout.
	 */
	public void Run(final BA ba, long TimeoutMs) throws ExecuteException, IOException {
		tempOut = new ByteArrayOutputStream();
		tempErr = new ByteArrayOutputStream();
		run(ba, TimeoutMs, tempOut, tempErr);
	}
	/**
	 * Returns the current output. Should only be used when the process was started with Run (not RunWithOutputEvents).
	 */
	public String GetTempOut() throws UnsupportedEncodingException {
		return GetTempOut2(false);
	}
	/**
	 * Same as GetTempOut.
	 *DiscardReadData - If true then the buffer is emptied after this call.
	 */
	public String GetTempOut2(boolean DiscardReadData) throws UnsupportedEncodingException {
		synchronized (tempOut) {
			if (tempOut == null)
				return "";
			String res = new String(tempOut.toByteArray(), encoding);
			if (DiscardReadData)
				tempOut.reset();
			return res;
		}
	}
	/**
	 * Returns the current error output. Should only be used when the process was started with Run (not RunWithOutputEvents).
	 */
	public String GetTempErr() throws UnsupportedEncodingException {
		return GetTempErr2(false);
	}
	/**
	 * Same as GetTempErr.
	 *DiscardReadData - If true then the buffer is emptied after this call.
	 */
	public String GetTempErr2(boolean DiscardReadData) throws UnsupportedEncodingException {
		synchronized (tempErr) {
			if (tempErr == null)
				return "";
			String res = new String(tempErr.toByteArray(), encoding);
			if (DiscardReadData)
				tempErr.reset();
			return res;
		}
	}

	/**
	 * Starts the process. The StdOut and StdErr events will be raised when new data is available.
	 *<b>Note that these events are raised on a background thread.</b>
	 */
	public void RunWithOutputEvents (final BA ba, long TimeoutMs) throws ExecuteException, IOException {
		run (ba, TimeoutMs, new EventOutput(ba, "_stdout"), new EventOutput(ba, "_stderr"));
	}
	/**
	 * Starts the process and waits for it to complete.
	 */
	public ShellSyncResult RunSynchronous(long TimeoutMs) throws IOException {
		ShellSyncResult sr = new ShellSyncResult();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final ByteArrayOutputStream err = new ByteArrayOutputStream();
		final ExecuteWatchdog wd = new ExecuteWatchdog(TimeoutMs);
		exec.setWatchdog(wd);
		PumpStreamHandler p = new PumpStreamHandler(out, err, processInputStream);
		exec.setStreamHandler(p);
		try {
			sr.ExitCode = exec.execute(cl, envVars);
			sr.Success = true;
			sr.StdOut = new String(((ByteArrayOutputStream)out).toByteArray(), encoding);
			sr.StdErr = new String(((ByteArrayOutputStream)err).toByteArray(), encoding);
		} catch (ExecuteException e) {
			sr.Success = false;
			sr.StdErr = e.toString();
			sr.ExitCode = e.getExitValue();
		}
		return sr;
	}
	private void run(final BA ba, final long TimeoutMs, final OutputStream out,final OutputStream err) throws ExecuteException, IOException {
		final ExecuteWatchdog wd = new ExecuteWatchdog(TimeoutMs);
		exec.setWatchdog(wd);
		PumpStreamHandler p = new PumpStreamHandler(out, err, processInputStream);
		p.setStopTimeout(500);
		exec.setStreamHandler(p);
		exec.execute(cl, envVars, new ExecuteResultHandler() {

			@Override
			public void onProcessFailed(ExecuteException e) {
				ba.raiseEventFromDifferentThread(Shell.this, null, 0, eventName + "_processcompleted", false,
						new Object[] {false, e.getExitValue(), "", e.toString()});
			}

			@Override
			public void onProcessComplete(int exitValue) {
				String so;
				String eo;
				try {
					if (out instanceof ByteArrayOutputStream) {
						so = new String(((ByteArrayOutputStream)out).toByteArray(), encoding);
						eo = new String(((ByteArrayOutputStream)err).toByteArray(), encoding);
					}
					else {
						so = "";
						eo = "";
					}

				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
				boolean success = true;
				if (wd.killedProcess()) {
					if (TimeoutMs != -1)
						eo += "\ntimeout";
					success = false;
				}
				ba.raiseEventFromDifferentThread(Shell.this, null, 0 , eventName + "_processcompleted", false,
						new Object[] {success, exitValue, so, eo});
				exec = null;

			}
		});
	}
	@ShortName("ShellSyncResult")
	public static class ShellSyncResult {
		public boolean Success;
		public int ExitCode;
		public String StdOut;
		public String StdErr;
		@Hide
		@Override
		public String toString() {
			return "ShellSyncResult [Success=" + Success + ", ExitCode=" + ExitCode + ", StdOut=" + StdOut + ", StdErr="
					+ StdErr + "]";
		}
		
	}
	class EventOutput extends OutputStream {
		private final BA ba;
		private final String event;
		public EventOutput (BA ba, String event) {
			this.ba = ba;
			this.event = event;
		}
		@Override
		public void write(int b) throws IOException {
			throw new RuntimeException("Not supported");
		}
		@Override
		public void write(byte b[], int off, int len) throws IOException {
			ba.raiseEvent2(Shell.this, false , eventName + event, false,
					new Object[] {b, len});
		}

	}


}
