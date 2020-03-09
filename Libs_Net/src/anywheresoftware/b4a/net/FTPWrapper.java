
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilters;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.CheckForReinitialize;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.streams.File;

/**
 * FTP allows you to communicate with FTP servers.
 */
@ShortName("FTP")
@Events(values={"DownloadCompleted (ServerPath As String, Success As Boolean)",
		"DownloadProgress (ServerPath As String, TotalDownloaded As Long, Total As Long)",
		"UploadCompleted (ServerPath As String, Success As Boolean)",
		"UploadProgress (ServerPath As String, TotalUploaded As Long, Total As Long)",
		"DeleteCompleted (ServerPath As String, Success As Boolean)",
		"CommandCompleted (Command As String, Success As Boolean, ReplyCode As Int, ReplyString As String)",
		"ListCompleted (ServerPath As String, Success As Boolean, Folders() As FTPEntry, Files() As FTPEntry)"})
@Permissions(values = {"android.permission.INTERNET"})
@Version(1.81f)
public class FTPWrapper implements CheckForReinitialize{
	FTPClient client;
	private String host, user, password;
	private int port;
	private int taskId;
	private volatile int numberOfTasks;
	private ReentrantLock lock = new ReentrantLock(true);
	private boolean passive;
	private String eventName;
	private MyCopyListener downloadProgress, uploadProgress;
	private boolean useSSL;
	private boolean useExplicitSSL = false;
	
	/**
	 * Communication timeout in milliseconds. The default value is 60000 (60 seconds).
	 */
	public int TimeoutMs = 60000;
	/**
	 * The Net library implements the following protocols: FTP, SMTP and POP3. Both regular connections and secured connections are supported.
	 *The implementations are based on <link>Apache Commons Net|http://commons.apache.org/net/</link>.
	 *All the methods in this library are non-blocking. 
	 *This library replaces the FTP library.
	 */
	public static void LIBRARY_DOC() {
		//
	}
	/**
	 * Initializes the object and sets the subs that will handle the events
	 */
	public void Initialize(BA ba, String EventName, String Host, int Port, String User, String Password) {
		client = new FTPClient();
		this.host = Host;
		this.port = Port;
		this.user = User;
		this.password = Password;
		this.eventName = EventName.toLowerCase(BA.cul);
		if (ba.subExists(eventName + "_downloadprogress")) {
			downloadProgress = new MyCopyListener(ba, "_downloadprogress");
		}
		if (ba.subExists(eventName + "_uploadprogress")) {
			uploadProgress = new MyCopyListener(ba, "_uploadprogress");
		}
		useSSL = false;
		useExplicitSSL = false;
		numberOfTasks = 0;
		passive = false;
	}
	private TrustManager[] trustManager;
	public void SetCustomSSLTrustManager(Object TrustManager) {
		trustManager = (TrustManager[])TrustManager;
	}
	/**
	 * Tests whether the object was initialized.
	 */
	public boolean IsInitialized() {
		return client != null;
	}
	/**
	 * Gets or sets whether the connection should be done with SSL sockets (FTPS Explicit).
	 */
	public void setUseSSLExplicit(boolean b) {
		useExplicitSSL = b;
	}
	public boolean getUseSSLExplicit() {
		return useExplicitSSL;
	}
	/**
	 * Gets or sets whether the connection should be done with SSL sockets (FTPS Implicit).
	 */
	public void setUseSSL(boolean b) {
		useSSL = b;
	}
	public boolean getUseSSL() {
		return useSSL;
	}
	/**
	 * Gets or sets whether FTP is in passive mode. The default mode is active mode.
	 */
	public void setPassiveMode(boolean b) {
		passive = b;
	}
	public boolean getPassiveMode() {
		return passive;
	}
	/**
	 * Downloads a file from the server. The DownloadCompleted event will be raised when download completes.
	 *Returns an object that can be used as the sender filter parameter in a Wait For call.
	 *DownloadProgress events will be raised during download.
	 *ServerFilePath - Full path to the remote file.
	 *AsciiFile - If True then end of line characters will be converted as needed. Note that Android end of line character is the same as Unix / Linux.
	 *DeviceFolder - Folder that the file will be saved to.
	 *DeviceFile - The name of the local file that will be created.
	 */
	public Object DownloadFile(final BA ba, final String ServerFilePath, 
			final boolean AsciiFile,final String DeviceFolder, final String DeviceFile) {
		
		final int myTask = taskId++;
		final Object senderFilter = new Object();
		numberOfTasks++;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				lock.lock();
				try
				{
					boolean success = true;
					try {
						connectIfNeeded();
						client.setFileType(AsciiFile ? FTP.ASCII_FILE_TYPE : FTP.BINARY_FILE_TYPE);
						if (downloadProgress != null) {
							downloadProgress.serverPath = ServerFilePath;
							client.setCopyStreamListener(downloadProgress);
						} else {
							client.setCopyStreamListener(null);
						}
						OutputStream out = File.OpenOutput(DeviceFolder, DeviceFile, false).getObject();
						try {

							if (!client.retrieveFile(ServerFilePath, out)) {
								throw new RuntimeException("Error retrieving file.\n" + client.getReplyString());
							}
						}
						finally {
							out.close();
						}
					} catch (Exception e) {
						success = false;
						ba.setLastException(e);
					}
					if (client != null) {
						ba.raiseEventFromDifferentThread(senderFilter, FTPWrapper.this, myTask, eventName + "_downloadcompleted", false, 
							new Object[] {ServerFilePath, success});
					}
				}
				finally {
					endOfTask();
				}
			}

		};
		BA.submitRunnable(runnable, this, myTask);
		return senderFilter;
	}
	/**
	 * Uploads a file to the server. The UploadCompleted event will be raised when upload completes.
	 *Returns an object that can be used as the sender filter parameter in a Wait For call.
	 *UploadProgress events will be raised during the upload.
	 *DeviceFolder - Local folder.
	 *DeviceFile - Local file name.
	 *AsciiFile - If True then end of line characters will be converted as needed. Note that Android end of line character is the same as Unix / Linux.
	 *ServerFilePath - Full path to file that will be created on the server.
	 */
	public Object UploadFile(final BA ba, final String DeviceFolder, final String DeviceFile, 
			final boolean AsciiFile, final String ServerFilePath) {
		return uploadFile(ba, DeviceFolder, DeviceFile, AsciiFile, ServerFilePath, false);
	}
	/**
	 * Similar to UploadFile. Appends the data to an existing file (if such exists).
	 */
	public Object AppendFile(final BA ba, final String DeviceFolder, final String DeviceFile, 
			final boolean AsciiFile, final String ServerFilePath) {
		return uploadFile(ba, DeviceFolder, DeviceFile, AsciiFile, ServerFilePath, true);
	}
	private Object uploadFile(final BA ba, final String DeviceFolder, final String DeviceFile, 
			final boolean AsciiFile, final String ServerFilePath, final boolean append) {
		final int myTask = taskId++;
		final Object senderFilter = new Object();
		numberOfTasks++;
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				lock.lock();
				try
				{
					boolean success = true;
					try {
						connectIfNeeded();
						client.setFileType(AsciiFile ? FTP.ASCII_FILE_TYPE : FTP.BINARY_FILE_TYPE);
						if (uploadProgress != null) {
							uploadProgress.serverPath = ServerFilePath;
							client.setCopyStreamListener(uploadProgress);
						} else {
							client.setCopyStreamListener(null);
						}
						InputStream in = File.OpenInput(DeviceFolder, DeviceFile).getObject();
						try {
							boolean res;
							if (append)
								res = client.appendFile(ServerFilePath, in);
							else
								res = client.storeFile(ServerFilePath, in);
							if (!res){
								throw new RuntimeException("Error uploading file.\n" + client.getReplyString());
							}
						}
						finally {
							in.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
						success = false;
						ba.setLastException(e);
					}
					if (client != null) {
						ba.raiseEventFromDifferentThread(senderFilter, FTPWrapper.this, myTask, eventName + "_uploadcompleted", false, 
							new Object[] {ServerFilePath, success});
					}
				}
				finally {
					endOfTask();
				}
			}

		};
		BA.submitRunnable(runnable, this, myTask);
		return senderFilter;
	}
	/**
	 * Fetches the list of folders and files in the specified path.
	 *The ListCompleted event will be raised when the data is available.
	 *Returns an object that can be used as the sender filter parameter in a Wait For call.

	 */
	public Object List(final BA ba, final String ServerPath) {
		final int myTask = taskId++;
		numberOfTasks++;
		final Object senderFilter = new Object();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				lock.lock();
				try
				{
					FTPFileWrapper[] dirs;
					FTPFileWrapper[] files;
					boolean success = true;
					try {
						connectIfNeeded();
						FTPFile[] all = client.listFiles(ServerPath, FTPFileFilters.NON_NULL);
						int countFolders = 0;
						for (FTPFile ff : all) {
							if (ff.isDirectory())
								countFolders++;
						}
						dirs = new FTPFileWrapper[countFolders];
						files = new FTPFileWrapper[all.length - countFolders];
						int d1 = 0, f1 = 0;
						for (int i = 0;i < all.length;i++) {
							FTPFileWrapper w = (FTPFileWrapper) AbsObjectWrapper.ConvertToWrapper(new FTPFileWrapper(), all[i]);
							if (all[i].isDirectory()) 
								dirs[d1++] = w;
							else
								files[f1++] = w;
						}
					} catch (Exception e) {
						e.printStackTrace();
						success = false;
						ba.setLastException(e);
						dirs = new FTPFileWrapper[0];
						files = new FTPFileWrapper[0];
					}
					if (client != null) {
						ba.raiseEventFromDifferentThread(senderFilter, FTPWrapper.this, myTask, eventName + "_listcompleted", false, 
							new Object[] {ServerPath, success, dirs, files});
					}

				}
				finally {
					endOfTask();
				}
			}

		};
		BA.submitRunnable(runnable, this, myTask);
		return senderFilter;
	}
	/**
	 * Sends an FTP command. The CommandCompleted event will be raised with the server reply.
	 *Should only be used with commands that return the reply in the command channel (not the data channel).
	 *It is possible that Success will be false and LastException will not be initialized.
	 	 *Returns an object that can be used as the sender filter parameter in a Wait For call.

	 *Common commands:
	 *MKD - Creates a new folder.
	 *RMD - Deletes an empty folder.
	 *Example:<code>
	 *FTP.SendCommand("MKD", "/somefolder/newfolder")</code>
	 */
	public Object SendCommand(final BA ba, final String Command, final String Parameters) {
		final int myTask = taskId++;
		numberOfTasks++;
		final Object senderFilter = new Object();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				lock.lock();
				try
				{
					boolean success = true;
					String replyString = "";
					int replyInt = 0;
					try {
						connectIfNeeded();
						success = client.doCommand(Command, Parameters.length() == 0 ? null : Parameters);
						replyString = client.getReplyString();
						replyInt = client.getReplyCode();
						if (!success) {
							ba.setLastException(null);
						}
					} catch (Exception e) {
						success = false;
						ba.setLastException(e);
						
					}
					if (client != null) {
						ba.raiseEventFromDifferentThread(senderFilter, FTPWrapper.this, myTask, eventName + "_commandcompleted", false, 
							new Object[] {Command, success, replyInt, replyString});
					}

				}
				finally {
					endOfTask();
				}
			}

		};
		BA.submitRunnable(runnable, this, myTask);
		return senderFilter;
	}
	/**
	 * Deletes a file from the server.
	 *The DeleteCompleted event will be raised when this task completes.
	 	 *Returns an object that can be used as the sender filter parameter in a Wait For call.

	 */
	public Object DeleteFile(final BA ba, final String ServerPath) {
		final int myTask = taskId++;
		numberOfTasks++;
		final Object senderFilter = new Object();
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				lock.lock();
				try
				{
					boolean success = true;
					try {
						connectIfNeeded();
						if (!client.deleteFile(ServerPath)) {
							throw new RuntimeException("Error deleting file.\n" + client.getReplyString());
						}
					} catch (Exception e) {
						success = false;
						ba.setLastException(e);
					}
					if (client != null) {
						ba.raiseEventFromDifferentThread(senderFilter, FTPWrapper.this, myTask, eventName + "_deletecompleted", false, 
							new Object[] {ServerPath, success});
					}

				}
				finally {
					endOfTask();
				}
			}

		};
		BA.submitRunnable(runnable, this, myTask);
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
					while (numberOfTasks > 0) {
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
	 *The data connection will only be closed when UploadProgress or DownloadProgress events fire.
	 */
	public void CloseNow() throws IOException {
		if (client == null)
			return;
		final FTPClient c = client;
		client = null;
		BA.submitRunnable(new Runnable() {

			@Override
			public void run() {
				if (c.isConnected()) {
					try {
						c.disconnect();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, null, 0);
		
	}
	private void connectIfNeeded() throws SocketException, IOException {
		if (client == null) {
			throw new RuntimeException("FTP should first be initialized.");
		}
		if (client.isConnected() == false) {
			client.setDefaultTimeout(TimeoutMs);
			if (useSSL && !useExplicitSSL)
				client.setSSL(trustManager);
			client.connect(host, port);
			client.setSoTimeout(TimeoutMs);
			client.setDataTimeout(TimeoutMs);
			if (passive)
				client.enterLocalPassiveMode();
			else
				client.enterLocalActiveMode();
			if (useExplicitSSL) {
				if (FTPReply.isPositiveCompletion(client.sendCommand("AUTH TLS")) == false) {
					throw new RuntimeException("AUTH refused.\n" + client.getReplyString());
				}
				client.mySslNegotiation(trustManager);
				if (!client.login(user, password)) {
					throw new RuntimeException(client.getReplyString());
				}
				if (FTPReply.isPositiveCompletion(client.sendCommand("PBSZ 0")) == false) {
					throw new RuntimeException(client.getReplyString());
				}

				if (FTPReply.isPositiveCompletion(client.sendCommand("PROT P")) == false) {
					throw new RuntimeException(client.getReplyString());
				}
			}
			else {
				if (FTPReply.isPositiveCompletion(client.getReplyCode()) == false) {
					throw new RuntimeException("Connection refused.\n" + client.getReplyString());
				}
				if (!client.login(user, password)) {
					throw new RuntimeException(client.getReplyString());
				}
			}
			
			
		}
	}
	private class MyCopyListener implements CopyStreamListener {
		String serverPath;
		private final BA ba;
		private final String eventType;
		public MyCopyListener(BA ba, String eventType) {
			this.ba = ba;
			this.eventType = eventType;
		}
		@Override
		public void bytesTransferred(CopyStreamEvent event) {

		}

		@Override
		public void bytesTransferred(long totalBytesTransferred,
				int bytesTransferred, long streamSize) {
			if (client == null)
				throw new RuntimeException("Client is null.");
			ba.raiseEventFromDifferentThread(FTPWrapper.this, null, 0, 
					eventName + eventType, false, new Object[] {serverPath, totalBytesTransferred, streamSize});
		}

	}
	
	/**
	 * FTPEntry represents a file or a folder. Call FTP.List to get the files and folders.
	 */
	@ShortName("FTPEntry")
	public static class FTPFileWrapper extends AbsObjectWrapper<FTPFile> {
		public String getName() {
			return getObject().getName();
		}
		public long getTimestamp() {
			return getObject().getTimestamp().getTimeInMillis();
		}
		public long getSize() {
			return getObject().getSize();
		}
	}
	/**
	 * CustomTrustManager allows you to create a SSL trust manager from a cert file or to create a trust manager that accepts all certificates. 
	 */
	@ShortName("CustomTrustManager")
	public static class CustomTrustManager extends AbsObjectWrapper<TrustManager[]>{
		/**
		 * Initializes the trust manager based on the given cert file.
		 */
		public void Initialize(String Dir, String FileName) throws Exception {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream caInput = File.OpenInput(Dir, FileName).getObject();
			Certificate ca;
			try {
			    ca = cf.generateCertificate(caInput);
			    BA.Log("certificate: ca=" + ((X509Certificate) ca).getSubjectDN());
			} finally {
			    caInput.close();
			}
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);

			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);
			setObject(tmf.getTrustManagers());
		}
		/**
		 * Initializes an "accept all" trust manager. This option should only be used in safe networks as it offers no real protection.
		 */
		public void InitializeAcceptAll() {
			setObject(new TrustManager[] {new X509TrustManager() {
				public void checkClientTrusted ( X509Certificate[] cert, String authType )
				throws CertificateException 
				{
				}

				public void checkServerTrusted ( X509Certificate[] cert, String authType ) 
				throws CertificateException 
				{
				}


				public X509Certificate[] getAcceptedIssuers ()
				{
					return null; 
				}
			}});
		}
	}

}
