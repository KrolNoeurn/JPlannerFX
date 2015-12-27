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
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/******************************* Main JPlanner application window ********************************/
/*************************************************************************************************/

public class MainWindow
{
  public static final Color COLOR_GENERAL_BACKGROUND = Color.rgb( 240, 240, 240 );

  private MainTabWidget     m_mainTabWidget          = new MainTabWidget();
  private MenuBar           m_menus                  = new Menus();
  private TextField         m_statusBar              = new TextField();

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
}
