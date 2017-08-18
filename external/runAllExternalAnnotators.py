####
# A simple script to run all the servers of the external annotators on different ports.
# it first selects a bunch of open ports, and then tries running the modules on the free ports
# designed for Python 2.7
####

from __future__ import print_function
import os
import sys
import socket
from subprocess import check_output

# list of potential port addresses
ports = [8080, 8443, 1503, 1720, 1731, 3283, 5988, 8009]
modules = ["clausie", "path-lstm", "stanford_3.8.0"]

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
    output = check_output(["netstat", "-lnt"])
    items_split = [ re.sub(' +',' ', i).split(' ') for i in output.split('\n')]
    return set([x[3].split(":")[-1] for x in items_split if len(x) == 7 and x[5] == 'LISTEN'])

def main():
    portsUsed = getUsedPorts()
    filteredPorts = [p for p in ports if p in portsUsed]
    assert len(modules) <= len(filteredPorts), "there are less number of open ports to be used for modules"
    for index, elem in enumerate(modules):
        port = filteredPorts[index]
        print("running module `" + elem + "` on port `" + port + "`")
        check_output([elem + "/scripts/runWebserver.sh", "-p", port])

if __name__ == '__main__':
    sys.exit(main())
