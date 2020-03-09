
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
 
 package anywheresoftware.b4a.objects.streams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.Properties;

import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.keywords.Bit;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
/**
 * File is a predefined object that holds methods for working with files.
 */
public class File {
	private static final String assetsDir = "AssetsDir";
	/**
	 * Returns a reference to the files added to the Files tab. These files are read-only (in Release mode).
	 */
	public static String getDirAssets() {
		return assetsDir;
	}
	/**
	 * Returns the temporary folder.
	 */
	public static String getDirTemp() {
		return System.getProperty("java.io.tmpdir");
	}
	/**
	 * Returns the application folder.
	 */
	public static String getDirApp() {
		return System.getProperty("user.dir");
	}
	private static int os;
	/**
	 * Returns the path to a folder that is suitable for writing files.
	 *On Windows, folders under Program Files are read-only. Therefore File.DirApp will be read-only as well.
	 *This method returns the same path as File.DirApp on non-Windows computers.
	 *On Windows it returns the path to the user data folder. For example:
	 *C:\Users\[user name]\AppData\Roaming\[AppName]
	 */
	public static String DirData(String AppName) {
		if (os == 0) {
			String s = System.getProperty("os.name", "").toLowerCase(BA.cul);
			if (s.contains("win"))
				os = 1;
			else
				os = 2;
		}
		if (os == 1) {
			String res = File.Combine(System.getenv("AppData"), AppName);
			File.MakeDir(res, "");
			return res;
		}
		else
			return File.getDirApp();

	}
	/**
	 * Returns the path of the file or folder parent.
	 */
	public static String GetFileParent(String FileName) {
		String s = new java.io.File(FileName).getParent();
		return s == null ? "" : s;
	}
	/**
	 * Returns the file name from the full path (or the directory name in case of a directory).
	 */
	public static String GetName(String FilePath) {
		return new java.io.File(FilePath).getName();
	}
	/**
	 * Returns true if the specified FileName exists (file or folder) in the specified Dir.
	 *Note that the file system is case sensitive.
	 *This method does not support File.DirAssets.
	 *
	 *Example:<code>
	 *If File.Exists(File.DirApp, "MyFile.txt") Then ...</code>
	 */
	public static boolean Exists(String Dir, String FileName) throws IOException {
		if ("".equals(Dir))
			Dir = null;
		return new java.io.File(Dir, FileName).exists();
	}
	/**
	 * Deletes the specified file. If the file name is a directory then it must be empty in order to be deleted.
	 *Returns true if the file was successfully deleted.
	 *Files in the assets folder cannot be deleted.
	 */
	public static boolean Delete(String Dir, String FileName) {
		if ("".equals(Dir))
			Dir = null;
		return new java.io.File(Dir, FileName).delete();
	}
	/**
	 * Creates the given folder (creates all folders as needed).
	 *Example:<code>
	 *File.MakeDir(File.DirApp, "music/90")</code>
	 */
	public static void MakeDir(String Parent, String Dir) {
		java.io.File file = new java.io.File(Parent, Dir);
		file.mkdirs();
	}
	/**
	 * Returns the size in bytes of the specified file.
	 *This method does not support files in the assets folder.
	 */
	public static long Size(String Dir, String FileName) {
		if ("".equals(Dir))
			Dir = null;
		return new java.io.File(Dir, FileName).length();
	}
	/**
	 * Returns the last modified date of the specified file.
	 *This method does not support files in the assets folder.
	 *Example:<code>
	 *Dim d As Long
	 *d = File.LastModified(File.DirApp, "1.txt")
	 *Msgbox(DateTime.Date(d), "Last modified")</code>
	 */
	public static long LastModified(String Dir, String FileName) {
		if ("".equals(Dir))
			Dir = null;
		return new java.io.File(Dir, FileName).lastModified();
	}
	/**
	 * Tests whether the specified file is a directory.
	 */
	public static boolean IsDirectory(String Dir, String FileName) {
		if ("".equals(Dir))
			Dir = null;
		return new java.io.File(Dir, FileName).isDirectory();
	}
	/**
	 * Returns a Uri string ("file://...") that points to the given file.
	 */
	public static String GetUri(String Dir, String FileName) {
		if (Dir.equals(File.getDirAssets())) {
			URL u = File.class.getResource("/Files/" + FileName);
			if (u == null)
				throw new RuntimeException("Asset file not found: " + FileName);
			return u.toString();
		}
		else {
			return new java.io.File(File.Combine(Dir, FileName)).toURI().toString();
		}
	}
	/**
	 * Returns the full path to the given file.
	 *This methods does not support files in the assets folder.
	 */
	public static String Combine(String Dir, String FileName) {
		if ("".equals(Dir))
			Dir = null;
		return new java.io.File(Dir, FileName).toString();
	}
	/**
	 * Returns a read only list with all the files and directories which are stored in the specified path.
	 * An uninitialized list will be returned if the folder is not accessible.
	 *This method does not support the assets folder.
	 */
	@SuppressWarnings("unchecked")
	public static anywheresoftware.b4a.objects.collections.List ListFiles(String Dir) throws IOException {
		anywheresoftware.b4a.objects.collections.List list = new anywheresoftware.b4a.objects.collections.List();
		if (Dir.equals(assetsDir) == false) {
			java.io.File folder = new java.io.File(Dir);
			if (!folder.isDirectory())
				throw new IOException(Dir + " is not a folder.");
			String[] f = folder.list();
			if (f != null)
				list.setObject((java.util.List)Arrays.asList(f));
		}
		else {
			throw new RuntimeException("Cannot list assets files");
		}
		return list;
	}


	/**
	 * Opens the specified file name which is located in the Dir folder for reading.
	 *Note that the file names are case sensitive.
	 */
	public static InputStreamWrapper OpenInput(String Dir, String FileName) throws IOException {

		InputStreamWrapper is = new InputStreamWrapper();
		if (Dir.equals(assetsDir)) {
			InputStream in = File.class.getResourceAsStream("/Files/" + FileName);
			if (in == null)
				throw new FileNotFoundException(FileName);

			is.setObject(in);
		}

		else
		{
			if ("".equals(Dir))
				Dir = null;
			is.setObject(new BufferedInputStream(new FileInputStream(new java.io.File(Dir, FileName)),
					8192));
		}
		return is;
	}

	/**
	 * Reads the entire file and returns a List with all lines (as strings).
	 *Example:<code>
	 *Dim List1 As List
	 *List1 = File.ReadList(File.DirApp, "1.txt")
	 *For i = 0 to List1.Size - 1
	 *	Log(List1.Get(i))
	 *Next </code>
	 */
	public static List ReadList(String Dir, String FileName) throws IOException {
		InputStreamWrapper in = OpenInput(Dir, FileName);
		TextReaderWrapper tr = new TextReaderWrapper();
		tr.Initialize(in.getObject());
		return tr.ReadList();
	}
	/**
	 * Writes each item in the list as a single line.
	 *Note that a value containing CRLF will be saved as two lines (which will return two item when read with ReadList).
	 *All values will be converted to strings.
	 *Example:<code>
	 *File.WriteList (File.DirApp, "mylist.txt", List1)</code>
	 */
	public static void WriteList(String Dir, String FileName, List List) throws IOException {
		OutputStreamWrapper out = OpenOutput(Dir, FileName, false);
		TextWriterWrapper tw = new TextWriterWrapper();
		tw.Initialize(out.getObject());
		tw.WriteList(List);
		tw.Close();
	}
	/**
	 * Writes the given text to a new file.
	 *Example:<code>
	 *File.WriteString(File.DirApp, "1.txt", "Some text")</code>
	 */
	public static void WriteString(String Dir, String FileName, String Text) throws IOException {
		OutputStreamWrapper out = OpenOutput(Dir, FileName, false);
		TextWriterWrapper tw = new TextWriterWrapper();
		tw.Initialize(out.getObject());
		tw.Write(Text);
		tw.Close();
	}
	/**
	 * Reads the file and returns its content as a string.
	 *Example:<code>
	 *Dim text As String
	 *text = File.ReadString(File.DirApp, "1.txt")</code>
	 */
	public static String ReadString(String Dir, String FileName) throws IOException {
		InputStreamWrapper in = OpenInput(Dir, FileName);
		TextReaderWrapper tr = new TextReaderWrapper();
		tr.Initialize(in.getObject());
		String res = tr.ReadAll();
		in.Close();
		return res;
	}
	/**
	 * Creates a new file and writes the given map. Each key value pair is written as a single line.
	 *All values are converted to strings.
	 *See this link for more information about the actual format: <link>Properties format|http://en.wikipedia.org/wiki/.properties</link>.
	 *You can use File.ReadMap to read this file.
	 */
	public static void WriteMap(String Dir, String FileName, Map Map) throws IOException {
		OutputStreamWrapper out = OpenOutput(Dir, FileName, false);
		Properties p = new Properties();
		java.util.Map<Object, Object> m = Map.getObject();
		for (Entry<Object, Object> e : m.entrySet()) {
			p.setProperty(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
		}
		p.store(out.getObject(), null);
		out.Close();
	}
	/**
	 * Reads the file and parses each line as a key-value pair (of strings).
	 *See this link for more information about the actual format: <link>Properties format|http://en.wikipedia.org/wiki/.properties</link>.
	 *You can use File.WriteMap to write a map to a file.
	 *Note that the order of items in the map may not be the same as the order in the file.
	 */
	public static Map ReadMap(String Dir, String FileName) throws IOException {
		return ReadMap2(Dir, FileName, null);
	}
	/**
	 * Similar to ReadMap. ReadMap2 adds the items to the given Map.
	 *By using ReadMap2 with a populated map you can force the items order as needed.
	 *Example:<code>
	 *Dim m As Map
	 *m.Initialize
	 *m.Put("Item #1", "")
	 *m.Put("Item #2", "")
	 *m = File.ReadMap2(File.DirApp, "settings.txt", m)</code>
	 */
	public static Map ReadMap2(String Dir, String FileName, Map Map) throws IOException {
		InputStreamWrapper in = OpenInput(Dir, FileName);
		Properties p = new Properties();
		p.load(in.getObject());
		if (Map == null) {
			Map = new Map();
		}
		if (Map.IsInitialized() == false) {
			Map.Initialize();
		}
		for (Entry<Object, Object> e : p.entrySet()) {
			Map.Put(e.getKey(), e.getValue());
		}
		in.Close();
		return Map;
	}
	/**
	 * Copies the specified source file to the target path.
	 *Note that it is not possible to copy files to the Assets folder.
	 */
	public static void Copy(String DirSource, String FileSource, String DirTarget, String FileTarget) throws IOException {
		File.Delete(DirTarget, FileTarget);
		InputStream in = File.OpenInput(DirSource, FileSource).getObject();
		OutputStream out = File.OpenOutput(DirTarget, FileTarget, false).getObject();
		Copy2(in, out);
		in.close();
		out.close();
	}
	/**
	 * Copies all the available data from the input stream into the output stream.
	 *The input stream is automatically closed at the end.
	 */
	public static void Copy2(InputStream In, OutputStream Out) throws IOException {
		byte[] buffer = new byte[8192];
		int count = 0;
		while ((count = In.read(buffer)) > 0) {
			Out.write(buffer, 0, count);
		}
		In.close();
	}
	/**
	 * Asynchronously copies all the available data from the input stream into the output stream.
	 *The input stream is automatically closed at the end.
	 *Returns an object that should be used as the sender filter.
	 *Example:<code>
	 *Wait For (File.Copy2Async(in, out)) Complete (Success As Boolean)
	 *Log("Success: " & Success)</code>
	 */
	public static Object Copy2Async(final BA ba, final InputStream In, final OutputStream Out) {
		final Object senderFilter = new Object();
		BA.runAsync(ba, senderFilter, "complete", new Object[] {false}, new Callable<Object[]>() {

			@Override
			public Object[] call() throws Exception {
				Copy2(In, Out);
				return new Object[] {true};
			}
		});
		return senderFilter;
	}
	/**
	 * Asynchronous version of ListFiles. Should be used with Wait For.
	 *Example:<code>
	 *Wait For (File.ListFilesAsync(Dir)) Complete (Success As Boolean, Files As List)</code>
	 */
	public static Object ListFilesAsync(final BA ba, final String Dir) {
		final Object senderFilter = new Object();
		BA.runAsync(ba, senderFilter, "complete", new Object[] {false, new List()}, new Callable<Object[]>() {
			@Override
			public Object[] call() throws Exception {
				List l = ListFiles(Dir);
				return new Object[] {true, l};
			}
		});
		return senderFilter;
	}


	/**
	 * Asynchronously copies the source file to the target path.
	 *Note that it is not possible to copy files to the Assets folder.
	 *Returns an object that should be used as the sender filter.
	 *Example: <code>
	 *Wait For (File.CopyAsync(File.DirAssets, "1.txt", File.DirTemp, "1.txt")) Complete (Success As Boolean)
	 *Log("Success: " & Success)</code>
	 */
	public static Object CopyAsync(BA ba, final String DirSource, final String FileSource, final String DirTarget, final String FileTarget) throws IOException {

		final Object senderFilter = new Object();
		BA.runAsync(ba, senderFilter, "complete", new Object[] {false}, new Callable<Object[]>() {
			@Override
			public Object[] call() throws Exception {
				Copy(DirSource, FileSource, DirTarget, FileTarget);
				return new Object[] {true};
			}
		});
		return senderFilter;
	}
	/**
	 * Reads the data from the given file.
	 */
	public static byte[] ReadBytes(String Dir, String FileName) throws IOException {
		return Bit.InputStreamToBytes(OpenInput(Dir, FileName).getObject());
	}
	/**
	 * Writes the data to the given file.
	 */
	public static void WriteBytes(String Dir, String FileName, byte[] Data) throws IOException {
		OutputStreamWrapper o = OpenOutput(Dir, FileName, false);
		try {
			o.WriteBytes(Data, 0, Data.length);
		} finally {
			o.Close();
		}
	}
	/**
	 * Opens (or creates) the specified file which is located in the Dir folder for writing.
	 *If Append is true then the new data will be written at the end of the existing file.
	 *This method does not support files in the assets folder.
	 */
	public static OutputStreamWrapper OpenOutput(String Dir, String FileName, boolean Append) throws FileNotFoundException {
		if ("".equals(Dir))
			Dir = null;
		OutputStreamWrapper o = new OutputStreamWrapper();
		o.setObject(
				new BufferedOutputStream(new FileOutputStream(new java.io.File(Dir, FileName), Append)));
		return o;
	}
	/**
	 * A stream that you can read from. Usually you will pass the stream to a "higher level" object like TextReader that will handle the reading.
	 *You can use File.OpenInput to get a file input stream.
	 */
	@ShortName("InputStream")
	public static class InputStreamWrapper extends AbsObjectWrapper<InputStream> {
		/**
		 * Use File.OpenInput to get a file input stream.
		 *This method should be used to read data from a bytes array.
		 *Initializes the input stream and sets it to read from the specified bytes array.
		 *StartOffset - The first byte that will be read.
		 *MaxCount - Maximum number of bytes to read.
		 */
		public void InitializeFromBytesArray (byte[] Buffer, int StartOffset, int MaxCount) {
			setObject(new ByteArrayInputStream(Buffer, StartOffset, MaxCount));
		}
		/**
		 * Closes the stream.
		 */
		public void Close() throws IOException {
			getObject().close();
		}
		/**
		 * Reads up to MaxCount bytes from the stream and writes it to the given Buffer.
		 *The first byte will be written at StartOffset.
		 *Returns the number of bytes actually read.
		 *Returns -1 if there are no more bytes to read.
		 *Otherwise returns at least one byte.
		 *Example:<code>
		 *Dim buffer(1024) As byte
		 *count = InputStream1.ReadBytes(buffer, 0, buffer.length)</code>
		 */
		public int ReadBytes(byte[] Buffer, int StartOffset, int MaxCount) throws IOException {
			return getObject().read(Buffer, StartOffset, MaxCount);
		}
		/**
		 * Returns an estimation of the number of bytes available without blocking.
		 */
		public int BytesAvailable() throws IOException {
			return getObject().available();
		}
	}
	/**
	 * A stream that you can write to. Usually you will pass the stream to a "higher level" object like TextWriter which will handle the writing.
	 *Use File.OpenOutput to get a file output stream.
	 */
	@ShortName("OutputStream")
	public static class OutputStreamWrapper extends AbsObjectWrapper<OutputStream> {
		/**
		 * Use File.OpenOutput to get a file output stream.
		 *This method should be used to write data to a bytes array.
		 *StartSize - The starting size of the internal bytes array. The size will increase if needed.
		 */
		public void InitializeToBytesArray(int StartSize) {
			setObject(new ByteArrayOutputStream(StartSize));
		}
		/**
		 * Returns a copy of the internal bytes array. Can only be used when the output stream was initialized with InitializeToBytesArray.
		 */
		public byte[] ToBytesArray() {
			if (!(getObject() instanceof ByteArrayOutputStream))
				throw new RuntimeException ("ToBytes can only be called after InitializeToBytesArray.");
			return ((ByteArrayOutputStream)getObject()).toByteArray();

		}
		/**
		 * Closes the stream.
		 */
		public void Close() throws IOException {
			getObject().close();
		}
		/**
		 * Flushes any buffered data.
		 */
		public void Flush() throws IOException {
			getObject().flush();
		}
		/**
		 * Writes the buffer to the stream. The first byte to be written is Buffer(StartOffset), 
		 *and the last is Buffer(StartOffset + Length - 1).
		 */
		public void WriteBytes(byte[] Buffer, int StartOffset, int Length) throws IOException {
			getObject().write(Buffer, StartOffset, Length);
		}
	}
	/**
	 * Writes text to the underlying stream.<br/>
	 *
	 *Example:<code>
	 *Dim Writer As TextWriter
	 *Writer.Initialize(File.OpenOutput(File.DirDefaultExternal, "1.txt", False))
	 *Writer.WriteLine("This is the first line.")
	 *Writer.WriteLine("This is the second line.")
	 *Writer.Close</code>
	 *
	 */
	@ShortName("TextWriter")
	public static class TextWriterWrapper extends AbsObjectWrapper<BufferedWriter> {
		/**
		 * Initializes this object by wrapping the given OutputStream using the UTF8 encoding.
		 */
		public void Initialize(OutputStream OutputStream) {
			setObject(new BufferedWriter(new OutputStreamWriter(OutputStream, Charset.forName("UTF8")),
					4096));
		}
		/**
		 * Initializes this object by wrapping the given OutputStream using the specified encoding.
		 */
		public void Initialize2(OutputStream OutputStream, String Encoding) {
			setObject(new BufferedWriter(new OutputStreamWriter(OutputStream, Charset.forName(Encoding)),
					4096));
		}
		/**
		 * Writes the given Text to the stream.
		 */
		public void Write(String Text) throws IOException {
			getObject().write(Text);
		}
		/**
		 * Writes the given Text to the stream followed by a new line character.
		 * Example:<code>
		 * 	Dim Writer As TextWriter
		 *	Writer.Initialize(File.OpenOutput(File.DirDefaultExternal, "1.txt", False))
		 *	Writer.WriteLine("This is the first line.")
		 *	Writer.WriteLine("This is the second line.")
		 *	Writer.Close </code>
		 */
		public void WriteLine(String Text) throws IOException {
			getObject().write(Text + "\n");
		}
		/**
		 * Writes each item in the list as a single line.
		 *Note that a value containing CRLF will be saved as two lines (which will return two item when read with ReadList).
		 *All values will be converted to strings.
		 */
		public void WriteList(List List) throws IOException {
			for (Object line : List.getObject()) {
				WriteLine(String.valueOf(line));
			}
		}

		/**
		 * Closes the stream.
		 */
		public void Close() throws IOException {
			getObject().close();
		}
		/**
		 * Flushes any buffered data.
		 */
		public void Flush() throws IOException {
			getObject().flush();
		}
	}
	/**
	 * Reads text from the underlying stream.
	 */
	@ShortName("TextReader")
	public static class TextReaderWrapper extends AbsObjectWrapper<BufferedReader> {
		/**
		 * Initializes this object by wrapping the given InputStream using the UTF8 encoding.
		 */
		public void Initialize(InputStream InputStream) {
			setObject(new BufferedReader(new InputStreamReader(InputStream, Charset.forName("UTF8")),
					4096));
		}
		/**
		 * Initializes this object by wrapping the given InputStream using the specified encoding.
		 */
		public void Initialize2(InputStream InputStream, String Encoding) {
			setObject(new BufferedReader(new InputStreamReader(InputStream, Charset.forName(Encoding)),
					4096));
		}
		/**
		 * Reads the next line from the stream. The new line characters are not returned.
		 *Returns Null if there are no more characters to read.
		 *
		 *Example:<code>
		 *	Dim Reader As TextReader
		 *	Reader.Initialize(File.OpenInput(File.InternalDir, "1.txt"))
		 *	Dim line As String
		 * 	line = Reader.ReadLine
		 * 	Do While line <> Null
		 *		Log(line)
		 *		line = Reader.ReadLine
		 *	Loop
		 *	Reader.Close</code>
		 */
		public String ReadLine() throws IOException {
			return getObject().readLine();
		}
		/**
		 * Reads characters from the stream and into the Buffer.
		 *Reads up to Length characters and puts them in the Buffer starting as StartOffset.
		 *Returns the actual number of characters read from the stream.
		 *Returns -1 if there are no more characters available.
		 */
		public int Read(char[] Buffer, int StartOffset, int Length) throws IOException {
			return getObject().read(Buffer, StartOffset, Length);
		}
		/**
		 * Tests whether there is at least one character ready for reading without blocking.
		 */
		public boolean Ready() throws IOException {
			return getObject().ready();
		}
		/**
		 * Reads all of the remaining text and closes the stream.
		 */
		public String ReadAll() throws IOException {
			char[] buffer = new char[1024];
			StringBuilder sb = new StringBuilder(1024);
			int count;
			while ((count = Read(buffer, 0, buffer.length)) != -1) {
				if (count < buffer.length)
					sb.append(new String(buffer, 0, count));
				else
					sb.append(buffer);
			}
			Close();
			return sb.toString();
		}
		/**
		 * Reads the remaining text and returns a List object filled with the lines.
		 *Closes the stream when done.
		 */
		public anywheresoftware.b4a.objects.collections.List ReadList() throws IOException {
			anywheresoftware.b4a.objects.collections.List List = new anywheresoftware.b4a.objects.collections.List();
			List.Initialize();
			String line;
			while ((line = ReadLine()) != null) {
				List.Add(line);
			}
			Close();
			return List;
		}

		/**
		 * Skips the specified number of characters.
		 *Returns the actual number of characters that were skipped (which may be less than the specified value).
		 */
		public int Skip(int NumberOfCharaceters) throws IOException {
			return (int)getObject().skip(NumberOfCharaceters);
		}
		/**
		 * Closes the stream.
		 */
		public void Close() throws IOException {
			getObject().close();
		}
	}
}
