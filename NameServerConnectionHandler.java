import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class NameServerConnectionHandler extends Thread implements Serializable  {
	private static ServerSocket server = null;
	static Socket socket = null;
	static ObjectOutputStream outputStream = null;
	static ObjectInputStream inputStream = null;
	static HashMap<Integer, String> data = new HashMap<>();
	static NSInfoHelperClass nsInfo = null;
	NameServerMain nameServer = new NameServerMain();;
	 static Socket fwdSocket = null;
	public NameServerConnectionHandler(NameServerMain nameServer) {
		// TODO Auto-generated constructor stub
		this.nameServer = nameServer;
	}

	public void run() {
		try {
		//	System.out.println("Port for connection : "+nameServer.nsInfo.serverPortForConnection+"\n");
			int port = nameServer.nsInfo.serverPortForConnection;
			server = new ServerSocket(port);
		
		  while(true) {
		    	socket = server.accept();
		    	outputStream = new  ObjectOutputStream(socket.getOutputStream());
				inputStream = new ObjectInputStream(socket.getInputStream());
		    	String nameServerDetails = (String) inputStream.readObject();
				String[] nameServerDetailsStr = nameServerDetails.split(" ");
				System.out.println("Current command : "+nameServerDetailsStr[0]);
				
				String nextServerIP;
				int nextServerListeningPort;
				switch(nameServerDetailsStr[0]) {
				case "entry":
					System.out.println("In server 2");
					int newNSId = Integer.parseInt(nameServerDetailsStr[1]);
					String newNSIP = nameServerDetailsStr[2];
					int newNSListeningPort = Integer.parseInt(nameServerDetailsStr[3]);
					if(newNSId < nameServer.nsInfo.id) {//its in between
						
						outputStream.writeObject(nameServer.nsInfo.serverPortForConnection);//write successor
						outputStream.writeObject(nameServer.nsInfo.predessorPortListning); //write predessor ip
						outputStream.writeObject(nameServer.nsInfo.id);//write successor id
						outputStream.writeObject(nameServer.nsInfo.getPredessorId());//write preccessor id
						outputStream.writeObject(Inet4Address.getLocalHost().getHostAddress()); //write successor ip
						outputStream.writeObject(nameServer.nsInfo.predessorIP); //write predessor ip
						
						for(int key = nameServer.nsInfo.getPredessorId(); key < newNSId; key++) {
							
							if(nameServer.data.containsKey(key)) {
								//System.out.println(key);
								outputStream.writeObject(key);
								outputStream.writeObject(nameServer.data.get(key));
								nameServer.data.remove(key);
							}
					
						}
						nameServer.nsInfo.predessorId = newNSId;
						nameServer.nsInfo.predessorIP = newNSIP;
						nameServer.nsInfo.predessorPortListning = newNSListeningPort;
						outputStream.writeObject(-1);
					}
					else if(newNSId > nameServer.nsInfo.id) {
						//after current server
						nextServerListeningPort = nameServer.nsInfo.successorPortListning;
						 nextServerIP = nameServer.nsInfo.getSuccessorIP();
						int nextServerId = nameServer.nsInfo.getSuccessorId();
						
						//1)if new nameserver is between current server and successor and update the successor of current server
						 if(nameServer.nsInfo.getSuccessorId() > newNSId)
							 nameServer.nsInfo.updateInformation(newNSListeningPort,nameServer.nsInfo.predessorPortListning, newNSId, nameServer.nsInfo.getPredessorId(), newNSIP,nameServer.nsInfo.predessorIP);
						 
					
						 fwdSocket = new Socket(nextServerIP, nextServerListeningPort);
						 
						 ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
						 ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
						 outputStreamFwd.writeObject("entry "+newNSId + " "+ newNSIP + " " + newNSListeningPort);
						
						 int successorPortListning = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(successorPortListning);//send successor port
						 int predessorPortListning = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(predessorPortListning);//send predessor port
						 int successorId = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(successorId);//send sender id
						 int predessorId = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(predessorId);//send predessor id
						 String successorIP = (String) inputStreamFwd.readObject();	
						 outputStream.writeObject(successorIP);//send successor ip
						 String predessorIP = (String) inputStreamFwd.readObject();	
						 outputStream.writeObject(predessorIP);//send predessor ip	
						 
						while(true) {
								
								int key =  (int) inputStreamFwd.readObject();
								outputStream.writeObject(key);
								if(key == -1)
									break;
								
								String value = (String) inputStreamFwd.readObject();
								outputStream.writeObject(value);
								
							}

						 fwdSocket.close();
						 System.out.println("done");
					}
					break;
					
				case "entryAtLast":
					System.out.println("In server 432 entryAtLast");
					
					newNSId = Integer.parseInt(nameServerDetailsStr[1]);
					 newNSIP = nameServerDetailsStr[2];
					 newNSListeningPort = Integer.parseInt(nameServerDetailsStr[3]);
					  
					 nextServerIP = nameServer.nsInfo.getSuccessorIP();
					 nextServerListeningPort = nameServer.nsInfo.successorPortListning;
					  
					if(newNSId > nameServer.nsInfo.id && nameServer.nsInfo.getSuccessorId() != 0)
					{
						fwdSocket = new Socket(nextServerIP, nextServerListeningPort);
						ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
						ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
						outputStreamFwd.writeObject("entryAtLast "+newNSId + " "+ newNSIP + " " + newNSListeningPort);
						
						nextServerListeningPort = (int) inputStreamFwd.readObject();
						int predessorPortListning = (int) inputStreamFwd.readObject();
						int successorID = (int) inputStreamFwd.readObject();
						int PredessorID = (int) inputStreamFwd.readObject();
						nextServerIP = (String) inputStreamFwd.readObject();
						String PredessorIP = (String) inputStreamFwd.readObject();
						
						outputStream.writeObject(nextServerListeningPort);//send successor port
						outputStream.writeObject(predessorPortListning);//send predessor port
						outputStream.writeObject(successorID);//send successor id
						outputStream.writeObject(PredessorID);//send predessor id
						outputStream.writeObject(nextServerIP);//send successor ip
						outputStream.writeObject(PredessorIP);//send predessor ip
						
					}
					else {
						
						outputStream.writeObject(nextServerListeningPort);//send successor port
						outputStream.writeObject(nameServer.nsInfo.predessorPortListning);//send predessor port
						outputStream.writeObject(nameServer.nsInfo.getSuccessorId());//send successor id
						outputStream.writeObject(nameServer.nsInfo.id);//send predessor id
						outputStream.writeObject(nextServerIP);//send successor ip
						outputStream.writeObject(nameServer.nsInfo.predessorIP);//send predessor ip
						
						System.out.println("Successor Id " + nameServer.nsInfo.getSuccessorId());
						//update successor of current server
						nameServer.nsInfo.updateInformation(newNSListeningPort, nameServer.nsInfo.predessorPortListning, newNSId, nameServer.nsInfo.getPredessorId(), newNSIP, nameServer.nsInfo.predessorIP);
						
					}
					break;
					
				case "updateYourPredessorAndTakeAllKeys":
					System.out.println("In Successor to updateYourPredessorAndTakeAllKeys");
					
					int predessorPortListning = (int) inputStream.readObject();//update successor port
					int predessorId = (int) inputStream.readObject();//update successor id
					String predessorIP = (String) inputStream.readObject();//update successor ip
					nameServer.nsInfo.updateInformation(nameServer.nsInfo.successorPortListning, predessorPortListning, nameServer.nsInfo.getSuccessorId(), predessorId,nameServer.nsInfo.successorIP,predessorIP);
					while(true) {
						
						int key =  (int) inputStream.readObject();
						if(key == -1)
							break;
						
						String value = (String) inputStream.readObject();
						nameServer.data.put(key, value);
						
					}
					System.out.println("Updated Informatio successorId" + nameServer.nsInfo.successorId);
					
					break;
				
				case "lookup":
					System.out.println("In 2nd server lookup");
					int key = Integer.parseInt(nameServerDetailsStr[1]);
					String serverTracker = (String) inputStream.readObject();
					String[] value = nameServer.lookup(key,serverTracker).split(" ");	
					if(value.length>1)
						serverTracker = serverTracker.concat("->"+value[1]);
					else
						serverTracker = serverTracker.concat("->"+nameServer.nsInfo.id);
					outputStream.writeObject(value[0]);
					outputStream.writeObject(serverTracker);
					break;
					
				case "delete":
					System.out.println("In 2nd server delete");
					 key = Integer.parseInt(nameServerDetailsStr[1]);
					 String val = nameServer.delete(key);	
					outputStream.writeObject(nameServer.nsInfo.id+"->"+val);
				break;
				
				case "Insert":
					System.out.println("In 2nd server insert");
					 key = Integer.parseInt(nameServerDetailsStr[1]);
					String valueToInsert;
					valueToInsert = nameServerDetailsStr[2];
					val = nameServer.insert(key, valueToInsert);	
					outputStream.writeObject(nameServer.nsInfo.id+"->"+val);
					
				break;
				
				case "updateYourSuccessor":
					System.out.println("In Predessor to updateYourSuccessor");
					int successorPort = (int) inputStream.readObject();//update predessor port
					int successorId = (int) inputStream.readObject();//update predessor id
					String successorIP = (String) inputStream.readObject();//update predessor ip
					nameServer.nsInfo.updateInformation(successorPort, nameServer.nsInfo.predessorPortListning,successorId, nameServer.nsInfo.predessorId, successorIP,nameServer.nsInfo.predessorIP);
				break;
				
				}
		    	
		    }
	} catch (IOException | ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
}
