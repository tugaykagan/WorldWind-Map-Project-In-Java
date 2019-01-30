package proje;
import java.io.File;
// Fig. 24.9: Server.java
// Server that receives and sends packets from/to a client.
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import proje.Proje;
public class Server extends JFrame 
{
   private static Logger logger = Logger.getLogger(Server.class);
   private JTextArea displayArea; // displays packets received
   private DatagramSocket socket; // socket to connect to client
   public static void gonder(String mesaj) {
	   Proje.AppFrame.getMesaj(mesaj);
	   logger.debug("Connection Message Sent As A Parameter For getMesaj Func. In Proje.java ");
   }
   // set up GUI and DatagramSocket
   public Server()
   {
      super( "Server" );
      
      org.apache.log4j.BasicConfigurator.configure();
      String log4jConfigFile = "C:\\Users\\orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\socketserver.properties";
      PropertyConfigurator.configure(log4jConfigFile);
      String resource ="C:\\Users\\orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\socketserver.properties";
      		java.net.URL configFileResource = Client.class.getResource(resource);
      //logger.debug("orkun");
      logger.debug("ServerSide Started");
      PropertyConfigurator.configure(log4jConfigFile);
      displayArea = new JTextArea(); // create displayArea
      add( new JScrollPane( displayArea ), BorderLayout.CENTER );
      setSize( 300, 500 ); // set size of window
      
      
      //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
      java.awt.Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
      int x = (int) rect.getMaxX() - getWidth();
      int y = (int) rect.getMaxY() - getHeight() - 550; //ekranýn neresinde baþlayacaðý
      setLocation(x, y);

      setVisible( true ); // show window
      
      try // create DatagramSocket for sending and receiving packets
      {
         socket = new DatagramSocket(8000 );
      } // end try
      catch ( SocketException socketException ) 
      {
         socketException.printStackTrace();
         logger.fatal("Socket Exception "+socketException);
         System.exit( 1 );
      } // end catch
   } // end Server constructor

   // wait for packets to arrive, display data and echo packet to client
   public void waitForPackets()
   {
      while ( true ) 
      {
         try // receive packet, display contents, return copy to client
         {
            byte data[] = new byte[ 100 ]; // set up packet
            DatagramPacket receivePacket = 
               new DatagramPacket( data, data.length );

            socket.receive( receivePacket ); // wait to receive packet
            logger.debug("Packets Received");
            // display information from received packet 
            displayMessage( new String( receivePacket.getData() ));

         } // end try
         catch ( IOException ioException )
         {
        	 logger.fatal( ioException.toString() + "\n" );
            ioException.printStackTrace();
         } // end catch
      } // end while
   } // end method waitForPackets

 
   // manipulates displayArea in the event-dispatch thread
   private void displayMessage( final String messageToDisplay )
   {
      SwingUtilities.invokeLater(
         new Runnable() 
         {
            public void run() // updates displayArea
            {
               logger.debug("Multithreading Is Working Via Invoke Later For Sending Param. To Func");
               displayArea.append( messageToDisplay ); // display message
               gonder(messageToDisplay);
               
            } // end method run
         } // end anonymous inner class
      ); // end call to SwingUtilities.invokeLater
   } // end method displayMessage
} // end class Server
