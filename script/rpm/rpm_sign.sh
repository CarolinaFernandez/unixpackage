#!/usr/bin/expect -f
  
### rpm-sign.exp -- Sign RPMs by sending the passphrase.

spawn rpm --addsign {*}$1
expect -exact "Enter pass phrase: "
send -- "$2\r"
expect eof

## end of rpm-sign.exp
