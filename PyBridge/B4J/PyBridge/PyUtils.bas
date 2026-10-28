B4J=true
Group=Default Group
ModulesStructureVersion=1
Type=Class
Version=10
@EndOfDesignText@
#ModuleVisibility: B4XLib
Sub Class_Globals
	Public TASK_TYPE_RUN = 1, TASK_TYPE_GET = 2, TASK_TYPE_RUN_ASYNC = 3, TASK_TYPE_CLEAN = 4 _
		, TASK_TYPE_ERROR = 5, TASK_TYPE_EVENT = 6, TASK_TYPE_PING = 7, TASK_TYPE_FLUSH = 8 As Int
	
	Public PythonBridgeCodeVersion As String = "1.00"
	Public PyOutPrefix = "(pyout) ", PyErrPrefix = "(pyerr) ", B4JPrefix = "(b4j) " As String
	Public EvalGlobals As PyWrapper
	Public ImportLib As PyWrapper
	
	Private mBridge As PyBridge
	Public TaskIdCounter, PyObjectCounter As Int
	Public CleanerClass As JavaObject
	Public CleanerIndex As Int
	Public Comm As PyComm
	Public mOptions As PyOptions
	Public cleaner As JavaObject
	Public RegisteredMembers As B4XSet
	Public Epsilon As Double = 0.0000001
	Private KeysThatNeedToBeRegistered As List
	Private ObjectsThatNeedToBeRegistered As List
	Private System As JavaObject
	Private MemorySlots As Map
	Private LastMemorySize As Int
	Public MEMORY_INCREASE_THRESHOLD As Int = 500000
	Public PackageName As String
	Public IO As PyWrapper
End Sub

'Internal method
Public Sub Initialize (bridge As PyBridge, vComm As PyComm)
	mBridge = bridge
	CleanerClass = CleanerClass.InitializeStatic(GetType(Me) & "$CleanRunnable")
	cleaner = cleaner.InitializeStatic("java.lang.ref.Cleaner").RunMethod("create", Null)
	System.InitializeStatic("System")
	PackageName = GetType(Me)
	PackageName = PackageName.SubString2(0, PackageName.Length - ".pyutils".Length)
	KeysThatNeedToBeRegistered.Initialize
	ObjectsThatNeedToBeRegistered.Initialize
	MemorySlots.Initialize
	If GetSystemProperty("b4j.ide", False) = True Then
		PyErrPrefix = ""
		PyOutPrefix = ""
		B4JPrefix = ""
	End If
End Sub

Public Sub Connected (vImportLib As PyObject, options As PyOptions)
	mOptions = options
	PyObjectCounter = 100
	ImportLib.Initialize(mBridge, vImportLib)
	EvalGlobals = mBridge.Builtins.Run("dict")
	RegisteredMembers.Initialize
	KeysThatNeedToBeRegistered.Clear
	ObjectsThatNeedToBeRegistered.Clear
	MemorySlots.Clear
	LastMemorySize = 0
	CheckKeysNeedToBeCleaned
	AddConverters
End Sub

Private Sub AddConverters
	IO = mBridge.ImportModule("io")
	Dim converters As PyWrapper = mBridge.Bridge.GetField("comm").GetField("serializator").GetField("converters")
	converters.Set(IO.GetField("BytesIO"), mBridge.Lambda("x: x.getvalue()"))
	mBridge.RunNoArgsCode("from b4x_bridge.bridge import bridge_instance")
End Sub

Public Sub Disconnected
	CleanerIndex = CleanerIndex + 1
End Sub

'Use PyWrapper.Run instead.
Public Sub Run (Target As PyObject, Method As String, Args As InternalPyMethodArgs) As PyObject
	Dim res As PyObject = CreatePyObject(0)
	Dim TASK As PyTask = CreatePyTask(0, TASK_TYPE_RUN, CreateExtra(Target, Method, Args, res))
	mBridge.ErrorHandler.AddDataToTask(TASK)
	Comm.SendTask(TASK)
	Return res
End Sub

Private Sub CreateExtra(Target As PyObject, Method As String, Args As InternalPyMethodArgs, res As PyObject) As Object()
	If mOptions.TrackLineNumbers Then
		Return Array(Target.Key, Method, Args.Args, Args.KWArgs, res.Key, "", "", 0)
	Else
		Return Array(Target.Key, Method, Args.Args, Args.KWArgs, res.Key)
	End If
End Sub

'Use PyWrapper.RunAsync instead.
Public Sub RunAsync(Target As PyObject, Method As String, Args As InternalPyMethodArgs) As ResumableSub
	Dim res As PyObject = CreatePyObject(0)
	Dim TASK As PyTask = CreatePyTask(0, TASK_TYPE_RUN_ASYNC, CreateExtra(Target, Method, Args, res))
	mBridge.ErrorHandler.AddDataToTask(TASK)
	Comm.SendTaskAndWait(TASK)
	Wait For (TASK) AsyncTask_Received (TASK As PyTask)
	Return CheckForErrorsAndReturn(TASK, res)
End Sub

Public Sub UnwrapBeforeSerialization (Extra As List)
	UnwrapList(Extra.Get(2))
	UnwrapMap(Extra.Get(3))
End Sub

Private Sub UnwrapList (Lst As List)
	If NotInitialized(Lst) Then Return
	For i = 0 To Lst.Size - 1
		Dim v As Object = Lst.Get(i)
		If v Is PyWrapper Then
			Lst.Set(i, v.As(PyWrapper).InternalKey)
		Else If v Is List Then
			UnwrapList(v)
		Else If v Is Map Then
			UnwrapMap(v)
		Else If IsArray(v) Then
			UnwrapTuple(v)
		End If
	Next
End Sub

Private Sub UnwrapTuple (Obj() As Object)
	For i = 0 To Obj.Length - 1
		Dim o As Object = Obj(i)
		If o Is PyWrapper Then
			Obj(i) = o.As(PyWrapper).InternalKey
		Else If o Is List Then
			UnwrapList(o)
		Else If o Is Map Then
			UnwrapMap(o)
		Else If IsArray(o) Then
			UnwrapTuple(o)
		End If
	Next
End Sub

Private Sub IsArray(obj As Object) As Boolean
	Return obj <> Null And "[Ljava.lang.Object;" = GetType(obj)
End Sub

Private Sub UnwrapMap (Map As Map)
	If NotInitialized(Map) Then Return
	Dim KeysThatNeedToBeUnwrapped As List
	KeysThatNeedToBeUnwrapped.Initialize
	For Each key As Object In Map.Keys
		Dim value As Object = Map.Get(key)
		If value Is PyWrapper Then
			KeysThatNeedToBeUnwrapped.Add(key)
		Else If value Is List Then
			UnwrapList(value)
		Else If value Is Map Then
			UnwrapMap(value)
		Else If IsArray(value) Then
			UnwrapTuple(value)
		End If
	Next
	For Each key As Object In KeysThatNeedToBeUnwrapped
		Dim value As Object = Map.Get(key)
		Map.Put(key, value.As(PyWrapper).InternalKey)
	Next
End Sub

Public Sub Flush As ResumableSub
	Dim task As PyTask = CreatePyTask(0, TASK_TYPE_FLUSH, Array())
	Comm.SendTaskAndWait(task)
	Wait For (task) AsyncTask_Received (task As PyTask)
	If task.TaskType = TASK_TYPE_ERROR Then
		mBridge.PyLastException = task.Extra.Get(0)
		Return False
	Else
		Return True
	End If
End Sub

'Use PyWrapper.Fetch instead.
Public Sub Fetch(PyObject As PyObject) As ResumableSub
	Dim TASK As PyTask = CreatePyTask(0, TASK_TYPE_GET, Array(PyObject.Key))
	Comm.SendTaskAndWait(TASK)
	Wait For (TASK) AsyncTask_Received (TASK As PyTask)
	Return CheckForErrorsAndReturn(TASK, PyObject)
End Sub

'Used internally.
Public Sub PyLog(Prefix As String, Clr As Int, O As Object)
	#if not(DISABLE_PYBRIDGE_LOGS)
	If o Is PyWrapper Then
		mBridge.PrintJoin(Array(o), Clr = mOptions.PyErrColor)
	Else
		Dim s As String = o
		If s.Length > 3900 Then
			s = s.SubString2(0, 3900) & CRLF & "(message truncated)"
		End If
		s = s.Trim.Replace(Chr(13), "")
		Dim lines() As String = Regex.Split("\n+", s)
		For Each line As String In lines
			line = line.Trim
			If line.StartsWith("~de:") Then
				mBridge.ErrorHandler.UntangleError(line)
			Else If Clr <> 0 Then
				If Comm.State = Comm.STATE_DISCONNECTED And line.StartsWith("Unhandled exception in task") Then
					Return
				End If
				LogColor(Prefix & line, Clr)
			Else
				Log(Prefix & line)
			End If
		Next
	End If
	#End If
End Sub

'Internal method
Public Sub CreatePyTask (TaskId As Int, TaskType As Int, Extra As List) As PyTask
	Dim t1 As PyTask
	t1.Initialize
	If TaskId = 0 Then
		TaskIdCounter = TaskIdCounter + 1
		TaskId = TaskIdCounter
	End If
	t1.TaskId = TaskId
	t1.TaskType = TaskType
	t1.Extra = Extra
	Return t1
End Sub

Public Sub CreatePyObject (Key As Int) As PyObject
	Dim t1 As PyObject
	t1.Initialize
	If Key = 0 Then
		PyObjectCounter = PyObjectCounter + 1
		Key = PyObjectCounter
	End If
	t1.Key = Key
	RegisterForCleaning(t1)
	Return t1
End Sub


Private Sub CheckForErrorsAndReturn (TASK As PyTask, PyObject As PyObject) As InternalPyTaskAsyncResult

	Return CreateInternalPyTaskAsyncResult(PyObject, TASK.Extra.Get(0), TASK.TaskType == TASK_TYPE_ERROR)
End Sub


Private Sub RegisterForCleaning (Py As PyObject)
	ObjectsThatNeedToBeRegistered.Add(Py)
	KeysThatNeedToBeRegistered.Add(Py.Key)
	MemorySlots.Put(Py.Key, Null)
	If MemorySlots.Size - LastMemorySize > MEMORY_INCREASE_THRESHOLD Then
		ForceGC
	End If
End Sub

Private Sub CheckKeysNeedToBeCleaned
	CleanerClass.RunMethod("getKeys", Null) 'clear any previous keys
	CleanerIndex = CleanerIndex + 1
	Dim MyIndex As Int = CleanerIndex
	CleanerClass.SetField("currentCleanerIndex", MyIndex)
	Do While MyIndex = CleanerIndex
		KeysImpl
		Sleep(200)
	Loop
End Sub

Private Sub KeysImpl
	Dim keys As List = CleanerClass.RunMethod("getKeys", Null)
	If keys.Size > 0 Then
		Comm.SendTask(CreatePyTask(0, TASK_TYPE_CLEAN, keys))
		For Each key As Int In keys
			MemorySlots.Remove(key)
		Next
		LastMemorySize = MemorySlots.Size
	End If
	RegisterKeys
End Sub

Public Sub ForceGC
	LastMemorySize = MemorySlots.Size
	PyLog(B4JPrefix, mOptions.B4JColor, "ForceGC: memory slots - " & LastMemorySize)
	System.RunMethod("gc", Null)
	KeysImpl
End Sub

Private Sub RegisterKeys
	CleanerClass.RunMethod("registerMultipleKeys", Array(ObjectsThatNeedToBeRegistered, KeysThatNeedToBeRegistered, cleaner))
	ObjectsThatNeedToBeRegistered.Clear
	KeysThatNeedToBeRegistered.Clear
End Sub

'Utility to prevent ints being treated as floats.
Public Sub ConvertToIntIfMatch (o As Object) As Object
	If o Is Float Or o Is Double Then
		Dim d As Double = o
		Dim i As Int = d
		If Abs(d - i) < Epsilon Then Return i
	End If
	Return o
End Sub

Public Sub ConvertLambdaIfMatch (o As Object) As PyWrapper
	If o Is PyWrapper Then Return o
	Return mBridge.RunStatement(o)	
End Sub

#if Java
public static class CleanRunnable implements Runnable {
	private final int key;
	private final int cleanerIndex;
	private final static java.util.List<Object> listOfKeys = java.util.Collections.synchronizedList(new java.util.ArrayList<Object>());
	public static volatile int currentCleanerIndex;
	public CleanRunnable(int key, int cleanerIndex) {
		this.key = key;
		this.cleanerIndex = cleanerIndex;
	}
	public void run() {
		if (this.cleanerIndex == currentCleanerIndex)
			listOfKeys.add(key);
	}
	public static java.util.List<Object> getKeys() {
		synchronized(listOfKeys) {
			java.util.ArrayList<Object> res = new java.util.ArrayList<Object>(listOfKeys);
			listOfKeys.clear();
			return res;
		}
	}
	public static void registerMultipleKeys(java.util.List<Object> objects, java.util.List<Integer> keys, java.lang.ref.Cleaner cleaner) {
		for (int i = 0;i < objects.size();i++) {
			Object object = objects.get(i);
			int key = keys.get(i);
			cleaner.register(object, new CleanRunnable(key, currentCleanerIndex));
		}
	}
}

#End If

Private Sub CreateInternalPyTaskAsyncResult (PyObject As PyObject, Value As Object, Error As Boolean) As InternalPyTaskAsyncResult
	Dim t1 As InternalPyTaskAsyncResult
	t1.Initialize
	t1.PyObject = PyObject
	t1.Value = Value
	t1.Error = Error
	Return t1
End Sub

Public Sub DetectOS As String
	Dim os As String = GetSystemProperty("os.name", "").ToLowerCase
	If os.Contains("win") Then
		Return "windows"
	Else If os.Contains("mac") Then
		Return "mac"
	Else
		Return "linux"
	End If
End Sub
