/* 
 * (C) Copyright 2018 by ORKUN DEMIRTAS AND TUGAY KAGAN
 * All Rights Reserved
 * In This Project, Designed Professional WorldWind Map Application
 * The Project Has These Main Features
 * 1. Visualization of Data Layers in WW be user selected (user decide to see them or
 * not).
 * 2. Visualization of WW controls (scale, compass, navigation controls, and map) user
 * selects (user decide to see them or not).
 * 3. When user enters latitude, longitude and altitude, the center of the map will switch to
 * this position.
 * 4. There are 3 predefined positions (PDP). When user selects one of them, the
 * program shall switch to that position (for example: the first PDP position might be the
 * Gazi University Campus, when user selects this PDP your application will show the Gazi
 * University Campus on the screen).
 * 5. Initial PDP positions will be read from â€œpdp1.xmlâ€�, â€œpdp2.xmlâ€�, and â€œpdp3.xmlâ€� files.
 * 6. Center of the map shown with an article (â€˜+â€™).
 * 7. User shall able to change the PDP position to the current position (center of the map).
 * 8. The program logs all the user selections from the GUI component. Using Log4J to log.
 * 9. The program handle and log all required exceptions.
 * 10. The program defined a user-friendly GUI. 
 * ------------------------------------------------------------
 * ADDITIONAL FEATURES
 */
package proje;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.CrosshairLayer;
import gov.nasa.worldwind.layers.GARSGraticuleLayer;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.layertree.LayerTree;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;
import gov.nasa.worldwindx.examples.GazetteerPanel;
import gov.nasa.worldwindx.examples.WMSLayersPanel;
import gov.nasa.worldwindx.examples.ContextMenusOnShapes.ContextMenuItemAction;
import gov.nasa.worldwindx.examples.WMSLayerManager.AppFrame;
import gov.nasa.worldwindx.examples.util.BalloonController;
import gov.nasa.worldwindx.examples.util.HotSpotController;
import gov.nasa.worldwindx.examples.util.ScreenShotAction;
import proje.Context.ContextMenu;
import proje.Context.ContextMenuInfo;
import proje.Context.ContextMenuItemInfo;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;

import com.jogamp.opengl.util.packrect.Level;
import com.sun.javafx.scene.layout.region.Margins.Converter;
import com.sun.xml.internal.txw2.Document;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.FileHandler;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
public class Proje extends ApplicationTemplate
{  // public static FileHandler fh = new FileHandler("D:/Users/orkun/eclipse-workspace/multithreading/src/odev/odev.log");
    protected static final String BROWSER_BALLOON_CONTENT_PATH = "C:\\Users\\orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\developers.html";
    protected static final String SURFACE_POLYGON_IMAGE_PATH = "gov/nasa/worldwindx/examples/images/georss.png";
    public static Logger logger = Logger.getLogger(Proje.class);
    
    public static class AppFrame extends ApplicationTemplate.AppFrame implements SelectListener
    {	
        protected PointPlacemark lastPickedPlacemark = null;
        public static PointPlacemark placemarkPDP1;
        public static PointPlacemark placemarkPDP2;
        public static PointPlacemark placemarkPDP3;
        public static Cone cone;
        protected Layer layerOpenStreetMap;
        protected Component basedLayers;
        public static PointPlacemark placemark;
        protected ViewControlsLayer viewControlsLayer;
        protected static JTextField textFieldPIN;
        protected static JTextField textFieldSphere;
        protected static JTextField textFieldDeleteSphere;
        protected ContextMenuItemInfo[] itemActionNames;
        protected ContextMenuItemInfo[] itemActionNames1;
        protected CompassLayer compassLayer;
        protected ScalebarLayer scaleBarLayer;
        protected WorldMapLayer worldMapLayer;
        protected HotSpotController hotSpotController;
        protected BalloonController balloonController;
        protected static RenderableLayer layer;
        private GARSGraticuleLayer layerGARS;
        private GazetteerPanel panelGazetteer;
        private UserFacingIcon icon;
        private boolean visualizationDataLayers = true;
        private ArrayList<Action> alarmTypes = new ArrayList<Action>();
        protected final Dimension wmsPanelSize = new Dimension(400, 600);
        protected JTabbedPane tabbedPane;
        protected int previousTabIndex;
        protected static final String[] servers = new String[]
        {
            "https://neowms.sci.gsfc.nasa.gov/wms/wms",
            "https://sedac.ciesin.columbia.edu/geoserver/wcs"
        };
        public AppFrame() throws IllegalAccessException, InstantiationException, ClassNotFoundException, IOException
        {
            super(true, false, false);

            org.apache.log4j.BasicConfigurator.configure();
            String log4jConfigFile = "C:\\Users\\orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\log4j.properties";
            PropertyConfigurator.configure(log4jConfigFile);
           
            logger.debug("Application Is Debugging ! ");
            
            this.tabbedPane = new JTabbedPane();

            this.tabbedPane.add(new JPanel());
            this.tabbedPane.setTitleAt(0, "+");
            this.tabbedPane.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent changeEvent)
                {
                	
                    if (tabbedPane.getSelectedIndex() != 0)
                    {
                        previousTabIndex = tabbedPane.getSelectedIndex();
                        return;
                    }

                    String server = JOptionPane.showInputDialog("Enter wms server URL");
                    if (server == null || server.length() < 1)
                    {
                        tabbedPane.setSelectedIndex(previousTabIndex);
                        return;
                    }

                    // Respond by adding a new WMSLayerPanel to the tabbed pane.
                    if (addTab(tabbedPane.getTabCount(), server.trim()) != null)
                        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                    logger.debug("tabbedPane Activated");
                }
            });

            // Create a tab for each server and add it to the tabbed panel.
            for (int i = 0; i < servers.length; i++)
            {
                this.addTab(i + 1, servers[i]); // i+1 to place all server tabs to the right of the Add Server tab
                logger.debug("More Than One tabbedPane Activated");
            }

            // Display the first server pane by default.
            this.tabbedPane.setSelectedIndex(this.tabbedPane.getTabCount() > 0 ? 1 : 0);
            this.previousTabIndex = this.tabbedPane.getSelectedIndex();

            // Add the tabbed pane to a frame separate from the WorldWindow.
            JFrame controlFrame = new JFrame();
            controlFrame.getContentPane().add(tabbedPane);
            controlFrame.pack();
            //controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            controlFrame.setVisible(true);
            controlFrame.setSize(400, 600);
            
            //wms layer end
           
            ////////////////
            this.getWwd().addSelectListener(new BasicDragger((this.getWwd())));
           
            // Create a layer of shapes to drag.
            getWwd().addPositionListener(new PositionListener() {
                @Override
                public void moved(PositionEvent event) {
                    logger.info("Moving On The Map"+getWwd().getCurrentPosition());
                }
            });
            
            viewControlsLayer = new ViewControlsLayer();
            // Find ViewControls layer and keep reference to it
            for (Layer layer : getWwd().getModel().getLayers())
            {
                if (layer instanceof ViewControlsLayer)
                {
                    viewControlsLayer = (ViewControlsLayer) layer;
                    logger.debug("Created ViewControlsLayer");
                }
            }
            insertBeforeCompass(getWwd(), viewControlsLayer);
            
            //compass layer
            for (Layer layer : getWwd().getModel().getLayers())
            {
                if (layer instanceof CompassLayer)
                {
                    compassLayer = (CompassLayer) layer;
                    logger.debug("Created CompassControlsLayer");
                }
            }
            insertBeforeCompass(getWwd(), compassLayer);
            //scalebar layer
            for (Layer layer : getWwd().getModel().getLayers())
            {
                if (layer instanceof ScalebarLayer)
                {
                    scaleBarLayer = (ScalebarLayer) layer;
                    logger.debug("Created ScaleBarLayer");
                }
            }
            insertBeforeCompass(getWwd(), scaleBarLayer);
            //worldMapLayer 
            for (Layer layer : getWwd().getModel().getLayers())
            {
                if (layer instanceof WorldMapLayer)
                {
                    worldMapLayer = (WorldMapLayer) layer;
                    logger.debug("Created WorldMapLayer");
                }
            }
            insertBeforeCompass(getWwd(), worldMapLayer);
            
            //layerGARS lets us the see coordinates on the world surface
            layerGARS = new GARSGraticuleLayer();
            layerGARS.setGraticuleLineColor(Color.WHITE, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_0);
            layerGARS.setGraticuleLineColor(Color.YELLOW, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_1);
            layerGARS.setGraticuleLineColor(Color.GREEN, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_2);
            layerGARS.setGraticuleLineColor(Color.CYAN, GARSGraticuleLayer.GRATICULE_GARS_LEVEL_3);
            layerGARS.set30MinuteThreshold(1200e3);
            layerGARS.set15MinuteThreshold(600e3);
            layerGARS.set5MinuteThreshold(180e3);
            insertBeforePlacenames(this.getWwd(), layerGARS);
            //panelGazetteer lets us to go to a place when write the place's name
            panelGazetteer = new GazetteerPanel(this.getWwd(), null);
            getContentPane().add(panelGazetteer, BorderLayout.NORTH);
            insertAfterPlacenames(getWwd(),new CrosshairLayer()); //artÄ± iÅŸareti
            this.getWwd().addSelectListener(new ViewControlsSelectListener(this.getWwd(), viewControlsLayer));
            
            IconLayer layer = new IconLayer();
            icon = new UserFacingIcon("C:\\Users\\orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\icon.png",
                new Position(Angle.fromDegrees(39.9455), Angle.fromDegrees(32.8611), 0));
            
            icon.setSize(new Dimension(64, 64));
            layer.addIcon(icon);
            ApplicationTemplate.insertAfterPlacenames(this.getWwd(), layer);
            logger.debug("Icon Added");
            getWwd().addSelectListener(this);
            
            this.getContentPane().add(this.makeControlPanel(), BorderLayout.WEST);
            this.setJMenuBar(this.createMenuBar());
            // Add a controller to send input events to BrowserBalloons.
            this.hotSpotController = new HotSpotController(this.getWwd());
            // Add a controller to handle link and navigation events in BrowserBalloons.
            this.balloonController = new BalloonController(this.getWwd());
            // Create a layer to display the balloons.
            this.layer = new RenderableLayer();
            this.layer.setName("Balloons");
            insertBeforePlacenames(getWwd(), this.layer);
            // Add an AnnotationBalloon and a BrowserBalloon to the balloon layer.
            //this.makeAnnotationBalloon();
            this.makeBrowserBalloon();
            logger.debug("BrowserBaloon Activated");
            // Size the World Window to provide enough screen space for the BrowserBalloon
            // And Center the World Window on the screen.
            Dimension size = new Dimension(1200, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
            LayerList list = this.getWwd().getModel().getLayers();
            ListIterator iterator = list.listIterator();
            while (iterator.hasNext())
            {
                layerOpenStreetMap = (Layer) iterator.next();
                if (layerOpenStreetMap.getName().contains("NAIP"))
                {
                    layerOpenStreetMap.setEnabled(true);
                    logger.debug("StreetMap View Activated");
                    break;
                }
            }
            insertBeforeCompass(getWwd(),layerOpenStreetMap);        
            
        }


        private JPanel makeControlPanel() throws IOException
        {   
            /*
             * Our Design Architecture 
             * We Obtain Nearly All We Need
             * Swing Components Inside Of This Function 
            */
        	 // Create bitmaps
           
            // Create Main Control Panel For LayerList
            JPanel controlPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            // Create A Panel That Holds Components Inside Of It
            // And This Panel Is Inside Of The controlPanel
            JPanel alarmsPanel = new JPanel(new GridLayout(0, 1, 0, 0));
            

            // Create A Hiding Button For Markers On The Map            
            JRadioButton hideBtnn = new JRadioButton("Hide Markers etc.", false);
            hideBtnn.setSelected(true);
            hideBtnn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                { 
                    // This Method Is Giving False For Enabling Markers On The Map
                    layer.setEnabled(((JRadioButton) actionEvent.getSource()).isSelected());
                }
            });
            JRadioButton hideBtnGARS = new JRadioButton("Lat-Lon Graticule", false);
            hideBtnGARS.setSelected(true);
            hideBtnGARS.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                { 
                    // This Method Is Giving False For Enabling Markers On The Map
                    layerGARS.setEnabled(((JRadioButton) actionEvent.getSource()).isSelected());
                }
            });
            JRadioButton hideBtnGazetteer = new JRadioButton("Gazetteer", false);
            hideBtnGazetteer.setSelected(true);
            hideBtnGazetteer.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                { 
                    // This Method Is Giving False For Enabling Markers On The Map
                    panelGazetteer.setVisible(((JRadioButton) actionEvent.getSource()).isSelected());
                }
            });
            
            JRadioButton hideBtnStreetMap = new JRadioButton("Open Street Map", false);
            hideBtnStreetMap.setSelected(true);
            hideBtnStreetMap.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                { 
                    // This Method Is Giving False For Enabling Markers On The Map
                    layerOpenStreetMap.setEnabled(((JRadioButton) actionEvent.getSource()).isSelected());
                }
            });
            alarmsPanel.add(hideBtnStreetMap);

            JRadioButton hideBtnCompass = new JRadioButton("Compass", false);
            hideBtnCompass.setSelected(true);
            hideBtnCompass.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                { 
                    // This Method Is Giving False For Enabling Markers On The Map
                    compassLayer.setEnabled(((JRadioButton) actionEvent.getSource()).isSelected());
                }
            });
            
            JRadioButton hideBtnScaleBar = new JRadioButton("Scale Bar", false);
            hideBtnScaleBar.setSelected(true);
            hideBtnScaleBar.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                { 
                    // This Method Is Giving False For Enabling Markers On The Map
                    scaleBarLayer.setEnabled(((JRadioButton) actionEvent.getSource()).isSelected());
                }
            });
            
            JRadioButton hideBtnWorldMap = new JRadioButton("World Map", false);
            hideBtnWorldMap.setSelected(true);
            hideBtnWorldMap.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                { 
                    // This Method Is Giving False For Enabling Markers On The Map
                    worldMapLayer.setEnabled(((JRadioButton) actionEvent.getSource()).isSelected());
                }
            });
            
            // Create A Hiding Button For WW Controls - (The Component In The Bottom Left Of The Map Screen)
            
            // Create A Hiding Button For Control Panel -(For LayerList Components)
            alarmsPanel.add(hideBtnn);
            alarmsPanel.add(hideBtnGARS);
            alarmsPanel.add(hideBtnGazetteer);
            alarmsPanel.add(hideBtnCompass);
            alarmsPanel.add(hideBtnScaleBar);           
            alarmsPanel.add(hideBtnWorldMap);
            alarmsPanel.add(new JLabel("--------------------------------------------------"));
            alarmsPanel.add(new JLabel("Predefined Positions:"));
            JButton gaziUniversityButton = new JButton("PDP1");
            gaziUniversityButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                	
                	if(placemarkPDP1 != null){
                        layer.removeRenderable(placemarkPDP1);
                    }

                    Double latitude = Double.parseDouble(readXMLPutMarkerHeader(1)[0]);
                    Double longtitude = Double.parseDouble(readXMLPutMarkerHeader(1)[1]);
                    String name = readXMLPutMarkerHeader(1)[2]; 
                    Position pos = Position.fromDegrees(latitude, longtitude);
                    placemarkPDP1 = new PointPlacemark(pos);
                    placemarkPDP1.setLabelText(name);
                    placemarkPDP1.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    placemarkPDP1.setVisible(true);
                    layer.addRenderable(placemarkPDP1);
                    getWwd().getView().goTo(pos, 8000);
                }
            });
            alarmsPanel.add(gaziUniversityButton);
            JButton newYorkButton = new JButton("PDP2");
            newYorkButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    //readXMLPutMarker(2);
                	
                	if(placemarkPDP2 != null){
                        layer.removeRenderable(placemarkPDP2);
                    }
                	
                	Double latitude = Double.parseDouble(readXMLPutMarkerHeader(2)[0]);
                    Double longtitude = Double.parseDouble(readXMLPutMarkerHeader(2)[1]);
                    String name = readXMLPutMarkerHeader(2)[2];
                    Position pos = Position.fromDegrees(latitude, longtitude);
                    placemarkPDP2 = new PointPlacemark(pos);
                    placemarkPDP2.setLabelText(name);
                    placemarkPDP2.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    placemarkPDP2.setVisible(true);
                    layer.addRenderable(placemarkPDP2);
                    getWwd().getView().goTo(pos, 8000);
                }
            });
            alarmsPanel.add(newYorkButton);
            JButton hongKongButton = new JButton("PDP3");
            hongKongButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    //readXMLPutMarker(3);
                	
                	if(placemarkPDP3 != null){
                        layer.removeRenderable(placemarkPDP3);
                    }
                	
                	Double latitude = Double.parseDouble(readXMLPutMarkerHeader(3)[0]);
                    Double longtitude = Double.parseDouble(readXMLPutMarkerHeader(3)[1]);
                    String name = readXMLPutMarkerHeader(3)[2];
                    Position pos = Position.fromDegrees(latitude, longtitude);
                    placemarkPDP3 = new PointPlacemark(pos);
                    placemarkPDP3.setLabelText(name);
                    placemarkPDP3.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    placemarkPDP3.setVisible(true);
                    layer.addRenderable(placemarkPDP3);
                    getWwd().getView().goTo(pos, 8000);
                }
            });
            alarmsPanel.add(hongKongButton);
            
            textFieldPIN = new JTextField("Type new PIN name");
            textFieldPIN.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    textFieldPIN.setText("");
                }

                public void focusLost(FocusEvent e) {
                    // nothing
                }
            });
            alarmsPanel.add(textFieldPIN);
            
            

            
            JButton pinBtn = new JButton("Put a PIN");
            pinBtn.addActionListener(new ActionListener()
            {
                
                public void actionPerformed(ActionEvent actionEvent)
                {
                    if(placemark != null){
                        layer.removeRenderable(placemark);
                    }
                    Vec4 screenCoords = getWwd().getView().getCenterPoint();
                    Vec4 cartesian = getWwd().getView().unProject(screenCoords);
                    Globe g=getWwd().getView().getGlobe();
                    Position pos=g.computePositionFromPoint(cartesian);
                    
                    itemActionNames = new ContextMenuItemInfo[]
                        {
                            new ContextMenuItemInfo("Set PDP1 Position"),
                            new ContextMenuItemInfo("Set PDP2 Position"),
                            new ContextMenuItemInfo("Set PDP3 Position"),
                        };
                    placemark = new PointPlacemark(pos);
                    placemark.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                    placemark.setValue(ContextMenu.CONTEXT_MENU_INFO, new ContextMenuInfo("Placemark User Selected", itemActionNames));
                    placemark.setLabelText(textFieldPIN.getText());
                    placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    // This Method Is Giving False For Enabling WW
                    //placemark.setVisible(((JRadioButton) actionEvent.getSource()).isSelected());
                    layer.addRenderable(placemark);
                    
                }
            });
            alarmsPanel.add(pinBtn);
            

            alarmsPanel.add(new JLabel("--------------------------------------------------"));
            
            alarmsPanel.add(new JLabel("3D geometric shape"));
            textFieldSphere = new JTextField("Type new Sphere name");
            textFieldSphere.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                	textFieldSphere.setText("");
                }

                public void focusLost(FocusEvent e) {
                    // nothing
                }
            });
            alarmsPanel.add(textFieldSphere);
            JButton putBtn = new JButton("Put");
            putBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    Vec4 screenCoords = getWwd().getView().getCenterPoint();
                    Vec4 cartesian = getWwd().getView().unProject(screenCoords);
                    Globe g=getWwd().getView().getGlobe();
                    Position pos=g.computePositionFromPoint(cartesian);
                    
                    Ellipsoid sphere = new Ellipsoid(pos, 6000, 6000, 6000);
                    sphere.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    sphere.setImageSources("gov/nasa/worldwindx/examples/images/500px-Checkerboard_pattern.png");
                    sphere.setValue(AVKey.DISPLAY_NAME,textFieldSphere.getText());
                    layer.addRenderable(sphere);
                }
            });
            alarmsPanel.add(putBtn);
            
            textFieldDeleteSphere = new JTextField("Type Sphere name to delete");
            textFieldDeleteSphere.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                	textFieldDeleteSphere.setText("");
                }

                public void focusLost(FocusEvent e) {
                    // nothing
                }
            });
            alarmsPanel.add(textFieldDeleteSphere);
            JButton putDeleteBtn = new JButton("Delete");
            putDeleteBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                	for (Renderable rend : layer.getRenderables())
                    {
                        if (rend instanceof Ellipsoid)
                        {
                        	
                        	String a = (String)((Ellipsoid) rend).getValue(AVKey.DISPLAY_NAME);
                        	String b = textFieldDeleteSphere.getText();


                        		if(a.equals(b)){
                        			layer.removeRenderable((Ellipsoid)rend);
                        		}

                        }
                    }
                }
            });
            alarmsPanel.add(putDeleteBtn);
            
            //VIEW CONTROL PANEL
            alarmsPanel.add(new JLabel("--------------------------------------------------"));
            alarmsPanel.add(new JLabel("View Controls:"));
            JRadioButton hideBtn = new JRadioButton("Hide Control Layer", false);
            hideBtn.setSelected(true);
            hideBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // This Method Is Giving False For Enabling WW
                    viewControlsLayer.setEnabled(((JRadioButton) actionEvent.getSource()).isSelected());
                }
            });
            alarmsPanel.add(hideBtn);
            ButtonGroup group = new ButtonGroup();
            JRadioButton button = new JRadioButton("Horizontal", true);
            group.add(button);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Making Horizontal
                    viewControlsLayer.setLayout(AVKey.HORIZONTAL);
                    getWwd().redraw();
                }
            });
            alarmsPanel.add(button);
            button = new JRadioButton("Vertical", false);
            group.add(button);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Making Vertical
                    viewControlsLayer.setLayout(AVKey.VERTICAL);
                    getWwd().redraw();
                }
            });
            alarmsPanel.add(button);
            // Scale slider - For Making WW As Bigger Or Smaller
            JSlider scaleSlider = new JSlider(1, 20, 10);
            scaleSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    // Making Change On WW Controls
                    viewControlsLayer.setScale(((JSlider) event.getSource()).getValue() / 10d);
                    getWwd().redraw();
                }
            });
            alarmsPanel.add(scaleSlider);
            // Check Boxes - These Checkbox Enabling Functions On WW Controls
            JCheckBox check = new JCheckBox("Pan");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Pan Controls
                    viewControlsLayer.setShowPanControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            alarmsPanel.add(check);
            check = new JCheckBox("Look");
            check.setSelected(false);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Look Controls
                    viewControlsLayer.setShowLookControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            alarmsPanel.add(check);
            check = new JCheckBox("Zoom");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Zoom Controls
                    viewControlsLayer.setShowZoomControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            alarmsPanel.add(check);
            check = new JCheckBox("Heading");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Heading Controls
                    viewControlsLayer.setShowHeadingControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            alarmsPanel.add(check);
            check = new JCheckBox("Pitch");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Pitch Controls
                    viewControlsLayer.setShowPitchControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            alarmsPanel.add(check);
            check = new JCheckBox("Field of view");
            check.setSelected(false);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowFovControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getWwd().redraw();
                }
            });
            alarmsPanel.add(check);
            alarmsPanel.add(new JLabel("--------------------------------------------------"));
            alarmsPanel.add(new JLabel("Enter Coordinates:"));
            // TextFields For Getting External Coordinates From The User
            JTextField textFieldLatitude = new JTextField("Type Latitude");
            textFieldLatitude.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    textFieldLatitude.setText("");
                }

                public void focusLost(FocusEvent e) {
                    // nothing
                }
            });
            alarmsPanel.add(textFieldLatitude);
            JTextField textFieldLongtitude = new JTextField("Type Longtitude");
            textFieldLongtitude.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    textFieldLongtitude.setText("");
                }

                public void focusLost(FocusEvent e) {
                    // nothing
                }
            });
            alarmsPanel.add(textFieldLongtitude);
            JTextField textFieldAltitude = new JTextField("Type Altitude(meter)");
            textFieldAltitude.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    textFieldAltitude.setText("");
                }

                public void focusLost(FocusEvent e) {
                    // nothing
                }
            });
            alarmsPanel.add(textFieldAltitude);
            JButton confirmButton = new JButton("Go To");
            confirmButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // After Pressing confirmButton
                    // That Puts Marker At That Coordinate And Zooming It
                    double Lat = 0,Lon = 0,Alt = 8000;
                    int control = 0;
                    try 
                    {
                        Lat = Double.parseDouble(textFieldLatitude.getText());
                        Lon = Double.parseDouble(textFieldLongtitude.getText());
                        Alt = Double.parseDouble(textFieldAltitude.getText());
                    }
                    catch(NumberFormatException e) {
                    	e.printStackTrace();
                        logger.fatal("NumberFormatException : "+e);
                        control = -1;
                    }
                    catch(InputMismatchException e) {
                    	e.printStackTrace();
                        logger.fatal("InputMisMatchExcepiton : "+e);
                        control = -1;
                        
                    }finally {
                        if(control != -1)
                        	makeUserMarker("User Marker",Lat, Lon,Alt);
                    }
                }
            });
            // Adding Components To The Panel
            alarmsPanel.add(confirmButton);
            
            controlPanel.add(alarmsPanel);
            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.add(controlPanel, BorderLayout.NORTH);
            p.setBorder(new CompoundBorder(new TitledBorder("OT YAZILIM"), new EmptyBorder(20, 10, 20, 10)));
            JPanel p2 = new JPanel(new BorderLayout(10, 10));
            JScrollPane s = new JScrollPane(p);
            p2.add(s);
            p2.setBorder(new EmptyBorder(10, 10, 10, 10));
            logger.debug("ControlPanel Added");
            return p2;
        }
        protected WMSLayersPanel addTab(int position, String server)
        {
            // Add a server to the tabbed dialog.
            try
            {
                WMSLayersPanel layersPanel = new WMSLayersPanel(AppFrame.this.getWwd(), server, wmsPanelSize);
                this.tabbedPane.add(layersPanel, BorderLayout.CENTER);
                String title = layersPanel.getServerDisplayString();
                this.tabbedPane.setTitleAt(position, title != null && title.length() > 0 ? title : server);
                logger.debug("Layers Panel Created");
                return layersPanel;
            }
            catch (URISyntaxException e)
            {
                JOptionPane.showMessageDialog(null, "Server URL is invalid", "Invalid Server URL",
                    JOptionPane.ERROR_MESSAGE);
                logger.error("invalid server URL put! ");
                tabbedPane.setSelectedIndex(previousTabIndex);
                return null;
            }
        }
        @SuppressWarnings("finally")
		private JMenuBar createMenuBar() throws IOException
        {
            // Creating JMenu Component For Saving Snapshot Of The Map
            JMenu menu;
            JMenuBar menuBar = null;
            try {
                menu = new JMenu("File");
                JMenuItem snapItem = new JMenuItem("Save Snapshot...");
                snapItem.addActionListener(new ScreenShotAction(this.getWwd()));
                menu.add(snapItem);
                menuBar = new JMenuBar();
                menuBar.add(menu);
            }finally
            {
            	logger.debug("menubar created");
                return menuBar;
            }
        }
        protected String[] readXMLPutMarkerHeader(int deger) 
        {
        	String name = null;
        	String adres = "";
        	String latitude = "";
        	String longtitude = "";
        	try 
        	{  		
        		if(deger == 1) 
        		{
        			adres = "C:\\Users\\Orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\pdp1.xml";
        			logger.debug("Reading XML File For pdp1");
        		}	
        		else if(deger == 2) 
        		{
        			adres = "C:\\Users\\Orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\pdp2.xml";
        			logger.debug("Reading XML File For pdp2");
        		}
        		else if(deger == 3) 
        		{
        			adres = "C:\\Users\\Orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\pdp3.xml";
        			logger.debug("Reading XML File For pdp3");
        		}
    	    	File file = new File(adres);
    		    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
    		            .newInstance();
    		    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    		    Node document = documentBuilder.parse(file);
    		    latitude = ((org.w3c.dom.Document) document).getElementsByTagName("latitude").item(0).getTextContent();
    		    longtitude = ((org.w3c.dom.Document) document).getElementsByTagName("longtitude").item(0).getTextContent();
    		    name = ((org.w3c.dom.Document) document).getElementsByTagName("name").item(0).getTextContent();
    		    
        	}catch(Exception e)
        	{
        		e.printStackTrace();
                logger.fatal("Exception Has Occurd : "+e);
        	}
        	finally
        	{
        		
        	}
        	String[] values = new String[3];
        	values[0] = latitude;
        	values[1] = longtitude;
        	values[2] = name;
			return values;
        }
        protected void readXMLPutMarker(int deger) 
        {
        	String adres = "";
            String name = "";
            double latitude = 0;
            double longtitude = 0;
            Node document = null;
            // Reading Coordinates From XML File And Putting Markers At There
            try 
            {
                
                if(deger == 1) 
                {
                    adres = "C:\\Users\\Orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\pdp1.xml";
                    logger.debug("Reading XML File For pdp1");
                }   
                else if(deger == 2) 
                {
                    adres = "C:\\Users\\Orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\pdp2.xml";
                    logger.debug("Reading XML File For pdp2");
                }
                else if(deger == 3) 
                {
                    adres = "C:\\Users\\Orkun\\Desktop\\WorldWindJava-develop\\src\\proje\\pdp3.xml";
                    logger.debug("Reading XML File For pdp3");
                }
                File file = new File(adres);
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                        .newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                document = documentBuilder.parse(file);
                latitude = Double.parseDouble(((org.w3c.dom.Document) document).getElementsByTagName("latitude").item(0).getTextContent());
                longtitude = Double.parseDouble(((org.w3c.dom.Document) document).getElementsByTagName("longtitude").item(0).getTextContent());
                
            }catch(SAXException e)
            {
                logger.error("SAXException Error ! : " + e);
            } catch (IOException e) 
            {
            	logger.error("IOException ! : " + e);
            } catch (ParserConfigurationException e) 
            {
            	logger.error("ParserConfigurationException Error ! : " + e);
            }
            finally
            {
            	makeMarker(name,latitude,longtitude);
                logger.info("Added Marker in This Lat : "+(((org.w3c.dom.Document) document).getElementsByTagName("latitude").item(0).getTextContent())+" Ant This Lon : " + (((org.w3c.dom.Document) document).getElementsByTagName("longtitude").item(0).getTextContent()));
                
            }
        }    
        protected void makeMarker(String markerName,double lat,double lon)
        {
            // This Function Puts Markers But Not Zooming At There
            Position balloonPosition = Position.fromDegrees(0, 0);
            try 
            {
                balloonPosition = Position.fromDegrees(lat, lon);
            }
            catch(InputMismatchException e) 
            {
                e.printStackTrace();
                logger.error("InputMisMatchException ! : " + e);
            }
            finally
            {
                PointPlacemark placemark = new PointPlacemark(balloonPosition);
                placemark.setLabelText(markerName);
                this.layer.addRenderable(placemark);
                getWwd().getView().goTo(balloonPosition, 8000);
                logger.info("Made Marker at "+balloonPosition);
            }
            
        }
        public void makeUserMarker(String markerName,double lat,double lon, double alt)
        {
            // This Function Puts Marker And Zooming There
            // Via Getting Coordinates From The User 
            Position balloonPosition = Position.fromDegrees(lat, lon, alt);
            PointPlacemark placemark = new PointPlacemark(balloonPosition);
            placemark.setLabelText(markerName);
            this.layer.addRenderable(placemark);
            Position pos = balloonPosition;
            getWwd().getView().goTo(balloonPosition,alt);
            logger.info("Maded User Marker at "+balloonPosition);
        }

        
        protected void makeAnnotationBalloon()
        {
            Balloon balloon = new ScreenAnnotationBalloon("<b>Designed By</b> Orkun Demirtas & Tugay Kagan",
                new Point(600, 650));
            BalloonAttributes attrs = new BasicBalloonAttributes();
            // Size The Balloon To Fit Its Text, Place Its Lower-Left Corner At The Point, Put Event Padding Between The
            // Balloon's Text And Its Sides, And Disable The Balloon's Leader.
            attrs.setSize(Size.fromPixels(300, 50));
            attrs.setOffset(new Offset(0d, 0d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setInsets(new Insets(10, 10, 10, 10)); // .
            attrs.setLeaderShape(AVKey.SHAPE_NONE);
            // Configure The Balloon's Colors To Display White Text Over A Semi-Transparent Black Background.
            attrs.setTextColor(Color.WHITE);
            attrs.setInteriorMaterial(Material.BLACK);
            attrs.setInteriorOpacity(0.6);
            attrs.setOutlineMaterial(Material.WHITE);
            balloon.setAttributes(attrs);

            this.layer.addRenderable(balloon);
            logger.debug("Made AnnotationBalloon");
        }
        protected void makeBrowserBalloon()
        {
            String htmlString = null;
            InputStream contentStream = null;
            try
            {
                // Read the URL content into a String using the default encoding (UTF-8).
                contentStream = WWIO.openFileOrResourceStream(BROWSER_BALLOON_CONTENT_PATH, this.getClass());
                htmlString = WWIO.readStreamToString(contentStream, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                logger.error("Exception Has Occurd ! : " + e);
            }
            finally
            {
                WWIO.closeStream(contentStream, BROWSER_BALLOON_CONTENT_PATH);
            }

            if (htmlString == null)
                htmlString = Logging.getMessage("generic.ExceptionAttemptingToReadFile", BROWSER_BALLOON_CONTENT_PATH);

            Position balloonPosition = Position.fromDegrees(38.883056, -77.016389);

            // Create a Browser Balloon attached to the globe, and pointing at the NASA headquarters in Washington, D.C.
            // We use the balloon page's URL as its resource resolver to handle relative paths in the page content.
            AbstractBrowserBalloon balloon = new GlobeBrowserBalloon(htmlString, balloonPosition);
            // Size the balloon to provide enough space for its content.
            BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(new Size(Size.NATIVE_DIMENSION, 0d, null, Size.NATIVE_DIMENSION, 0d, null));
            balloon.setAttributes(attrs);

            // Create a placemark on the globe that the user can click to open the balloon.
            PointPlacemark placemark = new PointPlacemark(balloonPosition);
            placemark.setLabelText("Developers Of This Project - Click It");
            // Associate the balloon with the placemark by setting AVKey.BALLOON. The BalloonController looks for this
            // value when an object is clicked.
            placemark.setValue(AVKey.BALLOON, balloon);
            
            this.layer.addRenderable(balloon);
            this.layer.addRenderable(placemark);
            logger.debug("Made BrowserBalloon");
        }
        @Override
        public void selected(SelectEvent event) {
            // TODO Auto-generated method stub
            try
            {
                if (event.getEventAction().equals(SelectEvent.ROLLOVER))
                    highlight(event, event.getTopObject());
                else if (event.getEventAction().equals(SelectEvent.RIGHT_PRESS)) // Could do RIGHT_CLICK instead
                    showContextMenu(event);
                logger.info("BrowserBalloon SelectEvent Working!");
            }
            catch (Exception e)
            {
                Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
                logger.error("Exception Has Occurd ! : " + e);
            }
        }   
        protected void highlight(SelectEvent event, Object o)
        {
            if (this.lastPickedPlacemark == o)
                return; // same thing selected

            // Turn off highlight if on.
            if (this.lastPickedPlacemark != null)
            {
                this.lastPickedPlacemark.setHighlighted(false);
                this.lastPickedPlacemark = null;
            }

            // Turn on highlight if object selected.
            if (o != null && o instanceof PointPlacemark)
            {
                this.lastPickedPlacemark = (PointPlacemark) o;
                this.lastPickedPlacemark.setHighlighted(true);
            }
        } 
        protected void showContextMenu(SelectEvent event)
        {
            if (!(event.getTopObject() instanceof PointPlacemark))
                return;

            // See if the top picked object has context-menu info defined. Show the menu if it does.

            Object o = event.getTopObject();
            if (o instanceof AVList) // Uses an AVList in order to be applicable to all shapes.
            {
                AVList params = (AVList) o;
                ContextMenuInfo menuInfo = (ContextMenuInfo) params.getValue(ContextMenu.CONTEXT_MENU_INFO);
                if (menuInfo == null)
                    return;

                if (!(event.getSource() instanceof Component))
                    return;

                ContextMenu menu = new ContextMenu((Component) event.getSource(), menuInfo);
                menu.show(event.getMouseEvent());
            }
        }
        
        @SuppressWarnings("static-access")
		public static void getMesaj(String mesaj) {
            // TODO Auto-generated method stub
            //JOptionPane.showMessageDialog( null, mesaj ); // display message
            //makeUserMarker("orkunnnn",55,35, 8000);
            // This Function Puts Markers But Not Zooming At There
            String[] LatLong = mesaj.split("//");
            //JOptionPane.showMessageDialog(null,0 + ". line :" + LatLong[0], "title",JOptionPane.PLAIN_MESSAGE);
            double lati = Double.parseDouble(LatLong[0]);
            double longi = Double.parseDouble(LatLong[1]);
            Position balloonPosition = Position.fromDegrees(0, 0);
            try 
            {
                balloonPosition = Position.fromDegrees(lati, longi);
            }
            catch(InputMismatchException e) 
            {
                e.printStackTrace();
                logger.error("InputMisMatchException Has Occurd ! : " + e);
            }
            finally
            {
                PointPlacemark placemark = new PointPlacemark(balloonPosition);
                placemark.setLabelText("Client Marker");
                layer.addRenderable(placemark);
                Proje.AppFrame.getWwd().getView().goTo(balloonPosition, 8000);
                logger.debug("From UDP Connection, The Marker Added And Zoomed.. :)");
              
            }
           
            
        }
    }  
    
    
}
