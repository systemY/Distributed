package project;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.Map.Entry;

//TODO thread starten wanneer nieuwe multicast bericht wordt ontvangen
//In deze thread moet het IP adres + naam in map gezet worden en een TCP
//connectie opgezet worden om het aantal nodes terug te geven
public class NameServer extends UnicastRemoteObject implements NameServerInterface
{
	static TreeMap<Integer,String> nodeMap = new TreeMap<Integer,String>();
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) throws IOException{
		String nodeIP=null;
		
		setUpRMI();
		
		MulticastSocket multicastSocket =null;
		InetAddress group = InetAddress.getByName("228.5.6.7");
		multicastSocket = new MulticastSocket(6789);
		multicastSocket.joinGroup(group);
		
		while(true)
		{
		byte[] buffer = new byte[100];
		DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
		multicastSocket.receive(messageIn);//blocks
		
		String msg = new String(messageIn.getData(), messageIn.getOffset(), messageIn.getLength());
		InetAddress addr=messageIn.getAddress();
		nodeIP = addr.getHostAddress().toString();
		System.out.println("Added NodeIP:" + nodeIP);
		NameServer nameserver = new NameServer();
		nameserver.addNode(msg,nodeIP);
		
		Integer numberOfNodes = NameServer.nodeMap.size(); 
		String numOfNodesString = numberOfNodes.toString();
		Socket clientSocket = new Socket(nodeIP,6790);
		DataOutputStream outToNode = new DataOutputStream(clientSocket.getOutputStream());
		outToNode.writeBytes(numOfNodesString + "\n");
		clientSocket.close();
		}
		//multicastSocket.close();
	}

	protected NameServer() throws RemoteException 
	{
		super();
	}

	public static void setUpRMI()
	{
		try{
			System.setProperty("java.security.policy","file:${workspace_loc}/Distributed/SystemY/bin/project/security.policy");
			System.setProperty("java.rmi.server.codebase","file:${workspace_loc}/Distributed/SystemY/bin/project/NameServer.class");
			
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
	}
	

	public void addNode(String nodeName, String nodeIP) throws RemoteException {
		int hashedNN = Math.abs(nodeName.hashCode()%32768);
    	nodeMap.put(hashedNN,nodeIP);
    	
    	System.out.println("************");
    	System.out.println("CURRENT MAP:");
    	for (Entry<Integer, String> entry : nodeMap.entrySet()) 
    	{
    	     System.out.println("Key: " + entry.getKey() + ", NodeIP: " + entry.getValue());
    	}
    	System.out.println("************");
	}

	public void rmNode(String nodeName, String nodeIP) throws RemoteException {
		int hashedNN = Math.abs(nodeName.hashCode()%32768);
		nodeMap.remove(hashedNN);
	}

	public TreeMap<Integer, String> showList() throws RemoteException {

		return (TreeMap<Integer, String>) nodeMap;
	}
	
	public String locateFile(String filename)throws RemoteException
	{
		int hashedFN = Math.abs(filename.hashCode()%32768);
		int destinationKey=nodeMap.lowerKey(hashedFN);
		if (destinationKey==0) destinationKey=nodeMap.lastKey();
		return nodeMap.get(destinationKey);
		
	}
}