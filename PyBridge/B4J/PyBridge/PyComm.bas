B4J=true
Group=Default Group
ModulesStructureVersion=1
Type=Class
Version=10
@EndOfDesignText@
#ModuleVisibility: B4XLib
Sub Class_Globals
	Private srvr As ServerSocket
	Public const STATE_DISCONNECTED = 1, STATE_CONNECTED = 2, STATE_WAITING_FOR_CONNECTION = 3 As Int
	Public State As Int
	Public Port As Int
	Private mBridge As PyBridge
	Private astream As AsyncStreams
	Private ser As B4XSerializator
	Private WaitingTasks As Map
	Private jME As JavaObject
	Public BufferedTasks As List
End Sub

Public Sub Initialize (Bridge As PyBridge, LocalPort As Int)
	InitializeWithLoopback(srvr, "srvr", LocalPort)
	Dim jo As JavaObject
	Dim correctClassesNames As Map = jo.InitializeStatic("anywheresoftware.b4a.randomaccessfile.RandomAccessFile").GetField("correctedClasses")
	correctClassesNames.Put("_pyobject", GetType(Bridge) & "$_pyobject")
	jME = Me
	WaitingTasks.Initialize
	Port = srvr.As(JavaObject).GetFieldJO("ssocket").RunMethod("getLocalPort", Null)
	mBridge = Bridge
	mBridge.Utils.PyLog(mBridge.Utils.B4JPrefix, mBridge.Utils.mOptions.B4JColor, "Server is listening on port: " & Port)
	srvr.Listen
	BufferedTasks.Initialize
	State = STATE_WAITING_FOR_CONNECTION
End Sub

Private Sub InitializeWithLoopback(Server As ServerSocket, EventName As String, vPort As Int)
	Server.Initialize(-1, EventName)
	Dim ia As JavaObject
	ia = ia.InitializeStatic("java.net.InetAddress").RunMethod("getLoopbackAddress", Null)
	Dim s As JavaObject = Server
	Dim socket As JavaObject
	socket.InitializeNewInstance("java.net.ServerSocket", Array(vPort, 50, ia))
	s.SetField("ssocket", socket)
End Sub

Private Sub Srvr_NewConnection (Successful As Boolean, NewSocket As Socket)
	If Successful Then
		mBridge.Utils.PyLog(mBridge.Utils.B4JPrefix, mBridge.Utils.mOptions.B4JColor, "connected")
'		astream.OutputQueueMaxSize = 1000000
		astream.InitializePrefix(NewSocket.InputStream, True, NewSocket.OutputStream, "astream")
		Sleep(100)
		ChangeState(STATE_CONNECTED)
	End If
End Sub

Public Sub CloseServer
	If State = STATE_CONNECTED Or State = STATE_WAITING_FOR_CONNECTION Then
		If astream.IsInitialized Then
			astream.Close
		End If
		srvr.Close
		ChangeState(STATE_DISCONNECTED)
	End If
End Sub

Private Sub AStream_NewData (Buffer() As Byte)
	Dim o() As Object = ser.ConvertBytesToObject(Buffer)
	Dim Task As PyTask = mBridge.Utils.CreatePyTask(o(0), o(1), o(2))
	If WaitingTasks.ContainsKey(Task.TaskId) Then
		jME.RunMethod("raiseEventWithSenderFilter", Array(mBridge.Utils, "asynctask_received", WaitingTasks.Remove(Task.TaskId), Array(Task)))
	Else
		CallSub2(mBridge, "Task_Received", Task)	
	End If
End Sub

Public Sub SendTask (Task As PyTask)
	If BufferedTasks.Size = 0 Then CallSubDelayed(Me, "Flush")
	BufferedTasks.Add(Task)
End Sub

Public Sub MoveTaskToLast(Task As PyTask)
	If BufferedTasks.Get(BufferedTasks.Size - 1) = Task Then
		Return
	End If
	Dim i As Int = BufferedTasks.IndexOf(Task)
	BufferedTasks.RemoveAt(i)
	BufferedTasks.Add(Task)
End Sub

Public Sub Flush
	If BufferedTasks.Size > 0 Then
		Dim FlatTasks As List
		FlatTasks.Initialize
		For Each Task As PyTask In BufferedTasks
			If Task.TaskType = mBridge.Utils.TASK_TYPE_RUN Or Task.TaskType = mBridge.Utils.TASK_TYPE_RUN_ASYNC Then
				mBridge.Utils.UnwrapBeforeSerialization(Task.Extra)
			End If
			FlatTasks.AddAll(Array(Task.TaskId, Task.TaskType, Task.Extra))
		Next
		Dim res As Boolean = astream.Write(ser.ConvertObjectToBytes(FlatTasks))
		If astream.OutputQueueSize > 100 Then
			mBridge.Utils.PyLog(mBridge.Utils.B4JPrefix, mBridge.Utils.mOptions.B4JColor, "Output queue size: " & astream.OutputQueueSize)
		End If
		If res = False And astream.OutputQueueSize > 0 Then
			LogError("Queue is full!")
		End If
		BufferedTasks.Clear
	End If
End Sub

Public Sub SendTaskAndWait (Task As PyTask)
	WaitingTasks.Put(Task.TaskId, Task)
	SendTask(Task)
	Flush
End Sub

Private Sub AStream_Error
	AStream_Terminated
End Sub

Private Sub AStream_Terminated
	ChangeState(STATE_DISCONNECTED)
	BufferedTasks.Clear
	srvr.Close
	If astream.IsInitialized Then astream.Close
	mBridge.Utils.PyLog(mBridge.Utils.B4JPrefix, mBridge.Utils.mOptions.B4JColor, "disconnected")
End Sub

Private Sub ChangeState (NewState As Int)
	If NewState = State Then Return
	Dim OldState As Int = State
	State = NewState
	CallSub3(mBridge, "state_changed", OldState, State)
End Sub


#if Java
public void raiseEventWithSenderFilter(B4AClass target, String eventName, Object senderFilter, Object[] params) {
	target.getBA().raiseEventFromUI(senderFilter, eventName, params);
}
#End If