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
package org.apache.commons.net.ftp;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.net.ftp.parser.MLSxEntryParser;
import org.apache.commons.net.ftp.parser.ParserInitializationException;
import org.apache.commons.net.io.CRLFLineReader;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamException;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.commons.net.io.Util;

/***
 * FTPClient encapsulates all the functionality necessary to store and
 * retrieve files from an FTP server.  This class takes care of all
 * low level details of interacting with an FTP server and provides
 * a convenient higher level interface.  As with all classes derived
 * from {@link org.apache.commons.net.SocketClient},
 * you must first connect to the server with
 * {@link org.apache.commons.net.SocketClient#connect  connect }
 * before doing anything, and finally
 * {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
 * after you're completely finished interacting with the server.
 * Then you need to check the FTP reply code to see if the connection
 * was successful.  For example:
 * <pre>
 *    boolean error = false;
 *    try {
 *      int reply;
 *      ftp.connect("ftp.foobar.com");
 *      System.out.println("Connected to " + server + ".");
 *      System.out.print(ftp.getReplyString());
 *
 *      // After connection attempt, you should check the reply code to verify
 *      // success.
 *      reply = ftp.getReplyCode();
 *
 *      if(!FTPReply.isPositiveCompletion(reply)) {
 *        ftp.disconnect();
 *        System.err.println("FTP server refused connection.");
 *        System.exit(1);
 *      }
 *      ... // transfer files
 *      ftp.logout();
 *    } catch(IOException e) {
 *      error = true;
 *      e.printStackTrace();
 *    } finally {
 *      if(ftp.isConnected()) {
 *        try {
 *          ftp.disconnect();
 *        } catch(IOException ioe) {
 *          // do nothing
 *        }
 *      }
 *      System.exit(error ? 1 : 0);
 *    }
 * </pre>
 * <p>
 * Immediately after connecting is the only real time you need to check the
 * reply code (because connect is of type void).  The convention for all the
 * FTP command methods in FTPClient is such that they either return a
 * boolean value or some other value.
 * The boolean methods return true on a successful completion reply from
 * the FTP server and false on a reply resulting in an error condition or
 * failure.  The methods returning a value other than boolean return a value
 * containing the higher level data produced by the FTP command, or null if a
 * reply resulted in an error condition or failure.  If you want to access
 * the exact FTP reply code causing a success or failure, you must call
 * {@link org.apache.commons.net.ftp.FTP#getReplyCode  getReplyCode } after
 * a success or failure.
 * <p>
 * The default settings for FTPClient are for it to use
 * <code> FTP.ASCII_FILE_TYPE </code>,
 * <code> FTP.NON_PRINT_TEXT_FORMAT </code>,
 * <code> FTP.STREAM_TRANSFER_MODE </code>, and
 * <code> FTP.FILE_STRUCTURE </code>.  The only file types directly supported
 * are <code> FTP.ASCII_FILE_TYPE </code> and
 * <code> FTP.BINARY_FILE_TYPE </code>.  Because there are at least 4
 * different EBCDIC encodings, we have opted not to provide direct support
 * for EBCDIC.  To transfer EBCDIC and other unsupported file types you
 * must create your own filter InputStreams and OutputStreams and wrap
 * them around the streams returned or required by the FTPClient methods.
 * FTPClient uses the {@link ToNetASCIIOutputStream NetASCII}
 * filter streams to provide transparent handling of ASCII files.  We will
 * consider incorporating EBCDIC support if there is enough demand.
 * <p>
 * <code> FTP.NON_PRINT_TEXT_FORMAT </code>,
 * <code> FTP.STREAM_TRANSFER_MODE </code>, and
 * <code> FTP.FILE_STRUCTURE </code> are the only supported formats,
 * transfer modes, and file structures.
 * <p>
 * Because the handling of sockets on different platforms can differ
 * significantly, the FTPClient automatically issues a new PORT (or EPRT) command
 * prior to every transfer requiring that the server connect to the client's
 * data port.  This ensures identical problem-free behavior on Windows, Unix,
 * and Macintosh platforms.  Additionally, it relieves programmers from
 * having to issue the PORT (or EPRT) command themselves and dealing with platform
 * dependent issues.
 * <p>
 * Additionally, for security purposes, all data connections to the
 * client are verified to ensure that they originated from the intended
 * party (host and port).  If a data connection is initiated by an unexpected
 * party, the command will close the socket and throw an IOException.  You
 * may disable this behavior with
 * {@link #setRemoteVerificationEnabled setRemoteVerificationEnabled()}.
 * <p>
 * You should keep in mind that the FTP server may choose to prematurely
 * close a connection if the client has been idle for longer than a
 * given time period (usually 900 seconds).  The FTPClient class will detect a
 * premature FTP server connection closing when it receives a
 * {@link org.apache.commons.net.ftp.FTPReply#SERVICE_NOT_AVAILABLE FTPReply.SERVICE_NOT_AVAILABLE }
 *  response to a command.
 * When that occurs, the FTP class method encountering that reply will throw
 * an {@link org.apache.commons.net.ftp.FTPConnectionClosedException}
 * .
 * <code>FTPConnectionClosedException</code>
 * is a subclass of <code> IOException </code> and therefore need not be
 * caught separately, but if you are going to catch it separately, its
 * catch block must appear before the more general <code> IOException </code>
 * catch block.  When you encounter an
 * {@link org.apache.commons.net.ftp.FTPConnectionClosedException}
 * , you must disconnect the connection with
 * {@link #disconnect  disconnect() } to properly clean up the
 * system resources used by FTPClient.  Before disconnecting, you may check the
 * last reply code and text with
 * {@link org.apache.commons.net.ftp.FTP#getReplyCode  getReplyCode },
 * {@link org.apache.commons.net.ftp.FTP#getReplyString  getReplyString },
 * and
 * {@link org.apache.commons.net.ftp.FTP#getReplyStrings  getReplyStrings}.
 * You may avoid server disconnections while the client is idle by
 * periodically sending NOOP commands to the server.
 * <p>
 * Rather than list it separately for each method, we mention here that
 * every method communicating with the server and throwing an IOException
 * can also throw a
 * {@link org.apache.commons.net.MalformedServerReplyException}
 * , which is a subclass
 * of IOException.  A MalformedServerReplyException will be thrown when
 * the reply received from the server deviates enough from the protocol
 * specification that it cannot be interpreted in a useful manner despite
 * attempts to be as lenient as possible.
 * <p>
 * Listing API Examples
 * Both paged and unpaged examples of directory listings are available,
 * as follows:
 * <p>
 * Unpaged (whole list) access, using a parser accessible by auto-detect:
 * <pre>
 *    FTPClient f = new FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = listFiles(directory);
 * </pre>
 * <p>
 * Paged access, using a parser not accessible by auto-detect.  The class
 * defined in the first parameter of initateListParsing should be derived
 * from org.apache.commons.net.FTPFileEntryParser:
 * <pre>
 *    FTPClient f = new FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPListParseEngine engine =
 *       f.initiateListParsing("com.whatever.YourOwnParser", directory);
 *
 *    while (engine.hasNext()) {
 *       FTPFile[] files = engine.getNext(25);  // "page size" you want
 *       //do whatever you want with these files, display them, etc.
 *       //expensive FTPFile objects not created until needed.
 *    }
 * </pre>
 * <p>
 * Paged access, using a parser accessible by auto-detect:
 * <pre>
 *    FTPClient f = new FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPListParseEngine engine = f.initiateListParsing(directory);
 *
 *    while (engine.hasNext()) {
 *       FTPFile[] files = engine.getNext(25);  // "page size" you want
 *       //do whatever you want with these files, display them, etc.
 *       //expensive FTPFile objects not created until needed.
 *    }
 * </pre>
 * <p>
 * For examples of using FTPClient on servers whose directory listings
 * <ul>
 * <li>use languages other than English</li>
 * <li>use date formats other than the American English "standard" <code>MM d yyyy</code></li>
 * <li>are in different timezones and you need accurate timestamps for dependency checking
 *     as in Ant</li>
 * </ul>see {@link  FTPClientConfig  FTPClientConfig}.
 * <p>
 * <b>Control channel keep-alive feature</b>:<br/>
 * During file transfers, the data connection is busy, but the control connection is idle.
 * FTP servers know that the control connection is in use, so won't close it through lack of activity,
 * but it's a lot harder for network routers to know that the control and data connections are associated
 * with each other.
 * Some routers may treat the control connection as idle, and disconnect it if the transfer over the data
 * connection takes longer than the allowable idle time for the router.
 * <br/>
 * One solution to this is to send a safe command (i.e. NOOP) over the control connection to reset the router's
 * idle timer. This is enabled as follows:
 * <pre>
 *     ftpClient.setControlKeepAliveTimeout(300); // set timeout to 5 minutes
 * </pre>
 * This will cause the file upload/download methods to send a NOOP approximately every 5 minutes.
 * <p>
 * The implementation currently uses a {@link CopyStreamListener} which is passed to the
 * {@link Util#copyStream(InputStream, OutputStream, int, long, CopyStreamListener, boolean)}
 * method, so the timing is partially dependent on how long each block transfer takes.
 * <p>
 * <b>Note:</b> this does not apply to the methods where the user is responsible for writing or reading
 * the data stream, i.e. {@link #retrieveFileStream(String)} , {@link #storeFileStream(String)}
 * and the other xxxFileStream methods
 * <p>
 *
 * @author Rory Winston
 * @see FTP
 * @see FTPConnectionClosedException
 * @see FTPFileEntryParser
 * @see FTPFileEntryParserFactory
 * @see DefaultFTPFileEntryParserFactory
 * @see FTPClientConfig
 *
 * @see org.apache.commons.net.MalformedServerReplyException
 **/
@anywheresoftware.b4a.BA.Hide
public class FTPClient extends FTP
implements Configurable
{
    /**
     * The system property ({@value}) which can be used to override the system type.<br/>
     * If defined, the value will be used to create any automatically created parsers.
     *
     * @since 3.0
     */
    public static final String FTP_SYSTEM_TYPE = "org.apache.commons.net.ftp.systemType";

    /**
     * The name of an optional systemType properties file ({@value}), which is loaded
     * using {@link Class#getResourceAsStream(String)}.<br/>
     * The entries are the systemType (as determined by {@link FTPClient#getSystemType})
     * and the values are the replacement type or parserClass, which is passed to
     * {@link FTPFileEntryParserFactory#createFileEntryParser(String)}.<br/>
     * For example:
     * <pre>
     * Plan 9=Unix
     * OS410=org.apache.commons.net.ftp.parser.OS400FTPEntryParser
     * </pre>
     *
     * @since 3.0
     */
    public static final String SYSTEM_TYPE_PROPERTIES = "/systemType.properties";

    /***
     * A constant indicating the FTP session is expecting all transfers
     * to occur between the client (local) and server and that the server
     * should connect to the client's data port to initiate a data transfer.
     * This is the default data connection mode when and FTPClient instance
     * is created.
     ***/
    public static final int ACTIVE_LOCAL_DATA_CONNECTION_MODE = 0;
    /***
     * A constant indicating the FTP session is expecting all transfers
     * to occur between two remote servers and that the server
     * the client is connected to should connect to the other server's
     * data port to initiate a data transfer.
     ***/
    public static final int ACTIVE_REMOTE_DATA_CONNECTION_MODE = 1;
    /***
     * A constant indicating the FTP session is expecting all transfers
     * to occur between the client (local) and server and that the server
     * is in passive mode, requiring the client to connect to the
     * server's data port to initiate a transfer.
     ***/
    public static final int PASSIVE_LOCAL_DATA_CONNECTION_MODE = 2;
    /***
     * A constant indicating the FTP session is expecting all transfers
     * to occur between two remote servers and that the server
     * the client is connected to is in passive mode, requiring the other
     * server to connect to the first server's data port to initiate a data
     * transfer.
     ***/
    public static final int PASSIVE_REMOTE_DATA_CONNECTION_MODE = 3;

    private int __dataConnectionMode, __dataTimeout;
    private int __passivePort;
    private String __passiveHost;
    private final Random __random;
    private int __activeMinPort, __activeMaxPort;
    private InetAddress __activeExternalHost;
    private int __fileType;
    @SuppressWarnings("unused") // fields are written, but currently not read
    private int __fileFormat, __fileStructure, __fileTransferMode;
    private boolean __remoteVerificationEnabled;
    private long __restartOffset;
    private FTPFileEntryParserFactory __parserFactory;
    private int __bufferSize;
    private boolean __listHiddenFiles;
    private boolean __useEPSVwithIPv4; // whether to attempt EPSV with an IPv4 connection

    // __systemName is a cached value that should not be referenced directly
    // except when assigned in getSystemName and __initDefaults.
    private String __systemName;

    // __entryParser is a cached value that should not be referenced directly
    // except when assigned in listFiles(String, String) and __initDefaults.
    private FTPFileEntryParser __entryParser;

    // Key used to create the parser; necessary to ensure that the parser type is not ignored
    private String __entryParserKey;

    private FTPClientConfig __configuration;

    // Listener used by store/retrieve methods to handle keepalive
    private CopyStreamListener __copyStreamListener;

    // How long to wait before sending another control keep-alive message
    private long __controlKeepAliveTimeout;

    // How long to wait (ms) for keepalive message replies before continuing
    // Most FTP servers don't seem to support concurrent control and data connection usage
    private int __controlKeepAliveReplyTimeout=1000;

    /** Pattern for PASV mode responses. Groups: (n,n,n,n),(n),(n) */
    private static final java.util.regex.Pattern __PARMS_PAT;
    static {
        __PARMS_PAT = java.util.regex.Pattern.compile(
                "(\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}),(\\d{1,3}),(\\d{1,3})");
    }

    /** Controls the automatic server encoding detection (only UTF-8 supported). */
    private boolean __autodetectEncoding = false;

    /** Map of FEAT responses. If null, has not been initialised. */
    private HashMap<String, Set<String>> __featuresMap;

    private static class PropertiesSingleton {

        static final Properties PROPERTIES;

        static {
            InputStream resourceAsStream = FTPClient.class.getResourceAsStream(SYSTEM_TYPE_PROPERTIES);
            Properties p = null;
            if (resourceAsStream != null) {
                p = new Properties();
                try {
                    p.load(resourceAsStream);
                } catch (IOException e) {
                } finally {
                    try {
                        resourceAsStream.close();
                    } catch (IOException e) {
                        // Ignored
                    }
                }
            }
            PROPERTIES = p;
        }

    }
    private static Properties getOverrideProperties(){
        return PropertiesSingleton.PROPERTIES;
    }

    /**
     * Default FTPClient constructor.  Creates a new FTPClient instance
     * with the data connection mode set to
     * <code> ACTIVE_LOCAL_DATA_CONNECTION_MODE </code>, the file type
     * set to <code> FTP.ASCII_FILE_TYPE </code>, the
     * file format set to <code> FTP.NON_PRINT_TEXT_FORMAT </code>,
     * the file structure set to <code> FTP.FILE_STRUCTURE </code>, and
     * the transfer mode set to <code> FTP.STREAM_TRANSFER_MODE </code>.
     * <p>
     * The list parsing auto-detect feature can be configured to use lenient future
     * dates (short dates may be up to one day in the future) as follows:
     * <pre>
     * FTPClient ftp = new FTPClient();
     * FTPClientConfig config = new FTPClientConfig();
     * config.setLenientFutureDates(true);
     * ftp.configure(config );
     * </pre>
     **/
    public FTPClient()
    {
        __initDefaults();
        __dataTimeout = -1;
        __remoteVerificationEnabled = true;
        __parserFactory = new DefaultFTPFileEntryParserFactory();
        __configuration      = null;
        __listHiddenFiles = false;
        __useEPSVwithIPv4 = false;
        __random = new Random();
    }


    private void __initDefaults()
    {
        __dataConnectionMode = ACTIVE_LOCAL_DATA_CONNECTION_MODE;
        __passiveHost        = null;
        __passivePort        = -1;
        __activeExternalHost = null;
        __activeMinPort = 0;
        __activeMaxPort = 0;
        __fileType           = FTP.ASCII_FILE_TYPE;
        __fileStructure      = FTP.FILE_STRUCTURE;
        __fileFormat         = FTP.NON_PRINT_TEXT_FORMAT;
        __fileTransferMode   = FTP.STREAM_TRANSFER_MODE;
        __restartOffset      = 0;
        __systemName         = null;
        __entryParser        = null;
        __entryParserKey    = "";
        __bufferSize         = Util.DEFAULT_COPY_BUFFER_SIZE;
        __featuresMap = null;
    }

    private String __parsePathname(String reply)
    {
        int begin, end;

        begin = reply.indexOf('"') + 1;
        end = reply.indexOf('"', begin);

        return reply.substring(begin, end);
    }


    private void __parsePassiveModeReply(String reply)
    throws MalformedServerReplyException
    {
        java.util.regex.Matcher m = __PARMS_PAT.matcher(reply);
        if (!m.find()) {
            throw new MalformedServerReplyException(
                    "Could not parse passive host information.\nServer Reply: " + reply);
        }

        __passiveHost = m.group(1).replace(',', '.'); // Fix up to look like IP address

        try
        {
            int oct1 = Integer.parseInt(m.group(2));
            int oct2 = Integer.parseInt(m.group(3));
            __passivePort = (oct1 << 8) | oct2;
        }
        catch (NumberFormatException e)
        {
            throw new MalformedServerReplyException(
                    "Could not parse passive port information.\nServer Reply: " + reply);
        }

        try {
            InetAddress host = InetAddress.getByName(__passiveHost);
            // reply is a local address, but target is not - assume NAT box changed the PASV reply
            if (host.isSiteLocalAddress() && !getRemoteAddress().isSiteLocalAddress()){
                String hostAddress = getRemoteAddress().getHostAddress();
                fireReplyReceived(0,
                            "[Replacing site local address "+__passiveHost+" with "+hostAddress+"]\n");
                __passiveHost = hostAddress;
            }
        } catch (UnknownHostException e) { // Should not happen as we are passing in an IP address
            throw new MalformedServerReplyException(
                    "Could not parse passive host information.\nServer Reply: " + reply);
        }
    }

    private void __parseExtendedPassiveModeReply(String reply)
    throws MalformedServerReplyException
    {
        int port;

        reply = reply.substring(reply.indexOf('(') + 1,
                reply.indexOf(')')).trim();

        char delim1, delim2, delim3, delim4;
        delim1 = reply.charAt(0);
        delim2 = reply.charAt(1);
        delim3 = reply.charAt(2);
        delim4 = reply.charAt(reply.length()-1);

        if (!(delim1 == delim2) || !(delim2 == delim3)
                || !(delim3 == delim4))
            throw new MalformedServerReplyException(
                    "Could not parse extended passive host information.\nServer Reply: " + reply);
        try
        {
            port = Integer.parseInt(reply.substring(3, reply.length()-1));
        }
        catch (NumberFormatException e)
        {
            throw new MalformedServerReplyException(
                    "Could not parse extended passive host information.\nServer Reply: " + reply);
        }


        // in EPSV mode, the passive host address is implicit
        __passiveHost = getRemoteAddress().getHostAddress();
        __passivePort = port;
    }

    private boolean __storeFile(int command, String remote, InputStream local)
    throws IOException
    {
        OutputStream output;
        Socket socket;

        if ((socket = _openDataConnection_(command, remote)) == null)
            return false;

        output = new BufferedOutputStream(socket.getOutputStream(),
                getBufferSize()
        );
        if (__fileType == ASCII_FILE_TYPE)
            output = new ToNetASCIIOutputStream(output);

        CSL csl = null;
        if (__controlKeepAliveTimeout > 0) {
            csl = new CSL(this, __controlKeepAliveTimeout, __controlKeepAliveReplyTimeout);
        }

        // Treat everything else as binary for now
        try
        {
            Util.copyStream(local, output, getBufferSize(),
                    CopyStreamEvent.UNKNOWN_STREAM_SIZE, __mergeListeners(csl),
                    false);
        }
        catch (IOException e)
        {
            Util.closeQuietly(socket); // ignore close errors here
            throw e;
        }

        output.close(); // ensure the file is fully written
        socket.close(); // done writing the file
        // Get the transfer response
        boolean ok = completePendingCommand();
        if (csl != null) {
            csl.cleanUp(); // fetch any outstanding keepalive replies
        }
        return ok;
    }

    private OutputStream __storeFileStream(int command, String remote)
    throws IOException
    {
        OutputStream output;
        Socket socket;

        if ((socket = _openDataConnection_(command, remote)) == null)
            return null;

        output = socket.getOutputStream();
        if (__fileType == ASCII_FILE_TYPE) {
            // We buffer ascii transfers because the buffering has to
            // be interposed between ToNetASCIIOutputSream and the underlying
            // socket output stream.  We don't buffer binary transfers
            // because we don't want to impose a buffering policy on the
            // programmer if possible.  Programmers can decide on their
            // own if they want to wrap the SocketOutputStream we return
            // for file types other than ASCII.
            output = new BufferedOutputStream(output,
                    getBufferSize());
            output = new ToNetASCIIOutputStream(output);

        }
        return new org.apache.commons.net.io.SocketOutputStream(socket, output);
    }


    /**
     * Establishes a data connection with the FTP server, returning
     * a Socket for the connection if successful.  If a restart
     * offset has been set with {@link #setRestartOffset(long)},
     * a REST command is issued to the server with the offset as
     * an argument before establishing the data connection.  Active
     * mode connections also cause a local PORT command to be issued.
     * <p>
     * @param command  The text representation of the FTP command to send.
     * @param arg The arguments to the FTP command.  If this parameter is
     *             set to null, then the command is sent with no argument.
     * @return A Socket corresponding to the established data connection.
     *         Null is returned if an FTP protocol error is reported at
     *         any point during the establishment and initialization of
     *         the connection.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     */
    protected Socket _openDataConnection_(int command, String arg)
    throws IOException
    {
        Socket socket;

        if (__dataConnectionMode != ACTIVE_LOCAL_DATA_CONNECTION_MODE &&
                __dataConnectionMode != PASSIVE_LOCAL_DATA_CONNECTION_MODE)
            return null;

        final boolean isInet6Address = getRemoteAddress() instanceof Inet6Address;

        if (__dataConnectionMode == ACTIVE_LOCAL_DATA_CONNECTION_MODE)
        {
            // if no activePortRange was set (correctly) -> getActivePort() = 0
            // -> new ServerSocket(0) -> bind to any free local port
            ServerSocket server = _serverSocketFactory_.createServerSocket(getActivePort(), 1, getHostAddress());

            // Try EPRT only if remote server is over IPv6, if not use PORT,
            // because EPRT has no advantage over PORT on IPv4.
            // It could even have the disadvantage,
            // that EPRT will make the data connection fail, because
            // today's intelligent NAT Firewalls are able to
            // substitute IP addresses in the PORT command,
            // but might not be able to recognize the EPRT command.
            if (isInet6Address)
            {
                if (!FTPReply.isPositiveCompletion(eprt(getHostAddress(), server.getLocalPort())))
                {
                    server.close();
                    return null;
                }
            }
            else
            {
                if (!FTPReply.isPositiveCompletion(port(getHostAddress(), server.getLocalPort())))
                {
                    server.close();
                    return null;
                }
            }

            if ((__restartOffset > 0) && !restart(__restartOffset))
            {
                server.close();
                return null;
            }

            if (!FTPReply.isPositivePreliminary(sendCommand(command, arg)))
            {
                server.close();
                return null;
            }

            // For now, let's just use the data timeout value for waiting for
            // the data connection.  It may be desirable to let this be a
            // separately configurable value.  In any case, we really want
            // to allow preventing the accept from blocking indefinitely.
            if (__dataTimeout >= 0)
                server.setSoTimeout(__dataTimeout);
            try {
                socket = server.accept();
            } finally {
                server.close();
            }
        }
        else
        { // We must be in PASSIVE_LOCAL_DATA_CONNECTION_MODE

            // Try EPSV command first on IPv6 - and IPv4 if enabled.
            // When using IPv4 with NAT it has the advantage
            // to work with more rare configurations.
            // E.g. if FTP server has a static PASV address (external network)
            // and the client is coming from another internal network.
            // In that case the data connection after PASV command would fail,
            // while EPSV would make the client succeed by taking just the port.
            boolean attemptEPSV = isUseEPSVwithIPv4() || isInet6Address;
            if (attemptEPSV && epsv() == FTPReply.ENTERING_EPSV_MODE)
            {
                __parseExtendedPassiveModeReply(_replyLines.get(0));
            }
            else
            {
                if (isInet6Address) {
                    return null; // Must use EPSV for IPV6
                }
                // If EPSV failed on IPV4, revert to PASV
                if (pasv() != FTPReply.ENTERING_PASSIVE_MODE) {
                    return null;
                }
                __parsePassiveModeReply(_replyLines.get(0));
            }

            socket = _socketFactory_.createSocket();
            socket.connect(new InetSocketAddress(__passiveHost, __passivePort), connectTimeout);
            if ((__restartOffset > 0) && !restart(__restartOffset))
            {
                socket.close();
                return null;
            }

            if (!FTPReply.isPositivePreliminary(sendCommand(command, arg)))
            {
                socket.close();
                return null;
            }
        }

        if (__remoteVerificationEnabled && !verifyRemote(socket))
        {
            InetAddress host1, host2;

            host1 = socket.getInetAddress();
            host2 = getRemoteAddress();

            socket.close();

            throw new IOException(
                    "Host attempting data connection " + host1.getHostAddress() +
                    " is not same as server " + host2.getHostAddress());
        }

        if (__dataTimeout >= 0)
            socket.setSoTimeout(__dataTimeout);
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket)socket;

            sslSocket.setUseClientMode(true);
            sslSocket.setEnableSessionCreation(true);
            sslSocket.startHandshake();
        }
        return socket;
    }


    @Override
    protected void _connectAction_() throws IOException
    {
        super._connectAction_(); // sets up _input_ and _output_
        __initDefaults();
        // must be after super._connectAction_(), because otherwise we get an
        // Exception claiming we're not connected
        if ( __autodetectEncoding )
        {
            ArrayList<String> oldReplyLines = new ArrayList<String> (_replyLines);
            int oldReplyCode = _replyCode;
            if ( hasFeature("UTF8") || hasFeature("UTF-8")) // UTF8 appears to be the default
            {
                 setControlEncoding("UTF-8");
                 _controlInput_ =
                     new CRLFLineReader(new InputStreamReader(_input_, getControlEncoding()));
                 _controlOutput_ =
                    new BufferedWriter(new OutputStreamWriter(_output_, getControlEncoding()));
            }
            // restore the original reply (server greeting)
            _replyLines.clear();
            _replyLines.addAll(oldReplyLines);
            _replyCode = oldReplyCode;
        }
    }


    /***
     * Sets the timeout in milliseconds to use when reading from the
     * data connection.  This timeout will be set immediately after
     * opening the data connection.
     * <p>
     * @param  timeout The default timeout in milliseconds that is used when
     *        opening a data connection socket.
     ***/
    public void setDataTimeout(int timeout)
    {
        __dataTimeout = timeout;
    }

    /**
     * set the factory used for parser creation to the supplied factory object.
     *
     * @param parserFactory
     *               factory object used to create FTPFileEntryParsers
     *
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     */
    public void setParserFactory(FTPFileEntryParserFactory parserFactory) {
        __parserFactory = parserFactory;
    }


    /***
     * Closes the connection to the FTP server and restores
     * connection parameters to the default values.
     * <p>
     * @exception IOException If an error occurs while disconnecting.
     ***/
    @Override
    public void disconnect() throws IOException
    {
        super.disconnect();
        __initDefaults();
    }


    /***
     * Enable or disable verification that the remote host taking part
     * of a data connection is the same as the host to which the control
     * connection is attached.  The default is for verification to be
     * enabled.  You may set this value at any time, whether the
     * FTPClient is currently connected or not.
     * <p>
     * @param enable True to enable verification, false to disable verification.
     ***/
    public void setRemoteVerificationEnabled(boolean enable)
    {
        __remoteVerificationEnabled = enable;
    }

    /***
     * Return whether or not verification of the remote host participating
     * in data connections is enabled.  The default behavior is for
     * verification to be enabled.
     * <p>
     * @return True if verification is enabled, false if not.
     ***/
    public boolean isRemoteVerificationEnabled()
    {
        return __remoteVerificationEnabled;
    }

    /***
     * Login to the FTP server using the provided username and password.
     * <p>
     * @param username The username to login under.
     * @param password The password to use.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean login(String username, String password) throws IOException
    {

        user(username);

        if (FTPReply.isPositiveCompletion(_replyCode))
            return true;

        // If we get here, we either have an error code, or an intermmediate
        // reply requesting password.
        if (!FTPReply.isPositiveIntermediate(_replyCode))
            return false;

        return FTPReply.isPositiveCompletion(pass(password));
    }


    /***
     * Login to the FTP server using the provided username, password,
     * and account.  If no account is required by the server, only
     * the username and password, the account information is not used.
     * <p>
     * @param username The username to login under.
     * @param password The password to use.
     * @param account  The account to use.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean login(String username, String password, String account)
    throws IOException
    {
        user(username);

        if (FTPReply.isPositiveCompletion(_replyCode))
            return true;

        // If we get here, we either have an error code, or an intermmediate
        // reply requesting password.
        if (!FTPReply.isPositiveIntermediate(_replyCode))
            return false;

        pass(password);

        if (FTPReply.isPositiveCompletion(_replyCode))
            return true;

        if (!FTPReply.isPositiveIntermediate(_replyCode))
            return false;

        return FTPReply.isPositiveCompletion(acct(account));
    }

    /***
     * Logout of the FTP server by sending the QUIT command.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean logout() throws IOException
    {
        return FTPReply.isPositiveCompletion(quit());
    }


    /***
     * Change the current working directory of the FTP session.
     * <p>
     * @param pathname  The new current working directory.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean changeWorkingDirectory(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(cwd(pathname));
    }


    /***
     * Change to the parent directory of the current working directory.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean changeToParentDirectory() throws IOException
    {
        return FTPReply.isPositiveCompletion(cdup());
    }


    /***
     * Issue the FTP SMNT command.
     * <p>
     * @param pathname The pathname to mount.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean structureMount(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(smnt(pathname));
    }

    /***
     * Reinitialize the FTP session.  Not all FTP servers support this
     * command, which issues the FTP REIN command.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    boolean reinitialize() throws IOException
    {
        rein();

        if (FTPReply.isPositiveCompletion(_replyCode) ||
                (FTPReply.isPositivePreliminary(_replyCode) &&
                        FTPReply.isPositiveCompletion(getReply())))
        {

            __initDefaults();

            return true;
        }

        return false;
    }


    /***
     * Set the current data connection mode to
     * <code>ACTIVE_LOCAL_DATA_CONNECTION_MODE</code>.  No communication
     * with the FTP server is conducted, but this causes all future data
     * transfers to require the FTP server to connect to the client's
     * data port.  Additionally, to accommodate differences between socket
     * implementations on different platforms, this method causes the
     * client to issue a PORT command before every data transfer.
     ***/
    public void enterLocalActiveMode()
    {
        __dataConnectionMode = ACTIVE_LOCAL_DATA_CONNECTION_MODE;
        __passiveHost = null;
        __passivePort = -1;
    }


    /***
     * Set the current data connection mode to
     * <code> PASSIVE_LOCAL_DATA_CONNECTION_MODE </code>.  Use this
     * method only for data transfers between the client and server.
     * This method causes a PASV (or EPSV) command to be issued to the server
     * before the opening of every data connection, telling the server to
     * open a data port to which the client will connect to conduct
     * data transfers.  The FTPClient will stay in
     * <code> PASSIVE_LOCAL_DATA_CONNECTION_MODE </code> until the
     * mode is changed by calling some other method such as
     * {@link #enterLocalActiveMode  enterLocalActiveMode() }
     * <p>
     * <b>N.B.</b> currently calling any connect method will reset the mode to
     * ACTIVE_LOCAL_DATA_CONNECTION_MODE.
     ***/
    public void enterLocalPassiveMode()
    {
        __dataConnectionMode = PASSIVE_LOCAL_DATA_CONNECTION_MODE;
        // These will be set when just before a data connection is opened
        // in _openDataConnection_()
        __passiveHost = null;
        __passivePort = -1;
    }


    /***
     * Set the current data connection mode to
     * <code> ACTIVE_REMOTE_DATA_CONNECTION </code>.  Use this method only
     * for server to server data transfers.  This method issues a PORT
     * command to the server, indicating the other server and port to which
     * it should connect for data transfers.  You must call this method
     * before EVERY server to server transfer attempt.  The FTPClient will
     * NOT automatically continue to issue PORT commands.  You also
     * must remember to call
     * {@link #enterLocalActiveMode  enterLocalActiveMode() } if you
     * wish to return to the normal data connection mode.
     * <p>
     * @param host The passive mode server accepting connections for data
     *             transfers.
     * @param port The passive mode server's data port.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean enterRemoteActiveMode(InetAddress host, int port)
    throws IOException
    {
        if (FTPReply.isPositiveCompletion(port(host, port)))
        {
            __dataConnectionMode = ACTIVE_REMOTE_DATA_CONNECTION_MODE;
            __passiveHost = null;
            __passivePort = -1;
            return true;
        }
        return false;
    }

    /***
     * Set the current data connection mode to
     * <code> PASSIVE_REMOTE_DATA_CONNECTION_MODE </code>.  Use this
     * method only for server to server data transfers.
     * This method issues a PASV command to the server, telling it to
     * open a data port to which the active server will connect to conduct
     * data transfers.  You must call this method
     * before EVERY server to server transfer attempt.  The FTPClient will
     * NOT automatically continue to issue PASV commands.  You also
     * must remember to call
     * {@link #enterLocalActiveMode  enterLocalActiveMode() } if you
     * wish to return to the normal data connection mode.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean enterRemotePassiveMode() throws IOException
    {
        if (pasv() != FTPReply.ENTERING_PASSIVE_MODE)
            return false;

        __dataConnectionMode = PASSIVE_REMOTE_DATA_CONNECTION_MODE;
        __parsePassiveModeReply(_replyLines.get(0));

        return true;
    }

    /***
     * Returns the hostname or IP address (in the form of a string) returned
     * by the server when entering passive mode.  If not in passive mode,
     * returns null.  This method only returns a valid value AFTER a
     * data connection has been opened after a call to
     * {@link #enterLocalPassiveMode enterLocalPassiveMode()}.
     * This is because FTPClient sends a PASV command to the server only
     * just before opening a data connection, and not when you call
     * {@link #enterLocalPassiveMode enterLocalPassiveMode()}.
     * <p>
     * @return The passive host name if in passive mode, otherwise null.
     ***/
    public String getPassiveHost()
    {
        return __passiveHost;
    }

    /***
     * If in passive mode, returns the data port of the passive host.
     * This method only returns a valid value AFTER a
     * data connection has been opened after a call to
     * {@link #enterLocalPassiveMode enterLocalPassiveMode()}.
     * This is because FTPClient sends a PASV command to the server only
     * just before opening a data connection, and not when you call
     * {@link #enterLocalPassiveMode enterLocalPassiveMode()}.
     * <p>
     * @return The data port of the passive server.  If not in passive
     *         mode, undefined.
     ***/
    public int getPassivePort()
    {
        return __passivePort;
    }


    /***
     * Returns the current data connection mode (one of the
     * <code> _DATA_CONNECTION_MODE </code> constants.
     * <p>
     * @return The current data connection mode (one of the
     * <code> _DATA_CONNECTION_MODE </code> constants.
     ***/
    public int getDataConnectionMode()
    {
        return __dataConnectionMode;
    }

    /**
     * Get the client port for active mode.
     * <p>
     * @return The client port for active mode.
     */
    private int getActivePort()
    {
        if (__activeMinPort > 0 && __activeMaxPort >= __activeMinPort)
        {
            if (__activeMaxPort == __activeMinPort)
                return __activeMaxPort;
            // Get a random port between the min and max port range
            return __random.nextInt(__activeMaxPort - __activeMinPort + 1) + __activeMinPort;
        }
        else
        {
            // default port
            return 0;
        }
    }

    /**
     * Get the host address for active mode.
     * <p>
     * @return The host address for active mode.
     */
    private InetAddress getHostAddress()
    {
        if (__activeExternalHost != null)
        {
            return __activeExternalHost;
        }
        else
        {
            // default local address
            return getLocalAddress();
        }
    }

    /***
     * Set the client side port range in active mode.
     * <p>
     * @param minPort The lowest available port (inclusive).
     * @param maxPort The highest available port (inclusive).
     * @since 2.2
     ***/
    public void setActivePortRange(int minPort, int maxPort)
    {
        this.__activeMinPort = minPort;
        this.__activeMaxPort = maxPort;
    }

    /***
     * Set the external IP address in active mode.
     * Useful when there are multiple network cards.
     * <p>
     * @param ipAddress The external IP address of this machine.
     * @throws UnknownHostException
     * @since 2.2
     ***/
    public void setActiveExternalIPAddress(String ipAddress) throws UnknownHostException
    {
        this.__activeExternalHost = InetAddress.getByName(ipAddress);
    }


    /***
     * Sets the file type to be transferred.  This should be one of
     * <code> FTP.ASCII_FILE_TYPE </code>, <code> FTP.BINARY_FILE_TYPE</code>,
     * etc.  The file type only needs to be set when you want to change the
     * type.  After changing it, the new type stays in effect until you change
     * it again.  The default file type is <code> FTP.ASCII_FILE_TYPE </code>
     * if this method is never called.
     * <p>
     * <b>N.B.</b> currently calling any connect method will reset the mode to
     * ACTIVE_LOCAL_DATA_CONNECTION_MODE.
     * @param fileType The <code> _FILE_TYPE </code> constant indcating the
     *                 type of file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean setFileType(int fileType) throws IOException
    {
        if (FTPReply.isPositiveCompletion(type(fileType)))
        {
            __fileType = fileType;
            __fileFormat = FTP.NON_PRINT_TEXT_FORMAT;
            return true;
        }
        return false;
    }


    /***
     * Sets the file type to be transferred and the format.  The type should be
     * one of  <code> FTP.ASCII_FILE_TYPE </code>,
     * <code> FTP.BINARY_FILE_TYPE </code>, etc.  The file type only needs to
     * be set when you want to change the type.  After changing it, the new
     * type stays in effect until you change it again.  The default file type
     * is <code> FTP.ASCII_FILE_TYPE </code> if this method is never called.
     * The format should be one of the FTP class <code> TEXT_FORMAT </code>
     * constants, or if the type is <code> FTP.LOCAL_FILE_TYPE </code>, the
     * format should be the byte size for that type.  The default format
     * is <code> FTP.NON_PRINT_TEXT_FORMAT </code> if this method is never
     * called.
     * <p>
     * <b>N.B.</b> currently calling any connect method will reset the mode to
     * ACTIVE_LOCAL_DATA_CONNECTION_MODE.
     * <p>
     * @param fileType The <code> _FILE_TYPE </code> constant indcating the
     *                 type of file.
     * @param formatOrByteSize  The format of the file (one of the
     *              <code>_FORMAT</code> constants.  In the case of
     *              <code>LOCAL_FILE_TYPE</code>, the byte size.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean setFileType(int fileType, int formatOrByteSize)
    throws IOException
    {
        if (FTPReply.isPositiveCompletion(type(fileType, formatOrByteSize)))
        {
            __fileType = fileType;
            __fileFormat = formatOrByteSize;
            return true;
        }
        return false;
    }


    /***
     * Sets the file structure.  The default structure is
     * <code> FTP.FILE_STRUCTURE </code> if this method is never called.
     * <p>
     * @param structure  The structure of the file (one of the FTP class
     *         <code>_STRUCTURE</code> constants).
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean setFileStructure(int structure) throws IOException
    {
        if (FTPReply.isPositiveCompletion(stru(structure)))
        {
            __fileStructure = structure;
            return true;
        }
        return false;
    }


    /***
     * Sets the transfer mode.  The default transfer mode
     * <code> FTP.STREAM_TRANSFER_MODE </code> if this method is never called.
     * <p>
     * @param mode  The new transfer mode to use (one of the FTP class
     *         <code>_TRANSFER_MODE</code> constants).
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean setFileTransferMode(int mode) throws IOException
    {
        if (FTPReply.isPositiveCompletion(mode(mode)))
        {
            __fileTransferMode = mode;
            return true;
        }
        return false;
    }


    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to retrieve a given file from
     * the other server.
     * <p>
     * @param filename  The name of the file to retrieve.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteRetrieve(String filename) throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(retr(filename));
        return false;
    }


    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to store a file on
     * the other server using the given filename.  The other server must
     * have had a <code> remoteRetrieve </code> issued to it by another
     * FTPClient.
     * <p>
     * @param filename  The name to call the file that is to be stored.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteStore(String filename) throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(stor(filename));
        return false;
    }


    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to store a file on
     * the other server using a unique filename based on the given filename.
     * The other server must have had a <code> remoteRetrieve </code> issued
     * to it by another FTPClient.
     * <p>
     * @param filename  The name on which to base the filename of the file
     *                  that is to be stored.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteStoreUnique(String filename) throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(stou(filename));
        return false;
    }


    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to store a file on
     * the other server using a unique filename.
     * The other server must have had a <code> remoteRetrieve </code> issued
     * to it by another FTPClient.  Many FTP servers require that a base
     * filename be given from which the unique filename can be derived.  For
     * those servers use the other version of <code> remoteStoreUnique</code>
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteStoreUnique() throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(stou());
        return false;
    }

    // For server to server transfers
    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to append to a given file on
     * the other server.  The other server must have had a
     * <code> remoteRetrieve </code> issued to it by another FTPClient.
     * <p>
     * @param filename  The name of the file to be appended to, or if the
     *        file does not exist, the name to call the file being stored.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteAppend(String filename) throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(appe(filename));
        return false;
    }

    /***
     * There are a few FTPClient methods that do not complete the
     * entire sequence of FTP commands to complete a transaction.  These
     * commands require some action by the programmer after the reception
     * of a positive intermediate command.  After the programmer's code
     * completes its actions, it must call this method to receive
     * the completion reply from the server and verify the success of the
     * entire transaction.
     * <p>
     * For example,
     * <pre>
     * InputStream input;
     * OutputStream output;
     * input  = new FileInputStream("foobaz.txt");
     * output = ftp.storeFileStream("foobar.txt")
     * if(!FTPReply.isPositiveIntermediate(ftp.getReplyCode())) {
     *     input.close();
     *     output.close();
     *     ftp.logout();
     *     ftp.disconnect();
     *     System.err.println("File transfer failed.");
     *     System.exit(1);
     * }
     * Util.copyStream(input, output);
     * input.close();
     * output.close();
     * // Must call completePendingCommand() to finish command.
     * if(!ftp.completePendingCommand()) {
     *     ftp.logout();
     *     ftp.disconnect();
     *     System.err.println("File transfer failed.");
     *     System.exit(1);
     * }
     * </pre>
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean completePendingCommand() throws IOException
    {
        return FTPReply.isPositiveCompletion(getReply());
    }


    /***
     * Retrieves a named file from the server and writes it to the given
     * OutputStream.  This method does NOT close the given OutputStream.
     * If the current file type is ASCII, line separators in the file are
     * converted to the local representation.
     * <p>
     * Note: if you have used {@link #setRestartOffset(long)},
     * the file data will start from the selected offset.
     * @param remote  The name of the remote file.
     * @param local   The local OutputStream to which to write the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean retrieveFile(String remote, OutputStream local)
    throws IOException
    {
        InputStream input;
        Socket socket;

        if ((socket = _openDataConnection_(FTPCommand.RETR, remote)) == null)
            return false;

        input = new BufferedInputStream(socket.getInputStream(),
                getBufferSize());
        if (__fileType == ASCII_FILE_TYPE)
            input = new FromNetASCIIInputStream(input);

        CSL csl = null;
        if (__controlKeepAliveTimeout > 0) {
            csl = new CSL(this, __controlKeepAliveTimeout, __controlKeepAliveReplyTimeout);
        }

        // Treat everything else as binary for now
        try
        {
            Util.copyStream(input, local, getBufferSize(),
                    CopyStreamEvent.UNKNOWN_STREAM_SIZE, __mergeListeners(csl),
                    false);
        } finally {
            Util.closeQuietly(socket);
        }

        // Get the transfer response
        boolean ok = completePendingCommand();
        if (csl != null) {
            csl.cleanUp(); // fetch any outstanding keepalive replies
        }
        return ok;
    }

    /***
     * Returns an InputStream from which a named file from the server
     * can be read.  If the current file type is ASCII, the returned
     * InputStream will convert line separators in the file to
     * the local representation.  You must close the InputStream when you
     * finish reading from it.  The InputStream itself will take care of
     * closing the parent data connection socket upon being closed.  To
     * finalize the file transfer you must call
     * {@link #completePendingCommand  completePendingCommand } and
     * check its return value to verify success.
     * <p>
     * Note: if you have used {@link #setRestartOffset(long)},
     * the file data will start from the selected offset.
     *
     * @param remote  The name of the remote file.
     * @return An InputStream from which the remote file can be read.  If
     *      the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public InputStream retrieveFileStream(String remote) throws IOException
    {
        InputStream input;
        Socket socket;

        if ((socket = _openDataConnection_(FTPCommand.RETR, remote)) == null)
            return null;

        input = socket.getInputStream();
        if (__fileType == ASCII_FILE_TYPE) {
            // We buffer ascii transfers because the buffering has to
            // be interposed between FromNetASCIIOutputSream and the underlying
            // socket input stream.  We don't buffer binary transfers
            // because we don't want to impose a buffering policy on the
            // programmer if possible.  Programmers can decide on their
            // own if they want to wrap the SocketInputStream we return
            // for file types other than ASCII.
            input = new BufferedInputStream(input,
                    getBufferSize());
            input = new FromNetASCIIInputStream(input);
        }
        return new org.apache.commons.net.io.SocketInputStream(socket, input);
    }


    /***
     * Stores a file on the server using the given name and taking input
     * from the given InputStream.  This method does NOT close the given
     * InputStream.  If the current file type is ASCII, line separators in
     * the file are transparently converted to the NETASCII format (i.e.,
     * you should not attempt to create a special InputStream to do this).
     * <p>
     * @param remote  The name to give the remote file.
     * @param local   The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean storeFile(String remote, InputStream local)
    throws IOException
    {
        return __storeFile(FTPCommand.STOR, remote, local);
    }


    /***
     * Returns an OutputStream through which data can be written to store
     * a file on the server using the given name.  If the current file type
     * is ASCII, the returned OutputStream will convert line separators in
     * the file to the NETASCII format  (i.e., you should not attempt to
     * create a special OutputStream to do this).  You must close the
     * OutputStream when you finish writing to it.  The OutputStream itself
     * will take care of closing the parent data connection socket upon being
     * closed.  To finalize the file transfer you must call
     * {@link #completePendingCommand  completePendingCommand } and
     * check its return value to verify success.
     * <p>
     * @param remote  The name to give the remote file.
     * @return An OutputStream through which the remote file can be written.  If
     *      the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public OutputStream storeFileStream(String remote) throws IOException
    {
        return __storeFileStream(FTPCommand.STOR, remote);
    }

    /***
     * Appends to a file on the server with the given name, taking input
     * from the given InputStream.  This method does NOT close the given
     * InputStream.  If the current file type is ASCII, line separators in
     * the file are transparently converted to the NETASCII format (i.e.,
     * you should not attempt to create a special InputStream to do this).
     * <p>
     * @param remote  The name of the remote file.
     * @param local   The local InputStream from which to read the data to
     *                be appended to the remote file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean appendFile(String remote, InputStream local)
    throws IOException
    {
        return __storeFile(FTPCommand.APPE, remote, local);
    }

    /***
     * Returns an OutputStream through which data can be written to append
     * to a file on the server with the given name.  If the current file type
     * is ASCII, the returned OutputStream will convert line separators in
     * the file to the NETASCII format  (i.e., you should not attempt to
     * create a special OutputStream to do this).  You must close the
     * OutputStream when you finish writing to it.  The OutputStream itself
     * will take care of closing the parent data connection socket upon being
     * closed.  To finalize the file transfer you must call
     * {@link #completePendingCommand  completePendingCommand } and
     * check its return value to verify success.
     * <p>
     * @param remote  The name of the remote file.
     * @return An OutputStream through which the remote file can be appended.
     *      If the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public OutputStream appendFileStream(String remote) throws IOException
    {
        return __storeFileStream(FTPCommand.APPE, remote);
    }

    /***
     * Stores a file on the server using a unique name derived from the
     * given name and taking input
     * from the given InputStream.  This method does NOT close the given
     * InputStream.  If the current file type is ASCII, line separators in
     * the file are transparently converted to the NETASCII format (i.e.,
     * you should not attempt to create a special InputStream to do this).
     * <p>
     * @param remote  The name on which to base the unique name given to
     *                the remote file.
     * @param local   The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean storeUniqueFile(String remote, InputStream local)
    throws IOException
    {
        return __storeFile(FTPCommand.STOU, remote, local);
    }


    /***
     * Returns an OutputStream through which data can be written to store
     * a file on the server using a unique name derived from the given name.
     * If the current file type
     * is ASCII, the returned OutputStream will convert line separators in
     * the file to the NETASCII format  (i.e., you should not attempt to
     * create a special OutputStream to do this).  You must close the
     * OutputStream when you finish writing to it.  The OutputStream itself
     * will take care of closing the parent data connection socket upon being
     * closed.  To finalize the file transfer you must call
     * {@link #completePendingCommand  completePendingCommand } and
     * check its return value to verify success.
     * <p>
     * @param remote  The name on which to base the unique name given to
     *                the remote file.
     * @return An OutputStream through which the remote file can be written.  If
     *      the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public OutputStream storeUniqueFileStream(String remote) throws IOException
    {
        return __storeFileStream(FTPCommand.STOU, remote);
    }

    /**
     * Stores a file on the server using a unique name assigned by the
     * server and taking input from the given InputStream.  This method does
     * NOT close the given
     * InputStream.  If the current file type is ASCII, line separators in
     * the file are transparently converted to the NETASCII format (i.e.,
     * you should not attempt to create a special InputStream to do this).
     * <p>
     * @param local   The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     */
    public boolean storeUniqueFile(InputStream local) throws IOException
    {
        return __storeFile(FTPCommand.STOU, null, local);
    }

    /**
     * Returns an OutputStream through which data can be written to store
     * a file on the server using a unique name assigned by the server.
     * If the current file type
     * is ASCII, the returned OutputStream will convert line separators in
     * the file to the NETASCII format  (i.e., you should not attempt to
     * create a special OutputStream to do this).  You must close the
     * OutputStream when you finish writing to it.  The OutputStream itself
     * will take care of closing the parent data connection socket upon being
     * closed.  To finalize the file transfer you must call
     * {@link #completePendingCommand  completePendingCommand } and
     * check its return value to verify success.
     * <p>
     * @return An OutputStream through which the remote file can be written.  If
     *      the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     */
    public OutputStream storeUniqueFileStream() throws IOException
    {
        return __storeFileStream(FTPCommand.STOU, null);
    }

    /***
     * Reserve a number of bytes on the server for the next file transfer.
     * <p>
     * @param bytes  The number of bytes which the server should allocate.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean allocate(int bytes) throws IOException
    {
        return FTPReply.isPositiveCompletion(allo(bytes));
    }

    /**
     * Query the server for supported features. The server may reply with a list of server-supported exensions.
     * For example, a typical client-server interaction might be (from RFC 2389):
     * <pre>
        C> feat
        S> 211-Extensions supported:
        S>  MLST size*;create;modify*;perm;media-type
        S>  SIZE
        S>  COMPRESSION
        S>  MDTM
        S> 211 END
     * </pre>
     * @see <a href="http://www.faqs.org/rfcs/rfc2389.html">http://www.faqs.org/rfcs/rfc2389.html</a>
     * @return True if successfully completed, false if not.
     * @throws IOException
     * @since 2.2
     */
    public boolean features() throws IOException {
        return FTPReply.isPositiveCompletion(feat());
    }

    /**
     * Query the server for a supported feature, and returns its values (if any).
     * Caches the parsed response to avoid resending the command repeatedly.
     *
     * @return if the feature is present, returns the feature values (empty array if none)
     * Returns {@code null} if the feature is not found or the command failed.
     * Check {@link #getReplyCode()} or {@link #getReplyString()} if so.
     * @throws IOException
     * @since 3.0
     */
    public String[] featureValues(String feature) throws IOException {
        if (!initFeatureMap()) {
            return null;
        }
        Set<String> entries = __featuresMap.get(feature.toUpperCase(Locale.ENGLISH));
        if (entries != null) {
            return entries.toArray(new String[entries.size()]);
        }
        return null;
    }

    /**
     * Query the server for a supported feature, and returns the its value (if any).
     * Caches the parsed response to avoid resending the command repeatedly.
     *
     * @return if the feature is present, returns the feature value or the empty string
     * if the feature exists but has no value.
     * Returns {@code null} if the feature is not found or the command failed.
     * Check {@link #getReplyCode()} or {@link #getReplyString()} if so.
     * @throws IOException
     * @since 3.0
     */
    public String featureValue(String feature) throws IOException {
        String [] values = featureValues(feature);
        if (values != null) {
            return values[0];
        }
        return null;
    }

    /**
     * Query the server for a supported feature.
     * Caches the parsed response to avoid resending the command repeatedly.
     *
     * @param feature the name of the feature; it is converted to upper case.
     * @return {@code true} if the feature is present, {@code false} if the feature is not present
     * or the {@link #feat()} command failed. Check {@link #getReplyCode()} or {@link #getReplyString()}
     * if it is necessary to distinguish these cases.
     *
     * @throws IOException
     * @since 3.0
     */
    public boolean hasFeature(String feature) throws IOException {
        if (!initFeatureMap()) {
            return false;
        }
        return __featuresMap.containsKey(feature.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Query the server for a supported feature with particular value,
     * for example "AUTH SSL" or "AUTH TLS".
     * Caches the parsed response to avoid resending the command repeatedly.
     *
     * @param feature the name of the feature; it is converted to upper case.
     * @param value the value to find.
     *
     * @return {@code true} if the feature is present, {@code false} if the feature is not present
     * or the {@link #feat()} command failed. Check {@link #getReplyCode()} or {@link #getReplyString()}
     * if it is necessary to distinguish these cases.
     *
     * @throws IOException
     * @since 3.0
     */
    public boolean hasFeature(String feature, String value) throws IOException {
        if (!initFeatureMap()) {
            return false;
        }
        Set<String> entries = __featuresMap.get(feature.toUpperCase(Locale.ENGLISH));
        if (entries != null) {
            return entries.contains(value);
        }
        return false;
    }

    /*
     * Create the feature map if not already created.
     */
    private boolean initFeatureMap() throws IOException {
        if (__featuresMap == null) {
            // Don't create map here, because next line may throw exception
            boolean success = FTPReply.isPositiveCompletion(feat());
            // we init the map here, so we don't keep trying if we know the command will fail
            __featuresMap = new HashMap<String, Set<String>>();
            if (!success) {
                return false;
            }
            for (String l : getReplyStrings()) {
                if (l.startsWith(" ")) { // it's a FEAT entry
                    String key;
                    String value="";
                    int varsep = l.indexOf(' ', 1);
                    if (varsep > 0) {
                        key = l.substring(1, varsep);
                        value = l.substring(varsep+1);
                    } else {
                        key = l.substring(1);
                    }
                    key = key.toUpperCase(Locale.ENGLISH);
                    Set<String> entries = __featuresMap.get(key);
                    if (entries == null) {
                        entries = new HashSet<String>();
                        __featuresMap.put(key, entries);
                    }
                    entries.add(value);
                }
            }
        }
        return true;
    }

    /**
     * Reserve space on the server for the next file transfer.
     * <p>
     * @param bytes  The number of bytes which the server should allocate.
     * @param recordSize  The size of a file record.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     */
    public boolean allocate(int bytes, int recordSize) throws IOException
    {
        return FTPReply.isPositiveCompletion(allo(bytes, recordSize));
    }


    /**
     * Issue a command and wait for the reply.
     * <p>
     * Should only be used with commands that return replies on the
     * command channel - do not use for LIST, NLST, MLSD etc.
     * <p>
     * @param command  The command to invoke
     * @param params  The parameters string, may be {@code null}
     * @return True if successfully completed, false if not, in which case
     * call {@link #getReplyCode()} or {@link #getReplyString()}
     * to get the reason.
     *
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @since 3.0
     */
    public boolean doCommand(String command, String params) throws IOException
    {
        return FTPReply.isPositiveCompletion(sendCommand(command, params));
    }

    /**
     * Issue a command and wait for the reply, returning it as an array of strings.
     * <p>
     * Should only be used with commands that return replies on the
     * command channel - do not use for LIST, NLST, MLSD etc.
     * <p>
     * @param command  The command to invoke
     * @param params  The parameters string, may be {@code null}
     * @return The array of replies, or {@code null} if the command failed, in which case
     * call {@link #getReplyCode()} or {@link #getReplyString()}
     * to get the reason.
     *
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @since 3.0
     */
    public String[] doCommandAsStrings(String command, String params) throws IOException
    {
        boolean success = FTPReply.isPositiveCompletion(sendCommand(command, params));
        if (success){
            return getReplyStrings();
        } else {
            return null;
        }
    }

    /**
     * Get file details using the MLST command
     *
     * @param pathname the file or directory to list, may be {@code} null
     * @return the file details, may be {@code null}
     * @throws IOException
     * @since 3.0
     */
    public FTPFile mlistFile(String pathname) throws IOException
    {
        boolean success = FTPReply.isPositiveCompletion(sendCommand(FTPCommand.MLST, pathname));
        if (success){
            String entry = getReplyStrings()[1].substring(1); // skip leading space for parser
            return MLSxEntryParser.parseEntry(entry);
        } else {
            return null;
        }
    }

    /**
     * Generate a directory listing for the current directory using the MSLD command.
     *
     * @return the array of file entries
     * @throws IOException
     * @since 3.0
     */
    public FTPFile[] mlistDir() throws IOException
    {
        return mlistDir(null);
    }

    /**
     * Generate a directory listing using the MSLD command.
     *
     * @param pathname the directory name, may be {@code null}
     * @return the array of file entries
     * @throws IOException
     * @since 3.0
     */
    public FTPFile[] mlistDir(String pathname) throws IOException
    {
        FTPListParseEngine engine = initiateMListParsing( pathname);
        return engine.getFiles();
    }

    /**
     * Generate a directory listing using the MSLD command.
     *
     * @param pathname the directory name, may be {@code null}
     * @param filter the filter to apply to the responses
     * @return the array of file entries
     * @throws IOException
     * @since 3.0
     */
    public FTPFile[] mlistDir(String pathname, FTPFileFilter filter) throws IOException
    {
        FTPListParseEngine engine = initiateMListParsing( pathname);
        return engine.getFiles(filter);
    }

    /***
     * Restart a <code>STREAM_TRANSFER_MODE</code> file transfer starting
     * from the given offset.  This will only work on FTP servers supporting
     * the REST comand for the stream transfer mode.  However, most FTP
     * servers support this.  Any subsequent file transfer will start
     * reading or writing the remote file from the indicated offset.
     * <p>
     * @param offset  The offset into the remote file at which to start the
     *           next file transfer.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    private boolean restart(long offset) throws IOException
    {
        __restartOffset = 0;
        return FTPReply.isPositiveIntermediate(rest(Long.toString(offset)));
    }

    /***
     * Sets the restart offset.  The restart command is sent to the server
     * only before sending the file transfer command.  When this is done,
     * the restart marker is reset to zero.
     * <p>
     * @param offset  The offset into the remote file at which to start the
     *           next file transfer.  This must be a value greater than or
     *           equal to zero.
     ***/
    public void setRestartOffset(long offset)
    {
        if (offset >= 0)
            __restartOffset = offset;
    }

    /***
     * Fetches the restart offset.
     * <p>
     * @return offset  The offset into the remote file at which to start the
     *           next file transfer.
     ***/
    public long getRestartOffset()
    {
        return __restartOffset;
    }



    /***
     * Renames a remote file.
     * <p>
     * @param from  The name of the remote file to rename.
     * @param to    The new name of the remote file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean rename(String from, String to) throws IOException
    {
        if (!FTPReply.isPositiveIntermediate(rnfr(from)))
            return false;

        return FTPReply.isPositiveCompletion(rnto(to));
    }


    /***
     * Abort a transfer in progress.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean abort() throws IOException
    {
        return FTPReply.isPositiveCompletion(abor());
    }

    /***
     * Deletes a file on the FTP server.
     * <p>
     * @param pathname   The pathname of the file to be deleted.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean deleteFile(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(dele(pathname));
    }


    /***
     * Removes a directory on the FTP server (if empty).
     * <p>
     * @param pathname  The pathname of the directory to remove.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean removeDirectory(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(rmd(pathname));
    }


    /***
     * Creates a new subdirectory on the FTP server in the current directory
     * (if a relative pathname is given) or where specified (if an absolute
     * pathname is given).
     * <p>
     * @param pathname The pathname of the directory to create.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean makeDirectory(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(mkd(pathname));
    }


    /***
     * Returns the pathname of the current working directory.
     * <p>
     * @return The pathname of the current working directory.  If it cannot
     *         be obtained, returns null.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String printWorkingDirectory() throws IOException
    {
        if (pwd() != FTPReply.PATHNAME_CREATED)
            return null;

        return __parsePathname(_replyLines.get( _replyLines.size() - 1));
    }


    /**
     * Send a site specific command.
     * @param arguments The site specific command and arguments.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     */
    public boolean sendSiteCommand(String arguments) throws IOException
    {
        return FTPReply.isPositiveCompletion(site(arguments));
    }


    /***
     * Fetches the system type from the server and returns the string.
     * This value is cached for the duration of the connection after the
     * first call to this method.  In other words, only the first time
     * that you invoke this method will it issue a SYST command to the
     * FTP server.  FTPClient will remember the value and return the
     * cached value until a call to disconnect.
     * <p>
     * @return The system type obtained from the server. Never null.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *  command to the server or receiving a reply from the server.
     *  @since 2.2
     ***/
    public String getSystemType() throws IOException
    {
        //if (syst() == FTPReply.NAME_SYSTEM_TYPE)
        // Technically, we should expect a NAME_SYSTEM_TYPE response, but
        // in practice FTP servers deviate, so we soften the condition to
        // a positive completion.
        if (__systemName == null){
            if (FTPReply.isPositiveCompletion(syst())) {
                // Assume that response is not empty here (cannot be null)
                __systemName = _replyLines.get(_replyLines.size() - 1).substring(4);
            } else {
                throw new IOException("Unable to determine system type - response: " + getReplyString());
            }
        }
        return __systemName;
    }


    /***
     * Fetches the system help information from the server and returns the
     * full string.
     * <p>
     * @return The system help string obtained from the server.  null if the
     *       information could not be obtained.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *  command to the server or receiving a reply from the server.
     ***/
    public String listHelp() throws IOException
    {
        if (FTPReply.isPositiveCompletion(help()))
            return getReplyString();
        return null;
    }


    /**
     * Fetches the help information for a given command from the server and
     * returns the full string.
     * @param command The command on which to ask for help.
     * @return The command help string obtained from the server.  null if the
     *       information could not be obtained.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *  command to the server or receiving a reply from the server.
     */
    public String listHelp(String command) throws IOException
    {
        if (FTPReply.isPositiveCompletion(help(command)))
            return getReplyString();
        return null;
    }


    /***
     * Sends a NOOP command to the FTP server.  This is useful for preventing
     * server timeouts.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean sendNoOp() throws IOException
    {
        return FTPReply.isPositiveCompletion(noop());
    }


    /***
     * Obtain a list of filenames in a directory (or just the name of a given
     * file, which is not particularly useful).  This information is obtained
     * through the NLST command.  If the given pathname is a directory and
     * contains no files,  a zero length array is returned only
     * if the FTP server returned a positive completion code, otherwise
     * null is returned (the FTP server returned a 550 error No files found.).
     * If the directory is not empty, an array of filenames in the directory is
     * returned. If the pathname corresponds
     * to a file, only that file will be listed.  The server may or may not
     * expand glob expressions.
     * <p>
     * @param pathname  The file or directory to list.
     * @return The list of filenames contained in the given path.  null if
     *     the list could not be obtained.  If there are no filenames in
     *     the directory, a zero-length array is returned.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String[] listNames(String pathname) throws IOException
    {
        String line;
        Socket socket;
        BufferedReader reader;
        ArrayList<String> results;

        if ((socket = _openDataConnection_(FTPCommand.NLST, getListArguments(pathname))) == null)
            return null;

        reader =
            new BufferedReader(new InputStreamReader(socket.getInputStream(), getControlEncoding()));

        results = new ArrayList<String>();
        while ((line = reader.readLine()) != null)
            results.add(line);

        reader.close();
        socket.close();

        if (completePendingCommand())
        {
            String[] names = new String[ results.size() ];
            return results.toArray(names);
        }

        return null;
    }


    /***
     * Obtain a list of filenames in the current working directory
     * This information is obtained through the NLST command.  If the current
     * directory contains no files, a zero length array is returned only
     * if the FTP server returned a positive completion code, otherwise,
     * null is returned (the FTP server returned a 550 error No files found.).
     * If the directory is not empty, an array of filenames in the directory is
     * returned.
     * <p>
     * @return The list of filenames contained in the current working
     *     directory.  null if the list could not be obtained.
     *     If there are no filenames in the directory, a zero-length array
     *     is returned.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String[] listNames() throws IOException
    {
        return listNames(null);
    }



    /**
     * Using the default system autodetect mechanism, obtain a
     * list of file information for the current working directory
     * or for just a single file.
     * <p>
     * This information is obtained through the LIST command.  The contents of
     * the returned array is determined by the<code> FTPFileEntryParser </code>
     * used.
     * <p>
     * @param pathname  The file or directory to list.  Since the server may
     *                  or may not expand glob expressions, using them here
     *                  is not recommended and may well cause this method to
     *                  fail.
     *
     * @return The list of file information contained in the given path in
     *         the format determined by the autodetection mechanism
     * @exception FTPConnectionClosedException
     *                   If the FTP server prematurely closes the connection
     *                   as a result of the client being idle or some other
     *                   reason causing the server to send FTP reply code 421.
     *                   This exception may be caught either as an IOException
     *                   or independently as itself.
     * @exception IOException
     *                   If an I/O error occurs while either sending a
     *                   command to the server or receiving a reply
     *                   from the server.
     * @exception ParserInitializationException
     *                   Thrown if the parserKey parameter cannot be
     *                   resolved by the selected parser factory.
     *                   In the DefaultFTPEntryParserFactory, this will
     *                   happen when parserKey is neither
     *                   the fully qualified class name of a class
     *                   implementing the interface
     *                   org.apache.commons.net.ftp.FTPFileEntryParser
     *                   nor a string containing one of the recognized keys
     *                   mapping to such a parser or if class loader
     *                   security issues prevent its being loaded.
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.FTPFileEntryParser
     */
    public FTPFile[] listFiles(String pathname)
    throws IOException
    {
        FTPListParseEngine engine = initiateListParsing((String) null, pathname);
        return engine.getFiles();

    }

    /**
     * Using the default system autodetect mechanism, obtain a
     * list of file information for the current working directory.
     * <p>
     * This information is obtained through the LIST command.  The contents of
     * the returned array is determined by the<code> FTPFileEntryParser </code>
     * used.
     * <p>
     * @return The list of file information contained in the current directory
     *         in the format determined by the autodetection mechanism.
     *         <p><b>
     *         NOTE:</b> This array may contain null members if any of the
     *         individual file listings failed to parse.  The caller should
     *         check each entry for null before referencing it.
     * @exception FTPConnectionClosedException
     *                   If the FTP server prematurely closes the connection
     *                   as a result of the client being idle or some other
     *                   reason causing the server to send FTP reply code 421.
     *                   This exception may be caught either as an IOException
     *                   or independently as itself.
     * @exception IOException
     *                   If an I/O error occurs while either sending a
     *                   command to the server or receiving a reply
     *                   from the server.
     * @exception ParserInitializationException
     *                   Thrown if the parserKey parameter cannot be
     *                   resolved by the selected parser factory.
     *                   In the DefaultFTPEntryParserFactory, this will
     *                   happen when parserKey is neither
     *                   the fully qualified class name of a class
     *                   implementing the interface
     *                   org.apache.commons.net.ftp.FTPFileEntryParser
     *                   nor a string containing one of the recognized keys
     *                   mapping to such a parser or if class loader
     *                   security issues prevent its being loaded.
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.FTPFileEntryParser
     */
    public FTPFile[] listFiles()
    throws IOException
    {
        return listFiles((String) null);
    }

    /**
     * Version of {@link #listFiles(String)} which allows a filter to be provided.
     * For example: <code>listFiles("site", FTPFileFilters.DIRECTORY);</code>
     * @param pathname the initial path, may be null
     * @param filter the filter, non-null
     * @return the list of FTPFile entries.
     * @throws IOException
     * @since 2.2
     */
    public FTPFile[] listFiles(String pathname, FTPFileFilter filter)
    throws IOException
    {
        FTPListParseEngine engine = initiateListParsing((String) null, pathname);
        return engine.getFiles(filter);

    }

    /**
     * Using the default system autodetect mechanism, obtain a
     * list of directories contained in the current working directory.
     * <p>
     * This information is obtained through the LIST command.  The contents of
     * the returned array is determined by the<code> FTPFileEntryParser </code>
     * used.
     * <p>
     * @return The list of directories contained in the current directory
     *         in the format determined by the autodetection mechanism.
     *
     * @exception FTPConnectionClosedException
     *                   If the FTP server prematurely closes the connection
     *                   as a result of the client being idle or some other
     *                   reason causing the server to send FTP reply code 421.
     *                   This exception may be caught either as an IOException
     *                   or independently as itself.
     * @exception IOException
     *                   If an I/O error occurs while either sending a
     *                   command to the server or receiving a reply
     *                   from the server.
     * @exception ParserInitializationException
     *                   Thrown if the parserKey parameter cannot be
     *                   resolved by the selected parser factory.
     *                   In the DefaultFTPEntryParserFactory, this will
     *                   happen when parserKey is neither
     *                   the fully qualified class name of a class
     *                   implementing the interface
     *                   org.apache.commons.net.ftp.FTPFileEntryParser
     *                   nor a string containing one of the recognized keys
     *                   mapping to such a parser or if class loader
     *                   security issues prevent its being loaded.
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.FTPFileEntryParser
     * @since 3.0
     */
    public FTPFile[] listDirectories() throws IOException {
        return listDirectories((String) null);
    }

    /**
     * Using the default system autodetect mechanism, obtain a
     * list of directories contained in the specified directory.
     * <p>
     * This information is obtained through the LIST command.  The contents of
     * the returned array is determined by the<code> FTPFileEntryParser </code>
     * used.
     * <p>
     * @return The list of directories contained in the specified directory
     *         in the format determined by the autodetection mechanism.
     *
     * @exception FTPConnectionClosedException
     *                   If the FTP server prematurely closes the connection
     *                   as a result of the client being idle or some other
     *                   reason causing the server to send FTP reply code 421.
     *                   This exception may be caught either as an IOException
     *                   or independently as itself.
     * @exception IOException
     *                   If an I/O error occurs while either sending a
     *                   command to the server or receiving a reply
     *                   from the server.
     * @exception ParserInitializationException
     *                   Thrown if the parserKey parameter cannot be
     *                   resolved by the selected parser factory.
     *                   In the DefaultFTPEntryParserFactory, this will
     *                   happen when parserKey is neither
     *                   the fully qualified class name of a class
     *                   implementing the interface
     *                   org.apache.commons.net.ftp.FTPFileEntryParser
     *                   nor a string containing one of the recognized keys
     *                   mapping to such a parser or if class loader
     *                   security issues prevent its being loaded.
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.FTPFileEntryParser
     * @since 3.0
     */
    public FTPFile[] listDirectories(String parent) throws IOException {
        return listFiles(parent, FTPFileFilters.DIRECTORIES);
    }

    /**
     * Using the default autodetect mechanism, initialize an FTPListParseEngine
     * object containing a raw file information for the current working
     * directory on the server
     * This information is obtained through the LIST command.  This object
     * is then capable of being iterated to return a sequence of FTPFile
     * objects with information filled in by the
     * <code> FTPFileEntryParser </code> used.
     * <p>
     * This method differs from using the listFiles() methods in that
     * expensive FTPFile objects are not created until needed which may be
     * an advantage on large lists.
     *
     * @return A FTPListParseEngine object that holds the raw information and
     * is capable of providing parsed FTPFile objects, one for each file
     * containing information contained in the given path in the format
     * determined by the <code> parser </code> parameter.   Null will be
     * returned if a data connection cannot be opened.  If the current working
     * directory contains no files, an empty array will be the return.
     *
     * @exception FTPConnectionClosedException
     *                   If the FTP server prematurely closes the connection as a result
     *                   of the client being idle or some other reason causing the server
     *                   to send FTP reply code 421.  This exception may be caught either
     *                   as an IOException or independently as itself.
     * @exception IOException
     *                   If an I/O error occurs while either sending a
     *                   command to the server or receiving a reply from the server.
     * @exception ParserInitializationException
     *                   Thrown if the autodetect mechanism cannot
     *                   resolve the type of system we are connected with.
     * @see FTPListParseEngine
     */
    public FTPListParseEngine initiateListParsing()
    throws IOException
    {
        return initiateListParsing((String) null);
    }

    /**
     * Using the default autodetect mechanism, initialize an FTPListParseEngine
     * object containing a raw file information for the supplied directory.
     * This information is obtained through the LIST command.  This object
     * is then capable of being iterated to return a sequence of FTPFile
     * objects with information filled in by the
     * <code> FTPFileEntryParser </code> used.
     * <p>
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * This method differs from using the listFiles() methods in that
     * expensive FTPFile objects are not created until needed which may be
     * an advantage on large lists.
     * <p>
     * <pre>
     *    FTPClient f=FTPClient();
     *    f.connect(server);
     *    f.login(username, password);
     *    FTPListParseEngine engine = f.initiateListParsing(directory);
     *
     *    while (engine.hasNext()) {
     *       FTPFile[] files = engine.getNext(25);  // "page size" you want
     *       //do whatever you want with these files, display them, etc.
     *       //expensive FTPFile objects not created until needed.
     *    }
     * </pre>
     *
     * @return A FTPListParseEngine object that holds the raw information and
     * is capable of providing parsed FTPFile objects, one for each file
     * containing information contained in the given path in the format
     * determined by the <code> parser </code> parameter.   Null will be
     * returned if a data connection cannot be opened.  If the current working
     * directory contains no files, an empty array will be the return.
     *
     * @exception FTPConnectionClosedException
     *                   If the FTP server prematurely closes the connection as a result
     *                   of the client being idle or some other reason causing the server
     *                   to send FTP reply code 421.  This exception may be caught either
     *                   as an IOException or independently as itself.
     * @exception IOException
     *                   If an I/O error occurs while either sending a
     *                   command to the server or receiving a reply from the server.
     * @exception ParserInitializationException
     *                   Thrown if the autodetect mechanism cannot
     *                   resolve the type of system we are connected with.
     * @see FTPListParseEngine
     */
    public FTPListParseEngine initiateListParsing(
            String pathname)
    throws IOException
    {
        String key = null;
        return initiateListParsing(key, pathname);
    }

    /**
     * Using the supplied parser key, initialize an FTPListParseEngine
     * object containing a raw file information for the supplied directory.
     * This information is obtained through the LIST command.  This object
     * is then capable of being iterated to return a sequence of FTPFile
     * objects with information filled in by the
     * <code> FTPFileEntryParser </code> used.
     * <p>
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * This method differs from using the listFiles() methods in that
     * expensive FTPFile objects are not created until needed which may be
     * an advantage on large lists.
     *
     * @param parserKey A string representing a designated code or fully-qualified
     * class name of an  <code> FTPFileEntryParser </code> that should be
     *               used to parse each server file listing.
     *               May be {@code null}, in which case the code checks first
     *               the system property {@link #FTP_SYSTEM_TYPE}, and if that is
     *               not defined the SYST command is used to provide the value.
     *               To allow for arbitrary system types, the return from the
     *               SYST command is used to look up an alias for the type in the
     *               {@link #SYSTEM_TYPE_PROPERTIES} properties file if it is available.
     *
     * @return A FTPListParseEngine object that holds the raw information and
     * is capable of providing parsed FTPFile objects, one for each file
     * containing information contained in the given path in the format
     * determined by the <code> parser </code> parameter.   Null will be
     * returned if a data connection cannot be opened.  If the current working
     * directory contains no files, an empty array will be the return.
     *
     * @exception FTPConnectionClosedException
     *                   If the FTP server prematurely closes the connection as a result
     *                   of the client being idle or some other reason causing the server
     *                   to send FTP reply code 421.  This exception may be caught either
     *                   as an IOException or independently as itself.
     * @exception IOException
     *                   If an I/O error occurs while either sending a
     *                   command to the server or receiving a reply from the server.
     * @exception ParserInitializationException
     *                   Thrown if the parserKey parameter cannot be
     *                   resolved by the selected parser factory.
     *                   In the DefaultFTPEntryParserFactory, this will
     *                   happen when parserKey is neither
     *                   the fully qualified class name of a class
     *                   implementing the interface
     *                   org.apache.commons.net.ftp.FTPFileEntryParser
     *                   nor a string containing one of the recognized keys
     *                   mapping to such a parser or if class loader
     *                   security issues prevent its being loaded.
     * @see FTPListParseEngine
     */
    public FTPListParseEngine initiateListParsing(
            String parserKey, String pathname)
    throws IOException
    {
        // We cache the value to avoid creation of a new object every
        // time a file listing is generated.
        if(__entryParser == null ||  ! __entryParserKey.equals(parserKey)) {
            if (null != parserKey) {
                // if a parser key was supplied in the parameters,
                // use that to create the parser
                __entryParser =
                    __parserFactory.createFileEntryParser(parserKey);
                __entryParserKey = parserKey;

            } else {
                // if no parserKey was supplied, check for a configuration
                // in the params, and if non-null, use that.
                if (null != __configuration) {
                    __entryParser =
                        __parserFactory.createFileEntryParser(__configuration);
                    __entryParserKey = __configuration.getServerSystemKey();
                } else {
                    // if a parserKey hasn't been supplied, and a configuration
                    // hasn't been supplied, and the override property is not set
                    // then autodetect by calling
                    // the SYST command and use that to choose the parser.
                    String systemType = System.getProperty(FTP_SYSTEM_TYPE);
                    if (systemType == null) {
                        systemType = getSystemType(); // cannot be null
                        Properties override = getOverrideProperties();
                        if (override != null) {
                            String newType = override.getProperty(systemType);
                            if (newType != null) {
                                systemType = newType;
                            }
                        }
                    }
                    __entryParser = __parserFactory.createFileEntryParser(systemType);
                    __entryParserKey = systemType;
                }
            }
        }

        return initiateListParsing(__entryParser, pathname);

    }

    /**
     * private method through which all listFiles() and
     * initiateListParsing methods pass once a parser is determined.
     *
     * @exception FTPConnectionClosedException
     *                   If the FTP server prematurely closes the connection as a result
     *                   of the client being idle or some other reason causing the server
     *                   to send FTP reply code 421.  This exception may be caught either
     *                   as an IOException or independently as itself.
     * @exception IOException
     *                   If an I/O error occurs while either sending a
     *                   command to the server or receiving a reply from the server.
     * @see FTPListParseEngine
     */
    private FTPListParseEngine initiateListParsing(
            FTPFileEntryParser parser, String pathname)
    throws IOException
    {
        Socket socket;
        FTPListParseEngine engine = new FTPListParseEngine(parser);
        if ((socket = _openDataConnection_(FTPCommand.LIST, getListArguments(pathname))) == null)
        {
            return engine;
        }
        try {
            engine.readServerList(socket.getInputStream(), getControlEncoding());
        }
        finally {
            Util.closeQuietly(socket);
        }
        completePendingCommand();
        return engine;
    }

    /**
     * Initiate list parsing for MLSD listings.
     *
     * @param pathname
     * @return the engine
     * @throws IOException
     */
    private FTPListParseEngine initiateMListParsing(String pathname) throws IOException
    {
        Socket socket;
        FTPListParseEngine engine = new FTPListParseEngine(MLSxEntryParser.getInstance());
        if ((socket = _openDataConnection_(FTPCommand.MLSD, pathname)) == null)
        {
            return engine;
        }

        try {
            engine.readServerList(socket.getInputStream(), getControlEncoding());
        }
        finally {
            Util.closeQuietly(socket);
            completePendingCommand();
        }
        return engine;
    }

    /**
     * @since 2.0
     */
    protected String getListArguments(String pathname) {
        if (getListHiddenFiles())
        {
            if (pathname != null)
            {
                StringBuilder sb = new StringBuilder(pathname.length() + 3);
                sb.append("-a ");
                sb.append(pathname);
                return sb.toString();
            }
            else
            {
                return "-a";
            }
        }

        return pathname;
    }


    /***
     * Issue the FTP STAT command to the server.
     * <p>
     * @return The status information returned by the server.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String getStatus() throws IOException
    {
        if (FTPReply.isPositiveCompletion(stat()))
            return getReplyString();
        return null;
    }


    /***
     * Issue the FTP STAT command to the server for a given pathname.  This
     * should produce a listing of the file or directory.
     * <p>
     * @return The status information returned by the server.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String getStatus(String pathname) throws IOException
    {
        if (FTPReply.isPositiveCompletion(stat(pathname)))
            return getReplyString();
        return null;
    }


    /**
     * Issue the FTP MDTM command (not supported by all servers to retrieve the last
     * modification time of a file. The modification string should be in the
     * ISO 3077 form "YYYYMMDDhhmmss(.xxx)?". The timestamp represented should also be in
     * GMT, but not all FTP servers honour this.
     *
     * @param pathname The file path to query.
     * @return A string representing the last file modification time in <code>YYYYMMDDhhmmss</code> format.
     * @throws IOException if an I/O error occurs.
     * @since 2.0
     */
    public String getModificationTime(String pathname) throws IOException {
        if (FTPReply.isPositiveCompletion(mdtm(pathname)))
            return getReplyString();
        return null;
    }


    /**
     * Issue the FTP MFMT command (not supported by all servers) which sets the last
     * modified time of a file.
     *
     * The timestamp should be in the form <code>YYYYMMDDhhmmss</code>. It should also
     * be in GMT, but not all servers honour this.
     *
     * An FTP server would indicate its support of this feature by including "MFMT"
     * in its response to the FEAT command, which may be retrieved by FTPClient.features()
     *
     * @param pathname The file path for which last modified time is to be changed.
     * @param timeval The timestamp to set to, in <code>YYYYMMDDhhmmss</code> format.
     * @return true if successfully set, false if not
     * @throws IOException if an I/O error occurs.
     * @since 2.2
     * @see <a href="http://tools.ietf.org/html/draft-somers-ftp-mfxx-04">http://tools.ietf.org/html/draft-somers-ftp-mfxx-04</a>
     */
    public boolean setModificationTime(String pathname, String timeval) throws IOException {
        return (FTPReply.isPositiveCompletion(mfmt(pathname, timeval)));
    }


    /**
     * Set the internal buffer size.
     *
     * @param bufSize The size of the buffer
     */
    public void setBufferSize(int bufSize) {
        __bufferSize = bufSize;
    }

    /**
     * Retrieve the current internal buffer size.
     * @return The current buffer size.
     */
    public int getBufferSize() {
        return __bufferSize;
    }


    /**
     * Implementation of the {@link Configurable Configurable} interface.
     * In the case of this class, configuring merely makes the config object available for the
     * factory methods that construct parsers.
     * @param config {@link FTPClientConfig FTPClientConfig} object used to
     * provide non-standard configurations to the parser.
     * @since 1.4
     */
    public void configure(FTPClientConfig config) {
        this.__configuration = config;
    }

    /**
     * You can set this to true if you would like to get hidden files when {@link #listFiles} too.
     * A <code>LIST -a</code> will be issued to the ftp server.
     * It depends on your ftp server if you need to call this method, also dont expect to get rid
     * of hidden files if you call this method with "false".
     *
     * @param listHiddenFiles true if hidden files should be listed
     * @since 2.0
     */
    public void setListHiddenFiles(boolean listHiddenFiles) {
        this.__listHiddenFiles = listHiddenFiles;
    }

    /**
     * @see #setListHiddenFiles(boolean)
     * @return the current state
     * @since 2.0
     */
    public boolean getListHiddenFiles() {
        return this.__listHiddenFiles;
    }

    /**
     * Whether should attempt to use EPSV with IPv4.
     * Default (if not set) is <code>false</code>
     * @return true if should attempt EPSV
     * @since 2.2
     */
    public boolean isUseEPSVwithIPv4() {
        return __useEPSVwithIPv4;
    }


    /**
     * Set whether to use EPSV with IPv4.
     * Might be worth enabling in some circumstances.
     *
     * For example, when using IPv4 with NAT it
     * may work with some rare configurations.
     * E.g. if FTP server has a static PASV address (external network)
     * and the client is coming from another internal network.
     * In that case the data connection after PASV command would fail,
     * while EPSV would make the client succeed by taking just the port.
     *
     * @param selected value to set.
     * @since 2.2
     */
    public void setUseEPSVwithIPv4(boolean selected) {
        this.__useEPSVwithIPv4 = selected;
    }

    /**
     * Set the listener to be used when performing store/retrieve operations.
     * The default value (if not set) is {@code null}.
     *
     * @param listener to be used, may be {@code null} to disable
     * @since 3.0
     */
    public void setCopyStreamListener(CopyStreamListener listener){
        __copyStreamListener = listener;
    }

    /**
     * Obtain the currently active listener.
     *
     * @return the listener, may be {@code null}
     * @since 3.0
     */
    public CopyStreamListener getCopyStreamListener(){
        return __copyStreamListener;
    }

    /**
     * Set the time to wait between sending control connection keepalive messages
     * when processing file upload or download.
     *
     * @param controlIdle the wait (in secs) between keepalive messages. Zero (or less) disables.
     * @since 3.0
     * @see #setControlKeepAliveReplyTimeout(int)
     */
    public void setControlKeepAliveTimeout(long controlIdle){
        __controlKeepAliveTimeout = controlIdle * 1000;
    }

    /**
     * Get the time to wait between sending control connection keepalive messages.
     * @return the number of seconds between keepalive messages.
     * @since 3.0
     */
    public long getControlKeepAliveTimeout() {
        return __controlKeepAliveTimeout / 1000;
    }

    /**
     * Set how long to wait for control keep-alive message replies.
     *
     * @param timeout number of milliseconds to wait (defaults to 1000)
     * @since 3.0
     * @see #setControlKeepAliveTimeout(long)
     */
    public void setControlKeepAliveReplyTimeout(int timeout) {
        __controlKeepAliveReplyTimeout = timeout;
    }

    /**
     * Get how long to wait for control keep-alive message replies.
     * @since 3.0
     */
    public int getControlKeepAliveReplyTimeout() {
        return __controlKeepAliveReplyTimeout;
    }

    // @since 3.0
    private static class CSL implements CopyStreamListener {

        private final FTPClient parent;
        private final long idle;
        private final int currentSoTimeout;

        private long time = System.currentTimeMillis();
        private int notAcked;

        CSL(FTPClient parent, long idleTime, int maxWait) throws SocketException {
            this.idle = idleTime;
            this.parent = parent;
            this.currentSoTimeout = parent.getSoTimeout();
            parent.setSoTimeout(maxWait);
        }
        public void bytesTransferred(CopyStreamEvent event) {
            bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
        }

        public void bytesTransferred(long totalBytesTransferred,
                int bytesTransferred, long streamSize) {
            long now = System.currentTimeMillis();
            if (now - time > idle) {
                try {
                    parent.__noop();
                } catch (SocketTimeoutException e) {
                    notAcked++;
                } catch (IOException e) {
                }
                time = now;
            }
        }

        void cleanUp() throws IOException {
            while(notAcked-- > 0) {
                parent.__getReplyNoReport();
            }
            parent.setSoTimeout(currentSoTimeout);
        }

    }

    /**
     * Merge two copystream listeners, either or both of which may be null.
     *
     * @param local the listener used by this class, may be null
     * @return a merged listener or a single listener or null
     * @since 3.0
     */
    private CopyStreamListener __mergeListeners(CopyStreamListener local) {
        if (local == null) {
            return __copyStreamListener;
        }
        if (__copyStreamListener == null) {
            return local;
        }
        // Both are non-null
        CopyStreamAdapter merged = new CopyStreamAdapter();
        merged.addCopyStreamListener(local);
        merged.addCopyStreamListener(__copyStreamListener);
        return merged;
    }

    /**
     * Enables or disables automatic server encoding detection (only UTF-8 supported).
     * @param autodetect If true, automatic server encoding detection will be enabled.
     */
    public void setAutodetectUTF8(boolean autodetect)
    {
        __autodetectEncoding = autodetect;
    }

    /**
     * Tells if automatic server encoding detection is enabled or disabled.
     * @return true, if automatic server encoding detection is enabled.
     */
    public boolean getAutodetectUTF8()
    {
        return __autodetectEncoding;
    }

    // DEPRECATED METHODS - for API compatibility only - DO NOT USE

    /**
     * @deprecated use {@link #getSystemType()} instead
     */
    @Deprecated
    public String getSystemName() throws IOException
    {
        if (__systemName == null && FTPReply.isPositiveCompletion(syst()))
            __systemName = _replyLines.get(_replyLines.size() - 1).substring(4);
        return __systemName;
    }
    public void mySslNegotiation(TrustManager[] custom) throws IOException {
    	SSLContext context = SSLContextFromTrustManager(custom);
        SSLSocketFactory ssf = context.getSocketFactory();
        String ip = _socket_.getInetAddress().getHostAddress();
        int port = _socket_.getPort();
        SSLSocket socket =
            (SSLSocket) ssf.createSocket(_socket_, ip, port, false);
        socket.setEnableSessionCreation(true);
        socket.setUseClientMode(true);
        // server mode
//        if (!isClientMode) {
//            socket.setNeedClientAuth(isNeedClientAuth);
//            socket.setWantClientAuth(isWantClientAuth);
//        }

//        if (protocols != null) socket.setEnabledProtocols(protocols);
//        if (suites != null) socket.setEnabledCipherSuites(suites);
        socket.startHandshake();

        _socket_ = socket;
        _controlInput_ = new BufferedReader(new InputStreamReader(
                socket .getInputStream(), getControlEncoding()));
        _controlOutput_ = new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream(), getControlEncoding()));
        setSocketFactory(new FTPSSocketFactory(context));
        setServerSocketFactory(new FTPSServerSocketFactory(context));
    }
    
}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
/* kate: indent-width 4; replace-tabs on; */
