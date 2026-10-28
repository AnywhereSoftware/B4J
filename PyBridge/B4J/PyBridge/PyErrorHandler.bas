B4J=true
Group=Default Group
ModulesStructureVersion=1
Type=Class
Version=10
@EndOfDesignText@
#ModuleVisibility: B4XLib
Sub Class_Globals
	Public IgnoredClasses As B4XSet
	Private ThreadClass As JavaObject
	Private mUtils As PyUtils
	Private BAClass As JavaObject
	Private FilesCache As Map
End Sub

Public Sub Initialize (Utils As PyUtils)
	BAClass.InitializeStatic("anywheresoftware.b4a.BA")
	ThreadClass = ThreadClass.InitializeStatic("java.lang.Thread").RunMethod("currentThread", Null)
	mUtils = Utils
	IgnoredClasses = B4XCollections.CreateSet2(Array("pyerrorhandler", "pyutils", "pywrapper", "pybridge", "pycomm"))
	FilesCache.Initialize
End Sub

Public Sub AddDataToTask (Task As PyTask)
	If mUtils.mOptions.TrackLineNumbers = False Then Return
	Dim elements() As Object = ThreadClass.RunMethod("getStackTrace", Null)
	For e = 8 To elements.Length - 1
		Dim element As JavaObject = elements(e)
		Dim origcls As String = element.RunMethod("getClassName", Null)
		If origcls.StartsWith(mUtils.PackageName) Then
			origcls = origcls.SubString(mUtils.PackageName.Length + 1)
			Dim i As Int = origcls.IndexOf("$")
			Dim cls As String = origcls
			If i > -1 Then cls = cls.SubString2(0, i)
			If IgnoredClasses.Contains(cls) Then Continue
			Task.Extra.Set(5, cls)
			Dim method As String
			If i > -1 Then
				method = origcls.SubString(i + 1)
				If method.StartsWith("ResumableSub") Then method = method.SubString("ResumableSub_".Length)
			Else
				method = element.RunMethod("getMethodName", Null)
			End If
			Task.Extra.Set(6, method)
			Task.Extra.Set(7, element.RunMethod("getLineNumber", Null))
			Return
		End If
	Next
End Sub

Public Sub UntangleError (s As String)
	Dim m As Matcher = Regex.Matcher("~de:([^,]+),(\d+)", s)
	m.Find
	Dim Module As String = m.Group(1)
	Dim LineNumber As Int = m.Group(2) - 1
	If FilesCache.ContainsKey(Module) = False Then
		Dim folder As String = File.Combine(File.DirApp, "src\" & mUtils.PackageName.Replace(".", "\"))
		Dim ffile As String = Module & ".java"
		If File.Exists(folder, ffile) Then
			FilesCache.Put(Module, File.ReadList(folder, ffile))
		Else
			FilesCache.Put(Module, Null)
		End If
	End If
	Dim lines As List = FilesCache.Get(Module)
	If NotInitialized(lines) Then Return
	For i = LineNumber To Max(0, LineNumber - 10) Step -1
		Dim line As String = lines.Get(i)
		If line.StartsWith(" //BA.debugLineNum") Then
			m = Regex.Matcher("BA\.debugLineNum\s*=\s*(\d+);", line)
			If m.Find Then
				BAClass.RunMethod("Log", Array("~de:" & Module & "," & m.Group(1)))
				Exit
			End If
		End If
	Next
	
End Sub