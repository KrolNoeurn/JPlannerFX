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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/*************************************************************************************************/
/******************************* Main JPlanner application window ********************************/
/*************************************************************************************************/

public class MainWindow
{
  private MainTabWidget m_mainTabWidget = new MainTabWidget();
  private MenuBar       m_menus         = new Menus();
  private TextField     m_statusBar     = new TextField();

  /**************************************** constructor ******************************************/
  public MainWindow( Stage stage )
  {
    // construct main application window
    GridPane grid = new GridPane();
    grid.add( m_menus, 0, 0 );
    grid.add( m_mainTabWidget, 0, 1 );
    grid.add( m_statusBar, 0, 2 );

    GridPane.setHgrow( m_mainTabWidget, Priority.ALWAYS );
    GridPane.setVgrow( m_mainTabWidget, Priority.ALWAYS );

    Scene scene = new Scene( grid, 700, 400, Color.rgb( 240, 240, 240 ) );
    stage.setScene( scene );
    stage.setTitle( "JPlannerFX" );
    stage.show();
  }

}
