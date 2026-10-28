B4J=true
Group=Default Group
ModulesStructureVersion=1
Type=Class
Version=4.19
@EndOfDesignText@
'Class module
Sub Class_Globals
	Private builder As XMLBuilder
	Type Clazz (Name As String, _
		ShortName As String, _
		comment As String, _
		ObjectWrapper As String, _
		Events As List, _
		Methods As Map, _
		Properties As Map, _
		DesignerProperties As List, _
		Helper As Boolean)
	Type Method(Name As String, _
		DesignerName As String, _
		Comment As String, _
		Parameters As List, _
		ReturnType As String, _
		Inlines As List, _
		SyncEvents As Boolean)
	Type Parameter (Name As String, _
		parType As String, DefaultValue As String)
	Private Classes As Map
	Private version As Float
	Private ObjCPP As Boolean
	Private author As String
	Private dependsOn As List
	Private customClasses As List
	Private lineIndex As Int
	Private lines As List
	Private currentClass As Clazz
	Private currentComment As String
	Private folder As String
	Private currentHelper As Boolean
	Private currentHide As Boolean
	Private currentShortname As String
	Private currentEvents As List
	Private currentDesignerProperties As List
	Private currentObjectWrapper As String
	Private currentRaisesSynchronousEvents As Boolean
	Private currentInlines As List
	Private XCFramework As Boolean = True
End Sub

Public Sub Initialize
	Classes.Initialize
	dependsOn.Initialize
	currentEvents.Initialize
	currentDesignerProperties.Initialize
	customClasses.Initialize
	currentInlines.Initialize
End Sub

Public Sub Parse(SourcePath As String, TargetPath As String)
	ParseH(File.GetFileParent(SourcePath), File.GetName(SourcePath))
	WriteXML(TargetPath)
End Sub

Private Sub ParseH(Dir As String, filename As String)
	folder = Dir
	lines = File.ReadList(Dir, filename)
	Do While lineIndex < lines.Size
		ParseLine(lines.Get(lineIndex))
		lineIndex = lineIndex + 1
	Loop
End Sub

Private Sub ParseLine(line As String)
	Dim trimmed As String = line.Trim
	If trimmed.StartsWith("//~") Then
		ParseSpecialComment(trimmed)
	Else If trimmed.StartsWith("//") Then
		ParseComment(False)
	Else If trimmed.StartsWith("/**") Then
		ParseComment(True)
	Else If trimmed.StartsWith("@interface") Then
		ParseInterface(trimmed)
	Else If trimmed.StartsWith("@end") Then
		ParseEnd
	Else If trimmed.StartsWith("#import") Then
		ParseImport(trimmed)
	Else If currentClass.IsInitialized Then
		If trimmed.StartsWith("@property") Then
			ParseProperty(trimmed)
		Else If trimmed.StartsWith("-") Then
			ParseMethod(trimmed)
		Else
			If trimmed.Length > 0 Then
				currentComment = ""
				If trimmed.StartsWith("@class") = False And trimmed.StartsWith("+") = False Then
					Log("Skipping: " & trimmed)
				End If
			End If
		End If
	End If
End Sub

Private Sub ParseInterface(line As String)
	If currentHide = False Then
		Dim currentClass As Clazz
		currentClass.Initialize
		currentClass.Events.Initialize
		currentClass.Events.AddAll(currentEvents)
		currentClass.comment = currentComment
		currentClass.DesignerProperties.Initialize
		currentClass.DesignerProperties.AddAll(currentDesignerProperties)
		currentClass.Methods.Initialize
		currentClass.Properties.Initialize
		Dim m As Matcher = Regex.Matcher("@interface\s+(\w+)\s*:\s*(\w+)", line)
		Dim parent As String
		If m.Find Then
			parent = m.Group(2)
		Else
			m = Regex.Matcher("@interface\s+(\w+)\s*\(.*", line) 'category
			m.Find
			parent = "NSObject"
		End If
		currentClass.Name = m.Group(1)
		currentClass.ShortName = currentShortname
		currentClass.ObjectWrapper = currentObjectWrapper
		currentClass.helper = currentHelper
		Classes.Put(currentClass.Name, currentClass)
		If parent <> "NSObject" Then
			If Classes.ContainsKey(parent) Then
				Dim c As Clazz = Classes.Get(parent)
				InheritClass(c)
			Else
				Log("Warning: parent class not found - " & parent)
			End If
		End If
	End If
	currentComment = ""
	currentEvents.Clear
	currentDesignerProperties.Clear
	currentShortname = ""
	currentObjectWrapper = ""
End Sub

Private Sub InheritClass(cls As Clazz)
	For Each event As String In cls.events
		currentClass.Events.Add(event)
	Next
	For Each dp As String In cls.DesignerProperties
		currentClass.DesignerProperties.Add(dp)
	Next
	For Each m As Method In cls.Methods.Values
		currentClass.Methods.Put(m.DesignerName, m)
	Next
	For Each P As Method In cls.Properties.Values
		currentClass.Properties.Put(P.Name, P)
	Next
End Sub

Private Sub ParseImport(line As String)
	Dim m As Matcher = Regex.Matcher("#import\s*<([^/]+)", line)
	If m.Find Then
		Dim P As Parser
		P.Initialize
		P.currentHelper = True
		Dim h As String = m.Group(1) & ".h"
		If h = "Foundation.h" Then Return
		If File.Exists(folder, h) = False Then
			Log("h file not found: " & h)
		Else
			P.ParseH(folder, h)
			For Each c As Clazz In P.Classes.Values
				Classes.Put(c.Name, c)
			Next
		End If
	End If
End Sub
Private Sub ParseProperty(line As String)
	Dim m As Matcher = Regex.Matcher("@property\s*(\([^)]+\))\s*(\w+\s*\*?(long)?)\s(/\*(\w+),(\d)\*/\s)?(\*?\w+)", line)
	If m.Find = False Then
		Log("Error parsing property: " & line)
	End If
	
	Dim prop As Method
	prop.Initialize
	prop.Parameters.Initialize
	prop.Name = m.Group(7)
	prop.ReturnType = m.Group(2)
	If prop.ReturnType.StartsWith("B4IArray") Then
		Dim t As String = m.Group(5)
		For i = 1 To m.Group(6)
			t = t & "[]"
		Next
		prop.ReturnType = t
	End If
	If m.Group(3) <> Null And m.Group(3).Length > 0 Then 'long long
		prop.ReturnType = prop.ReturnType.Trim
	Else
		prop.ReturnType = prop.ReturnType.Replace(" ", "")
	End If
	If prop.Name.StartsWith("*") Then
		prop.Name = prop.Name.SubString(1)
		prop.ReturnType = prop.ReturnType & "*"
	End If
	prop.Comment = currentComment
	currentComment = ""
	If currentInlines.Size > 0 Then
		prop.Inlines = currentInlines
		Dim currentInlines As List
		currentInlines.Initialize
	End If
	If Not(m.Group(1) <> Null And m.Group(1).Contains("readonly")) Then
		Dim P As Parameter
		P.Initialize
		P.Name = "p"
		P.parType = prop.ReturnType
		prop.Parameters.Add(P)
	End If
	If currentHide = False Then
		currentClass.Properties.Put(prop.Name, prop)
	Else
		currentClass.Properties.Remove(prop.Name)
	End If
End Sub

Private Sub ParseMethod(line As String)
	Dim m1 As Method
	m1.Initialize
	m1.Parameters.Initialize
	m1.Comment = currentComment
	currentComment = ""
	m1.SyncEvents = currentRaisesSynchronousEvents
	currentRaisesSynchronousEvents = False
	If currentInlines.Size > 0 Then
		m1.Inlines = currentInlines
		Dim currentInlines As List
		currentInlines.Initialize
	End If
	Dim returnTypeM As Matcher = Regex.Matcher("-\s*\(([^)]+)\)", line)
	returnTypeM.Find
	m1.ReturnType = ParseType(returnTypeM.Group(1))
	Dim methodName As StringBuilder
	methodName.Initialize
	Dim m As Matcher = Regex.Matcher("[) ]\s*(\w+)?\s*(:\s*\(([^)]+)\)\s*(\w+))?(\s*/\*\s*=\s*([^*]+)\*/)?", _
		line.SubString(returnTypeM.GetEnd(0) - 1))
	Dim isfirst As Boolean = True
	Do While m.Find And m.Match.Trim.Length > 0
		If isfirst Then
			m1.DesignerName = m.Group(1)
			methodName.Append(m.Group(1))
			If m.Group(2) <> Null Then
				methodName.Append(":")
				Dim P As Parameter
				P.Initialize
				P.Name = m.Group(4)
				P.parType = ParseType(m.Group(3))
				m1.Parameters.Add(P)
			End If
			isfirst = False
		Else	
			If m.Group(1) <> Null Then methodName.Append(m.Group(1))
			methodName.Append(":")
			Dim P As Parameter
			P.Initialize
			P.Name = m.Group(4)
			P.parType = ParseType(m.Group(3))
			m1.Parameters.Add(P)
		End If
		If m.Group(6) <> Null Then
			P.DefaultValue = m.Group(6).Trim
		End If
	Loop
	m1.Name = methodName.ToString
	If m1.Name.Length = 0 Then
		Log("Error: Cannot parse: " & line)
	End If
	If currentClass.Methods.ContainsKey(m1.DesignerName) Then
		Dim mm As Method = currentClass.Methods.Get(m1.DesignerName)
		If mm.Name <> m1.Name Then
			Log("Error: two or more methods with the same name: " & mm.DesignerName)
		End If
	End If
	If currentHide = False Then
		currentClass.Methods.Put(m1.DesignerName, m1)
	Else
		currentClass.Methods.Remove(m1.DesignerName)
	End If
End Sub

Private Sub ParseType(raw As String) As String
	raw = raw.Trim
	Dim i As Int
	If raw.StartsWith("B4IArray") Then
		Dim m As Matcher =  Regex.Matcher("\/\*([^,]+),(\d+)\*\/", raw)
		If m.Find = False Then
			Log("Error parsing array type: " & raw)
		End If
		Dim t As String = m.Group(1)
		For i = 1 To m.Group(2)
			t = t & "[]"
		Next
		Return t
	Else
		Dim i As Int = raw.IndexOf("*")
		If i > -1 Then
			Dim t As String = raw.SubString2(0, i)
			t = t.Trim & "*"
			Return t
		End If
	End If
	Return raw
End Sub

Private Sub ParseEnd
	If currentClass.IsInitialized = False Then Return
	If currentClass.ShortName = "" Then
		Log("No shortname for class: " & currentClass.Name)
	End If
	Dim currentClass As Clazz 'this line causes currentClass to point to a new (uninitialized) clazz.
End Sub

Private Sub ParseComment (multiple As Boolean)
	Dim comment As StringBuilder
	comment.Initialize
	Do While lineIndex < lines.Size
		Dim line As String = lines.Get(lineIndex)
		line = line.Trim
		If multiple Then
			Dim ex As Boolean
			If line.Contains("*/") Then
				line = line.SubString2(0, line.IndexOf("*/"))
				ex = True
			End If
			If line.StartsWith("/**") Then
				line = line.SubString(3)
			Else If line.StartsWith("*") Then
				line = line.SubString(1)
				If line.StartsWith(" ") Then line = line.SubString(1)
			End If
			comment.Append(line).Append(CRLF)
			If ex Then Exit
		Else
			If line.StartsWith("///") Then
				comment.Append(line.SubString(3)).Append(CRLF)
			Else If line.StartsWith("//") Then
				comment.Append(line.SubString(2)).Append(CRLF)
			Else
				Exit
			End If
		End If
		lineIndex = lineIndex + 1
	Loop
	If Not(multiple) Then lineIndex = lineIndex - 1
	currentComment = comment.ToString.Trim
End Sub

Private Sub ParseSpecialComment(line As String)
	Dim command As String = line.SubString(3) 'remove //~
	If command = "hide" Then
		lineIndex = lineIndex + 1
		currentHide = True
		ParseLine(lines.Get(lineIndex))
		currentHide = False
	Else
		Dim m As Matcher = Regex.Matcher("(\w+):(.*)", command)
		If m.Find Then
			Dim key As String = m.Group(1).ToLowerCase
			Dim value As String = m.Group(2).Trim
			Select key
				Case "shortname"
					currentShortname = value
				Case "version"
					version = value
				Case "author"
					author = value
				Case "event"
					currentEvents.Add(value)
				Case "designerproperty"
					currentDesignerProperties.Add(value)
				Case "objectwrapper"
					currentObjectWrapper = value
				Case "raisessynchronousevents"
					currentRaisesSynchronousEvents = value
				Case "dependson"
					dependsOn.Add(value)
				Case "customclasstemplate"
					customClasses.Add(value)
				Case "inline"
					currentInlines.Add(value)
				Case "objcpp"
					ObjCPP = value.ToLowerCase
				Case "xcframework"
					XCFramework = value.ToLowerCase
			End Select
		End If
	End If
End Sub

Private Sub WriteXML (TargetPath As String)
	builder = builder.create("root")
	builder.e("doclet-version-NOT-library-version").t("1.01").up
	
	For Each cls As Clazz In Classes.Values
		If cls.Helper Then
			Continue
		End If
		builder = builder.e("class")
		builder.e("name").t(cls.Name).up
		WriteElement("shortname", cls.ShortName)
		WriteElement("objectwrapper", cls.ObjectWrapper)
		WriteElement("comment", cls.comment)
		WriteList("event", cls.Events)
		WriteList("designerProperty", cls.DesignerProperties)
		For Each m As Method In cls.Methods.Values
			WriteMethod(m, "method")
		Next
		
		For Each prop As Method In cls.Properties.Values
			WriteMethod(prop, "property")
		Next
		builder = builder.up
	Next
	If version > 0 Then	WriteElement("version", NumberFormat(version, 1, 2))
	WriteElement("author", author)
	WriteList("dependsOn", dependsOn)
	WriteList("customClass", customClasses)
	If ObjCPP Then WriteElement("ObjCPP", True)
	If XCFramework Then WriteElement("XCFramework", True)
	builder.up
	Dim props As Map
	props.Initialize
	props.Put("indent", "yes")
	props.Put("{http://xml.apache.org/xslt}indent-amount", "4")
	File.WriteString(TargetPath, "",builder.asString2(props))
End Sub

Private Sub WriteMethod(m As Method, mtype As String)
	builder = builder.e(mtype)
	builder = builder.e("name")
	If m.DesignerName.Length > 0 Then builder.a("DesignerName", m.DesignerName)
	If m.SyncEvents Then builder.a("RaisesSynchronousEvents", True)
	builder = builder.t(m.Name).up
	WriteElement("comment", m.Comment)
	WriteElement("returntype", m.ReturnType)
	For Each P As Parameter In m.Parameters
		WriteParameter(P)
	Next
	If m.Inlines.IsInitialized Then
		builder = builder.e("inlines")
		For Each inline As String In m.Inlines
			WriteElement("inline", inline)
		Next
		builder = builder.up
	End If
	builder = builder.up
End Sub

Private Sub WriteParameter(P As Parameter)
	builder = builder.e("parameter")
	WriteElement("name", P.Name)
	WriteElement("type", P.parType)
	If P.DefaultValue <> "" Then
		WriteElement("default_value", P.DefaultValue)
	End If
	builder = builder.up
End Sub

Private Sub WriteElement(e As String, t As String)
	If t.Length > 0 Then
		builder.e(e).t(t).up
	End If
End Sub

Private Sub WriteList(fieldName As String, l1 As List)
	For Each lib In l1
		builder.e(fieldName).t(lib).up
	Next
End Sub


