/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package setjgroup;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

/**
 *
 * @author ndro.tb
 */
public class ReplSet extends ReceiverAdapter {
    JChannel channel;
    private boolean isOnline = true;
    public static Set<String> set = new HashSet();
    
    /**
     * mengembalikan true jika obj ditambahkan,
     * dan false jika obj telah ada pada set
     */
    public boolean add(String obj){
        if(!contains(obj)){
            set.add(obj);
            return true;
        }
        else{
            return false;
        }
    } 
    
    /**
     * mengembalikan true jika obj ada pada set
     */
    public boolean contains(String obj){
        return set.contains(obj);
    }
    
    /**
     * mengembalikan true jika obj ada pada set, dan
     * kemudian obj dihapus dari set. Mengembalikan false
     * jika obj tidak ada pada set
     */
    public boolean remove(String obj){
        if(contains(obj)){
            set.remove(obj);
            return true;
        }
        else{
            return false;
        }
    }
    
    public void print(){
        TreeSet sortedSet = new TreeSet(set);
        System.out.println("Isi dari Set: ");
        System.out.println(sortedSet);
    }
    
    private void start() throws Exception {
        channel=new JChannel(); // use the default config, udp.xml
        channel.setReceiver(this);
        channel.connect("TestSet");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }
    
    private void eventLoop() {
        Scanner in = new Scanner(System.in);
        while(isOnline) {
            try {
                String input = in.nextLine();
                String command = input.split(" ")[0].toLowerCase();
                
                String elemen;
                boolean cek;
                
                Message msg;
                
                switch (command) {
                    case "/print":  // mencetak isi set
                        print();  
                        break;
                    case "/add":
                        elemen = input.substring(5);
                        cek = add(elemen);  // memasukan satu elemen ke dalam set
                        if(cek){
                            msg = new Message(null, null, input);
                            channel.send(msg);
                            System.out.println(cek + ": Elemen '" + elemen + "' baru ditambahkan");
                        } else {
                            System.out.println(cek + ": Elemen '" + elemen + "' sudah ada");
                        }
                        break;
                    case "/remove":
                        elemen = input.substring(8);
                        cek = remove(input.substring(8));   // menghapus satu elemen pada set
                        if(cek){
                            msg = new Message(null, null, input);
                            channel.send(msg);
                            System.out.println(cek + ": Elemen '" + elemen + "' sudah dihapus");
                        } else {
                            System.out.println(cek + ": Elemen '" + elemen + "' tidak ada dalam set");
                        }
                        break;
                    case "/contain":
                        elemen = input.substring(9);
                        cek = contains(elemen);
                        if (cek){
                            System.out.println("Elemen '" + elemen + "' ada pada set");
                        } else {
                            System.out.println("Tidak ada '" + elemen + "' pada set");
                        }
                        break;
                    case "/exit":
                        System.out.println("bye");
                        isOnline = false;
                        break;
                    default:
                        System.out.println("no command");
                }
            }
            catch(Exception e) {
            }
        }
    }
    
    @Override
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    @Override
    public void receive(Message msg) {
        String text= (String) msg.getObject();
        //Integer line = Integer.parseInt(text);
        synchronized(set) {
            System.out.println(msg.getSrc() + ": " + text);
            if (text.startsWith("/add")){
                set.add(text.substring(5));
            } else {
                set.remove(text.substring(8));
            }
            
        }
    }
    
    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized(set) {
            Util.objectToStream(set, new DataOutputStream(output));
        }
    }
    
    @Override
    public void setState(InputStream input) throws Exception {
        Set<String> newSet;
        newSet=(Set<String>)Util.objectFromStream(new DataInputStream(input));
        synchronized(set) {
            set.clear();
            set.addAll(newSet);
        }
        System.out.println("Set memiliki " + newSet.size() + " elemen yang terdiri dari:");
        TreeSet sortedSet = new TreeSet(newSet);
        System.out.println(sortedSet);
    }
    
    public static void main(String[] args) throws Exception {
        new ReplSet().start();
    }
    
}
