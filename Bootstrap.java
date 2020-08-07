import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import java.util.*;


public class Bootstrap implements Serializable  {

	private static ServerSocket server;
	 private static int serverPortForConnection;
	 static int count = 0;
	 static Socket socket = null;
	 static Socket fwdSocket = null;
	static ArrayList<Integer> serverIDS = new ArrayList<>();
	HashMap<Integer, String> data = new HashMap<>();
	NSInfoHelperClass nsInfo;
	public Bootstrap(){
		nsInfo = new NSInfoHelperClass(0,serverPortForConnection);
		serverIDS.add(0);
	}
	
	String lookup(int key) throws UnknownHostException, IOException, ClassNotFoundException {
		
		if(data.containsKey(key)) {
			System.out.println("Server Visited 0"  );
			return (data.get(key));
		}
			
		//else contact successor
		//System.out.println(nsInfo.getSuccessorIP());
	//	System.out.println(nsInfo.successorPortListning);
		 fwdSocket = new Socket(nsInfo.getSuccessorIP(), nsInfo.successorPortListning);
		 ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
		 ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
		 outputStreamFwd.writeObject("lookup "+key);
		 outputStreamFwd.writeObject("0");
		 String value = (String) inputStreamFwd.readObject();
		 String serverTracker = (String) inputStreamFwd.readObject();
		 int count = 0;
		 for(int i = 0; i < serverTracker.length(); i++)
		 {
			 if(serverTracker.charAt(i) == '-')
				 count++;
		 }
		 Collections.sort(serverIDS);
			System.out.println("Server Visited : "  );
		 for(int id : serverIDS) {
			 if(count-1 < 0)
				 System.out.println(id);
			 else
				 System.out.print(id + "->");
				
			 count--;
			 if(count< 0)
				 break;
		 }
		 fwdSocket.close();
		return value;
		
	}
	void insert(int key, String value) throws IOException, ClassNotFoundException {
		//check if the key should be in bootstrap
		if(key > Collections.max(serverIDS)) {
			System.out.println("Server Visited 0"  );
			System.out.println("Key Inserted at 0"  );
			data.put(key,value);
		}
			
		else {
		//if no then contact successor
		 fwdSocket = new Socket(nsInfo.getSuccessorIP(), nsInfo.successorPortListning);
		 ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
		 ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
		 outputStreamFwd.writeObject("Insert "+key+" "+value);
		
		 String serverTracker = (String) inputStreamFwd.readObject();
		 int count = 0;
		 for(int i = 0; i < serverTracker.length(); i++)
		 {
			 if(serverTracker.charAt(i) == '-')
				 count++;
		 }
		 Collections.sort(serverIDS);
			System.out.println("Server Visited : "  );
		 for(int id : serverIDS) {
			 if(count-1 < 0)
				 System.out.println(id);
			 else
				 System.out.print(id + "->");
				
			 count--;
			 if(count< 0)
				 break;
		 }
		 fwdSocket.close();
		}
	}
	void delete(int key) throws UnknownHostException, IOException, ClassNotFoundException {
		
		//if key in bootstrap server then dekete
		if(key > Collections.max(serverIDS)) {
			System.out.println("Server Visited 0"  );
			System.out.println("Key deleted at 0"  );
			data.remove(key);
		}
			
		else {
		 fwdSocket = new Socket(nsInfo.getSuccessorIP(), nsInfo.successorPortListning);
		 ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
		 ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
		 outputStreamFwd.writeObject("delete "+key);

		 String serverTracker = (String) inputStreamFwd.readObject();
		 int count = 0;boolean notFound = false;
		 for(int i = 0; i < serverTracker.length(); i++)
		 {
			 if(serverTracker.charAt(i) == '-')
				 count++;
			 else if(serverTracker.charAt(i) == 'N')
				 notFound = true;
		 }
		 if(notFound)
			 System.out.println("Key Not Found");
		 else
			 System.out.println("Deletion Succesful");
		 Collections.sort(serverIDS);
			System.out.print("Server Visited : "  );
		 for(int id : serverIDS) {
			 if(count-1 < 0)
				 System.out.println(id);
			 else
				 System.out.print(id + "->");
				
			 count--;
			 if(count< 0)
				 break;
		 } 
		fwdSocket.close();
		//else check in successor
		}
	}

	
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
	try {
	
		
		serverPortForConnection = 0;
		int id = 0;
    
		File configFile = new File("C:\\Users\\akshay1\\eclipse-workspace\\distributed-4\\bnConfigFile.txt"); 
		
		//read data from config file 	
	
			Scanner sc = new Scanner(configFile);
			id = Integer.parseInt(sc.nextLine());
			serverPortForConnection = Integer.parseInt(sc.nextLine());
			server = new ServerSocket(serverPortForConnection);//server for listing to new name server
			Bootstrap bootstrap = new Bootstrap();
		    while (sc.hasNextLine()) {
				String[] line = sc.nextLine().split(" "); 
				bootstrap.data.put(Integer.parseInt(line[0]),line[1]);
		    }
		    
		    BootstrapUserInteraction UI = new BootstrapUserInteraction(bootstrap);
		    UI.start();
		    int maxServerID = 0;
		    while(true) {
		    	socket = server.accept();
		    	//System.out.println("added new NameServer");
		    	ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
				ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
				String nameServerDetails = (String) inputStream.readObject();
				String[] nameServerDetailsStr = nameServerDetails.split(" ");
				//System.out.println(nameServerDetailsStr[0]);
				int newNSId = 0;
				int newNSListeningPort = 0;
				String newNSIP = "";
				if(!nameServerDetailsStr[0].equals("updateYourPredessorAndTakeAllKeys")) {
					if( !nameServerDetailsStr[0].equals("updateYourSuccessor") && !nameServerDetailsStr[0].equals("updateMaxServerID") ) {
						newNSId = Integer.parseInt(nameServerDetailsStr[1]);
						newNSIP = nameServerDetailsStr[2];
						newNSListeningPort = Integer.parseInt(nameServerDetailsStr[3]);
					}
				
				}
				
				
				
				switch(nameServerDetailsStr[0]) {
				case "entry":
					bootstrap.serverIDS.add(newNSId);
					outputStream.writeObject(Inet4Address.getLocalHost().getHostAddress());
					outputStream.writeObject(serverPortForConnection);
					Collections.sort(bootstrap.serverIDS);
					String serverTracker = "0";
					for(int visitedId : bootstrap.serverIDS)
						if(visitedId < newNSId)
							serverTracker.concat("->"+visitedId);
					
					outputStream.writeObject(serverTracker);
					if(bootstrap.nsInfo.getSuccessorId() == 0)//if only one server intial
					{
						outputStream.writeObject(bootstrap.nsInfo.serverPortForConnection);//succssor port
						outputStream.writeObject(bootstrap.nsInfo.serverPortForConnection);//predessor port
						outputStream.writeObject(bootstrap.nsInfo.id);//sucessor id
						outputStream.writeObject(bootstrap.nsInfo.id);//predessor id
						outputStream.writeObject(Inet4Address.getLocalHost().getHostAddress());//successor ip
						outputStream.writeObject(Inet4Address.getLocalHost().getHostAddress());//predessor ip
						bootstrap.nsInfo.updateInformation(newNSListeningPort, newNSListeningPort, newNSId, newNSId, newNSIP, newNSIP);
						
						//give all the value from 0 to id
						for(int key = 0; key < newNSId; key++) {
							
							if(bootstrap.data.containsKey(key)) {
								//System.out.println(key);
								outputStream.writeObject(key);
								outputStream.writeObject(bootstrap.data.get(key));
								bootstrap.data.remove(key);
							}
					
						}
						outputStream.writeObject(-1);
					}
					else if(maxServerID < newNSId) {//this means bootstrap server has keys for this ns i.e enter at last
						
						System.out.println("Server with greates value");
						bootstrap.nsInfo.predessorId = newNSId; //update predessor of bootstrap
						
						int nextServerListeningPort = bootstrap.nsInfo.successorPortListning;
						String nextServerIP = bootstrap.nsInfo.getSuccessorIP();
							
						fwdSocket = new Socket(nextServerIP, nextServerListeningPort);
						 
						 ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
						 ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
						 outputStreamFwd.writeObject("entryAtLast "+newNSId + " "+ newNSIP + " " + newNSListeningPort);
						 
						 int successorPortListning = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(successorPortListning);//send successor port
						 int predessorPortListning = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(predessorPortListning);//send predessor port
						 int successorId = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(successorId);//send successor id
						 int predessorId = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(predessorId);//send predessor id
						 String successorIP = (String) inputStreamFwd.readObject();	
						 outputStream.writeObject(successorIP);//send sucessor ip
						 String predessorIP = (String) inputStreamFwd.readObject();	
						 outputStream.writeObject(predessorIP);//send predessor ip
						 
						 
						for(int key = maxServerID; key < newNSId; key++) {
							
							if(bootstrap.data.containsKey(key)) {
								//System.out.println(key);
								outputStream.writeObject(key);
								outputStream.writeObject(bootstrap.data.get(key));
								bootstrap.data.remove(key);
							}
						}
						outputStream.writeObject(-1);
					}
					else {//if new ip is between two servers
						
						int nextServerListeningPort = bootstrap.nsInfo.successorPortListning;
						String nextServerIP = bootstrap.nsInfo.getSuccessorIP();
						
						//1)if new nameserver is between current server and successor and update the successor of current server
						 if(bootstrap.nsInfo.getSuccessorId() > newNSId)
							 bootstrap.nsInfo.updateInformation(newNSListeningPort,bootstrap.nsInfo.predessorPortListning, newNSId, bootstrap.nsInfo.predessorId, newNSIP,bootstrap.nsInfo.predessorIP);
						
						//2) if new server is not in between,or even if it is between contact the next nameserver
						 fwdSocket = new Socket(nextServerIP, nextServerListeningPort);
						 
						 ObjectInputStream inputStreamFwd = new ObjectInputStream(fwdSocket.getInputStream());
						 ObjectOutputStream outputStreamFwd = new ObjectOutputStream(fwdSocket.getOutputStream());
						 outputStreamFwd.writeObject("entry "+newNSId + " "+ newNSIP + " " + newNSListeningPort);
						 
						 int successorPortListning = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(successorPortListning);//send successor port
						 int predessorPortListning = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(predessorPortListning);//send predessor port						 
						 int successorId = (int) inputStreamFwd.readObject();
						 outputStream.writeObject(successorId);//send successor id
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
					
					
					outputStream.close();
					inputStream.close();
					socket.close();
						
					break;
					
				case "updateYourPredessorAndTakeAllKeys":
					System.out.println("In Successor to updateYourPredessorAndTakeAllKeys");
					
					int predessorPortListning = (int) inputStream.readObject();//update successor port
					int predessorId = (int) inputStream.readObject();//update successor id
					String predessorIP = (String) inputStream.readObject();//update successor ip
					bootstrap.nsInfo.updateInformation(bootstrap.nsInfo.successorPortListning, predessorPortListning, bootstrap.nsInfo.getSuccessorId(), predessorId,bootstrap.nsInfo.successorIP,predessorIP);
					while(true) {
						
						int key =  (int) inputStream.readObject();
						if(key == -1)
							break;
						
						String value = (String) inputStream.readObject();
						bootstrap.data.put(key, value);
						System.out.println("Key : "+key+" Value : "+value);
						
					}
					System.out.println("Updated Informatio successorId" + bootstrap.nsInfo.successorId);
					
					break;
				
				case "updateYourSuccessor":
					System.out.println("In Predessor to updateYourSuccessor");
					int successorPort = (int) inputStream.readObject();//update predessor port
					int successorId = (int) inputStream.readObject();//update predessor id
					String successorIP = (String) inputStream.readObject();//update predessor ip
					bootstrap.nsInfo.updateInformation(successorPort, bootstrap.nsInfo.predessorPortListning,successorId, bootstrap.nsInfo.predessorId, successorIP,bootstrap.nsInfo.predessorIP);
				break;
				
				case "updateMaxServerID":
					int exitedID = (int) inputStream.readObject();
					bootstrap.serverIDS.remove(Integer.valueOf(exitedID));
					System.out.println("UpdatingMaxServerID..");
					break;
				
				}
				maxServerID = Collections.max(bootstrap.serverIDS);
				System.out.println("BOOTSTRAP SuccessorId : "+bootstrap.nsInfo.successorId + " PredessorId :"+bootstrap.nsInfo.predessorId);
				

		    }
	
		} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
}
