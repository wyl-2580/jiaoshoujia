@echo off
chcp 65001 >nul
cd /d "e:\Projects\jiaoshoujia"

set GIT_AUTHOR_NAME=wyl
set GIT_AUTHOR_EMAIL=15631511175@163.com
set GIT_COMMITTER_NAME=wyl
set GIT_COMMITTER_EMAIL=15631511175@163.com

git add -A

for /f "delims=" %%i in ('git write-tree') do set TREE=%%i
echo Initial commit| git commit-tree %TREE% > newcommit.txt
for /f "delims=" %%j in (newcommit.txt) do set NEWCOMMIT=%%j
git update-ref refs/heads/main %NEWCOMMIT%
del newcommit.txt

git push --force origin main

echo Done: %NEWCOMMIT%
