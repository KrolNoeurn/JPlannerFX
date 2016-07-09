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

package rjc.jplanner.gui.tasks;

import javafx.scene.paint.Paint;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandTaskSetValue;
import rjc.jplanner.gui.table.CellEditor;
import rjc.jplanner.gui.table.EditorDateTime;
import rjc.jplanner.gui.table.EditorSpin;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.gui.table.EditorTimeSpan;
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
  public String getColumnTitle( int columnIndex )
  {
    return Task.sectionName( columnIndex );
  }

  /**************************************** getRowTitle ******************************************/
  @Override
  public String getRowTitle( int rowIndex )
  {
    return Integer.toString( rowIndex );
  }

  /**************************************** getCellText ******************************************/
  @Override
  public String getCellText( int columnIndex, int rowIndex )
  {
    return JPlanner.plan.task( rowIndex ).toString( columnIndex );
  }

  /************************************* getCellAlignment ****************************************/
  @Override
  public Alignment getCellAlignment( int columnIndex, int rowIndex )
  {
    return Alignment.LEFT;
  }

  /************************************* getCellBackground ***************************************/
  @Override
  public Paint getCellBackground( int columnIndex, int rowIndex )
  {
    // cell colour determined by if editable
    if ( JPlanner.plan.task( rowIndex ).isSectionEditable( columnIndex ) )
      return TableCanvas.COLOR_NORMAL_CELL;

    return TableCanvas.COLOR_DISABLED_CELL;

  }

  /***************************************** getEditor *******************************************/
  @Override
  public CellEditor getEditor( int columnIndex, int rowIndex )
  {
    // return editor for table body cell
    switch ( columnIndex )
    {
      case Task.SECTION_DURATION:
      case Task.SECTION_WORK:
        return new EditorTimeSpan( columnIndex, rowIndex );
      case Task.SECTION_START:
      case Task.SECTION_END:
        return new EditorDateTime( columnIndex, rowIndex );
      case Task.SECTION_PRIORITY:
        return new EditorSpin( columnIndex, rowIndex );
      default:
        return new EditorText( columnIndex, rowIndex );
    }
  }

  /****************************************** setValue *******************************************/
  @Override
  public void setValue( int columnIndex, int rowIndex, Object newValue )
  {
    // if new value equals old value, exit with no command
    Object oldValue = getValue( columnIndex, rowIndex );
    if ( newValue.equals( oldValue ) )
      return;

    JPlanner.plan.undostack().push( new CommandTaskSetValue( rowIndex, columnIndex, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int columnIndex, int rowIndex )
  {
    return getCellText( columnIndex, rowIndex );
  }

}
