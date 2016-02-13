/**************************************************************************
 *  Copyright (C) 2016 by Richard Crook                                   *
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
import java.util.ArrayList;
import java.util.Optional;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
import rjc.jplanner.gui.plan.PlanNotes;
import rjc.jplanner.gui.plan.PlanProperties;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.Plan;

/*************************************************************************************************/
/******************************* Main JPlanner application window ********************************/
/*************************************************************************************************/

public class MainWindow
{
  public static final Color        COLOR_GENERAL_BACKGROUND = Color.rgb( 240, 240, 240 );

  private Stage                    m_stage;
  private MainTabWidget            m_mainTabWidget          = new MainTabWidget();       // MainTabWidget associated with MainWindow
  private MenuBar                  m_menus                  = new Menus();
  private TextField                m_statusBar              = new TextField();
  private UndoStackWindow          m_undoWindow;                                         // window to show plan undo-stack
  private ArrayList<MainTabWidget> m_tabWidgets;                                         // list of MainTabWidgets including one in MainWindow

  /**************************************** constructor ******************************************/
  public MainWindow( Stage stage )
  {
    // arrange main application window layout
    GridPane grid = new GridPane();
    grid.add( m_menus, 0, 0 );
    grid.add( m_mainTabWidget, 0, 1 );
    grid.add( m_statusBar, 0, 2 );
    GridPane.setHgrow( m_mainTabWidget, Priority.ALWAYS );
    GridPane.setVgrow( m_mainTabWidget, Priority.ALWAYS );

    // configure status bar
    m_statusBar.setEditable( false );
    m_statusBar.setBackground( new Background( new BackgroundFill( COLOR_GENERAL_BACKGROUND, null, null ) ) );
    m_statusBar.setText( "JPlanner started" );

    // construct main application window
    Scene scene = new Scene( grid, 800, 500, COLOR_GENERAL_BACKGROUND );
    stage.setScene( scene );
    stage.setTitle( "JPlannerFX" );
    stage.show();

    // initialise private variables
    m_stage = stage;
    m_tabWidgets = new ArrayList<MainTabWidget>();
    m_tabWidgets.add( m_mainTabWidget );

    // on close request also close (via hide) other windows
    stage.setOnCloseRequest( event ->
    {
      for ( MainTabWidget tabs : m_tabWidgets )
        if ( tabs != m_mainTabWidget )
          tabs.getScene().getWindow().hide();

      if ( m_undoWindow != null )
        m_undoWindow.close();
    } );

  }

  /****************************************** message ********************************************/
  public void message( String msg )
  {
    // display message on status-bar
    if ( m_statusBar == null )
      JPlanner.trace( "MESSAGE BUT NO STATUS-BAR: " + msg );
    else
      m_statusBar.setText( msg );
  }

  /******************************************** load *********************************************/
  public boolean load()
  {
    // if undo-stack not clean, ask user what to do
    if ( !JPlanner.plan.undostack().isClean() )
    {
      boolean ask = true;
      while ( ask )
      {
        ButtonType save = new ButtonType( "Save", ButtonData.YES );
        ButtonType discard = new ButtonType( "Discard", ButtonData.NO );
        ButtonType cancel = new ButtonType( "Cancel", ButtonData.CANCEL_CLOSE );

        Alert dialog = new Alert( AlertType.CONFIRMATION );
        dialog.setTitle( "Open plan" );
        dialog.setHeaderText( "Do you want to save before opening new?" );
        dialog.getButtonTypes().setAll( save, discard, cancel );
        Optional<ButtonType> result = dialog.showAndWait();

        if ( result.get() == save ) // save
          ask = !saveAs();

        if ( result.get() == discard ) // discard
          ask = false;

        if ( result.get() == cancel ) // cancel
          return false;
      }
    }

    // use file-chooser to ask user which file to be opened
    FileChooser fc = new FileChooser();
    fc.setTitle( "Open plan" );
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
      if ( JPlanner.plan.errors() != null )
      {
        message( "Plan '" + file.getPath() + "' not valid (" + JPlanner.plan.errors() + ")" );
        JPlanner.plan = oldPlan;
        fis.close();
        xsr.close();
        return false;
      }

      // load display data
      resetGui();
      loadDisplayData( xsr );

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
    // use file-chooser to ask user which file to save to
    FileChooser fc = new FileChooser();
    fc.setTitle( "Save plan" );
    File file = fc.showOpenDialog( m_stage );

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
    if ( JPlanner.plan.filename() == null || JPlanner.plan.filename().equals( "" ) )
      return saveAs();

    // attempt to save using existing filename & location
    return save( new File( JPlanner.plan.fileLocation(), JPlanner.plan.filename() ) );
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
      if ( !JPlanner.plan.savePlan( xsw, fos ) )
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
      tempFile.renameTo( file );
      JPlanner.plan.setFileDetails( file.getName(), file.getParent(), saveUser, saveWhen );
    }
    catch ( XMLStreamException | IOException exception )
    {
      // some sort of exception thrown
      exception.printStackTrace();
      return false;
    }

    // save succeed, so update gui
    properties().updateFromPlan();
    JPlanner.plan.undostack().setClean();
    updateWindowTitles();
    message( "Saved plan to '" + file.getPath() + "'" );
    return true;
  }

  /**************************************** temporaryFile ****************************************/
  private File temporaryFile( File file )
  {
    // return temporary file name based on given file
    String path = file.getParent();
    String name = file.getName();
    int last = name.lastIndexOf( '.' );
    if ( last >= 0 )
      name = name.substring( 0, last ) + DateTime.now().milliseconds() + name.substring( last, name.length() );
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
          case XmlLabels.XML_DISPLAY_DATA:
            if ( tabs == null )
              tabs = m_mainTabWidget;
            else
              tabs = newWindow();

            int tab = 0;

            // read XML attributes
            for ( int i = 0; i < xsr.getAttributeCount(); i++ )
              switch ( xsr.getAttributeLocalName( i ) )
              {
                case XmlLabels.XML_WINDOW:
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

            // set selected tab and check window bounding rectangle fits display(s)
            tabs.select( tab );
            // TODO
            //if ( tabs == m_mainTabWidget )
            //  updateMenus();
            //tabs.getShell().setBounds( checkShellBounds( rect ) );
            break;

          case XmlLabels.XML_TASKS_GANTT_TAB:
            //tabs.loadXmlTasksGantt( xsr );
            break;
          case XmlLabels.XML_RESOURCES_TAB:
            //tabs.loadXmlResources( xsr );
            break;
          case XmlLabels.XML_CALENDARS_TAB:
            //tabs.loadXmlCalendars( xsr );
            break;
          case XmlLabels.XML_DAYS_TAB:
            //tabs.loadXmlDayTypes( xsr );
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
    properties().updateFromPlan();
    notes().updateFromPlan();

    // if reset set all table row heights to default
    //m_tabWidgets.forEach( tabs -> tabs.tasks().setRowsHeightToDefault() );
    m_tabWidgets.forEach( tabs -> tabs.getTasksTab().getTable().hideRow( 0, false ) );
    //m_tabWidgets.forEach( tabs -> tabs.gantt().setDefault() );
    //m_tabWidgets.forEach( tabs -> tabs.gantt().updateAll() );
    resetTaskTables();

    //m_tabWidgets.forEach( tabs -> tabs.resources().setRowsHeightToDefault() );
    //m_tabWidgets.forEach( tabs -> tabs.resources().hideRow( 0 ) );
    resetResourceTables();

    //m_tabWidgets.forEach( tabs -> tabs.calendars().setRowsHeightToDefault() );
    resetCalendarTables();

    //m_tabWidgets.forEach( tabs -> tabs.days().setRowsHeightToDefault() );
    resetDayTypeTables();

    // update undo-stack window if exists
    //if ( undoWindow != null )
    //  undoWindow.setList();
  }

  /*************************************** resetTaskTables ***************************************/
  public void resetTaskTables()
  {
    // reset all tasks tables, needed if number of columns or rows changing etc
    m_tabWidgets.forEach( tabs -> tabs.getTasksTab().getTable().reset() );
  }

  /************************************* resetResourceTables *************************************/
  public void resetResourceTables()
  {
    // reset all resources tables, needed if number of columns or rows changing etc
    m_tabWidgets.forEach( tabs -> tabs.getResourcesTab().getTable().reset() );
  }

  /************************************* resetCalendarTables *************************************/
  public void resetCalendarTables()
  {
    // reset all calendars tables, needed if number of columns or rows changing etc
    m_tabWidgets.forEach( tabs -> tabs.getCalendarsTab().getTable().reset() );
  }

  /************************************** resetDayTypeTables *************************************/
  public void resetDayTypeTables()
  {
    // reset all day-type tables, needed if number of columns or rows changing etc
    m_tabWidgets.forEach( tabs -> tabs.getDaysTab().getTable().reset() );
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
  private void schedule()
  {
    // TODO Auto-generated method stub

  }

  /***************************************** properties ******************************************/
  private PlanProperties properties()
  {
    return m_mainTabWidget.getPlanTab().getPlanProperties();
  }

  /******************************************** notes ********************************************/
  private PlanNotes notes()
  {
    return m_mainTabWidget.getPlanTab().getPlanNotes();
  }

  /************************************* updateWindowTitles **************************************/
  private void updateWindowTitles()
  {
    // TODO Auto-generated method stub

  }

  /*************************************** saveDisplayData ***************************************/
  private void saveDisplayData( XMLStreamWriter xsw )
  {
    // TODO Auto-generated method stub

  }

  /***************************************** newWindow *******************************************/
  public MainTabWidget newWindow()
  {
    // create new window
    Stage stage = new Stage();
    MainTabWidget newTabWidget = new MainTabWidget();
    stage.setScene( new Scene( newTabWidget ) );
    stage.setTitle( m_stage.getTitle() );
    stage.show();

    // add new MainTabWidget to tracking list
    m_tabWidgets.add( newTabWidget );

    // on close (via hiding) remove from tracking list
    stage.setOnHiding( event -> m_tabWidgets.remove( newTabWidget ) );

    return newTabWidget;
  }

  /************************************* showUndoStackWindow *************************************/
  public void showUndoStackWindow( boolean show )
  {
    JPlanner.trace( "SHOW UNDOSTACK WINDOW " + show );

    // show undo-stack window
    if ( m_undoWindow == null )
    {
      m_undoWindow = new UndoStackWindow();
      m_undoWindow.initOwner( m_stage );
    }

    if ( show )
    {
      m_undoWindow.show();
      m_undoWindow.toFront();
    }
    else
    {
      m_undoWindow.hide();
    }
  }

}
