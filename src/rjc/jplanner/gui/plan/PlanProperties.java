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

package rjc.jplanner.gui.plan;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandPlanSetProperties;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/************************** Widget display & editing of plan properties **************************/
/*************************************************************************************************/

public class PlanProperties extends ScrollPane
{
  private GridPane          m_grid            = new GridPane();
  private TextField         m_title           = new TextField();
  private TextField         m_defaultStart    = new TextField();
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

  private static Color      LIGHTGRAY         = Color.rgb( 225, 225, 225 );
  private static Background READONLY          = new Background( new BackgroundFill( LIGHTGRAY, null, null ) );

  /**************************************** constructor ******************************************/
  public PlanProperties()
  {
    // setup scrolling properties panel
    super();
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

  /**************************************** updateFromPlan ***************************************/
  public void updateFromPlan()
  {
    // update the gui property widgets with values from plan
    m_title.setText( JPlanner.plan.title() );
    m_defaultStart.setText( JPlanner.plan.start().toString() );
    m_actualStart.setText( JPlanner.plan.earliest().toString() );
    m_end.setText( JPlanner.plan.end().toString() );
    m_defaultCalendar.setText( JPlanner.plan.calendar().name() );
    m_DTformat.setText( JPlanner.plan.datetimeFormat() );
    m_Dformat.setText( JPlanner.plan.dateFormat() );
    m_fileName.setText( JPlanner.plan.filename() );
    m_fileLocation.setText( JPlanner.plan.fileLocation() );
    m_savedBy.setText( JPlanner.plan.savedBy() );
    m_savedWhen.setText( JPlanner.plan.savedWhen().toString() );

    // update the gui "number of" pane
    m_numberOf.redraw();
  }

  /************************************ updatePlanProperties *************************************/
  public void updatePlan()
  {
    // get values from gui editors
    String title = m_title.getText();
    DateTime start = JPlanner.plan.start(); // TODO
    Calendar cal = JPlanner.plan.calendar(); // TODO
    String DTformat = m_DTformat.getText();
    String Dformat = m_Dformat.getText();

    // if properties not changed, return doing nothing
    if ( JPlanner.plan.title().equals( title ) && JPlanner.plan.start().equals( start )
        && JPlanner.plan.calendar() == cal && JPlanner.plan.datetimeFormat().equals( DTformat )
        && JPlanner.plan.dateFormat().equals( Dformat ) )
      return;

    // update plan via undo-stack
    JPlanner.plan.undostack().push( new CommandPlanSetProperties( title, start, cal, DTformat, Dformat ) );
  }

}
