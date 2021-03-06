package nodeManager;

import java.net.DatagramPacket;
import java.net.InetAddress;

import networkFunctions.TCP;
import nodeFileManagers.FileOwnershipT;
import nodeStart.NodeData;

public class NodeOrderThread extends Thread {
	TCP tcp=new TCP();
	DatagramPacket messageIn;
	NodeData nodedata1;
	int myNodeID;
	int myPrevNode;
	int myNextNode;
	String myNextNodeIP;
	public NodeOrderThread(DatagramPacket messageIn, NodeData nodedata1)
	{
		this.messageIn=messageIn;
		this.nodedata1=nodedata1;
		myNodeID=nodedata1.getMyNodeID();
		myPrevNode=nodedata1.getPrevNode();
		myNextNode=nodedata1.getNextNode();
		myNextNodeIP=nodedata1.getNextNodeIP();
	}

	public void run() {
		String msg = new String(messageIn.getData(), messageIn.getOffset(), messageIn.getLength());
		InetAddress addr=messageIn.getAddress();
		String nodeIP = addr.getHostAddress().toString();
		String[] msgs = msg.split("-");
		int toLeave = Integer.parseInt(msgs[0]);
		int newNodeID= Integer.parseInt(msgs[1]);
		
		if(toLeave == 1||toLeave ==2)
		{
			//Strings van ID's naar int parsen
			int newPrevID=Integer.parseInt(msgs[2]);
			int newNextID=Integer.parseInt(msgs[3]);
			String newPrevIP=msgs[4];
			String newNextIP=msgs[5];
			
			if(myPrevNode == newNodeID)
			{
				nodedata1.setPrevNode(newPrevID);
				nodedata1.setPrevNodeIP(newPrevIP);
			}
			//if mynext == his id => mynext to his next
			if(myNextNode == newNodeID)
			{
				nodedata1.setNextNode(newNextID);
				nodedata1.setNextNodeIP(newNextIP);
				//telkens als setNextNodeIP opgeroepen wordt zal FileOwnershipT gestart worden
				//wanneer de next node veranderd kan het namelijk zijn dat de bestanden een nieuwe
				//replicatie eigenaar moeten hebben
			}
			if(newNodeID==nodedata1.getMyNodeID())
			{
				System.exit(1);
			}
			if(myNextNode==nodedata1.getMyNodeID())
			{
				nodedata1.allNetworkFiles.clear();
				nodedata1.setChanged(true);
			}
			if (toLeave==1)
			{
				FileOwnershipT COT =new FileOwnershipT(nodedata1,5,newNodeID,newPrevIP,newPrevID);
				COT.start();
			}
		}
		//adding new node
		else if (toLeave==0)
		{
			if (myNodeID == newNodeID || newNodeID==myNextNode ||newNodeID==myPrevNode )
			{
				System.out.println("the new node already existed and won't be added");
			}
			else if(myPrevNode == myNextNode && myNextNode == myNodeID)
			{
				tcp.sendTextWithTCP(myNodeID+"-"+myNodeID+"-"+myNextNodeIP, nodeIP, 6770);
				nodedata1.setPrevNode(newNodeID);
				nodedata1.setPrevNodeIP(nodeIP);
				nodedata1.setNextNode(newNodeID);
				nodedata1.setNextNodeIP(nodeIP);
				System.out.println("I am the previous and next of the new node (second)");
			}
			
			
			else if(myNodeID < newNodeID && newNodeID < myNextNode)
			{
				tcp.sendTextWithTCP(myNodeID+"-"+myNextNode+"-"+myNextNodeIP, nodeIP, 6770);
				nodedata1.setNextNode(newNodeID);
				nodedata1.setNextNodeIP(nodeIP);
				System.out.println("I am the previous of the new node (middle)");
			}
			else if( myNodeID > newNodeID && newNodeID > myPrevNode)
			{
				nodedata1.setPrevNode(newNodeID);	
				nodedata1.setPrevNodeIP(nodeIP);
				System.out.println("I am the next of the new node (middle)");
			}

			else if(myNodeID>myPrevNode && myNodeID>myNextNode)
			{
				//potential prev of new node
				if (newNodeID>myNodeID && newNodeID>myPrevNode && newNodeID>myNextNode)
				{
					tcp.sendTextWithTCP(myNodeID+"-"+myNextNode+"-"+myNextNodeIP, nodeIP, 6770);
					nodedata1.setNextNode(newNodeID);
					nodedata1.setNextNodeIP(nodeIP);
					System.out.println("I am the previous of the new node (end)");
				}
				else if (newNodeID<myNodeID && newNodeID<myPrevNode && newNodeID<myNextNode)
				{
					tcp.sendTextWithTCP(myNodeID+"-"+myNextNode+"-"+myNextNodeIP, nodeIP, 6770);
					nodedata1.setNextNode(newNodeID);
					nodedata1.setNextNodeIP(nodeIP);
					System.out.println("I am the previous of the new node (begin)");
				}
				
			}
			else if(myNodeID<myPrevNode && myNodeID<myNextNode)
			{
				//potential next of new node
				if (newNodeID>myNodeID && newNodeID>myPrevNode && newNodeID>myNextNode)
				{
					nodedata1.setPrevNode(newNodeID);
					nodedata1.setPrevNodeIP(nodeIP);
					System.out.println("I am the next of the new node (end)");
				}
				else if (newNodeID<myNodeID && newNodeID<myPrevNode && newNodeID<myNextNode)
				{
					nodedata1.setPrevNode(newNodeID);
					nodedata1.setPrevNodeIP(nodeIP);
					System.out.println("I am the next of the new node (begin)");
				}	
			}
			FileOwnershipT COT =new FileOwnershipT(nodedata1,4,newNodeID,nodeIP,0);
			COT.start();
		}
		System.out.println("My: "+nodedata1.getMyNodeID()+" Next: "+nodedata1.getNextNode()+" prev: "+nodedata1.getPrevNode());
	}
}
