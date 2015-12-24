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

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import rjc.jplanner.gui.days.DayTypesData;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/******************************* Main JPlanner application window ********************************/
/*************************************************************************************************/

public class MainWindow
{

  public MainWindow( Stage stage )
  {
    //TasksData tasks = new TasksData();
    DayTypesData days = new DayTypesData();

    // create the scene and setup the stage
    Table daysTable = new Table( days );
    daysTable.setDefaultColumnWidth( 60 );
    daysTable.setColumnWidth( Day.SECTION_NAME, 150 );

    GridPane grid = new GridPane();
    grid.setGridLinesVisible( true );
    grid.add( new Canvas( 50, 50 ), 0, 0 );
    grid.add( daysTable, 1, 1 );
    grid.add( new Canvas( 50, 50 ), 2, 2 );
    GridPane.setHgrow( daysTable, Priority.ALWAYS );
    GridPane.setVgrow( daysTable, Priority.ALWAYS );

    Scene scene = new Scene( grid, 700, 400, Color.rgb( 240, 240, 240 ) );
    stage.setScene( scene );
    stage.setTitle( "JPlannerFX" );
    stage.show();
  }

}
