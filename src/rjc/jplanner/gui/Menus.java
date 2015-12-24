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

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/*************************************************************************************************/
/****************************** Main JPlanner application menu bar *******************************/
/*************************************************************************************************/

public class Menus extends MenuBar
{

  /**************************************** constructor ******************************************/
  public Menus()
  {
    // construct main menu bar
    super();

    // top level menus
    Menu menuFile = new Menu( "File" );
    Menu menuEdit = new Menu( "Edit" );
    Menu menuReport = new Menu( "Report" );
    Menu menuView = new Menu( "View" );
    Menu menuHelp = new Menu( "Help" );
    getMenus().addAll( menuFile, menuEdit, menuReport, menuView, menuHelp );

    // file menu
    MenuItem fileNew = new MenuItem( "New" );
    MenuItem fileOpen = new MenuItem( "Open..." );
    MenuItem fileSave = new MenuItem( "Save" );
    MenuItem fileSaveAs = new MenuItem( "Save As..." );
    MenuItem filePrintPreview = new MenuItem( "Print preview..." );
    filePrintPreview.setDisable( true );
    MenuItem filePrint = new MenuItem( "Print..." );
    filePrint.setDisable( true );
    MenuItem fileExit = new MenuItem( "Exit" );

    menuFile.getItems().addAll( fileNew, fileOpen, fileSave, fileSaveAs );
    menuFile.getItems().addAll( new SeparatorMenuItem(), filePrintPreview, filePrint );
    menuFile.getItems().addAll( new SeparatorMenuItem(), fileExit );

    // edit menu
    MenuItem editUndo = new MenuItem( "Undo" );
    MenuItem editRedo = new MenuItem( "Redo" );
    MenuItem editInsert = new MenuItem( "Insert" );
    MenuItem editDelete = new MenuItem( "Delete" );
    menuEdit.getItems().addAll( editUndo, editRedo, new SeparatorMenuItem(), editInsert, editDelete );

  }

}
