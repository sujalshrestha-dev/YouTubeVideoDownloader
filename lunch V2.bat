@echo off
:: Compile the file
javac YoutubeDownloaderV2.java

:: Launch the app in a new process and close CMD
start javaw YoutubeDownloaderV2
