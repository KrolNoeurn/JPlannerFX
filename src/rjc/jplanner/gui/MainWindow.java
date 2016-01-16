/**************************************************************************
 *  Copyright (C) 2015 by Richard Crook                                   *
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
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.Plan;

/*************************************************************************************************/
/******************************* Main JPlanner application window ********************************/
/*************************************************************************************************/

public class MainWindow
{
  public static final Color COLOR_GENERAL_BACKGROUND = Color.rgb( 240, 240, 240 );

  private Stage             m_stage;
  private MainTabWidget     m_mainTabWidget          = new MainTabWidget();
  private MenuBar           m_menus                  = new Menus();
  private TextField         m_statusBar              = new TextField();
  private UndoStackWindow   m_undoWindow;

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
      message( "Cannot find '" + file.getPath() + "'" );
      return false;
    }

    // check file can be read
    if ( !file.canRead() )
    {
      message( "Cannot read '" + file.getPath() + "'" );
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
      message( "Cannot write to '" + file.getPath() + "'" );
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
    //properties().updateFromPlan(); ########################## TODO
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
  private void loadDisplayData( XMLStreamReader xsr )
  {
    // TODO Auto-generated method stub

  }

  /****************************************** resetGui *******************************************/
  private void resetGui()
  {
    // TODO Auto-generated method stub

  }

  /****************************************** schedule *******************************************/
  private void schedule()
  {
    // TODO Auto-generated method stub

  }

  /***************************************** properties ******************************************/
  private Object properties()
  {
    // TODO Auto-generated method stub
    return null;
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
