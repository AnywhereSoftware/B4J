
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
import java.io.Writer;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.net.ssl.TrustManager;

import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.commons.net.smtp.SimpleSMTPHeader;
import org.apache.commons.net.smtp.AuthenticatingSMTPClient.AUTH_METHOD;
import org.apache.commons.net.util.Base64;
import org.apache.james.mime4j.codec.EncoderUtil;
import org.apache.james.mime4j.codec.EncoderUtil.Encoding;
import org.apache.james.mime4j.codec.EncoderUtil.Usage;

import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
import anywheresoftware.b4a.objects.streams.File;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;
import anywheresoftware.b4a.objects.streams.File.OutputStreamWrapper;

/**
 * SMTP object allows you to send emails with no user intervention and without relying on the device installed mail clients.
 *Both text messages and Html messages are supported as well as file attachments.
 *There are two encryption modes supported: UseSSL and StartTLSMode.
 *UseSSL means that the connection will be based on a SSL connection right from the start.
 *StartTLSMode means that the connection will only be upgraded to SSL after the client send the STARTTLS command. Most SMTP servers support this mode.
 *Gmail for example supports both modes. UseSSL on port 465 and StartTLSMode on port 587.
 *
 *Example:<code>
 *Sub Process_Globals
 *	Dim SMTP As SMTP
 *End Sub
 *Sub Globals
 *
 *End Sub
 *
 *Sub Activity_Create(FirstTime As Boolean)
 *	If FirstTime Then
 *		SMTP.Initialize("smtp.gmail.com", 587, "example@gmail.com", "mypassword", "SMTP")
 *		SMTP.StartTLSMode = True
 *	End If
 *	SMTP.To.Add("othermail@example.com")
 *	SMTP.Subject = "This is the subject"
 *	SMTP.Body = "This is the message body."
 *	SMTP.AddAttachment(File.DirRootExternal, "somefile")
 *	SMTP.Send
 *End Sub
 *Sub SMTP_MessageSent(Success As Boolean)
 *	Log(Success)
 *	If Success Then
 *		ToastMessageShow("Message sent successfully", True)
 *	Else
 *		ToastMessageShow("Error sending message", True)
 *		Log(LastException.Message)
 *	End If
 *End Sub</code>
 */
@ShortName("SMTP")
@Permissions(values = {"android.permission.INTERNET"})
@Events(values = {"MessageSent(Success As Boolean)"})
public class SMTPWrapper {
	private String user, password, server;
	private int port;
	private List to;
	private List cc;
	private List bcc;
	private ArrayList<String[]> attachments ;
	private String body;
	private String subject;
	private static int taskId;
	private String eventName;
	private boolean html;
	private boolean useSSL;
	private boolean startTLSMode;
	/**
	 * Gets or sets the Sender header. By default it is the same as the Username.
	 */
	public String Sender;
	/**
	 * Gets or sets the mail address that is sent with the MAIL command. By default it is the same as the Username.
	 */
	public String MailFrom;
	
	public static final AuthenticatingSMTPClient.AUTH_METHOD AUTH_PLAIN = AUTH_METHOD.PLAIN;
	public static final AuthenticatingSMTPClient.AUTH_METHOD AUTH_LOGIN = AUTH_METHOD.LOGIN;
	public static final AuthenticatingSMTPClient.AUTH_METHOD AUTH_CRAM_MD5 = AUTH_METHOD.CRAM_MD5;
	/**
	 * Additional headers that will be added to the message.
	 */
	public Map AdditionalHeaders;
	private AUTH_METHOD autoMethod = AUTH_PLAIN;
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
		prepareForNewMessage();
		html = false;
		useSSL = false;
		Sender = Username;
		MailFrom = Username;

	}
	private void prepareForNewMessage() {
		to = new List();to.Initialize();
		cc = new List();cc.Initialize();
		bcc = new List();bcc.Initialize();
		attachments = new ArrayList<String[]>();
		AdditionalHeaders = new Map();
		AdditionalHeaders.Initialize();
		body = "";
		subject = "";
	}
	private TrustManager[] trustManager;
	public void SetCustomSSLTrustManager(Object TrustManager) {
		trustManager = (TrustManager[])TrustManager;
	}

	/**
	 * Gets or sets the list of "To" recipients.
	 *Example:<code>SMTP.To.Add("email@example.com")</code>
	 */
	public void setTo(List To) {
		this.to = To;
	}
	public List getTo() {
		return to;
	}
	/**
	 * Gets or sets the list of "CC" recipients.
	 */
	public void setCC(List CC) {
		this.cc = CC;
	}
	public List getCC() {
		return cc;
	}
	/**
	 * Gets or sets the list of "BCC" recipients.
	 */
	public void setBCC(List BCC) {
		this.bcc = BCC;
	}
	public List getBCC() {
		return bcc;
	}
	/**
	 * Gets or sets the message body.
	 */
	public void setBody(String text)  {
		body = text;
	}
	public String getBody() {
		return body;
	}
	/**
	 * Gets or sets the message subject.
	 */
	public void setSubject(String text)  {
		subject = text;
	}
	public String getSubject() {
		return subject;
	}
	/**
	 * Gets or sets whether this message body is Html text.
	 */
	public void setHtmlBody(boolean b) {
		html = b;
	}
	public boolean getHtmlBody() {
		return html;
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
	/**
	 * Gets or sets whether the connection should be done in StartTLS mode.
	 */
	public void setStartTLSMode(boolean b) {
		startTLSMode = b;
	}
	public boolean getStartTLSMode() {
		return startTLSMode;
	}
	/**
	 * Gets or sets the SMTP AUTH method. Default value is PLAIN.
	 */
	public void setAuthMethod(AUTH_METHOD m) {
		autoMethod = m;
	}
	public AUTH_METHOD getAuthMethod() {
		return autoMethod;
	}
	/**
	 * Adds a file attachment.
	 */
	public void AddAttachment(String Dir, String FileName) {
		attachments.add(new String[] {Dir, FileName});
	}
	/**
	 * Sends the message. The MessageSent event will be raised after the message was sent.
	 *Returns an object that can be used as the sender filter parameter in a Wait For call.
	 *Note that the message fields are cleared after this method to allow you to send new messages with the same object.
	 */
	public Object Send(final BA ba) {
		if (to.getSize() == 0)
			throw new RuntimeException("To must include at least one recipient.");
		final List myTo = to, myCC = cc, myBCC = bcc;
		final ArrayList<String[]> myAttachments = attachments;
		final int task = taskId++;
		final String myBody = body, mySubject = subject;
		final boolean myHtml = html;
		final Map aheaders = AdditionalHeaders;
		final String mailSender = Sender;
		final String mailFrom = MailFrom;
		prepareForNewMessage();
		final Object senderFilter = new Object();
		Runnable r = new Runnable() {

			@Override
			public void run() {
				synchronized (SMTPWrapper.this) {


					AuthenticatingSMTPClient client = null;
					int reply;
					try {
						client = new AuthenticatingSMTPClient();
						if (useSSL && !startTLSMode)
							client.setSSL(trustManager);
						client.setDefaultTimeout(60000);
						client.connect(server, port);
						client.setSoTimeout(60000);
						reply = client.getReplyCode();

						if(!SMTPReply.isPositiveCompletion(reply)) {
							client.disconnect();
							throw new IOException("SMTP server refused connection.");
						}
						InetAddress ia = client.getLocalAddress();
						String hostAddress;
						if (ia instanceof Inet6Address)
							hostAddress = "[IPv6:"  + ia.getHostAddress() + "]";
						else
							hostAddress = ia.getHostAddress();
						client.ehlo(hostAddress);
						if (startTLSMode) {
							client.execTLS(trustManager);
							client.ehlo(hostAddress);
						}
						client.auth(autoMethod, user, password);
						client.setSender(mailFrom);
						String sub;
						if (EncoderUtil.hasToBeEncoded(mySubject, 0))
							sub = EncoderUtil.encodeEncodedWord(mySubject, Usage.TEXT_TOKEN,
									0, Charset.forName("UTF8"), Encoding.Q);
						else
							sub = mySubject;
						String encodedSender;
//						if (EncoderUtil.hasToBeEncoded(Sender, 0))
//							encodedSender = EncoderUtil.encodeEncodedWord(Sender, Usage.TEXT_TOKEN, 0, Charset.forName("UTF8"), Encoding.Q);
//						else
							encodedSender = mailSender;
						SimpleSMTPHeader header = new SimpleSMTPHeader(encodedSender, sub);
						for (Object key : aheaders.getObject().keySet()) {
							header.addHeaderField((String)key, (String)aheaders.Get(key));
						}
						if (myAttachments.size() == 0) {
							if (aheaders.ContainsKey("Content-Type") == false)
								header.addHeaderField("Content-Type", "text/" + (myHtml ? "html" : "plain") + "; charset=\"utf-8\"");
							if (aheaders.ContainsKey("Content-Transfer-Encoding") == false)
								header.addHeaderField("Content-Transfer-Encoding", "quoted-printable");
						}
						String bound = "asdasdwdwqd__HV_qwdqwdddwq";
						if (myAttachments.size() > 0)
							header.addHeaderField("Content-Type", "multipart/mixed; boundary=\"" + bound + "\"");
						for (int i = 0;i < myTo.getSize();i++) {
							String r = myTo.Get(i).toString();
							client.addRecipient(r);
							header.addTo(r);
						}
						for (int i = 0;i < myCC.getSize();i++) {
							String r = myCC.Get(i).toString();
							client.addRecipient(r);
							header.addCC(r);
						}
						for (int i = 0;i < myBCC.getSize();i++) {
							String r = myBCC.Get(i).toString();
							client.addRecipient(r);
						}
						Writer w = client.sendMessageData();
						if (w == null)
							throw new RuntimeException("Empty writer returned: " + client.getReplyString());
						w.append(header.toString());
						if (myAttachments.size() == 0)
							w.append(myBody);
						else {
							w.append("--" + bound + "\r\n");
							w.append("Content-Type: text/$TEXT$; charset=\"utf-8\"\r\n".replace("$TEXT$", myHtml ? "html" : "plain"));
							w.append("Content-Transfer-Encoding: quoted-printable\r\n\r\n"); //this was fixed and not released yet (January 2016).
							w.append(myBody);
							for (String[] s : myAttachments) {
								w.append("\r\n").append("--").append(bound).append("\r\n");
								w.append("Content-Type: application/octet-stream\r\n");
								w.append("Content-Transfer-Encoding: base64\r\n");
								w.append("Content-Disposition: attachment; filename=\"" + s[1] + "\"\r\n\r\n");

								InputStreamWrapper in = File.OpenInput(s[0], s[1]);
								OutputStreamWrapper out = new OutputStreamWrapper();
								out.InitializeToBytesArray(100);
								File.Copy2(in.getObject(), out.getObject());
								w.append(Base64.encodeBase64String(out.ToBytesArray()));
								w.append("\r\n");
							}
							w.append("--" + bound + "--");
						}
						w.close();
						if (client.completePendingCommand() == false)
							throw new RuntimeException("Error sending message: " + client.getReplyString());
						client.quit();
						client.disconnect();
						ba.raiseEventFromDifferentThread(senderFilter, SMTPWrapper.this, task,eventName + "_messagesent", false, new Object[]{true});
					} catch(Exception e) {
						e.printStackTrace();
						ba.setLastException(e);
						if(client != null && client.isConnected()) {
							try {
								client.disconnect();
							} catch(IOException f) {
								//
							}
						}
						ba.raiseEventFromDifferentThread(senderFilter, SMTPWrapper.this, task,eventName + "_messagesent", false, new Object[]{false});

					}

				}
			}
		};
		BA.submitRunnable(r, this, task);
		return senderFilter;
	}
}
