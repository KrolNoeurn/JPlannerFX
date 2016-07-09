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

package rjc.jplanner.gui;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.CellEditor;

/*************************************************************************************************/
/****************************** Main JPlanner application menu bar *******************************/
/*************************************************************************************************/

public class Menus extends MenuBar
{
  public CheckMenuItem viewUndoStack;
  public MenuItem      editUndo;
  public MenuItem      editRedo;

  /**************************************** constructor ******************************************/
  public Menus()
  {
    // construct main menu bar
    super();

    // top level menus
    Menu menuFile = fileMenu();
    Menu menuEdit = editMenu();
    Menu menuReport = reportMenu();
    Menu menuView = viewMenu();
    Menu menuHelp = helpMenu();

    getMenus().addAll( menuFile, menuEdit, menuReport, menuView, menuHelp );
  }

  /***************************************** onMenuShow ******************************************/
  private void onMenuShow()
  {
    // clear status bar message
    JPlanner.gui.message( "" );

    // if any table cell editing in progress, end it
    CellEditor.endEditing();

    // ensure plan is up-to-date
    if ( JPlanner.gui.isPlanTabSelected() )
    {
      JPlanner.gui.properties().updatePlan();
      JPlanner.gui.notes().updatePlan();
    }
  }

  /****************************************** fileMenu *******************************************/
  private Menu fileMenu()
  {
    // file menu
    Menu menu = new Menu( "File" );
    menu.setOnShowing( event -> onMenuShow() );

    MenuItem fileNew = new MenuItem( "New" );
    fileNew.setAccelerator( new KeyCodeCombination( KeyCode.N, KeyCombination.CONTROL_DOWN ) );
    fileNew.setDisable( true );

    MenuItem fileOpen = new MenuItem( "Open..." );
    fileOpen.setAccelerator( new KeyCodeCombination( KeyCode.O, KeyCombination.CONTROL_DOWN ) );
    fileOpen.setOnAction( event -> JPlanner.gui.load() );

    MenuItem fileSave = new MenuItem( "Save" );
    fileSave.setAccelerator( new KeyCodeCombination( KeyCode.S, KeyCombination.CONTROL_DOWN ) );
    fileSave.setOnAction( event -> JPlanner.gui.save() );

    MenuItem fileSaveAs = new MenuItem( "Save As..." );
    fileSaveAs.setOnAction( event -> JPlanner.gui.saveAs() );

    MenuItem filePrintPreview = new MenuItem( "Print preview..." );
    filePrintPreview.setDisable( true );

    MenuItem filePrint = new MenuItem( "Print..." );
    filePrint.setDisable( true );

    MenuItem fileExit = new MenuItem( "Exit" );
    fileExit.setAccelerator( new KeyCodeCombination( KeyCode.Q, KeyCombination.CONTROL_DOWN ) );
    fileExit.setDisable( true );

    menu.getItems().addAll( fileNew, fileOpen, fileSave, fileSaveAs );
    menu.getItems().addAll( new SeparatorMenuItem(), filePrintPreview, filePrint );
    menu.getItems().addAll( new SeparatorMenuItem(), fileExit );
    return menu;
  }

  /****************************************** editMenu *******************************************/
  private Menu editMenu()
  {
    // edit menu
    Menu menu = new Menu( "Edit" );
    menu.setOnShowing( event -> onMenuShow() );

    editUndo = new MenuItem( "Undo" );
    editUndo.setAccelerator( new KeyCodeCombination( KeyCode.Z, KeyCombination.CONTROL_DOWN ) );
    editUndo.setDisable( true );

    editRedo = new MenuItem( "Redo" );
    editRedo.setAccelerator( new KeyCodeCombination( KeyCode.Y, KeyCombination.CONTROL_DOWN ) );
    editRedo.setDisable( true );

    MenuItem editInsert = new MenuItem( "Insert" );
    editInsert.setAccelerator( new KeyCodeCombination( KeyCode.INSERT, KeyCombination.CONTROL_ANY ) );
    editInsert.setDisable( true );

    MenuItem editDelete = new MenuItem( "Delete" );
    editDelete.setAccelerator( new KeyCodeCombination( KeyCode.DELETE, KeyCombination.CONTROL_ANY ) );
    editDelete.setDisable( true );

    MenuItem editCut = new MenuItem( "Cut" );
    editCut.setAccelerator( new KeyCodeCombination( KeyCode.X, KeyCombination.CONTROL_DOWN ) );
    editCut.setDisable( true );

    MenuItem editCopy = new MenuItem( "Copy" );
    editCopy.setAccelerator( new KeyCodeCombination( KeyCode.C, KeyCombination.CONTROL_DOWN ) );
    editCopy.setDisable( true );

    MenuItem editPaste = new MenuItem( "Paste" );
    editPaste.setAccelerator( new KeyCodeCombination( KeyCode.V, KeyCombination.CONTROL_DOWN ) );
    editPaste.setDisable( true );

    MenuItem editFind = new MenuItem( "Find/Replace..." );
    editFind.setAccelerator( new KeyCodeCombination( KeyCode.F, KeyCombination.CONTROL_DOWN ) );
    editFind.setDisable( true );

    MenuItem editSchedule = new MenuItem( "Schedule" );
    editSchedule.setDisable( true );

    menu.getItems().addAll( editUndo, editRedo, new SeparatorMenuItem(), editInsert, editDelete );
    menu.getItems().addAll( new SeparatorMenuItem(), editCut, editCopy, editPaste );
    menu.getItems().addAll( new SeparatorMenuItem(), editFind, editSchedule );
    return menu;
  }

  /***************************************** reportMenu ******************************************/
  private Menu reportMenu()
  {
    // report menu
    Menu menu = new Menu( "Report" );
    menu.setOnShowing( event -> onMenuShow() );

    MenuItem reportTBD = new MenuItem( "TBD" );
    reportTBD.setDisable( true );

    menu.getItems().addAll( reportTBD );
    return menu;
  }

  /****************************************** viewMenu *******************************************/
  private Menu viewMenu()
  {
    // view menu
    Menu menu = new Menu( "View" );
    menu.setOnShowing( event -> onMenuShow() );

    viewUndoStack = new CheckMenuItem( "Undo Stack..." );
    viewUndoStack.setOnAction( event -> JPlanner.gui.showUndoStackWindow( viewUndoStack.isSelected() ) );

    MenuItem viewNewWindow = new MenuItem( "New Window..." );
    viewNewWindow.setOnAction( event -> JPlanner.gui.newWindow() );

    CheckMenuItem viewStretch = new CheckMenuItem( "Stretch tasks" );
    viewStretch.setSelected( true );
    viewStretch.setDisable( true );

    menu.getItems().addAll( viewUndoStack, viewNewWindow, new SeparatorMenuItem(), viewStretch );
    return menu;
  }

  /****************************************** helpMenu *******************************************/
  private Menu helpMenu()
  {
    // help menu
    Menu menu = new Menu( "Help" );
    menu.setOnShowing( event -> onMenuShow() );

    MenuItem helpAbout = new MenuItem( "About JPlanner" );
    helpAbout.setDisable( true );

    menu.getItems().addAll( helpAbout );
    return menu;
  }

}
