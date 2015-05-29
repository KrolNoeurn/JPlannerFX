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

package rjc.jplanner.gui;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*************************************************************************************************/
/******************************* Main JPlanner application window ********************************/
/*************************************************************************************************/

public class MainWindow
{

  public MainWindow( Stage stage ) throws IOException
  {
    // load the MainWindow FXML
    URL location = getClass().getResource( "MainWindow.fxml" );
    FXMLLoader fxmlLoader = new FXMLLoader( location );
    Parent root = fxmlLoader.load();

    // create the scene and setup the stage
    Scene scene = new Scene( root, 400, 400 );
    stage.setScene( scene );
    stage.setTitle( "JPlanner" );
    stage.show();
  }

}
