package Chat;

import java.io.Serializable;


public class ClientInfoSeirialized implements Serializable {

    private final static long serialVersionUID = 1;//Adding a serialVersionUID
    //to the class protects against a problem when new fields being added

    String name; //Client's name

    String msg; //The message that the client wants to send

    String recipient; //Holds other client's name to send a private message
    //or 'all' to send a message to everyone

    boolean showOnline; //If it's true server returns a string with all the 
    //online user

}
