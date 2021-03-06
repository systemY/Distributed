package nodeManager;

import java.rmi.RemoteException;

import nodeAgent.AgentMain;
import nodeFileManagers.FileData;

public interface RMICommunicationInt extends java.rmi.Remote
{
	boolean receiveThisFile(FileData file1) throws RemoteException;
	void removeThisOwner(FileData file1) throws RemoteException;
	void rmiFileAgentExecution(AgentMain fileAgent) throws RemoteException;
	void rmiFailAgentExecution(AgentMain failAgent) throws RemoteException;
	boolean sendThisFile(FileData file1) throws RemoteException;
}
