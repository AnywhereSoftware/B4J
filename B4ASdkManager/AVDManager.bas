B4J=true
Group=Default Group
ModulesStructureVersion=1
Type=StaticCode
Version=5.9
@EndOfDesignText@
'Static code module
Sub Process_Globals
	Private fx As JFX
	Private btnRefresh As Button
	Private OutputTimer As Timer
	Type AVD (Name As String, Target As String)
	Type DeviceDefinition (Name As String, Id As Int)
	Type AVDScreen (ScreenName As String, Skin As String, Scale As String, DDName() As String, EmulatorName As String)
	Type PlatformData (Version As String, ABI As String, CPU As String)
	Private screens As List
	Private AVDs As List
	Private Devices As List
	Public Form As Form
	Private lblName As Label
	Private lblTarget As Label
	Private btnStart As Button
	Private cmbPlatform As ComboBox
	Private platforms As List
	Private CreateAvdShell As Shell
	Private SplitPane1 As SplitPane
	Private lstItems As ListView
	Private txtLogs As TextArea
	Private PrevOutputLength As Int
	Private btnDelete As Button
	Private cmbScreenSize As ComboBox
	Private xui As XUI
	Private AnotherProgressBar1 As AnotherProgressBar
	Private Dialog As B4XDialog
End Sub

Public Sub Show
	If Form.IsInitialized = False Then
		Form.Initialize("Form", Main.MainForm.Width - 20, 600)
		Form.RootPane.LoadLayout("AVDManager")
		Dialog.Initialize(Form.RootPane)
		Dialog.Title = "B4A Sdk Manager"
		OutputTimer.Initialize("OutputTimer", 100)
		SplitPane1.LoadLayout("Top")
		SplitPane1.LoadLayout("Bottom")
		SplitPane1.DividerPositions = Array As Double(0.8)
		lstItems.StyleClasses.Add("AVDList")
		screens.Initialize
		For Each line As String In File.ReadList(File.DirAssets, "AVDScreens.txt")
			Dim screen As AVDScreen
			screen.Initialize
			Dim s() As String = Regex.Split("\|", line)
			screen.ScreenName = s(0)
			screen.Skin = s(1)
			screen.Scale = s(2)
			screen.DDName = Regex.Split(",", s(3))
			screen.EmulatorName = s(4)
			cmbScreenSize.Items.Add(screen.ScreenName)
			screens.Add(screen)
		Next
	End If
	Form.Show
	RefreshList
End Sub

Sub SetBusy(busy As Boolean)
	btnRefresh.Enabled = Not(busy)
	AnotherProgressBar1.Visible = busy
	OutputTimer.Enabled = busy
End Sub

Private Sub CreateAvdManagerExec (args As List) As Shell
	Dim s As Shell
	Dim allArgs As List
	allArgs.Initialize
	allArgs.AddAll(Array("/c", "avdmanager.bat"))
	allArgs.Add("-v")
	allArgs.AddAll(args)
	s.Initialize("shl", "cmd", allArgs)
	If Main.SDKVersion = Main.SDK6609375 Then
		s.WorkingDirectory = File.Combine(Main.GetBinFolder, "stub")
	Else
		s.WorkingDirectory = Main.GetBinFolder
	End If
	s.Encoding = "ISO-8859-1"
	Main.SetEnvironment(s)
	Return s
End Sub

Sub OutputTimer_Tick
	If CreateAvdShell.IsInitialized = False Then Return
	Dim str As String = CreateAvdShell.GetTempOut
	If str.Length <> PrevOutputLength Then
		PrevOutputLength = str.Length
		txtLogs.Text = str
		txtLogs.SetSelection(txtLogs.Text.Length, txtLogs.Text.Length)
		Log(str)
	End If
End Sub

Sub btnRefresh_Action
	RefreshList
End Sub

Private Sub RefreshList
	SetBusy(True)
	AVDs.Initialize
	AVDList("avd", AVDs, "Parse_AVDItem")
	Wait For AVDList_Complete
	FillList
	lstItems.ScrollTo(0)
	Devices.Initialize
	AVDList("device", Devices, "Parse_DeviceItem")
	Wait For AVDList_Complete
	SetBusy(False)
	UpdateAfterSdkManagerIsUpdated
End Sub

Public Sub UpdateAfterSdkManagerIsUpdated
	If Main.InstalledItems.IsInitialized = False Then Return
	cmbPlatform.Items.Clear
	platforms.Initialize
	'Log(Main.InstalledItems)
	For Each si As SdkItem In Main.InstalledItems
		If Regex.IsMatch("platforms;android-\d+", si.Key) Then
			Dim si2 As SdkItem
			si2.Initialize
			For Each abi As String In Array("default", "google_apis", "android-wear", "google_apis_playstore")
				For Each cpu As String In Array("x86", "x86_64")
					si2.Key = $"system-images;android-${si.AndroidVersion};${abi};${cpu}"$
					If Main.IsInstalled(si2, False) Then
						cmbPlatform.Items.Add($"Platform ${si.AndroidVersion} (${abi}, ${cpu})"$)
						platforms.Add(CreatePlatformData(si.AndroidVersion, abi, cpu))
					End If
				Next
				
			Next
			
		End If
	Next
End Sub

Private Sub CreatePlatformData (Version As String, ABI As String, CPU As String) As PlatformData
	Dim pd As PlatformData
	pd.Initialize
	pd.Version = Version
	pd.ABI = ABI
	pd.CPU = CPU
	Return pd
End Sub


Sub Parse_AVDItem(Index() As Int, Lines() As String) As AVD
	Dim AV As AVD
	AV.Initialize
	Dim i As Int = Index(0)
	Do While i < Lines.Length
		Dim m As Matcher = Regex.Matcher("\s+([^:]+):(.*)", Lines(i))
		If m.Find Then
			Dim field As String = m.Group(1).Trim
			Dim value As String = m.Group(2).Trim
			If field = "Name" Then
				AV.Name = value
			Else If field = "Target" Then
				Dim NextLine As String = Lines(i + 1).Trim
				If NextLine.StartsWith("Based on") Then
					value = value & " " & NextLine
					i = i + 1
				End If
				AV.Target = value
			End If
		Else
			Exit
		End If
		i = i + 1
	Loop
	Index(0) = i - 1
	Return AV
End Sub

Private Sub AVDList(Command As String, TargetList As List, ParserSub As String)
	Dim s As Shell = CreateAvdManagerExec(Array("list", Command))
	s.Run(60000)
	Wait For (s) shl_ProcessCompleted (Success As Boolean, ExitCode As Int, StdOut As String, StdErr As String)
	If Success = False Or ExitCode <> 0 Then
		ShowMessage("Error: "  & Main.MergeErrorAndOutput(StdOut, StdErr))
	Else
'		Log("******** " & Command & " **************")
'		Log(StdOut)
		Dim lines() As String = Regex.Split2("\R", Regex.MULTILINE, StdOut)
		Dim index(1) As Int
		Dim InAvailable As Boolean
		If Command = "device" Then 
			InAvailable = True
		End If
		Do While index(0) < lines.Length
			Dim line As String = lines(index(0))
			If InAvailable = False And line.Trim.StartsWith("Name:") Then InAvailable = True
			If line.StartsWith("Available") Then
				InAvailable = True
			Else if InAvailable Then
				If line.Trim.Length = 0 Then
					Exit
				Else if line.StartsWith("-----") = False Then
					TargetList.Add(CallSub3(Me, ParserSub, index, lines))
				End If
			End If
			index(0) = index(0) + 1
		Loop
	End If
	TargetList.SortType("Name", True)
	
	CallSubDelayed(Me, "AVDList_Complete")
End Sub

Sub Parse_DeviceItem(Index() As Int, Lines() As String) As DeviceDefinition 
	Dim device As DeviceDefinition
	device.Initialize
	Dim i As Int = Index(0)
	Do While i < Lines.Length
		Dim m As Matcher = Regex.Matcher("^id: (\d+)", Lines(i))
		If m.Find Then
			device.Id = m.Group(1)
		Else
			m = Regex.Matcher("\s+([^:]+):(.*)", Lines(i))
			If m.Find Then
				Dim field As String = m.Group(1).Trim
				Dim value As String = m.Group(2).Trim
				If field = "Name" Then
					device.Name = value
				End If
			Else
				Exit
			End If
		End If
	
		i = i + 1
	Loop
	Index(0) = i - 1
	Return device
End Sub


Sub FillList
	lstItems.Items.Clear
	For Each av As AVD In AVDs
		lstItems.Items.Add(AVDItemToListItem(av))
	Next
End Sub
Sub CreatePaneForListItem As Pane
	Dim pane As Pane
	pane.Initialize("")
	pane.SetSize(lstItems.Width - 40, 72)
	Return pane
End Sub

Sub AVDItemToListItem(av As AVD) As Pane
	Dim pane As Pane = CreatePaneForListItem
	pane.LoadLayout("AVDListItem")
	lblName.Text = av.Name
	lblTarget.Text = av.Target.Replace(CRLF, " ")
	btnStart.Tag = av
	btnDelete.Tag = av
	Return pane
End Sub

Private Sub ShowMessage(msg As String)
	Main.LongText.Text = msg
	Dialog.ShowTemplate(Main.LongText, "Ok", "", "")
End Sub

Sub Form_Resize (Width As Double, Height As Double)
	For Each p As Pane In lstItems.Items
		p.SetSize(lstItems.Width - 40, p.Height)
	Next
End Sub

Sub btnStart_Action
	Dim btn As Button = Sender
	Dim av As AVD = btn.Tag
	Dim shl As Shell
	Dim args As List = Array("-avd", av.Name)
	shl.Initialize("shl", File.Combine(Main.GetSdkRootFolder, "emulator\emulator.exe"), args)
	Main.SetEnvironment(shl)
	shl.Run(-1)
	Dim Ignore(1) As Boolean
	SetBusy(True)
	RemoveBusyAfter(5000, Ignore)
	Wait For (shl) shl_ProcessCompleted (Success As Boolean, ExitCode As Int, StdOut As String, StdErr As String)
	If Ignore(0) = False Then
		'will only happen if there is an immediate error
		Ignore(0) = True
		SetBusy(False)
	End If
	If Success = False Or ExitCode <> 0 Then
		ShowMessage("Error: " & Main.MergeErrorAndOutput(StdOut, StdErr))
	End If
End Sub

Sub RemoveBusyAfter(DelayMs As Int, Ignore() As Boolean)
	Sleep(DelayMs)
	If Ignore(0) = False Then
		SetBusy(False)
		Ignore(0) = True
	End If
End Sub

Sub btnDelete_Action
	
	Dim btn As Button = Sender
	Dim av As AVD = btn.Tag
	Wait For (Dialog.Show($"Do you want to delete '${av.Name}' ?"$, "Yes", "Cancel", "No")) Complete (Result As Int)
	If Result = xui.DialogResponse_Positive Then
		SetBusy(True)
		Dim shl As Shell = CreateAvdManagerExec(Array("delete", "avd", "-n", av.Name))
		shl.Run(-1)
		Wait For (shl) shl_ProcessCompleted (Success As Boolean, ExitCode As Int, StdOut As String, StdErr As String)
		If Success = False Or ExitCode <> 0 Then
			ShowMessage("Error: " & Main.MergeErrorAndOutput(StdOut, StdErr))
		End If
		RefreshList
	End If
	
End Sub


Private Sub GetSkinFolder(pd As PlatformData) As String
	If pd.ABI = "android-wear" Then
		Return File.Combine(Main.GetSdkRootFolder, $"system-images\${AddAndroidToVersion(pd.Version)}\android-wear\${pd.CPU}\skins"$)
	Else
		Return File.Combine(Main.GetSdkRootFolder, $"platforms\${AddAndroidToVersion(pd.Version)}\skins"$)
	End If
End Sub

Private Sub AddAndroidToVersion(v As String) As String
	Return $"android-${v}"$
End Sub

Sub btnCreateAVD_Action
	If cmbScreenSize.SelectedIndex = -1 Or cmbPlatform.SelectedIndex = -1  Then
		ShowMessage("Please fill all fields.")
		Return
	End If
	Dim pd As PlatformData = platforms.Get(cmbPlatform.SelectedIndex)
	Dim screen As AVDScreen = screens.Get(cmbScreenSize.SelectedIndex)
	
	Dim EmulatorName As String = $"${screen.EmulatorName}_Platform_${pd.Version}_${pd.ABI}"$
	Dim TargetPath As String = File.Combine(Main.GetSdkRootFolder, "B4AEmulator\" & EmulatorName)
	File.MakeDir("", TargetPath)
	If (screen.EmulatorName.StartsWith("Wear") = True) <> (pd.ABI = "android-wear") Then
		ShowMessage("Wear screen size and android-wear platform must be selected.")
		Return
	End If
	If IsNumber(pd.Version) And pd.Version >= 28 And pd.CPU = "x86" Then
		ShowMessage("Version 28+ requires x86_64 CPU.")
		Return
	End If
	Dim image As String = $"system-images;android-${pd.Version};${pd.ABI};${pd.CPU}"$
'	Log("************** devices *********************")
'	Log(Devices)
'	Log("*****************")
'	Log(screen)
	Dim dd As DeviceDefinition = FindDeviceDefinition(screen)
	If dd.IsInitialized = False Then
		ShowMessage("Device definition not found: " & screen.ScreenName)
		Return
	End If
	Dim args As List = Array As String("create", "avd", "-n", EmulatorName, "-k", image, "-f", "-c", "256M", "-d", dd.Id, "-p", TargetPath, _
		"--abi", pd.ABI & "/" & pd.CPU)
	SetBusy(True)
	CreateAvdShell = CreateAvdManagerExec(args)
	CreateAvdShell.Run(-1)
	Wait For (CreateAvdShell) shl_ProcessCompleted (Success As Boolean, ExitCode As Int, StdOut As String, StdErr As String)
	Log("***" & Success & ": " & StdErr)
	If Success And ExitCode = 0 Then
		Dim ConfigFile As String = File.Combine(TargetPath, "config.ini")
		Dim skin As String = screen.Skin
		
		Dim Config As Map = ConfigFileToMap(File.ReadList(ConfigFile, ""))
		Config.Put("skin.name", skin)
		Config.Put("skin.path", File.Combine(GetSkinFolder(pd), skin))
		Config.Put("hw.gpu.enabled", "yes")
		Config.Put("hw.gpu.mode", "auto")
		Config.Put("hw.lcd.density", screen.Scale)
		Config.Put("hw.keyboard", "yes")
		File.WriteList(ConfigFile, "", MapToConfigFile(Config))
	Else
		ShowMessage("Error: " & Main.MergeErrorAndOutput(StdOut, StdErr))
	End If
	RefreshList
	SetBusy(False)
End Sub

Private Sub FindDeviceDefinition (screen As AVDScreen) As DeviceDefinition
	For Each device As DeviceDefinition In Devices
		For Each ddname As String In screen.DDName
			If device.Name.StartsWith(ddname) Then
				Return device
			End If
		Next
	Next
	Dim Empty As DeviceDefinition
	Return Empty
End Sub

Private Sub MapToConfigFile(m As Map) As List
	Dim lines As List
	lines.Initialize
	For Each k As String In m.Keys
		lines.Add($"${k}=${m.Get(k)}"$)
	Next
	Return lines
End Sub

Private Sub ConfigFileToMap(Lines As List) As Map
	Dim m As Map
	m.Initialize
	For Each l As String In Lines
		Dim matcher As Matcher = Regex.Matcher("([^=]+)=(.*)", l)
		If matcher.Find Then
			m.Put(matcher.Group(1), matcher.Group(2))
		Else
			Log("Invalid line: " & l)
		End If
	Next
	Return m
End Sub

Sub btnDownloadHAXM_Click
	
	fx.ShowExternalDocument("https://github.com/intel/haxm#downloads")
End Sub