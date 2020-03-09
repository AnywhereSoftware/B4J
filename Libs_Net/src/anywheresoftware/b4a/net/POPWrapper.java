
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
 
 package anywheresoftware.b4a.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.TrustManager;

import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3MessageInfo;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.streams.File.TextReaderWrapper;
/**
 * POP3 object allows you to connect to mail servers and read the mail messages.
 *This object returns the raw string of each message, including the headers. Parsing the raw string is currently out of the scope of this library.
 *The connection is established when it is first required.
 *ListCompleted event passes a parameter named Messages. This is a map with the messages IDs as keys and the messages sizes as values.
 *DownloadCompleted event passes the message raw string in the Message parameter.
 *Example:<code>
 *Sub Process_Globals
 *	Dim POP As POP3
 *End Sub
 *Sub Globals
 *
 *End Sub
 *
 *Sub Activity_Create(FirstTime As Boolean)
 *	If FirstTime Then
 *		POP.Initialize("pop.gmail.com", 995, "example@gmail.com", "mypassword", "pop")
 *		POP.UseSSL = True 'Gmail requires SSL.
 *	End If
 *	POP.ListMessages
 *End Sub
 *
 *Sub POP_ListCompleted (Success As Boolean, Messages As Map)
 *	Log("List: " & Success)
 *	If Success Then 
 *		For i = 0 To Messages.Size - 1
 *			Pop.DownloadMessage(Messages.GetKeyAt(i), True) 'Download all messages and delete them
 *		Next
 *	Else 
 *		Log(LastException.Message)
 *	End If
 *	POP.Close 'The connection will be closed after all messages are downloaded
 *End Sub
 *Sub POP_DownloadCompleted (Success As Boolean, MessageId As Int, Message As String)
 *	Log("Download: " & Success & ", " & MessageId)
 *	If Success Then 
 *		Log(Message)
 *		Log(Message.Length)
 *		Log(MessageId)
 *	Else 
 *		Log(LastException.Message)
 *	End If
 *End Sub</code>
 */
@Events(values= {"ListCompleted (Success As Boolean, Messages As Map)",
"DownloadCompleted (Success As Boolean, MessageId As Int, Message As String)",
"StatusCompleted (Success As Boolean, NumberOfMessages As Int, TotalSize As Int)"})
@Permissions(values = {"android.permission.INTERNET"})
@ShortName("POP3")
public class POPWrapper implements CheckForReinitialize{
	private String user, password, server;
	private int port;
	private static int taskId;
	private String eventName;
	private boolean useSSL;
	private POP3Client client;
	private volatile int numberOfTasks;
	private ReentrantLock lock = new ReentrantLock(true);
	/**
	 * Initializes the object.
	 *Server - Server address. Host name or Ip.
	 *Port - Mail server port.
	 *Username - Account user name.
	 *Password - Account password.
	 *EventName - The name of the sub that will handle the MessageSent event.
	 */
	public void Initialize(String Server, int Port, String Username, String Password, String EventName) {
		this.user = Username;
		this.password = Password;
		this.server = Server;
		this.port = Port;
		this.eventName = EventName.toLowerCase(BA.cul);
		numberOfTasks = 0;
		useSSL = false;
		client = new POP3Client();

	}
	public boolean IsInitialized() {
		return client != null;
	}
	private TrustManager[] trustManager;
	public void SetCustomSSLTrustManager(Object TrustManager) {
		trustManager = (TrustManager[])TrustManager;
	}
	/**
	 * Gets or sets whether the connection should be done with SSL sockets.
	 */
	public void setUseSSL(boolean b) {
		useSSL = b;
	}
	public boolean getUseSSL() {
		return useSSL;
	}
	private void connectIfNeeded(final BA ba) throws Exception {
		if (client == null) {
			throw new RuntimeException("POP3 should first be initialized.");
		}
		if (client.isConnected() == false) {
			if (useSSL)
				client.setSSL(trustManager);
			client.connect(server, port);
			if (client.login(user, password) == false)
				throw new RuntimeException("Error during login: " + client.getReplyString());
		}
	}
	/**
	 * Calls the server and when data is ready raises the ListCompleted event.
 	 *Returns an object that can be used as the sender filter parameter in a Wait For call.
	 *See the example described above.
	 */
	public Object ListMessages(final BA ba) {
		final int myTask = taskId++;
		numberOfTasks++;
		final Object senderFilter = new Object();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				Map m = new Map(); m.Initialize();
				lock.lock();
				try {
					try {
						connectIfNeeded(ba);
						POP3MessageInfo[] msgs = client.listMessages();
						if (msgs == null)
							throw new RuntimeException("Error listing messages: " + client.getReplyString()); 

						for (POP3MessageInfo msg : msgs) {
							m.Put(msg.number, msg.size);
						}
						ba.raiseEventFromDifferentThread(senderFilter, POPWrapper.this, myTask, eventName + "_listcompleted", false, new Object[] {true, m});
					} catch (Exception e) {
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(senderFilter, POPWrapper.this, myTask, eventName + "_listcompleted", false, new Object[] {false, m});

						try {
							CloseNow();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} finally {
					endOfTask();
				}
			}
		};
		BA.submitRunnable(r, POPWrapper.this, myTask);
		return senderFilter;
	}
	/**
	 * Gets the mailbox status. The StatusCompleted event will be raised when the request is completed with the number of messages and the total size.
	 *Returns an object that can be used as the sender filter parameter in a Wait For call.
	 */
	public Object Status(final BA ba) {
		final int myTask = taskId++;
		numberOfTasks++;
		final Object senderFilter = new Object();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				lock.lock();
				try {
					try {
						connectIfNeeded(ba);
						POP3MessageInfo p = client.status();
						if (p == null)
							throw new RuntimeException("Error g: " + client.getReplyString()); 
						ba.raiseEventFromDifferentThread(senderFilter, POPWrapper.this, myTask, eventName + "_statuscompleted", false, new Object[] {true, p.number, p.size});
					} catch (Exception e) {
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(senderFilter, POPWrapper.this, myTask, eventName + "_statuscompleted", false, new Object[] {false, 0, 0});

						try {
							CloseNow();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} finally {
					endOfTask();
				}
			}
		};
		BA.submitRunnable(r, POPWrapper.this, myTask);
		return senderFilter;
	}
	/**
	 * Calls the server and downloads the top number of lines from the message. When the message is ready the DownloadedCompleted event is raised.
	 *Returns an object that can be used as the sender filter parameter in a Wait For call.
	 *MessageId - The message id which was previously retrieved by calling ListMessages.
	 *NumberOfLines - Maximum number of lines to read from the message. 
	 *Delete - Whether to delete the message after it is downloaded. Note that the message will only be deleted after the connection is closed.
	 */
	public Object DownloadMessageTop(final BA ba,final int MessageId, final int NumberOfLines, final boolean Delete) {
		return downloadMessage(ba, MessageId, Delete, NumberOfLines);
	}
	/**
	 * Calls the server and downloads a message. When the message is ready the DownloadedCompleted event is raised.
	 *Returns an object that can be used as the sender filter parameter in a Wait For call.
	 *MessageId - The message id which was previously retrieved by calling ListMessages.
	 *Delete - Whether to delete the message after it is downloaded. Note that the message will only be deleted after the connection is closed.
	 */
	public Object DownloadMessage(final BA ba,final int MessageId,final boolean Delete) {
		return downloadMessage(ba, MessageId, Delete, -1);
	}
	private Object downloadMessage(final BA ba,final int MessageId,final boolean Delete, final int top) {
		final int myTask = taskId++;
		numberOfTasks++;
		final Object senderFilter = new Object();
		Runnable r = new Runnable() {
			@Override
			public void run() {
				lock.lock();
				try {
					try {
						connectIfNeeded(ba);
						Reader reader;
						if (top == -1) {
							if ((reader = client.retrieveMessage(MessageId)) == null)
								throw new RuntimeException("Error retrieving message: " + client.getReplyString());
						}
						else {
							if ((reader = client.retrieveMessageTop(MessageId, top)) == null)
								throw new RuntimeException("Error retrieving message: " + client.getReplyString());
						}
						BufferedReader br = (BufferedReader)reader;
						TextReaderWrapper t = new TextReaderWrapper();
						t.setObject(br);
						String msg = t.ReadAll();
						if (Delete) {
							if (client.deleteMessage(MessageId) == false) {
								throw new RuntimeException("Error deleting message: " + client.getReplyString());
							}
						}
						ba.raiseEventFromDifferentThread(senderFilter, POPWrapper.this, myTask, eventName + "_downloadcompleted", false, new Object[] {true, MessageId, msg});
					} catch (Exception e) {
						e.printStackTrace();
						ba.setLastException(e);
						ba.raiseEventFromDifferentThread(senderFilter, POPWrapper.this, myTask, eventName + "_downloadcompleted", false, new Object[] {false, MessageId, ""});
						try {
							CloseNow();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} finally {
					endOfTask();
				}
			}
		};
		BA.submitRunnable(r, POPWrapper.this, myTask);
		return senderFilter;
	}
	private void endOfTask() {
		lock.unlock();
		numberOfTasks--;
		if (client != null && numberOfTasks == 0) {
			synchronized (client) {
				client.notifyAll();
			}
		}
	}
	/**
	 * Closes the connection after all submitted tasks finish. Note that this method does not block.
	 */
	public void Close() {
		final int myTask = taskId++;
		if (client == null) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				synchronized (client) {
					while (numberOfTasks > 0 && client != null) {
						try {
							client.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					try {
						CloseNow();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		BA.submitRunnable(runnable, this, myTask);
	}
	/**
	 * Closes the connection immediately without waiting for current tasks to finish.
	 */
	public synchronized void CloseNow() throws IOException {
		if (client == null)
			return;
		if (client.isConnected()) {
			client.logout();
			client.disconnect();
		}
		synchronized (client) {
			client.notifyAll();
		}
		client = null;
	}

}
