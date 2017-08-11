/**************************************************************************
 *  Copyright (C) 2017 by Richard Crook                                   *
 *  https://github.com/dazzle50/JPlannerFX                                *
 *                                                                        *
 *  This program is free software: you can redistribute it and/or modify  *
 *  it under the terms of the GNU General Public License as published by  *
 *  the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                   *
 *                                                                        *
 *  This program is distributed in the hope that it will be useful,       *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *  GNU General Public License for more details.                          *
 *                                                                        *
 *  You should have received a copy of the GNU General Public License     *
 *  along with this program.  If not, see http://www.gnu.org/licenses/    *
 **************************************************************************/

package rjc.jplanner.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
import rjc.jplanner.command.UndoStack;
import rjc.jplanner.gui.plan.PlanNotes;
import rjc.jplanner.gui.plan.PlanProperties;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.Plan;

/*************************************************************************************************/
/******************************* Main JPlanner application window ********************************/
/*************************************************************************************************/

public class MainWindow
{
  public static final String       STYLE_ERROR  = "-fx-text-fill: red;";
  public static final String       STYLE_NORMAL = "-fx-text-fill: black;";
  public static String             STYLE_TOOLTIP;
  public static Image              JPLANNER_ICON;                         // icon for all JPlanner Windows

  private Stage                    m_stage;                               // operating system window holding MainWindow
  private MainTabWidget            m_mainTabWidget;                       // MainTabWidget associated with MainWindow
  private Menus                    m_menus      = new Menus();            // menus at top of MainWindow
  private TextField                m_statusBar  = new TextField();        // status bar at bottom of MainWindow
  private UndoStackWindow          m_undoWindow;                          // window to show plan undo-stack
  private ArrayList<MainTabWidget> m_tabWidgets;                          // list of MainTabWidgets including one in MainWindow

  /**************************************** constructor ******************************************/
  public MainWindow( Stage stage )
  {
    // set style for tool tips
    STYLE_TOOLTIP = "-fx-text-fill: black;";
    STYLE_TOOLTIP += "-fx-background-color: lightyellow;";
    STYLE_TOOLTIP += "-fx-padding: 0.2em 1em 0.2em 0.5em;";
    STYLE_TOOLTIP += "-fx-background-radius: 3px;";
    m_mainTabWidget = new MainTabWidget( true, false );

    // arrange main application window layout
    GridPane grid = new GridPane();
    grid.add( m_menus, 0, 0 );
    grid.add( m_mainTabWidget, 0, 1 );
    grid.add( m_statusBar, 0, 2 );
    GridPane.setHgrow( m_mainTabWidget, Priority.ALWAYS );
    GridPane.setVgrow( m_mainTabWidget, Priority.ALWAYS );

    // configure status bar
    m_statusBar.setEditable( false );
    m_statusBar.setFocusTraversable( false );
    m_statusBar.setBackground( new Background( new BackgroundFill( Colors.GENERAL_BACKGROUND, null, null ) ) );
    m_statusBar.setText( "JPlanner started" );

    // construct main application window
    JPLANNER_ICON = new Image( getClass().getResourceAsStream( "jplanner.png" ) );
    Scene scene = new Scene( grid, 763, 530, Colors.GENERAL_BACKGROUND );
    stage.setScene( scene );
    stage.setTitle( "JPlannerFX" );
    stage.show();
    stage.getIcons().add( JPLANNER_ICON );

    // initialise private variables
    m_stage = stage;
    m_tabWidgets = new ArrayList<MainTabWidget>();
    m_tabWidgets.add( m_mainTabWidget );
    getPropertiesPane().updateFromPlan();

    // on minimising to icon, minimise other windows
    stage.iconifiedProperty().addListener( ( observable, wasI, isI ) ->
    {
      for ( MainTabWidget tabs : new ArrayList<MainTabWidget>( m_tabWidgets ) )
        ( (Stage) tabs.getScene().getWindow() ).setIconified( isI );

      if ( m_undoWindow != null )
        m_undoWindow.setIconified( isI );
    } );

    // on close request if undo-stack not clean check if user wants to save
    stage.setOnCloseRequest( event ->
    {
      event.consume();
      quit();
    } );

    // adjust menus on main-tab-widget selected tab change
    m_mainTabWidget.getSelectionModel().selectedItemProperty().addListener( ( observable, oldS, newS ) ->
    {
      if ( newS == m_mainTabWidget.getTasksTab() )
        m_menus.menuTasks.setDisable( false );
      else
        m_menus.menuTasks.setDisable( true );
    } );

    // when focus leaves plan properties or notes, check for any plan updates
    scene.focusOwnerProperty().addListener( ( property, oldNode, newNode ) ->
    {
      if ( isChildNode( m_mainTabWidget.getPlanTab().getPlanProperties(), oldNode ) )
        getPropertiesPane().updatePlan();
      if ( isChildNode( m_mainTabWidget.getPlanTab().getPlanNotes(), oldNode ) )
        getNotesPane().updatePlan();
    } );

    // check for plan updates when focus moves to different window
    stage.focusedProperty().addListener( ( observable, oldFocus, newFocus ) ->
    {
      getPropertiesPane().updatePlan();
      getNotesPane().updatePlan();
    } );
  }

  /**************************************** isChildNode ******************************************/
  public static boolean isChildNode( Parent parent, Node node )
  {
    // return true if node is child of parent or child of a parent child
    ObservableList<Node> children = parent.getChildrenUnmodifiable();
    if ( children.contains( node ) )
      return true;

    for ( Node child : children )
      if ( child instanceof Parent )
        if ( isChildNode( (Parent) child, node ) )
          return true;

    return false;
  }

  /****************************************** message ********************************************/
  public void message( String... msg )
  {
    // display message on status-bar in normal style
    if ( m_statusBar == null )
      JPlanner.trace( "MESSAGE BUT NO STATUS-BAR: ", msg );
    else
    {
      m_statusBar.setStyle( STYLE_NORMAL );
      m_statusBar.setText( String.join( "", msg ) );
    }
  }

  /******************************************** quit *********************************************/
  public void quit()
  {
    // on close request if undo-stack not clean check if user wants to save
    if ( okToProceed( "Do you want to save before closing?" ) )
    {
      // close other windows
      for ( MainTabWidget tabs : new ArrayList<MainTabWidget>( m_tabWidgets ) )
        if ( tabs != m_mainTabWidget )
          tabs.getScene().getWindow().hide();

      // close undo stack window
      if ( m_undoWindow != null )
        m_undoWindow.close();

      // and finally close this window
      m_stage.close();
    }
  }

  /***************************************** okToProceed *****************************************/
  public boolean okToProceed( String msg )
  {
    // if undo-stack not clean, ask user what to do, true to proceed
    checkPlanUpToDate();
    if ( !JPlanner.plan.getUndostack().isClean() )
    {
      ButtonType save = new ButtonType( "Save", ButtonData.YES );
      ButtonType saveAs = new ButtonType( "Save As", ButtonData.NO );
      ButtonType discard = new ButtonType( "Discard", ButtonData.NO );
      ButtonType cancel = new ButtonType( "Cancel", ButtonData.CANCEL_CLOSE );

      Alert dialog = new Alert( AlertType.CONFIRMATION );
      dialog.initOwner( m_stage );
      dialog.setHeaderText( msg );
      dialog.getButtonTypes().setAll( save, saveAs, discard, cancel );

      while ( true )
      {
        Optional<ButtonType> result = dialog.showAndWait();

        if ( result.get() == save && save() ) // save
          return true;

        if ( result.get() == saveAs && saveAs() ) // save as
          return true;

        if ( result.get() == discard && discard() ) // discard
          return true;

        if ( result.get() == cancel ) // cancel
          return false;
      }
    }

    return true;
  }

  /******************************************* discard *******************************************/
  private boolean discard()
  {
    // if no existing file location set or not writable, use default temporary file path
    String path = JPlanner.plan.getFileLocation();
    if ( path == null || path.length() < 1 || !Files.isWritable( Paths.get( path ) ) )
      path = System.getProperty( "java.io.tmpdir" );

    // save to discard.xml
    return save( new File( path, "discarded.xml" ) );
  }

  /******************************************* newPlan *******************************************/
  public boolean newPlan()
  {
    // if undo-stack not clean, ask user what to do
    if ( !okToProceed( "Do you want to save before starting new?" ) )
      return false;

    // create new plan
    JPlanner.plan = new Plan();
    JPlanner.plan.initialise();

    // update gui
    resetGui();
    redrawTaskTables();
    redrawGantts();
    redrawResourceTables();
    redrawCalendarTables();
    redrawDayTypeTables();
    message( "New plan" );
    return true;
  }

  /******************************************** load *********************************************/
  public boolean load()
  {
    // if undo-stack not clean, ask user what to do
    if ( !okToProceed( "Do you want to save before opening new?" ) )
      return false;

    // use file-chooser defaulting if available to current directory
    FileChooser fc = new FileChooser();
    fc.setTitle( "Open plan" );
    File initialDirectory = new File( JPlanner.plan.getFileLocation() );
    if ( initialDirectory.isDirectory() )
      fc.setInitialDirectory( initialDirectory );
    fc.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Plan files (*.xml)", "*.xml" ) );
    File file = fc.showOpenDialog( m_stage );

    // if user cancels file is null, so exit immediately
    if ( file == null )
      return false;

    // attempt to load from user supplied file
    return load( file );
  }

  /******************************************** load *********************************************/
  public boolean load( File file )
  {
    // check file exists
    if ( !file.exists() )
    {
      message( "Could not find '" + file.getPath() + "'" );
      return false;
    }

    // check file can be read
    if ( !file.canRead() )
    {
      message( "Could not read '" + file.getPath() + "'" );
      return false;
    }

    // create temporary plan for loading into
    Plan oldPlan = JPlanner.plan;
    JPlanner.plan = new Plan();

    // attempt to load plan and display-data from XML file
    JPlanner.trace( "Loading '" + file.getPath() + "'" );
    try
    {
      // create XML stream reader
      XMLInputFactory xif = XMLInputFactory.newInstance();
      FileInputStream fis = new FileInputStream( file );
      XMLStreamReader xsr = xif.createXMLStreamReader( fis );

      // check first element is JPlanner
      while ( xsr.hasNext() && !xsr.isStartElement() )
        xsr.next();
      if ( !xsr.isStartElement() || !xsr.getLocalName().equals( XmlLabels.XML_JPLANNER ) )
        throw new XMLStreamException( "Missing JPlanner element" );

      // load plan data
      JPlanner.plan.loadXML( xsr, file.getName(), file.getParent() );

      // if new plan not okay, revert back to old plan
      if ( JPlanner.plan.checkForErrors() != null )
      {
        message( "Plan '" + file.getPath() + "' not valid (" + JPlanner.plan.checkForErrors() + ")" );
        JPlanner.plan = oldPlan;
        fis.close();
        xsr.close();
        return false;
      }

      // load display data
      resetGui();
      loadDisplayData( xsr );
      relayoutTaskTables();
      relayoutResourceTables();
      relayoutCalendarTables();
      relayoutDayTypeTables();
      m_stage.requestFocus();

      fis.close();
      xsr.close();
    }
    catch ( Exception exception )
    {
      // some sort of exception thrown
      message( "Failed to load '" + file.getPath() + "'" );
      JPlanner.plan = oldPlan;
      exception.printStackTrace();
      return false;
    }

    // plan loaded successfully, so schedule
    message( "Successfully loaded '" + file.getPath() + "'" );
    schedule();

    return true;
  }

  /******************************************* saveAs ********************************************/
  public boolean saveAs()
  {
    // use file-chooser defaulting if available to current directory and file-name
    checkPlanUpToDate();
    FileChooser fc = new FileChooser();
    fc.setTitle( "Save plan" );
    File initialDirectory = new File( JPlanner.plan.getFileLocation() );
    if ( initialDirectory.isDirectory() )
      fc.setInitialDirectory( initialDirectory );
    fc.setInitialFileName( JPlanner.plan.getFilename() );
    fc.getExtensionFilters().add( new FileChooser.ExtensionFilter( "Plan files (*.xml)", "*.xml" ) );
    File file = fc.showSaveDialog( m_stage );

    // if user cancels file is null, so exit immediately
    if ( file == null )
      return false;

    // attempt to save to user specified file
    return save( file );
  }

  /******************************************** save *********************************************/
  public boolean save()
  {
    // if no existing filename set, use save-as
    checkPlanUpToDate();
    if ( JPlanner.plan.getFilename() == null || JPlanner.plan.getFilename().length() < 1 )
      return saveAs();

    // attempt to save using existing filename & location
    return save( new File( JPlanner.plan.getFileLocation(), JPlanner.plan.getFilename() ) );
  }

  /******************************************** save *********************************************/
  public boolean save( File file )
  {
    // if file exists already, check file can be written
    if ( file.exists() && !file.canWrite() )
    {
      message( "Could not write to '" + file.getPath() + "'" );
      return false;
    }

    // create XML stream writer to temporary file
    try
    {
      File tempFile = temporaryFile( file );
      XMLOutputFactory xof = XMLOutputFactory.newInstance();
      FileOutputStream fos = new FileOutputStream( tempFile );
      XMLStreamWriter xsw = xof.createXMLStreamWriter( fos, XmlLabels.ENCODING );

      // start XML document
      xsw.writeStartDocument( XmlLabels.ENCODING, XmlLabels.VERSION );
      xsw.writeStartElement( XmlLabels.XML_JPLANNER );
      xsw.writeAttribute( XmlLabels.XML_FORMAT, XmlLabels.FORMAT );
      String saveUser = System.getProperty( "user.name" );
      xsw.writeAttribute( XmlLabels.XML_SAVEUSER, saveUser );
      DateTime saveWhen = DateTime.now();
      xsw.writeAttribute( XmlLabels.XML_SAVEWHEN, saveWhen.toString() );
      xsw.writeAttribute( XmlLabels.XML_SAVENAME, file.getName() );
      xsw.writeAttribute( XmlLabels.XML_SAVEWHERE, file.getParent() );

      // save plan data to stream
      if ( !JPlanner.plan.savePlan( xsw ) )
      {
        message( "Failed to save plan to '" + file.getPath() + "'" );
        return false;
      }

      // save display data to stream
      saveDisplayData( xsw );

      // close XML document
      xsw.writeEndElement(); // XML_JPLANNER
      xsw.writeEndDocument();
      xsw.flush();
      xsw.close();
      fos.close();

      // rename files, and update plan file details
      File backupFile = new File( file.getAbsolutePath() + "~" );
      backupFile.delete();
      file.renameTo( backupFile );
      prettyPrintXML( tempFile, file );
      JPlanner.plan.setFileDetails( file.getName(), file.getParent(), saveUser, saveWhen );
    }
    catch ( XMLStreamException | IOException exception )
    {
      // some sort of exception thrown
      exception.printStackTrace();
      return false;
    }

    // save succeed, so update gui
    getPropertiesPane().updateFromPlan();
    JPlanner.plan.getUndostack().setClean();
    updateWindowTitles();
    message( "Saved plan to '" + file.getPath() + "'" );
    return true;
  }

  /*************************************** prettyPrintXML ****************************************/
  private void prettyPrintXML( File unformatted, File formatted )
  {
    // take XML contents of unindented unformatted file and write out indented to formatted file
    try
    {
      final int SPACE_CHAR = 32;
      final int LINE_FEED = 10;
      final int EOF = -1;

      FileInputStream fis = new FileInputStream( unformatted );
      FileOutputStream fos = new FileOutputStream( formatted );
      int content = fis.read();
      int next;
      int indent = -1;
      boolean inQuotes = false;
      boolean inSeq = false;

      while ( ( next = fis.read() ) != EOF )
      {
        if ( content == '<' && next != '?' && ( next != '/' || inSeq ) )
        {
          if ( next != '/' )
            indent++;
          fos.write( LINE_FEED );
          for ( int count = 0; count < indent; count++ )
          {
            fos.write( SPACE_CHAR );
            fos.write( SPACE_CHAR );
          }
          inSeq = false;
        }

        if ( content == '/' && !inQuotes )
        {
          indent--;
          inSeq = true;
        }

        if ( content == '"' )
          inQuotes = !inQuotes;

        fos.write( content );
        content = next;
      }
      fos.write( content );
      fos.close();
      fis.close();
      unformatted.delete();
    }
    catch ( Exception exception )
    {
      exception.printStackTrace();
    }
  }

  /**************************************** temporaryFile ****************************************/
  private File temporaryFile( File file )
  {
    // return temporary file name based on given file
    String path = file.getParent();
    String name = file.getName();
    int last = name.lastIndexOf( '.' );
    if ( last >= 0 )
      name = name.substring( 0, last ) + DateTime.now().getMilliseconds() + name.substring( last, name.length() );
    else
      name += DateTime.now();

    return new File( path + File.separator + name );
  }

  /*************************************** loadDisplayData ***************************************/
  private void loadDisplayData( XMLStreamReader xsr ) throws XMLStreamException
  {
    // close (done by hiding) all but main window, need to loop around copy to avoid concurrent modification
    for ( MainTabWidget tabs : new ArrayList<MainTabWidget>( m_tabWidgets ) )
      if ( tabs != m_mainTabWidget )
        tabs.getScene().getWindow().hide();

    // read XML display data
    MainTabWidget tabs = null;
    while ( xsr.hasNext() )
    {
      xsr.next();

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_WINDOW:
            if ( tabs == null )
              tabs = m_mainTabWidget;
            else
              tabs = newWindow();

            int tab = 0;

            // read XML attributes
            for ( int i = 0; i < xsr.getAttributeCount(); i++ )
              switch ( xsr.getAttributeLocalName( i ) )
              {
                case XmlLabels.XML_ID:
                  break;
                case XmlLabels.XML_X:
                  tabs.getScene().getWindow().setX( Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_Y:
                  tabs.getScene().getWindow().setY( Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_WIDTH:
                  tabs.getScene().getWindow().setWidth( Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_HEIGHT:
                  tabs.getScene().getWindow().setHeight( Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_TAB:
                  tab = Integer.parseInt( xsr.getAttributeValue( i ) );
                  break;

                default:
                  JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
                  break;
              }

            // set selected tab
            tabs.select( tab );
            break;

          case XmlLabels.XML_UNDOSTACK:
            if ( m_undoWindow == null )
              showUndoStackWindow( false );

            // read XML attributes
            for ( int i = 0; i < xsr.getAttributeCount(); i++ )
              switch ( xsr.getAttributeLocalName( i ) )
              {
                case XmlLabels.XML_VISIBLE:
                  showUndoStackWindow( Boolean.parseBoolean( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_X:
                  m_undoWindow.setX( Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_Y:
                  m_undoWindow.setY( Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_WIDTH:
                  m_undoWindow.setWidth( Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;
                case XmlLabels.XML_HEIGHT:
                  m_undoWindow.setHeight( Integer.parseInt( xsr.getAttributeValue( i ) ) );
                  break;

                default:
                  JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
                  break;
              }
            break;

          case XmlLabels.XML_DISPLAY_DATA:
            break;
          case XmlLabels.XML_TASKS_TABLE:
            tabs.getTasksTab().getTable().loadXML( xsr );
            break;
          case XmlLabels.XML_RESOURCES_TABLE:
            tabs.getResourcesTab().getTable().loadXML( xsr );
            break;
          case XmlLabels.XML_CALENDARS_TABLE:
            tabs.getCalendarsTab().getTable().loadXML( xsr );
            break;
          case XmlLabels.XML_DAYTYPES_TABLE:
            tabs.getDaysTab().getTable().loadXML( xsr );
            break;
          case XmlLabels.XML_GANTT:
            tabs.getTasksTab().getGantt().loadXML( xsr );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }

  }

  /****************************************** resetGui *******************************************/
  private void resetGui()
  {
    // update window titles and plan tab
    updateWindowTitles();
    getPropertiesPane().updateFromPlan();
    getNotesPane().updateFromPlan();

    // reset set all tables, i.e. set to default column widths, row heights, etc
    m_tabWidgets.forEach( tabs -> tabs.getTasksTab().getTable().reset() );
    m_tabWidgets.forEach( tabs -> tabs.getResourcesTab().getTable().reset() );
    m_tabWidgets.forEach( tabs -> tabs.getCalendarsTab().getTable().reset() );
    m_tabWidgets.forEach( tabs -> tabs.getDaysTab().getTable().reset() );

    // reset all gantts to default settings  
    m_tabWidgets.forEach( tabs -> tabs.getTasksTab().getGantt().setDefault() );

    // update undo-stack window if exists
    if ( m_undoWindow != null )
      m_undoWindow.updateScrollBarAndCanvas( true );
  }

  /************************************* relayoutTaskTables **************************************/
  public void relayoutTaskTables()
  {
    // re-layout all tasks tables, needed if number of columns or rows changing etc
    m_tabWidgets.forEach( tabs -> tabs.getTasksTab().getTable().relayout() );
  }

  /*********************************** relayoutResourceTables ************************************/
  public void relayoutResourceTables()
  {
    // re-layout all resources tables, needed if number of columns or rows changing etc
    m_tabWidgets.forEach( tabs -> tabs.getResourcesTab().getTable().relayout() );
  }

  /*********************************** relayoutCalendarTables ************************************/
  public void relayoutCalendarTables()
  {
    // re-layout all calendars tables, needed if number of columns or rows changing etc
    m_tabWidgets.forEach( tabs -> tabs.getCalendarsTab().getTable().relayout() );
  }

  /************************************ relayoutDayTypeTables ************************************/
  public void relayoutDayTypeTables()
  {
    // re-layout all day-type tables, needed if number of columns or rows changing etc
    m_tabWidgets.forEach( tabs -> tabs.getDaysTab().getTable().relayout() );
  }

  /**************************************** redrawGantts *****************************************/
  public void redrawGantts()
  {
    // redraw all plan gantts (does not need calling if redrawing tasks tables)
    m_tabWidgets.forEach( tabs -> tabs.getTasksTab().getGantt().redraw() );
  }

  /************************************** redrawTaskTables ***************************************/
  public void redrawTaskTables()
  {
    // redraw all tasks tables
    m_tabWidgets.forEach( tabs -> tabs.getTasksTab().getTable().redraw() );
  }

  /************************************ redrawResourceTables *************************************/
  public void redrawResourceTables()
  {
    // redraw all resource tables
    m_tabWidgets.forEach( tabs -> tabs.getResourcesTab().getTable().redraw() );
  }

  /************************************ redrawCalendarTables *************************************/
  public void redrawCalendarTables()
  {
    // redraw all calendar tables
    m_tabWidgets.forEach( tabs -> tabs.getCalendarsTab().getTable().redraw() );
  }

  /************************************* redrawDayTypeTables *************************************/
  public void redrawDayTypeTables()
  {
    // redraw all day-type tables
    m_tabWidgets.forEach( tabs -> tabs.getDaysTab().getTable().redraw() );
  }

  /****************************************** schedule *******************************************/
  public void schedule()
  {
    // check plan default-calendar is working before starting re-schedule
    Calendar cal = JPlanner.plan.getDefaultCalendar();
    if ( !cal.isWorking() )
    {
      setError( m_statusBar, "Default calendar '" + cal.getName() + "' has no working periods." );
      return;
    }

    // schedule the plan, then redraw task tables (which also triggers gantt redraws) to show result
    JPlanner.plan.schedule();
    redrawTaskTables();
  }

  /************************************** checkPlanUpToDate **************************************/
  public void checkPlanUpToDate()
  {
    // ensure plan is up-to-date
    if ( m_mainTabWidget.getPlanTab().isSelected() )
    {
      getPropertiesPane().updatePlan();
      getNotesPane().updatePlan();
    }
  }

  /************************************** getPropertiesPane **************************************/
  public PlanProperties getPropertiesPane()
  {
    return m_mainTabWidget.getPlanTab().getPlanProperties();
  }

  /**************************************** getNotesPane *****************************************/
  public PlanNotes getNotesPane()
  {
    return m_mainTabWidget.getPlanTab().getPlanNotes();
  }

  /************************************* updateWindowTitles **************************************/
  private void updateWindowTitles()
  {
    // refresh title on each JPlanner window
    String title = "JPlannerFX " + JPlanner.VERSION;
    if ( JPlanner.plan.getFilename() != null || JPlanner.plan.getFilename().length() > 0 )
    {
      if ( JPlanner.plan.getUndostack().isClean() )
        title = JPlanner.plan.getFilename() + " - " + title;
      else
        title = JPlanner.plan.getFilename() + "* - " + title;
    }

    for ( MainTabWidget tabs : m_tabWidgets )
      ( (Stage) tabs.getScene().getWindow() ).setTitle( title );
  }

  /*************************************** saveDisplayData ***************************************/
  private void saveDisplayData( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // save display data to XML stream
    xsw.writeStartElement( XmlLabels.XML_DISPLAY_DATA );

    // save data for each plan window
    for ( MainTabWidget tabs : m_tabWidgets )
    {
      Window window = tabs.getScene().getWindow();

      xsw.writeStartElement( XmlLabels.XML_WINDOW );
      xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( m_tabWidgets.indexOf( tabs ) ) );
      xsw.writeAttribute( XmlLabels.XML_X, Integer.toString( (int) window.getX() ) );
      xsw.writeAttribute( XmlLabels.XML_Y, Integer.toString( (int) window.getY() ) );
      xsw.writeAttribute( XmlLabels.XML_WIDTH, Integer.toString( (int) window.getWidth() ) );
      xsw.writeAttribute( XmlLabels.XML_HEIGHT, Integer.toString( (int) window.getHeight() ) );
      xsw.writeAttribute( XmlLabels.XML_TAB, Integer.toString( tabs.getSelectionModel().getSelectedIndex() ) );

      tabs.writeXML( xsw );

      xsw.writeEndElement(); // XML_WINDOW
    }

    // save data for undo-stack window
    if ( m_undoWindow != null )
    {
      xsw.writeEmptyElement( XmlLabels.XML_UNDOSTACK );
      xsw.writeAttribute( XmlLabels.XML_VISIBLE, Boolean.toString( m_undoWindow.isShowing() ) );
      xsw.writeAttribute( XmlLabels.XML_X, Integer.toString( (int) m_undoWindow.getX() ) );
      xsw.writeAttribute( XmlLabels.XML_Y, Integer.toString( (int) m_undoWindow.getY() ) );
      xsw.writeAttribute( XmlLabels.XML_WIDTH, Integer.toString( (int) m_undoWindow.getWidth() ) );
      xsw.writeAttribute( XmlLabels.XML_HEIGHT, Integer.toString( (int) m_undoWindow.getHeight() ) );
    }

    xsw.writeEndElement(); // XML_DISPLAY_DATA
  }

  /***************************************** newWindow *******************************************/
  public MainTabWidget newWindow()
  {
    // create new window
    Stage stage = new Stage();
    MainTabWidget newTabWidget = new MainTabWidget( false, true );
    stage.setScene( new Scene( newTabWidget ) );
    stage.setTitle( m_stage.getTitle() );
    stage.show();
    stage.getIcons().add( JPLANNER_ICON );

    // position and size new window
    double w = m_stage.getWidth();
    double h = m_stage.getHeight();
    stage.setX( m_stage.getX() + w / 10 );
    stage.setY( m_stage.getY() + h / 10 );
    stage.setWidth( w / 1.3 );
    stage.setHeight( h / 1.3 );

    // add new MainTabWidget to tracking list
    m_tabWidgets.add( newTabWidget );

    // on close (via hiding) remove from tracking list
    stage.setOnHiding( event -> m_tabWidgets.remove( newTabWidget ) );

    return newTabWidget;
  }

  /************************************* showUndoStackWindow *************************************/
  public void showUndoStackWindow( boolean show )
  {
    // create undo-stack window if not already created
    if ( m_undoWindow == null )
    {
      m_undoWindow = new UndoStackWindow();
      m_undoWindow.showingProperty()
          .addListener( ( observable, oldValue, newValue ) -> m_menus.viewUndoStack.setSelected( newValue ) );
    }

    // make the undo-stack window visible or hidden
    if ( show )
    {
      m_undoWindow.show();
      m_undoWindow.toFront();
      m_undoWindow.makeCurrentIndexVisible();
    }
    else
      m_undoWindow.hide();
  }

  /*************************************** updateUndoRedo ****************************************/
  public void updateUndoRedo()
  {
    // update undo-stack window if visible
    if ( m_undoWindow != null )
      m_undoWindow.updateScrollBarAndCanvas( true );

    // if clean state changed, update window titles
    UndoStack stack = JPlanner.plan.getUndostack();
    if ( stack.getPreviousCleanState() != stack.isClean() )
    {
      updateWindowTitles();
      stack.setPreviousCleanState( stack.isClean() );
    }
  }

  /****************************************** setError *******************************************/
  public void setError( Control control, String errorMessage )
  {
    // update control error state
    if ( errorMessage == null )
    {
      if ( control != null )
      {
        control.setId( null );
        control.setStyle( STYLE_NORMAL );
      }
      message();
    }
    else
    {
      if ( control != null )
      {
        control.setId( JPlanner.ERROR );
        control.setStyle( STYLE_ERROR );
      }
      m_statusBar.setText( errorMessage );
      m_statusBar.setStyle( STYLE_ERROR );
    }

  }

  /******************************************* isError *******************************************/
  public static Boolean isError( Control control )
  {
    // return if control in error state
    return control == null || control.getId() == JPlanner.ERROR;
  }

  /******************************************* getTabs *******************************************/
  public ArrayList<MainTabWidget> getTabs()
  {
    // return list of main tab widgets
    return m_tabWidgets;
  }

}
