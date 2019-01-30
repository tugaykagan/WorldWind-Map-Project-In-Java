package proje;

import javax.swing.JFrame;

import org.apache.log4j.PropertyConfigurator;

import proje.Proje.AppFrame;

public class Main {
	public static void main(String[] args)
    {   
		
		Proje.logger.debug("Application Perfectly Started");
    	Server application = new Server(); // create server
        ApplicationTemplate.start("OT YAZILIM", AppFrame.class);
        
        //application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        application.waitForPackets(); // run server application
        
        Proje.logger.debug("Server Perfectly Started");
        
    }
}
