#!/usr/bin/expect -f
  
### rpm-sign.exp -- Sign RPMs by sending the passphrase.

set path [lindex $argv 1]
set pass [lindex $argv 2]

#spawn rpmbuild -bb --sign $path
spawn rpm --addsign $path
expect -exact "Enter pass phrase: "
send -- "$pass\r"
expect eof

## end of rpm-sign.exp
