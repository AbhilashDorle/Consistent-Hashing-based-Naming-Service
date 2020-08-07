# Consistent-Hashing-based-Naming-Service

## Introduction
  Consistent Hashing is a distributed hashing scheme that operates independently of the number of servers or objects in a distributed hash table by assigning them a position on an abstract circle, or hash ring. This allows servers and objects to scale without affecting the overall system.

## Bootstrap Name Server
The bootstrap name server is a permanent name server. Its startup signals
the creation of the CH system and its close-down brings the entire system down. It is assumed
that the Bootstrap name server is the first node in the system. In other words, at startup it is
responsible for the entire [0, 1023] range. The Bootstrap name server (bnserver) takes a single command line parameter – the name of
the bootstrap name server configuration file (bnConfigFile). The bnConfigFile has the following
format: ID of the server (which is 0, as it is the Bootstrap name server). Port number on which the Bootstrap server will wait for connections from other servers. A series of initial key value pairs. One pair per line (key and value delimited by blank space).
Upon startup the Bootstrap name server will manage the entire [0, 1023] range until other
servers join the system. The Bootstrap name server will also provide a user interaction thread.

### The user interaction thread will support the following commands.

#### lookup key : 
retrieves the value corresponding to the given key (if the key is in the
system). If the given key is not in the system, “Key not found” is printed. In addition to
the value, this commands also printout the sequence of server IDs that were contacted
and the ID of the server from which the final response was obtained.

#### Insert key value 
 Insert the key value pair into the system. The command should print out the ID of the server into which the key value pair was inserted and the sequence of server IDS that were contacted.
####  delete key 
 Delete the key value pair corresponding to the given key. If successfully deleted, “Successful deletion” should be printed. If key was not in the system “Key not found” should be printed. In addition, the sequence of server IDs that were contacted in
the deletion process should be printed.

## Name Server
These are non-permanent servers in the system. Each name server has an ID (specified in the configuration file). Each name server is responsible for maintaining the key, value pairs for a specific range of keys as specified in the consistent hashing algorithm. Each name server knows its predecessor and successor name servers (i.e., it maintains the IP address and port number of its predecessor and successor name servers). The Name server (nameserver) takes a single command line parameter – the name of the name
server configuration file (nsConfigFile). The nsConfigFile has the following format:ID of this name server (number in [1, 1023] range) Port number on which this server will wait for connections from
other servers.IP address and port number of the Bootstrap server (delimited byspace).Each name server will also provide a user interaction thread. 
### The user interaction thread will support the following commands.
#### enter 
 the name server will enter into the system. This process is initiated by first
contacting the Bootstrap server. It will figure out the range of the keys that it should maintain
by following the consistent hashing protocol (i.e., traversing the existing name servers in clockwise direction). It will acquire the key value pairs corresponding to its range from the name
server that will become its successor name server. Once the entire entry procedure is complete,
“successful entry” message is printed out. In addition, the following information is printed out:
(1) The range of keys that will be managed by this server; (2) the ID of the predecessor and
successor name servers; (3) the IDs of the servers that were traversed during the entry process.
####  exit
 the name server will gracefully exit the system. The name server will inform its
successor and predecessor name servers. It will hand over the key value pairs that it was
maintaining to the successor. Upon successful exit, the server will print “Successful exit”
message. It will also print out the ID of the successor and the key range that was handed over.
