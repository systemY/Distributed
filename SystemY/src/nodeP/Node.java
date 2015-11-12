package nodeP;
import java.awt.List;
import java.net.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

import fileManagers.*;
import neworkFunctions.*;
import nodeManager.*;

public class Node 
{	
	TCP tcp=new TCP();
	Multicast multi=new Multicast("228.5.6.7", 6789);
	
	public static void main(String[] args) throws Exception
	{		
		String name="3";
		Node node1=new Node();
		final NodeData nodedata1=new NodeData();
		node1.startNieuweNode(name,nodedata1);
	}
	
	public void startNieuweNode(String nodeNaam,NodeData nodedata1)
	{
		nodedata1.setNodeName(nodeNaam);
		System.out.println("My name is: "+nodedata1.getNodeName());
		System.out.println("My id is: "+nodedata1.getMyNodeID());

		multi.joinMulticastGroup();
		multi.sendMulticast("0"+"-"+nodedata1.getNodeName());
		multi.LeaveMulticast();
		
		setSurroundingNodes(nodedata1);
			
		try 
		{
			RMICommunication rmiCom=new RMICommunication(nodedata1);
			rmiCom.setUpRMI();
		} catch (RemoteException e1) {e1.printStackTrace();}
		ArrayList<Object> threadList = new ArrayList<Object>();
		
		FileDetectionT filedetector =new FileDetectionT(nodedata1);
		filedetector.start();
		threadList.add(filedetector);
		Remover remover =new Remover(nodedata1);
		remover.start();
		threadList.add(remover);
		Receiver receiver = new Receiver(nodedata1);
		receiver.start();
		threadList.add(receiver);
		Sender sender = new Sender(nodedata1);
		sender.start();
		threadList.add(sender);
		NodeDetection nodedetection =new NodeDetection(nodedata1,multi);
		nodedetection.start();
		threadList.add(nodedetection);
		ShutdownT shutdown = new ShutdownT(nodedata1,threadList,multi);
		shutdown.start();
	}
	
	private void setSurroundingNodes(NodeData nodedata1) 
	{
		int numberOfNodes=getNameServerRespons(nodedata1);
		if (numberOfNodes>1)
		{
			String nodes = tcp.receiveTextWithTCP(6770, 5000)[0];
			String[] node = nodes.split("-");
			nodedata1.setPrevNode(Integer.parseInt(node[0]));
			nodedata1.setNextNode(Integer.parseInt(node[1]));
			System.out.println("My: "+nodedata1.getMyNodeID()+" Next: "+nodedata1.getNextNode()+" prev: "+nodedata1.getPrevNode());
		}
		else if(numberOfNodes==1)
		{
			System.out.println("I am the first node");
			 nodedata1.setPrevNode(nodedata1.getMyNodeID());
			 nodedata1.setNextNode(nodedata1.getMyNodeID());
		}
		else if(numberOfNodes==0)
		{
			System.out.println("this node name already exists, please try again with a different name");
			return;
		}
		else
		{
			System.out.println("no nameserver was found");
			return;
		}
		
	}
	
	public int getNameServerRespons(NodeData nodedata1) 
	{
		int nodes=-1;
		String[] received=tcp.receiveTextWithTCP(6790, 5000);
		nodedata1.setNameServerIP(received[1]);
		nodes=Integer.parseInt(received[0]);
		return nodes;
	}
}