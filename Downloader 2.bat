@echo off
setlocal enabledelayedexpansion
title YouTube Downloader by PSH TRIGGER
:: Removed color line to keep default console color
:: color 0A

:MainMenu
cls
echo ================================================
echo             YouTube Downloader
echo ================================================
echo.
echo 1. Download Multiple Videos
echo 2. Download Full Playlist
echo 3. Download Audio Only
echo 4. Help / About
echo 5. Exit
echo.
set /p choice="Please choose an option (1-5): "

if "%choice%"=="1" goto MultipleVideos
if "%choice%"=="2" goto FullPlaylist
if "%choice%"=="3" goto AudioOnly
if "%choice%"=="4" goto About
if "%choice%"=="5" exit
goto MainMenu

:: -------------------------------------
:MultipleVideos
cls
echo Select Video Quality:
echo 1. 360p
echo 2. 480p
echo 3. 720p
echo 4. Best Quality
echo 5. Go Back
echo.
set /p quality="Enter choice (1-5): "
if "%quality%"=="5" goto MainMenu

:: Set video format
set "format="
if "%quality%"=="1" set "format=bestvideo[height<=360]+bestaudio/best[height<=360]"
if "%quality%"=="2" set "format=bestvideo[height<=480]+bestaudio/best[height<=480]"
if "%quality%"=="3" set "format=bestvideo[height<=720]+bestaudio/best[height<=720]"
if "%quality%"=="4" set "format=bestvideo+bestaudio/best"

set "urls="
:MV_CollectURLs
echo.
echo Enter YouTube video URLs one at a time.
echo Type ^<done^> when finished or ^<back^> to return to main menu.
set /p "input=Enter video URL: "
if /i "%input%"=="done" goto MV_AskFolder
if /i "%input%"=="back" goto MainMenu
set "urls=!urls! %input%"
goto MV_CollectURLs

:MV_AskFolder
echo.
set /p "folder=Enter folder path to save videos (e.g., E:\Videos or .): "
if not exist "%folder%" mkdir "%folder%"
echo.
echo Downloading videos...

for %%A in (!urls!) do (
    echo Downloading: %%A
    yt-dlp --no-warnings --cookies cookies.txt -f "%format%" -P "%folder%" "%%A"
)

echo.
echo All downloads completed.
pause
goto MainMenu

:: -------------------------------------
:FullPlaylist
cls
echo Select Playlist Video Quality:
echo 1. 360p
echo 2. 480p
echo 3. 720p
echo 4. Best Quality
echo 5. Go Back
echo.
set /p quality="Enter choice (1-5): "
if "%quality%"=="5" goto MainMenu

:: Set video format
set "format="
if "%quality%"=="1" set "format=bestvideo[height<=360]+bestaudio/best[height<=360]"
if "%quality%"=="2" set "format=bestvideo[height<=480]+bestaudio/best[height<=480]"
if "%quality%"=="3" set "format=bestvideo[height<=720]+bestaudio/best[height<=720]"
if "%quality%"=="4" set "format=bestvideo+bestaudio/best"

set "urls="
:PL_CollectURLs
echo.
echo Enter playlist URLs one at a time.
echo Type ^<done^> when finished or ^<back^> to return to main menu.
set /p "input=Enter playlist URL: "
if /i "%input%"=="done" goto PL_Numbering
if /i "%input%"=="back" goto MainMenu
set "urls=!urls! %input%"
goto PL_CollectURLs

:PL_Numbering
cls
echo Do you want to add numbering to playlist videos?
echo 1. 1, 2, 3...
echo 2. I, II, III...
echo 3. a, b, c...
echo 4. No numbering
echo.
set /p numbering="Choose numbering style (1-4): "

set "prefix="
if "%numbering%"=="1" set "prefix=%%(playlist_index)s. "
if "%numbering%"=="2" set "prefix=%%(playlist_index,roman)s. "
if "%numbering%"=="3" set "prefix=%%(playlist_index,alpha)s. "
if "%numbering%"=="4" set "prefix="

echo.
set /p "folder=Enter folder path to save playlists (e.g., E:\Playlists or .): "
if not exist "%folder%" mkdir "%folder%"
echo.
echo Downloading playlists...

for %%A in (!urls!) do (
    echo Downloading: %%A
    yt-dlp --no-warnings --cookies cookies.txt -f "%format%" -o "%folder%\%%(playlist_title)s\!prefix!%%(title)s.%%(ext)s" "%%A"
)

echo.
echo Playlist downloads completed.
pause
goto MainMenu

:: -------------------------------------
:AudioOnly
cls
set "urls="
:AU_CollectURLs
echo.
echo Enter video or playlist URLs for audio download.
echo Type ^<done^> when finished or ^<back^> to return to main menu.
set /p "input=Enter URL: "
if /i "%input%"=="done" goto AU_AskFolder
if /i "%input%"=="back" goto MainMenu
set "urls=!urls! %input%"
goto AU_CollectURLs

:AU_AskFolder
echo.
set /p "folder=Enter folder path to save audio files (e.g., E:\Music or .): "
if not exist "%folder%" mkdir "%folder%"
echo.
echo Downloading audio...

for %%A in (!urls!) do (
    echo Downloading: %%A
    yt-dlp --no-warnings --cookies cookies.txt -f bestaudio -x --audio-format mp3 -o "%folder%\%%(title)s.%%(ext)s" "%%A"
)

echo.
echo Audio downloads completed.
pause
goto MainMenu

:: -------------------------------------
:About
cls
echo ================================================
echo            YouTube Downloader Tool
echo ================================================
echo.
echo This tool uses yt-dlp to download:
echo - Individual videos
echo - Full playlists with optional numbering
echo - Audio-only files
echo.
echo Special thanks to:
echo   >> PSH TRIGGER <<
echo For inspiring and helping to make this tool!
echo.
pause
goto MainMenu
