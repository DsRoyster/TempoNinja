import sys

fn = 'tempoDebug1.log'
if len(sys.argv) > 1:
	fn = sys.argv[1]
ofn = 'tempoDebug.log'

ifi = open(fn, 'r')
ofi = open(ofn, 'w')

ls = ifi.readlines()
for l in ls:
	if l[0].isdigit():
		ofi.write(l)

ifi.close()
ofi.close()