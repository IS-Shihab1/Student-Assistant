# StudentUtilityHub

Simple Java Swing student utilities demo. Source files are under `src/com/studenthub`.

How to compile & run (Windows PowerShell):

```powershell
# compile
javac -d out (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })
# run
java -cp out com.studenthub.Main
```

Resources are in `resources/` and runtime data is stored in `data/` (created on first run).
