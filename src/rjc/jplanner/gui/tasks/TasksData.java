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

package rjc.jplanner.gui.tasks;

import javafx.scene.paint.Paint;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandTaskSetValue;
import rjc.jplanner.gui.table.CellEditor;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.gui.table.ITableDataSource;
import rjc.jplanner.gui.table.Table.Alignment;
import rjc.jplanner.gui.table.TableCanvas;
import rjc.jplanner.model.Task;

/*************************************************************************************************/
/****************************** Table data source for showing tasks ******************************/
/*************************************************************************************************/

public class TasksData implements ITableDataSource
{

  /************************************** getColumnCount *****************************************/
  @Override
  public int getColumnCount()
  {
    return Task.SECTION_MAX + 1;
  }

  /**************************************** getRowCount ******************************************/
  @Override
  public int getRowCount()
  {
    return JPlanner.plan.tasksCount();
  }

  /************************************** getColumnTitle *****************************************/
  @Override
  public String getColumnTitle( int column )
  {
    return Task.sectionName( column );
  }

  /**************************************** getRowTitle ******************************************/
  @Override
  public String getRowTitle( int row )
  {
    return Integer.toString( row );
  }

  /**************************************** getCellText ******************************************/
  @Override
  public String getCellText( int column, int row )
  {
    return JPlanner.plan.task( row ).toString( column );
  }

  /************************************* getCellAlignment ****************************************/
  @Override
  public Alignment getCellAlignment( int column, int row )
  {
    return Alignment.LEFT;
  }

  /************************************* getCellBackground ***************************************/
  @Override
  public Paint getCellBackground( int column, int row )
  {
    // cell colour determined by if editable
    if ( JPlanner.plan.task( row ).isSectionEditable( column ) )
      return TableCanvas.COLOR_NORMAL_CELL;

    return TableCanvas.COLOR_DISABLED_CELL;

  }

  /***************************************** getEditor *******************************************/
  @Override
  public CellEditor getEditor()
  {
    // return editor for the table body cell with focus
    return new EditorText();
  }

  /****************************************** setValue *******************************************/
  @Override
  public void setValue( int column, int row, Object newValue )
  {
    // if new value equals old value, exit with no command
    Object oldValue = getValue( column, row );
    if ( newValue.equals( oldValue ) )
      return;

    JPlanner.plan.undostack().push( new CommandTaskSetValue( row, column, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int column, int row )
  {
    return getCellText( column, row );
  }

}
