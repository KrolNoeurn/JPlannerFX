/**************************************************************************
 *  Copyright (C) 2015 by Richard Crook                                   *
 *  https://github.com/dazzle50/JPlanner                                  *
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

package rjc.jplanner;

import javafx.application.Application;
import javafx.stage.Stage;
import rjc.jplanner.gui.MainWindow;
import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
// JPlanner by Richard Crook
// Aims to be a project planner similar to M$Project with table entry of tasks & Gantt chart
// Also aims to have automatic resource levelling and scheduling based on task priority
// Also aims to have resource levels variable within single task
// Also aims to have Gantt chart task bar thickness showing this variable resource usage
// Based on work I started as early as 2005
/*************************************************************************************************/

public class JPlanner extends Application
{
  public static MainWindow gui; // globally accessible main-window

  /******************************************** main *********************************************/
  public static void main( String[] args )
  {
    // main entry point for application startup
    trace( "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ JPlanner started ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
    // trace( "" + plan );
    // plan = new Plan();
    // trace( "" + plan );
    // plan.initialise();
    // trace( "" + plan );

    // launch main application display
    launch( args );

    trace( "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ JPlanner ended ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
  }

  /******************************************* trace *********************************************/
  public static void trace( String txt )
  {
    // prints txt prefixed by date-time
    System.out.println( DateTime.fromNow() + " " + txt );
  }

  /******************************************** start ********************************************/
  @Override
  public void start( Stage primaryStage ) throws Exception
  {
    // create main window
    gui = new MainWindow( primaryStage );
  }

}
