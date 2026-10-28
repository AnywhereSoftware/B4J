B4J=true
Group=Default Group
ModulesStructureVersion=1
Type=Class
Version=5.98
@EndOfDesignText@
Sub Class_Globals
	Private fx As JFX
	Public path As String
	Private mSdkPath As String
	Private x2m As Xml2Map
	Private const MAVEN_LINK As String = "https://dl.google.com/dl/android/maven2"
	Private const INCLUDE_BETA As Boolean = False
	Private CachedAvailableItems As List
End Sub

Public Sub Initialize
	x2m.Initialize
	CachedAvailableItems.Initialize
End Sub

Public Sub SetPath(sdkpath As String)
	If sdkpath <> mSdkPath Then
		path = File.Combine(File.GetFileParent(sdkpath), "..\..\extras\b4a_remote")
		If File.Exists(path, "") = False Then
			File.MakeDir(path, "")
		End If
		If File.Exists(path, "androidx.txt") = False Then
			File.WriteString(path, "androidx.txt", "")
		End If
		mSdkPath = sdkpath
		CachedAvailableItems.Clear
	End If
End Sub

Public Sub FindAvailableItems (NoCache As Boolean) As ResumableSub
	If NoCache = False And CachedAvailableItems.Size > 0 Then
'		For Each si As SdkItem In CachedAvailableItems
'			Log(si.Key)
'		Next
		Return CachedAvailableItems
	End If
	Wait For (Download("master-index.xml", True)) JobDone(j As HttpJob)
	CachedAvailableItems.Clear
	If j.Success Then
		Dim m As Map = x2m.Parse(j.GetString)
		m = m.Get("metadata")
		For Each key As String In m.Keys
			DownloadGroupIndex(key, CachedAvailableItems)
		Next
		For i = 1 To m.Size
			Wait For GroupIndexComplete
		Next
	Else
		Main.LogMessage("error: " & j.ErrorMessage)
	End If
	j.Release
	Return CachedAvailableItems
End Sub


Private Sub DownloadGroupIndex (key As String, res As List)
	Wait For (Download(key.Replace(".", "/") & "/group-index.xml", False)) JobDone (j As HttpJob)
	If j.Success Then
		Dim m As Map = x2m.Parse(j.GetString)
		m = m.Get(key)
		For Each component As String In m.Keys
			
			Dim componentMap As Map = m.Get(component)
			Dim attributes As Map = componentMap.Get("Attributes")
			Dim versions As String = attributes.Get("versions")
			Dim vers() As String = Regex.Split(",", versions)
			Dim version As String
			Dim KeyAndComponent As String = key & ":" & component
			If Main.RecommendedResourcesWithExplicitVersions.ContainsKey(KeyAndComponent) Then
				Dim ExplicitVersion As String = Main.RecommendedResourcesWithExplicitVersions.Get(KeyAndComponent)
				For i = vers.Length - 1 To 0 Step -1
					If vers(i) = ExplicitVersion Then
						version = vers(i)
					End If
				Next
			End If
			If version = "" Then
				For i = vers.Length - 1 To 0 Step -1
					If INCLUDE_BETA = True Or vers(i).Contains("-") = False Then
						version = vers(i)
						Exit
					End If
				Next
				If version = "" Then version = vers(vers.Length - 1)
			End If
			res.Add(SdkItemFromKeyAndVersion(KeyAndComponent, version))
		Next
	End If
	j.Release
	CallSubDelayed(Me, "GroupIndexComplete")
End Sub

Private Sub SdkItemFromKeyAndVersion (key As String, version As String) As SdkItem
	Dim si As SdkItem
	si.Initialize
	si.Key = key
	si.version = version
	si.GoogleMaven = True
	si.description = si.Key & " (Google Maven)"
	Return si
End Sub


Private Sub Download(LastPath As String, WithLog As Boolean) As HttpJob
	Dim j As HttpJob
	j.Initialize("", Me)
	j.Download(MAVEN_LINK & "/" & LastPath)
	If WithLog Then
		Main.LogMessage("Downloading: " & MAVEN_LINK & "/" & LastPath)
	End If
	Return j
End Sub

Public Sub DownloadItems(items As List) As ResumableSub
	Dim m As Map = LoadInstalledComponents
	For Each si As SdkItem In items
		Wait For (DownloadItem(si)) Complete (result As Boolean)
		Sleep(50)
		If result Then
			m.Put(si.Key, si.version)
		End If
	Next
	File.WriteMap(path, "installed-components.txt", m)
	Return True
End Sub

Private Sub LoadInstalledComponents As Map
	Dim m As Map
	If File.Exists(path, "installed-components.txt") Then
		m = File.ReadMap(path, "installed-components.txt")
	Else
		m.Initialize
	End If
	Return m
End Sub

Private Sub DownloadItem(si As SdkItem) As ResumableSub
	Dim parts() As String = Regex.Split(":", si.Key)
	Dim GroupId As String = parts(0)
	Dim ArtifactId As String = parts(1)
	Dim UriPath As String = GroupId.Replace(".", "/") & "/" & ArtifactId
	Dim targetFolder As String = File.Combine(path, UriPath & "\" & si.version)
	File.MakeDir(targetFolder, "")
	
	Dim FileNameWithoutExtension As String = $"${ArtifactId}-${si.version}"$
	Wait For (Download($"${UriPath}/${si.version}/${FileNameWithoutExtension}.pom"$, True)) JobDone (j As HttpJob)
	If j.Success Then
		Dim pom As String = j.GetString
		Dim jarExtension As String
		If pom.Contains("<packaging>aar</packaging>") Then
			jarExtension = "aar"
		Else
			jarExtension = "jar"
		End If
		File.WriteString(targetFolder, FileNameWithoutExtension & ".pom", pom)
		j.Release
		Wait For (Download($"${UriPath}/${si.version}/${FileNameWithoutExtension}.${jarExtension}"$, True)) JobDone (j As HttpJob)
		If j.Success Then
			Dim out As OutputStream = File.OpenOutput(targetFolder, FileNameWithoutExtension & "." & jarExtension, False)
			File.Copy2(j.GetInputStream, out)
			out.Close
			Main.LogMessage($"${si.description} downloaded successfully."$)
			Return True
		Else
			Main.LogMessage(j.ErrorMessage)
		End If
	Else
		Main.LogMessage(j.ErrorMessage)
	End If
	j.Release
	Return False
End Sub

Public Sub FindInstalledItems As List
	Dim m As Map = LoadInstalledComponents
	Dim res As List
	res.Initialize
	For Each key As String In m.Keys
		Dim version As String = m.Get(key)
		res.Add(SdkItemFromKeyAndVersion(key, version))
	Next
	Return res
End Sub

