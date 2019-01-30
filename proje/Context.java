package proje;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.NodeList;

import com.sun.corba.se.impl.orbutil.graph.Node;
import com.sun.xml.internal.txw2.Document;

import proje.Proje.AppFrame;

import java.io.File;
import com.sun.javafx.scene.layout.region.Margins.Converter;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Context {
	/** The ContextMenu class implements the context menu. */
    protected static class ContextMenu
    {
        public static final String CONTEXT_MENU_INFO = "ContextMenuInfo";

        protected ContextMenuInfo ctxMenuInfo;
        protected Component sourceComponent;
        protected JMenuItem menuTitleItem;
        protected ArrayList<JMenuItem> menuItems = new ArrayList<JMenuItem>();

        public ContextMenu(Component sourceComponent, ContextMenuInfo contextMenuInfo)
        {
            this.sourceComponent = sourceComponent;
            this.ctxMenuInfo = contextMenuInfo;

            this.makeMenuTitle();
            this.makeMenuItems();
            Proje.logger.debug("ContextMenu Created");
        }

        protected void makeMenuTitle()
        {
            this.menuTitleItem = new JMenuItem(this.ctxMenuInfo.menuTitle);
        }

        protected void makeMenuItems()
        {
            for (ContextMenuItemInfo itemInfo : this.ctxMenuInfo.menuItems)
            {
                this.menuItems.add(new JMenuItem(new ContextMenuItemAction(itemInfo)));
            }
        }

        public void show(final MouseEvent event)
        {
            JPopupMenu popup = new JPopupMenu();

            popup.add(this.menuTitleItem);

            popup.addSeparator();

            for (JMenuItem subMenu : this.menuItems)
            {
                popup.add(subMenu);
            }

            popup.show(sourceComponent, event.getX(), event.getY());
        }
    }

    /** The ContextMenuInfo class specifies the contents of the context menu. */
    protected static class ContextMenuInfo
    {
        protected String menuTitle;
        protected ContextMenuItemInfo[] menuItems;

        public ContextMenuInfo(String title, ContextMenuItemInfo[] menuItems)
        {
            this.menuTitle = title;
            this.menuItems = menuItems;
        }
    }

    /** The ContextMenuItemInfo class specifies the contents of one entry in the context menu. */
    protected static class ContextMenuItemInfo
    {
        protected String displayString;
        public ContextMenuItemInfo(String displayString)
        {
            this.displayString = displayString;
        }
    }

    /** The ContextMenuItemAction responds to user selection of a context menu item. */
    public static class ContextMenuItemAction extends AbstractAction
    {
        protected ContextMenuItemInfo itemInfo;

        public ContextMenuItemAction(ContextMenuItemInfo itemInfo) 
        {
            super(itemInfo.displayString);

            this.itemInfo = itemInfo;
        }

        public void actionPerformed(ActionEvent event) 
        {
        	if(itemInfo.displayString.equals("Set PDP1 Position")){
            	try {
					updateXMLFile(1);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            else if(itemInfo.displayString.equals("Set PDP2 Position")){
            	try {
					updateXMLFile(2);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            else if(itemInfo.displayString.equals("Set PDP3 Position")){
            	try {
					updateXMLFile(3);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        	System.out.println(this.itemInfo.displayString); // Replace with application's menu-item response.
        }
    }
    
    public static void updateXMLFile(int pdpvalue) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException{
    	//try{
    	String adres = "";
		if(pdpvalue == 1) 
		{
			adres = "C:\\Users\\Orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\pdp1.xml";
		}	
		else if(pdpvalue == 2) 
		{
			adres = "C:\\Users\\Orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\pdp2.xml";
		}
		else if(pdpvalue == 3) 
		{
			adres = "C:\\Users\\Orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\pdp3.xml";
		}
		
		Double placemarkLatitude = AppFrame.placemark.getPosition().getLatitude().getDegrees();
		Double placemarkLongitude = AppFrame.placemark.getPosition().getLongitude().getDegrees();
		String name = AppFrame.textFieldPIN.getText();

    	String filepath = adres;
    	DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = docBuilder.parse(filepath);
        org.w3c.dom.Node coordinatesXML = doc.getElementsByTagName("coordinates").item(0);
        
        // update coordinates attribute
        NodeList nodes = coordinatesXML.getChildNodes();
        
        for (int i = 0; i < nodes.getLength(); i++) {
			org.w3c.dom.Node element = nodes.item(i);
			if ("latitude".equals(element.getNodeName())) {
				element.setTextContent(placemarkLatitude.toString());
            }
            if ("longtitude".equals(element.getNodeName())) {
            	element.setTextContent(placemarkLongitude.toString());
            }
            if ("name".equals(element.getNodeName())) {
            	element.setTextContent(name);
            }
		}
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(new DOMSource(doc), new StreamResult(adres));
    }
}
