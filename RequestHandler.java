import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

class RequestHandler extends Thread {
    private Socket socket;
    private GameHandler gameHandler;
    Message returnedMessage;
    ObjectInputStream ois;
    ObjectOutputStream oos;
    Player player;

    RequestHandler(Socket socket, GameHandler gameHandler) {
        this.socket = socket;
        this.gameHandler = gameHandler;
        //player = Player();

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("New client connected.");

            //Print the thread id
            System.out.println("Thread ID is: " + getId());

            //Create a new player and assign its id from the thread ID. 
            player = new Player(getId());

            //add the new player to the gamestate
            gameHandler.addPlayerToGame(player, getId());

            //increase the number of players and display amount
            gameHandler.incrementAmountOfPlayers();
            System.out.println("Player amount: " + gameHandler.getNumberOfCurrentPlayers());

            // sleep gives time to client to set up input & output streams
            Thread.sleep(100); 

            while (socket.isConnected()) {
                 /*listen for a message from the client and handle
                 the message logic. Also return new message to be sent
                 to the client */
                 returnedMessage = listenForMessageFromClient();
                 //Send the new messaget to the client. 
                 sendMessageToClient(returnedMessage);
            }

            //oos.close();
       	 	//ois.close();
       	 	//socket.close();
        
        } catch (Exception e) {
        	System.out.println("\nException handled in Request Handler run(), client is disconnected");
            try {
                socket.close(); //close the socket
                Server.removeSocket(socket); //remove socket from clientSocketList
            } catch (IOException e1) {
                System.out.println("Error closing connection");
            }
            System.out.println("Connection closed for player: " + this.getId());
            System.out.println("Server listening for more connectoins...");
        }
    }

    synchronized private void sendMessageToClient(Message gameObj) throws IOException {
        //Send message to the client
        oos.writeObject(gameObj);
    }

    synchronized private Message listenForMessageFromClient() {
        try {
            Message gameObject = (Message) ois.readObject();
            Message returnObject = gameHandler.parseMessage(gameObject, getId());
            return returnObject;
        } catch (SocketException e) {
            //e.printStackTrace();
            Message errorMessage = null;
            try {
                ois.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return errorMessage;
        } catch (IOException e2){
           // e2.printStackTrace();
            Message errorMessage = null;
            try {
                ois.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return errorMessage;
        } catch(ClassNotFoundException e3) {
            e3.printStackTrace();
            Message errorMessage = null;
            try {
                ois.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return errorMessage;
        }
    }

} // Class RequestHandler