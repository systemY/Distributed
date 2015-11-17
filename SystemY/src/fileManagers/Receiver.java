package fileManagers;

import neworkFunctions.TCP;
import nodeP.NodeData;

public class Receiver extends Thread 
{
	TCP tcp=new TCP();
	NodeData nodedata1;
	
	public Receiver(NodeData nodedata1)
	{
		super();
		this.nodedata1=nodedata1;
	}
	
	public void run() 
	{
		while(!Thread.interrupted())
		{
			FileData file1=null;
			try 
			{
				file1=nodedata1.receiveQueue.take();
			} 
			catch (InterruptedException e) {return;}
			receiveFile(file1,nodedata1); 
			}
		}
	
	public void receiveFile(FileData file1, NodeData nodedata1) 
	{
        int serverPort = file1.getSourceID()+32768;
        String fileOutput = nodedata1.getMyReplFolder()+"\\"+file1.getFileName();
        boolean fnExists = false;
        for (FileData tempfile : nodedata1.replFiles) 
    	{
        	if(tempfile.getFileName().equals(file1.getFileName()))
        	{
        		//if owner is different add second owner (multiple owners still has to implemented
        		fnExists = true;
       		}
    	}
        if(!fnExists)
        {
        	file1.setFolderLocation(nodedata1.getMyReplFolder());
        	file1.setRemoveAfterSend(false);
        	nodedata1.replFiles.add(file1);
        }
        	tcp.receiveFile(file1.getSourceIP(), serverPort, fileOutput); //TODO if return false --> start failure
    }
}
