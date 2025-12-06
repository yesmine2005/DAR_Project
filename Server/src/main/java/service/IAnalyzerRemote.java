package service;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAnalyzerRemote extends Remote
{
    String analyzeInput(String code, String language) throws RemoteException;
}


