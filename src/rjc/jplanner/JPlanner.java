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

package rjc.jplanner;

import java.io.File;
import java.util.TreeSet;

import javafx.application.Application;
import javafx.stage.Stage;
import rjc.jplanner.gui.MainWindow;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.Plan;

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
  public static Plan         plan;                    // globally accessible plan
  public static MainWindow   gui;                     // globally accessible main-window

  public static final String ERROR   = "error";
  public static final String VERSION = "v0.0.2-alpha";

  /******************************************** main *********************************************/
  public static void main( String[] args )
  {
    // main entry point for application startup
    trace( "JPlannerFX VERSION = '" + VERSION + "'" );
    trace( "################################# Java properties #################################" );
    for ( Object property : new TreeSet<Object>( System.getProperties().keySet() ) )
      trace( property + " = '" + System.getProperty( property.toString() ) + "'" );
    trace( "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ JPlanner started ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
    trace( plan );
    plan = new Plan();
    trace( plan );
    plan.initialise();
    trace( plan );

    // launch main application display
    launch( args );

    trace( "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ JPlanner ended ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
  }

  /******************************************** start ********************************************/
  @Override
  public void start( Stage stage ) throws Exception
  {
    // create main window
    gui = new MainWindow( stage );
    gui.load( new File( "C:\\Users\\Richard\\Desktop\\tests\\ttt.xml" ) );
  }

  /******************************************* trace *********************************************/
  public static void trace( Object... objects )
  {
    // prints space separated objects in string representation prefixed by date-time and suffixed by file+line-number & method
    StringBuilder str = new StringBuilder();
    for ( Object obj : objects )
    {
      if ( obj == null )
        str.append( "null " );
      else if ( obj instanceof String )
        str.append( "\"" + obj + "\" " );
      else if ( obj instanceof Character )
        str.append( "'" + obj + "' " );
      else
        str.append( obj + " " );
    }

    System.out.println( DateTime.now() + " " + str.toString() + caller( 1 ) );
  }

  /******************************************* stack *********************************************/
  public static void stack( Object... objects )
  {
    // prints space separated objects in string representation prefixed by date-time
    StringBuilder str = new StringBuilder();
    for ( Object obj : objects )
    {
      if ( obj == null )
        str.append( "null " );
      else if ( obj instanceof String )
        str.append( "\"" + obj + "\" " );
      else if ( obj instanceof Character )
        str.append( "'" + obj + "' " );
      else
        str.append( obj + " " );
    }
    System.out.println( DateTime.now() + " " + str.toString() );

    // prints stack
    StackTraceElement[] stack = new Throwable().getStackTrace();
    for ( int i = 1; i < stack.length; i++ )
      System.out.println( "\t" + stack[i] );
  }

  /******************************************* caller *******************************************/
  public static String caller( int pos )
  {
    // returns stack entry at specified position
    StackTraceElement[] stack = new Throwable().getStackTrace();
    String method = stack[++pos].getMethodName() + "()";
    String file = "(" + stack[pos].getFileName() + ":" + stack[pos].getLineNumber() + ") ";
    return file + method;
  }

  /******************************************* clean *********************************************/
  public static String clean( String txt )
  {
    // returns a clean string
    return txt.trim().replaceAll( "(\\s+)", " " );
  }

}
