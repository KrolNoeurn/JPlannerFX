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
  }

  /**************************************** updateFromPlan ***************************************/
  public void updateFromPlan()
  {
    // TODO Auto-generated method stub
  }

}
