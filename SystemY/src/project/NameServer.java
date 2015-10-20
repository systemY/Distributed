package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.Map.Entry; 


public class NameServer extends UnicastRemoteObject implements NameServerInterface{
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) throws IOException{
		try{
			System.setProperty("java.security.policy","file:$git/Distributed/SystemY/bin/project/security.policy");
			System.setProperty("java.rmi.server.codebase","file:$git/Distributed/SystemY/bin/project/NameServer.class");
			LocateRegistry.createRegistry(1099);
			NameServerInterface nameint = new NameServer();
			Naming.rebind("//localhost/NameServer", nameint);
			
			System.out.println("NameServer is ready.");
			}
			catch(Exception e)
			{
			System.out.println("NameServer error: " + e.getMessage());
			e.printStackTrace();
			}
		MulticastSocket multiastSocket =null;
		
		InetAddress group = InetAddress.getByName("228.5.6.7");
		multiastSocket = new MulticastSocket(6789);
		multiastSocket.joinGroup(group);
		byte[] buffer = new byte[10];
		for(int i=0; i< 2;i++)	//receive 3 messages
		 {					
			//TODO fix da de lengte van de byte array exact is wa we gaan ontvangen
			
			// get messages from others in group
			DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
			multiastSocket.receive(messageIn);
			if (i==0)
			{
				System.out.println("Received:" + new String(messageIn.getData()));
			}
			else if (i==1)
			{
				InetAddress addr = InetAddress.getByAddress(messageIn.getData());
			}
			
			
		}
		multiastSocket.close();
	}

	protected NameServer() throws RemoteException 
	{
		super();
	}

	TreeMap<Integer,InetAddress> nodeMap = new TreeMap<Integer,InetAddress>();
	

	public void addNode(String nodeName, InetAddress nodeIP) throws RemoteException {
		int hashedNN = Math.abs(nodeName.hashCode()%32768);
    	nodeMap.put(hashedNN,nodeIP);
    	

    	System.out.print("CURRENT MAP");
    	for (Entry<Integer, InetAddress> entry : nodeMap.entrySet()) 
    	{
    	     System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue());
    	}
    	System.out.print("***");
	}

	public void rmNode(String nodeName, InetAddress nodeIP) throws RemoteException {
		int hashedNN = Math.abs(nodeName.hashCode()%32768);
		nodeMap.remove(hashedNN);
	}

	public TreeMap<Integer, InetAddress> showList() throws RemoteException {
		// TODO Auto-generated method stub
		return (TreeMap<Integer, InetAddress>) nodeMap;
	}
	
	public InetAddress locateFile(String filename)throws RemoteException
	{
		int hashedFN = Math.abs(filename.hashCode()%32768);
		int destinationKey=nodeMap.lowerKey(hashedFN);
		if (destinationKey==0) destinationKey=nodeMap.lastKey();
		return nodeMap.get(destinationKey);
		
	}
}