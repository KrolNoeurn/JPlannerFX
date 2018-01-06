/**************************************************************************
 *  Copyright (C) 2018 by Richard Crook                                   *
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

package rjc.jplanner.gui.gantt;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCombination;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.DateTime.Interval;

/*************************************************************************************************/
/********************************* Context menu for Gantt scales *********************************/
/*************************************************************************************************/

class GanttScaleMenu extends ContextMenu
{
  private static GanttScaleMenu m_menu;

  private class DisplayFormat extends KeyCombination // method to display right justified text
  {
    private String m_format;

    public DisplayFormat( String format )
    {
      m_format = "   (" + format + ')';
    }

    @Override
    public String getDisplayText()
    {
      return m_format;
    }
  }

  /**************************************** constructor ******************************************/
  private GanttScaleMenu()
  {
    // create the gantt context menu
    getItems().addAll( yearMenu(), halfYearMenu(), quarterYearMenu(), monthMenu(), weekMenu(), dayMenu() );

    // delete
    MenuItem delete = new MenuItem( "Delete" );
    delete.setOnAction( event -> JPlanner.trace( "NOT IMPLEMENTED" ) );

    // insert above
    MenuItem insertAbove = new MenuItem( "Insert above" );
    insertAbove.setOnAction( event -> JPlanner.trace( "NOT IMPLEMENTED" ) );

    // insert below
    MenuItem insertBelow = new MenuItem( "Insert below" );
    insertBelow.setOnAction( event -> JPlanner.trace( "NOT IMPLEMENTED" ) );

    getItems().addAll( new SeparatorMenuItem(), delete, insertAbove, insertBelow );
  }

  /******************************************** open *********************************************/
  public static void open( GanttScale scale, ContextMenuEvent event )
  {
    // show the gantt plot context menu
    if ( m_menu == null )
      m_menu = new GanttScaleMenu();

    AbstractCellEditor.endEditing();
    m_menu.show( scale, event.getScreenX(), event.getScreenY() );
  }

  /************************************* unselectAllItems ****************************************/
  private void unselectAllItems()
  {
    // remove tick from all check menu items
    getItems().forEach( submenu ->
    {
      if ( submenu instanceof Menu )
        ( (Menu) submenu ).getItems().forEach( option ->
        {
          if ( option instanceof CheckMenuItem )
            ( (CheckMenuItem) option ).setSelected( false );
        } );
    } );

  }

  /***************************************** menuItem ********************************************/
  private void menuItem( Menu menu, Interval interval, String format )
  {
    // build menu item for given interval and format
    CheckMenuItem item = new CheckMenuItem( DateTime.now().toString( format ) );
    item.setAccelerator( new DisplayFormat( format ) );

    item.setOnAction( event ->
    {
      unselectAllItems();
      item.setSelected( true );
      GanttScale scale = (GanttScale) this.getOwnerNode();
      scale.setScale( interval, format );
      scale.redraw();
    } );

    menu.getItems().add( item );
  }

  /************************************** menuItemFormat *****************************************/
  private void menuItemFormat( Menu menu )
  {
    // build menu item for user defined format
    CheckMenuItem format = new CheckMenuItem( "Format..." );
    format.setDisable( true );

    menu.getItems().addAll( new SeparatorMenuItem(), format );
  }

  /***************************************** yearMenu ********************************************/
  private Menu yearMenu()
  {
    // build and return year menu
    Menu menu = new Menu( "Year" );
    menuItem( menu, Interval.YEAR, "yy" );
    menuItem( menu, Interval.YEAR, "yyyy" );
    menuItemFormat( menu );

    return menu;
  }

  /*************************************** halfYearMenu ******************************************/
  private Menu halfYearMenu()
  {
    // build and return year-half menu
    Menu menu = new Menu( "Half Year" );
    menuItem( menu, Interval.HALFYEAR, "BB" );
    menuItem( menu, Interval.HALFYEAR, "yyyy BB" );
    menuItemFormat( menu );

    return menu;
  }

  /************************************* quarterYearMenu *****************************************/
  private Menu quarterYearMenu()
  {
    // build and return year-quarter menu
    Menu menu = new Menu( "Quarter Year" );
    menuItem( menu, Interval.QUARTERYEAR, "QQQ" );
    menuItem( menu, Interval.QUARTERYEAR, "yyyy QQQ" );
    menuItemFormat( menu );

    return menu;
  }

  /**************************************** monthMenu ********************************************/
  private Menu monthMenu()
  {
    // build and return month menu
    Menu menu = new Menu( "Month" );
    menuItem( menu, Interval.MONTH, "MM" );
    menuItem( menu, Interval.MONTH, "MMM" );
    menuItem( menu, Interval.MONTH, "MMMM" );
    menuItem( menu, Interval.MONTH, "MMMMM" );
    menuItem( menu, Interval.MONTH, "MMM-yy" );
    menuItem( menu, Interval.MONTH, "MMM-yyyy" );
    menuItemFormat( menu );

    return menu;
  }

  /***************************************** weekMenu ********************************************/
  private Menu weekMenu()
  {
    // build and return week menu
    Menu menu = new Menu( "Week" );
    menuItem( menu, Interval.WEEK, "'W'w" );
    menuItem( menu, Interval.WEEK, "dd" );
    menuItem( menu, Interval.WEEK, "dd-MMM" );
    menuItemFormat( menu );

    return menu;
  }

  /***************************************** dayMenu *********************************************/
  private Menu dayMenu()
  {
    // build and return day menu
    Menu menu = new Menu( "Day" );
    menuItem( menu, Interval.DAY, "eee" );
    menuItem( menu, Interval.DAY, "eeee" );
    menuItem( menu, Interval.DAY, "eeeee" );
    menuItem( menu, Interval.DAY, "dd" );
    menuItem( menu, Interval.DAY, "dd-MMM" );
    menuItem( menu, Interval.DAY, "dd/MM/yy" );
    menuItemFormat( menu );

    return menu;
  }

}
