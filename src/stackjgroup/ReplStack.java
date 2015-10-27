/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package stackjgroup;

/**
 *
 * @author Lenovo
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

public class ReplStack<T> extends ReceiverAdapter{
    
    private final Stack<T> container = new Stack();
    public static JChannel channel;
    public String userName = System.getProperty("user.name", "n/a");
    
    public void push(T obj) {
        synchronized(container){
            container.push(obj);
        }
    }
    
    public T pop() {
        synchronized(container) {
            return container.pop();
        }
    }
    
    public T top() {
        synchronized(container) {
            return container.peek();   
        }
    }
    
    public boolean isEmpty() {
        return container.empty();
    }
    
    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (container) {
            Util.objectToStream(container, new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        Stack<T> tStack = (Stack<T>) Util.objectFromStream(new DataInputStream(input));
        synchronized (container) {
            container.clear();
            container.addAll(tStack);
        }
    }

    @Override
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }
    
    @Override
    public void receive(Message msg) {
        String response = (String)msg.getObject();
        if(response.startsWith("pop")){
            pop();
        }
        else {
            push((T)response);
        }
    }
    
    public void start() throws Exception{      
        channel=new JChannel(); // use the default config, udp.xml
        channel.setDiscardOwnMessages(true);
        channel.setReceiver(this);
        channel.connect("StackCluster");
        channel.getState(null, 10000);
    }
    
}
