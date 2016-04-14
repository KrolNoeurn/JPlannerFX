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

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandPlanSetProperties;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/************************** Widget display & editing of plan properties **************************/
/*************************************************************************************************/

public class PlanProperties extends ScrollPane
{
  private TextField m_title           = new TextField();
  private TextField m_defaultStart    = new TextField();
  private TextField m_actualStart     = new TextField();
  private TextField m_end             = new TextField();
  private TextField m_defaultCalendar = new TextField();
  private TextField m_DTformat        = new TextField();
  private TextField m_Dformat         = new TextField();
  private TextField m_fileName        = new TextField();
  private TextField m_fileLocation    = new TextField();
  private TextField m_savedBy         = new TextField();
  private TextField m_savedWhen       = new TextField();
  private NumberOf  m_numberOf        = new NumberOf();

  /**************************************** constructor ******************************************/
  public PlanProperties()
  {
    // setup scrolling properties panel
    super();
    setMinWidth( 0.0 );

    // grid for properties layout
    GridPane grid = new GridPane();
    grid.setVgap( 5.0 );
    grid.setHgap( 5.0 );
    grid.setPadding( new Insets( 5.0 ) );
    setContent( grid );
    setFitToWidth( true );
    setFitToHeight( true );

    // title
    int row = 0;
    grid.add( new Label( "Title" ), 0, row );
    grid.add( m_title, 1, row );
    GridPane.setHgrow( m_title, Priority.ALWAYS );

    // default start
    row++;
    grid.add( new Label( "Default Start" ), 0, row );
    grid.add( m_defaultStart, 1, row );
    GridPane.setHgrow( m_defaultStart, Priority.ALWAYS );

    // actual start
    row++;
    grid.add( new Label( "Actual Start" ), 0, row );
    grid.add( m_actualStart, 1, row );
    GridPane.setHgrow( m_actualStart, Priority.ALWAYS );

    // end
    row++;
    grid.add( new Label( "End" ), 0, row );
    grid.add( m_end, 1, row );
    GridPane.setHgrow( m_end, Priority.ALWAYS );

    // default calendar
    row++;
    grid.add( new Label( "Default Calendar" ), 0, row );
    grid.add( m_defaultCalendar, 1, row );
    GridPane.setHgrow( m_defaultCalendar, Priority.ALWAYS );

    // date-time format
    row++;
    grid.add( new Label( "Date-time format" ), 0, row );
    grid.add( m_DTformat, 1, row );
    GridPane.setHgrow( m_DTformat, Priority.ALWAYS );

    // date format
    row++;
    grid.add( new Label( "Date format" ), 0, row );
    grid.add( m_Dformat, 1, row );
    GridPane.setHgrow( m_Dformat, Priority.ALWAYS );

    // file name
    row++;
    grid.add( new Label( "File name" ), 0, row );
    grid.add( m_fileName, 1, row );
    GridPane.setHgrow( m_fileName, Priority.ALWAYS );

    // file location
    row++;
    grid.add( new Label( "File location" ), 0, row );
    grid.add( m_fileLocation, 1, row );
    GridPane.setHgrow( m_fileLocation, Priority.ALWAYS );

    // saved by
    row++;
    grid.add( new Label( "Saved by" ), 0, row );
    grid.add( m_savedBy, 1, row );
    GridPane.setHgrow( m_savedBy, Priority.ALWAYS );

    // saved when
    row++;
    grid.add( new Label( "Saved when" ), 0, row );
    grid.add( m_savedWhen, 1, row );
    GridPane.setHgrow( m_savedWhen, Priority.ALWAYS );

    // number of
    row++;
    grid.add( m_numberOf, 0, row, 2, 1 );
    GridPane.setHgrow( m_numberOf, Priority.ALWAYS );
    GridPane.setVgrow( m_numberOf, Priority.ALWAYS );
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
