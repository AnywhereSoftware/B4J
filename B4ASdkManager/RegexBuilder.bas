B4J=true
Group=Default Group
ModulesStructureVersion=1
Type=Class
Version=5.9
@EndOfDesignText@
'v1.00
'Class module
Sub Class_Globals
	Private pat As StringBuilder
End Sub

'Initializes the object.
Public Sub Initialize As RegexBuilder
	Clear
	Return Me
End Sub

'Clears the current pattern.
Public Sub Clear As RegexBuilder
	pat.Initialize
	Return Me
End Sub

'Returns an escaped string that will match the given string.
'This is useful if there is any special character. Nothing bad will happen if there is no special character in the string.
Public Sub Escape(Value As String) As String
	Dim i As Int = Value.IndexOf("\E")
	If i = -1 Then Return "\Q" & Value & "\E"
	Dim sb As StringBuilder
	sb.Initialize
	sb.Append("\Q")
	Dim start As Int = 0
	Do While i > -1
		sb.Append(Value.SubString2(start, i))
		sb.Append("\E\\E\Q")
		start = i + 2
		i = Value.IndexOf2("\E", start)
	Loop
	sb.Append(Value.SubString(start)).Append("\E")
	Return sb.ToString
End Sub

'Gets the pattern.
Public Sub getPattern As String
	Return pat.ToString
End Sub

'Appends the regex value to the pattern (as a non-capturing group).
Public Sub Append(Value As String) As RegexBuilder
	Return AddRegex("(?:" & Value & ")")
End Sub
'Similar to Append. Escapes the value first.
'The value will be matched as-is.
Public Sub AppendEscaped (Value As String) As RegexBuilder
	Return Append(Escape(Value))
End Sub

Private Sub AddRegex(RegexPattern As String) As RegexBuilder
	pat.Append(RegexPattern)
	Return Me
End Sub

'Matches a character from the given list.
Public Sub AppendAnyOf (Characters As List) As RegexBuilder
	Return Append("[" & CharactersToString(Characters) & "]")
End Sub

'Matches a character that is not in the list.
Public Sub AppendAnyBut (Characters As List) As RegexBuilder
	Return Append("[^" & CharactersToString(Characters) & "]")
End Sub

Private Sub CharactersToString (chars As List) As String
	Dim sb As StringBuilder
	sb.Initialize
	For Each s As String In chars
		sb.Append(s)
	Next
	Return sb.ToString
End Sub

'The last group will be matched at most once.
Public Sub AppendAtMostOne As RegexBuilder
	Return AddRegex("?")
End Sub
'The last group will be matched at least once.
Public Sub AppendAtLeastOne As RegexBuilder
	Return AddRegex("+")
End Sub
'The last group will be matched any number of times.
Public Sub AppendZeroOrMore As RegexBuilder
	Return AddRegex("*")
End Sub

'The last group will be matched the given number of times.
Public Sub AppendCount(MinValue As Int, MaxValue As Int) As RegexBuilder
	pat.Append("{").Append(MinValue).Append(",").Append(MaxValue).Append("}")
	Return Me
End Sub
'Appends an 'or' operator.
Public Sub AppendOr As RegexBuilder
	Return AddRegex("|")
End Sub

Public Sub AppendStartString As RegexBuilder
	Return AddRegex("^")
End Sub

Public Sub AppendEndString As RegexBuilder
	Return AddRegex("$")
End Sub

'Starts a capture group.
Public Sub StartCapture As RegexBuilder
	Return AddRegex("(")
End Sub
'Ends a capture group.
Public Sub EndCapture As RegexBuilder
	Return AddRegex(")")
End Sub
'Starts a non-capture group.
Public Sub StartNonCapture As RegexBuilder
	Return AddRegex("(?:")
End Sub
'Ends a non-capture group.
Public Sub EndNonCapture As RegexBuilder
	Return AddRegex(")")
End Sub

Public Sub getCharTab As String
	Return "\t"
End Sub

Public Sub getCharQuote As String
	Return $"\""$
End Sub

Public Sub getCharWord As String
	Return "\w"
End Sub

Public Sub getCharNonWord As String
	Return "\W"
End Sub

Public Sub getCharNonDigit As String
	Return "\D"
End Sub

Public Sub getCharDigit As String
	Return "\d"
End Sub

Public Sub getCharWhitespace As String
	Return "\s"
End Sub

Public Sub getCharNonWhitespace As String
	Return "\S"
End Sub

Public Sub getCharWindowsNewLine As String
	Return "\r\n"
End Sub

Public Sub getCharUnixNewLine As String
	Return "\n"
End Sub

Public Sub CharAny As String
	Return "."
End Sub

