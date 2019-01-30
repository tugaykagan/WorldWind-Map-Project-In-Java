package proje;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.print.DocFlavor.URL;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.sun.javafx.geom.Rectangle;
import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;

public class Client extends JFrame 
{
	
	public static Logger logger = Logger.getLogger(Client.class);
private JTextField latitude; // for entering messages
private JTextField longtitude; // for entering messages
private JButton buton; // for entering messages
private JTextArea displayArea; // for displaying messages
private DatagramSocket socket; // socket to connect to server
public static boolean isNumeric(String strNum) {
try {
	String[] LatLong = strNum.split("//");
	 double lati = Double.parseDouble(LatLong[0]);
     double longi = Double.parseDouble(LatLong[1]);
} catch (NumberFormatException | NullPointerException nfe) {
    return false;
}
return true;
}
//set up GUI and DatagramSocket
public Client()
{
 super( "Client" );
 org.apache.log4j.BasicConfigurator.configure();
 String log4jConfigFile = "C:\\Users\\orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\socketclient.properties";
 PropertyConfigurator.configure(log4jConfigFile);
 String resource ="C:\\Users\\orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\socketclient.properties";
 		java.net.URL configFileResource = Client.class.getResource(resource);

 //logger.debug("orkun");
 latitude = new JTextField( "Enter Latitude");
 latitude.addFocusListener(new FocusListener() {
     public void focusGained(FocusEvent e) {
         latitude.setText("");
     }

     public void focusLost(FocusEvent e) {
         // nothing
     }
 });
 longtitude = new JTextField( "Enter Longtitude");
 longtitude.addFocusListener(new FocusListener() {
     public void focusGained(FocusEvent e) {
         longtitude.setText("");
     }

     public void focusLost(FocusEvent e) {
         // nothing
     }
 });
 buton = new JButton("Send");

 buton.addActionListener(
    new ActionListener() 
    { 
       public void actionPerformed( ActionEvent event )
       {
          try // create and send packet
          {
             // get message from textfield 
        	 String lat = latitude.getText();
        	 String lon = longtitude.getText();
        	 
             String message = lat+"//"+lon;
             if(isNumeric(message)) 
             {
            	 displayArea.append( "\nLatitude: " +
                         message + "\n" );

                  byte data[] = message.getBytes(); // convert to bytes
             
                      // create sendPacket
                  DatagramPacket sendPacket = new DatagramPacket( data,data.length, InetAddress.getLocalHost(), 8000 );
                  socket.send( sendPacket ); // send packet
                  displayArea.append( "Packet sent\n" );
                  displayArea.setCaretPosition( 
                  displayArea.getText().length() ); 
             }else 
             {
            	 logger.error("InputMisMatch Has Occurd In ClientSide! : ");
            	 displayMessage( "You Didn't Put Valid Numbers .." + "\n" );
             }
             
          } // end try
          catch ( IOException ioException ) 
          {
             displayMessage( ioException.toString() + "\n" );
             ioException.printStackTrace();
             logger.error("IOException Has Occurd In ClientSide! : "+ioException );
          } // end catch
       } // end actionPerformed
    } // end inner class
 ); // end call to addActionListener


 displayArea = new JTextArea(500,1);
 displayArea.setSize(5, 500);
 //add( new JScrollPane( displayArea ));
 JPanel panel = new JPanel(new GridLayout(2,2));
 panel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

 JPanel asd = new JPanel(new GridLayout(4,2));
 asd.add(latitude);
 asd.add(longtitude);
 asd.add(buton);
 asd.setBackground(new Color(200, 221, 242));
 panel.add(asd);
 panel.add(new JScrollPane( displayArea ));
 
 add(panel);
 setSize( 300, 200 ); // set window size
 setVisible( true ); // show window
 

 //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
 java.awt.Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
 int x = (int) rect.getMaxX() - getWidth();
 int y = (int) rect.getMaxY() - getHeight() - 80; //ekranýn neresinde baþlayacaðý
 setLocation(x, y);

 try // create DatagramSocket for sending and receiving packets
 {
    socket = new DatagramSocket();
 } // end try
 catch ( SocketException socketException ) 
 {
    socketException.printStackTrace();
    logger.error("SocketException Has Occurd In ClientSide! : "+socketException );
    System.exit( 1 );
 } // end catch

} // end Client constructor

//wait for packets to arrive from Server, display packet contents
public void waitForPackets()
{
 while ( true ) 
 {
    try // receive packet and display contents
    {
       byte data[] = new byte[ 100 ]; // set up packet
       DatagramPacket receivePacket = new DatagramPacket( 
          data, data.length );

       socket.receive( receivePacket ); // wait for packet

       // display packet contents
       displayMessage( "\nPacket received:" + 
          "\nFrom host: " + receivePacket.getAddress() + 
          "\nHost port: " + receivePacket.getPort() + 
          "\nLength: " + receivePacket.getLength() + 
          "\nContaining:\n\t" + new String( receivePacket.getData(), 
             0, receivePacket.getLength() ) );
    } // end try
    catch ( IOException exception ) 
    {
       displayMessage( exception.toString() + "\n" );
       exception.printStackTrace();
       logger.error("IOException Has Occurd In ClientSide! : "+exception );
       
    } // end catch
 } // end while
} // end method waitForPackets

//manipulates displayArea in the event-dispatch thread
private void displayMessage( final String messageToDisplay )
{
 SwingUtilities.invokeLater(
    new Runnable()
    {
       public void run() // updates displayArea
       {
          displayArea.append( messageToDisplay );
          logger.debug("Message Perfectly Sent From ClientSide To Server : ");
       } // end method run
    }  // end inner class
 ); // end call to SwingUtilities.invokeLater
} // end method displayMessage
public static void main( String args[] )
{
	
    Client application = new Client(); // create client
    logger.debug("ClientSide Started ");
 application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 application.waitForPackets(); // run client application
} // end main
}  // end class Client
