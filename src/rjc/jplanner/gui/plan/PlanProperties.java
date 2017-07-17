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

package rjc.jplanner.gui.plan;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandPlanSetProperties;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.gui.DateTimeEditor;
import rjc.jplanner.gui.MainWindow;
import rjc.jplanner.gui.calendars.CalendarCombo;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/************************** Widget display & editing of plan properties **************************/
/*************************************************************************************************/

public class PlanProperties extends ScrollPane
{
  private GridPane          m_grid            = new GridPane();
  private TextField         m_title           = new TextField();
  private DateTimeEditor    m_defaultStart    = new DateTimeEditor();
  private TextField         m_actualStart     = new TextField();
  private TextField         m_end             = new TextField();
  private CalendarCombo     m_defaultCalendar = new CalendarCombo();
  private TextField         m_DTformat        = new TextField();
  private TextField         m_Dformat         = new TextField();
  private TextField         m_fileName        = new TextField();
  private TextField         m_fileLocation    = new TextField();
  private TextField         m_savedBy         = new TextField();
  private TextField         m_savedWhen       = new TextField();
  private NumberOf          m_numberOf        = new NumberOf();

  private static Background READONLY          = new Background( new BackgroundFill( Colors.LIGHTERGRAY, null, null ) );

  /**************************************** constructor ******************************************/
  public PlanProperties()
  {
    // setup scrolling properties panel
    setMinWidth( 0.0 );

    // grid for properties layout
    m_grid.setVgap( 5.0 );
    m_grid.setHgap( 5.0 );
    m_grid.setPadding( new Insets( 5.0 ) );
    setContent( m_grid );
    setFitToWidth( true );
    setFitToHeight( true );

    // add field rows
    int row = 0;
    addRow( row++, "Title", m_title, false );
    addRow( row++, "Default Start", m_defaultStart, false );
    addRow( row++, "Actual Start", m_actualStart, true );
    addRow( row++, "End", m_end, true );
    addRow( row++, "Default Calendar", m_defaultCalendar, false );
    addRow( row++, "Date-time format", m_DTformat, false );
    addRow( row++, "Date format", m_Dformat, false );
    addRow( row++, "File name", m_fileName, true );
    addRow( row++, "File location", m_fileLocation, true );
    addRow( row++, "Saved by", m_savedBy, true );
    addRow( row++, "Saved when", m_savedWhen, true );

    // add number of
    row++;
    m_grid.add( m_numberOf, 0, row, 2, 1 );
    GridPane.setHgrow( m_numberOf, Priority.ALWAYS );
    GridPane.setVgrow( m_numberOf, Priority.ALWAYS );

    // set tool tips
    Tooltip DTtip = new Tooltip(
        "Symbol\tMeaning\t\t\t\t\tPresentation\tExamples\n" + "--------\t---------\t\t\t\t\t--------------\t---------\n"
            + " G\t\t era\t\t\t\t\t\t text\t\t\t AD; Anno Domini; A\n" + " u\t\t year\t\t\t\t\t\t year\t\t\t 2004; 04\n"
            + " y\t\t year-of-era\t\t\t\t year\t\t\t 2004; 04\n" + " D\t\t day-of-year\t\t\t\t number\t\t 189\n"
            + " M/L\t\t month-of-year\t\t\t number/text\t 7; 07; Jul; July; J\n"
            + " d\t\t day-of-month\t\t\t\t number\t\t 10\n\n"
            + " B\t\t half-of-year\t\t\t\t number/text\t 2; H2; 2nd half\n"
            + " Q/q\t\t quarter-of-year\t\t\t number/text\t 3; 03; Q3; 3rd quarter\n"
            + " Y\t\t week-based-year\t\t\t year\t\t\t 1996; 96\n" + " w\t\t week-of-week-based-year\t number\t\t 27\n"
            + " W\t\t week-of-month\t\t\t number\t\t 4\n" + " E\t\t day-of-week\t\t\t\t text\t\t\t Tue; Tuesday; T\n"
            + " e/c\t\t localized day-of-week\t\t number/text\t 2; 02; Tue; Tuesday; T\n"
            + " F\t\t week-of-month\t\t\t number\t\t 3\n\n" + " a\t\t am-pm-of-day\t\t\t text\t\t\t PM\n"
            + " h\t\t clock-hour-of-am-pm (1-12)\t number\t\t 12\n" + " K\t\t hour-of-am-pm (0-11)\t\t number\t\t 0\n"
            + " k\t\t clock-hour-of-am-pm (1-24)\t number\t\t 0\n\n" + " H\t\t hour-of-day (0-23)\t\t\t number\t\t 0\n"
            + " m\t\t minute-of-hour\t\t\t number\t\t 30\n" + " s\t\t second-of-minute\t\t\t number\t\t 55\n"
            + " S\t\t fraction-of-second\t\t\t fraction\t\t 978\n" + " A\t\t milli-of-day\t\t\t\t number\t\t 1234\n"
            + " N\t\t nano-of-day\t\t\t\t number\t\t 1234000000\n\n" + " p\t\t pad next\t\t\t\t\t pad modifier\t 1\n"
            + " '\t\t escape for text\t\t\t delimiter\n" + " ''\t\t single quote\t\t\t\t literal\t\t '\n" );
    DTtip.setStyle( MainWindow.STYLE_TOOLTIP );
    m_DTformat.setTooltip( DTtip );

    Tooltip Dtip = new Tooltip(
        "Symbol\tMeaning\t\t\t\t\tPresentation\tExamples\n" + "--------\t---------\t\t\t\t\t--------------\t---------\n"
            + " G\t\t era\t\t\t\t\t\t text\t\t\t AD; Anno Domini; A\n" + " u\t\t year\t\t\t\t\t\t year\t\t\t 2004; 04\n"
            + " y\t\t year-of-era\t\t\t\t year\t\t\t 2004; 04\n" + " D\t\t day-of-year\t\t\t\t number\t\t 189\n"
            + " M/L\t\t month-of-year\t\t\t number/text\t 7; 07; Jul; July; J\n"
            + " d\t\t day-of-month\t\t\t\t number\t\t 10\n\n"
            + " B\t\t half-of-year\t\t\t\t number/text\t 2; H2; 2nd half\n"
            + " Q/q\t\t quarter-of-year\t\t\t number/text\t 3; 03; Q3; 3rd quarter\n"
            + " Y\t\t week-based-year\t\t\t year\t\t\t 1996; 96\n" + " w\t\t week-of-week-based-year\t number\t\t 27\n"
            + " W\t\t week-of-month\t\t\t number\t\t 4\n" + " E\t\t day-of-week\t\t\t\t text\t\t\t Tue; Tuesday; T\n"
            + " e/c\t\t localized day-of-week\t\t number/text\t 2; 02; Tue; Tuesday; T\n"
            + " F\t\t week-of-month\t\t\t number\t\t 3\n\n" + " p\t\t pad next\t\t\t\t\t pad modifier\t 1\n"
            + " '\t\t escape for text\t\t\t delimiter\n" + " ''\t\t single quote\t\t\t\t literal\t\t '\n" );
    Dtip.setStyle( MainWindow.STYLE_TOOLTIP );
    m_Dformat.setTooltip( Dtip );

    // show updated examples of formats
    m_DTformat.textProperty().addListener( ( observable, oldValue, newValue ) -> dateTimeFormatChange() );
    m_DTformat.setOnKeyPressed( event -> keyPressed( event ) );
    m_Dformat.textProperty().addListener( ( observable, oldValue, newValue ) -> dateFormatChange() );
    m_Dformat.setOnKeyPressed( event -> keyPressed( event ) );

    // if escape pressed, revert, if return, commit
    m_title.addEventHandler( KeyEvent.KEY_PRESSED, event -> keyPressed( event ) );
    m_defaultStart.addEventHandler( KeyEvent.KEY_PRESSED, event -> keyPressed( event ) );
    m_defaultCalendar.addEventHandler( KeyEvent.KEY_PRESSED, event -> keyPressed( event ) );
  }

  /***************************************** keyPressed ******************************************/
  private void keyPressed( KeyEvent event )
  {
    // event source is the text field
    TextField source = (TextField) event.getSource();

    // if escape pressed, revert back to plan format
    if ( event.getCode() == KeyCode.ESCAPE )
    {
      int pos = source.getCaretPosition();
      if ( source == m_DTformat )
        source.setText( JPlanner.plan.getDateTimeFormat() );
      if ( source == m_Dformat )
        source.setText( JPlanner.plan.getDateFormat() );
      if ( source == m_title )
        source.setText( JPlanner.plan.getTitle() );
      if ( source == m_defaultStart )
        displayDateTime( m_defaultStart, JPlanner.plan.getStart() );
      if ( source == m_defaultCalendar )
        source.setText( JPlanner.plan.getDefaultcalendar().getName() );
      source.selectRange( pos, pos );
    }

    // if enter pressed, update plan
    if ( event.getCode() == KeyCode.ENTER )
    {
      int pos = source.getCaretPosition();
      updatePlan();
      source.selectRange( pos, pos );
    }
  }

  /************************************* dateTimeFormatChange ************************************/
  private void dateTimeFormatChange()
  {
    // update display for date-time format change
    if ( JPlanner.gui == null )
      return;

    try
    {
      if ( m_DTformat.getText().length() < 1 )
        throw new NumberFormatException( "Invalid format" );

      JPlanner.gui.setError( m_DTformat, null );
      JPlanner.gui.message( "Date-time format example: " + DateTime.now().toString( m_DTformat.getText() ) );
    }
    catch ( Exception exception )
    {
      String err = exception.getMessage();
      JPlanner.gui.setError( m_DTformat, "Date-time format error '" + err + "'" );
    }

    displayDateTime( m_defaultStart, JPlanner.plan.getStart() );
    displayDateTime( m_actualStart, JPlanner.plan.getEarliestTaskStart() );
    displayDateTime( m_end, JPlanner.plan.getLatestTaskEnd() );
    displayDateTime( m_savedWhen, JPlanner.plan.getSavedWhen() );
  }

  /*************************************** dateFormatChange **************************************/
  private void dateFormatChange()
  {
    // update display for date format change
    if ( JPlanner.gui == null )
      return;

    try
    {
      if ( m_Dformat.getText().length() < 1 )
        throw new NumberFormatException( "Invalid format" );

      JPlanner.gui.setError( m_Dformat, null );
      JPlanner.gui.message( "Date format example: " + Date.now().toString( m_Dformat.getText() ) );
    }
    catch ( Exception exception )
    {
      String err = exception.getMessage();
      JPlanner.gui.setError( m_Dformat, "Date format error '" + err + "'" );
    }
  }

  /******************************************** addRow *******************************************/
  private void addRow( int row, String text, Region control, boolean readonly )
  {
    // add row to grid
    Label label = new Label( text );
    m_grid.addRow( row, label, control );
    GridPane.setHalignment( label, HPos.RIGHT );
    GridPane.setHgrow( control, Priority.ALWAYS );

    if ( readonly )
    {
      ( (TextField) control ).setEditable( false );
      control.setBackground( READONLY );
    }
  }

  /*************************************** displayDateTime ***************************************/
  private void displayDateTime( TextField field, DateTime dt )
  {
    // display date-time in user specified format, or if that fails in plan format
    if ( dt == null )
      field.setText( null );
    else if ( m_DTformat.getId() == JPlanner.ERROR )
      field.setText( dt.toString( JPlanner.plan.getDateTimeFormat() ) );
    else
      field.setText( dt.toString( m_DTformat.getText() ) );
  }

  /**************************************** updateFromPlan ***************************************/
  public void updateFromPlan()
  {
    // update the gui property widgets with values from plan
    m_title.setText( JPlanner.plan.getTitle() );
    m_defaultCalendar.setCalendar( JPlanner.plan.getDefaultcalendar() );
    m_DTformat.setText( JPlanner.plan.getDateTimeFormat() );
    m_Dformat.setText( JPlanner.plan.getDateFormat() );
    m_fileName.setText( JPlanner.plan.getFilename() );
    m_fileLocation.setText( JPlanner.plan.getFileLocation() );
    m_savedBy.setText( JPlanner.plan.getSavedBy() );

    m_defaultStart.setDateTime( JPlanner.plan.getStart() );
    displayDateTime( m_actualStart, JPlanner.plan.getEarliestTaskStart() );
    displayDateTime( m_end, JPlanner.plan.getLatestTaskEnd() );
    displayDateTime( m_savedWhen, JPlanner.plan.getSavedWhen() );

    // update the gui "number of" pane
    m_numberOf.redraw();
  }

  /***************************************** updatePlan ******************************************/
  public void updatePlan()
  {
    // get values from gui editors
    String title = m_title.getText();
    DateTime start = m_defaultStart.getDateTime();
    Calendar cal = m_defaultCalendar.getCalendar();

    String DTformat = m_DTformat.getId() == JPlanner.ERROR ? JPlanner.plan.getDateTimeFormat() : m_DTformat.getText();

    String Dformat = m_Dformat.getId() == JPlanner.ERROR ? JPlanner.plan.getDateFormat() : m_Dformat.getText();

    // if properties not changed, return doing nothing
    if ( JPlanner.plan.getTitle().equals( title ) && JPlanner.plan.getStart().equals( start )
        && JPlanner.plan.getDefaultcalendar() == cal && JPlanner.plan.getDateTimeFormat().equals( DTformat )
        && JPlanner.plan.getDateFormat().equals( Dformat ) )
      return;

    // update plan via undo-stack
    JPlanner.plan.getUndostack().push( new CommandPlanSetProperties( title, start, cal, DTformat, Dformat ) );
  }

  /***************************************** getCalendar *****************************************/
  public Calendar getCalendar()
  {
    // return displayed calendar, may differ from default calendar in plan
    return m_defaultCalendar.getCalendar();
  }

  /**************************************** getDateFormat ****************************************/
  public String getDateFormat()
  {
    // return date format, may differ from format in plan
    return m_Dformat.getText();
  }

  /************************************** getDateTimeFormat **************************************/
  public String getDateTimeFormat()
  {
    // return date-time, may differ from format in plan
    return m_DTformat.getText();
  }

}
