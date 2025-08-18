# run.ps1
Write-Output "Compiling PlantSketch..."
javac -d bin src/*.java
if ($LASTEXITCODE -eq 0) {
    Write-Output "Running PlantSim..."
    java -cp bin PlantSim
} else {
    Write-Output "Compilation failed."
}
