####
# A simple script to list the available ports on the current server
# designed to work for Python 3.5
####

from __future__ import print_function
import os
import re
import sys
import socket
from subprocess import check_output

# list of potential port addresses
ports = [8080, 8443, 1503, 1720, 1731, 3283, 5988, 8009]

def checkPortAvailability(port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    result = sock.connect_ex(('127.0.0.1', port))
    print("Post `" + str(port) + "` availability: `" + str(result) + "`")
    if result == 0:
        return True
    else:
        return False

# full command in linux: netstat -lnt | awk '$6 == "LISTEN" && $4 ~ ".445"'
def getUsedPorts():
    output = str(check_output(["netstat", "-lnt"]))
    items_split = [ re.sub(' +',' ', i).split(' ') for i in output.split('\\n')]
    return [int(x[3].split(":")[-1]) for x in items_split if len(x) == 7 and x[5] == 'LISTEN']

def main():
    portsUsed = getUsedPorts()
    filteredPorts = [p for p in ports if p not in portsUsed]
    print("list of open ports: " + str(filteredPorts))

if __name__ == '__main__':
    sys.exit(main())