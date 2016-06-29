#sbt

evicted

# Build
How run sbt assembly command without tests
```
sbt 'set test in assembly := {}' clean assembly
```


# Starting Docker machine on MAC
$ docker-machine rm default
$ docker-machine create --driver virtualbox default

To configure Docker
docker-machine env default

Build docker image
sbt docker:publishLocal


# GIT
### git diff
show differences between working tree and index, changes you haven't staged to commit
```
git diff [filename]
```

show differences between index and current commit, changes you're about to commit
```
git diff --cached [filename]
```

show differences between working tree and current commit
```
git diff HEAD [filename]
```

### git add
Don’t actually add the file(s), just show if they exist and/or will be ignored.
```
git add . --dry-run
```

### git log
lists the commits made in that repository in reverse chronological order
```
git log
```

shows the difference introduced in each 2 last commit
```
git log -p -2
```

### Discard changes

discard changes in the working copy that are not in the index (not added)
```
git checkout -- .
git checkout path/to/file/to/revert
```

revert to current commit 
(http://stackoverflow.com/questions/927358/how-do-you-undo-the-last-commit)
```
git reset HEAD
```