Type=Class
Version=5.51
ModulesStructureVersion=1
B4J=true
@EndOfDesignText@
'Class module
Sub Class_Globals
	Private fx As JFX
	Private texts As List
	Private lastItem As JavaObject
End Sub

Public Sub Initialize
	texts.Initialize
End Sub

Public Sub AddText(text As String) As TextFlow
	Dim lastItem As JavaObject
	lastItem.InitializeNewInstance("javafx.scene.text.Text", Array(text))
	texts.Add(lastItem)
	Return Me
End Sub

Public Sub SetFont(Font As Font) As TextFlow
	lastItem.RunMethod("setFont", Array(Font))
	Return Me
End Sub

Public Sub SetColor(Color As Paint) As TextFlow
	lastItem.RunMethod("setFill", Array(Color))
	Return Me	
End Sub

Public Sub SetUnderline(Underline As Boolean) As TextFlow
	lastItem.RunMethod("setUnderline", Array(Underline))
	Return Me
End Sub

Public Sub SetStrikethrough(Strikethrough As Boolean) As TextFlow
	lastItem.RunMethod("setStrikethrough", Array(Strikethrough))
	Return Me
End Sub



Public Sub CreateTextFlow As Pane
	Dim tf As JavaObject
	tf.InitializeNewInstance("javafx.scene.text.TextFlow", Null)
	tf.RunMethodJO("getChildren", Null).RunMethod("addAll", Array(texts))
	Return tf
End Sub