/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package stackjgroup;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static stackjgroup.ReplStack.channel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

/**
 *
 * @author Lenovo
 */ 

public class NewCluster extends ReceiverAdapter{
   
public static void main(String[] args) {
        
        ReplStack<String> stack = new ReplStack<>();
        
        try {
            stack.start();
            System.out.println("Available command:");
            System.out.println("/push");
            System.out.println("/pop");
            System.out.println("/top");
            System.out.println("/exit");
            System.out.println("------------------");
            
            Scanner in = new Scanner(System.in);
            boolean on = true;
            while(on) {
                String choice = in.nextLine();               
                switch(choice) {
                    case "/push":
                        System.out.println("Input value to push");
                        String element = in.nextLine();
                        Message msg1 = new Message(null, null, element);
                        channel.send(msg1);
                        stack.push(element);
                        System.out.println(element+" has been pushed");
                        break;
                    case "/pop":
                        if(!stack.isEmpty()) {
                            String value = "pop "+stack.pop();
                            Message msg2 = new Message(null, null, value);
                            channel.send(msg2);
                            System.out.println("You get "+value.substring(4));
                        }
                        else {
                            System.out.println("Stack is empty");
                        }
                        break;
                    case "/top":
                        if(!stack.isEmpty()) {
                            String top = stack.top();
                            Message msg3 = new Message(null, null, top);
                            channel.send(msg3);
                            System.out.println("Top element is "+top);
                        }
                        else {
                            System.out.println("Stack is empty");
                        }
                        break;
                    case "/exit":
                        on = false;
                        System.out.println("Exiting cluster...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again");
                }
            }
            channel.close();
            
        } catch (Exception ex) {
            Logger.getLogger(ReplStack.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
}
