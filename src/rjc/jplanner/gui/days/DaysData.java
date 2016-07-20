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

package rjc.jplanner.gui.days;

import javafx.scene.paint.Paint;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandDaySetNumPeriods;
import rjc.jplanner.command.CommandDaySetValue;
import rjc.jplanner.gui.table.CellEditor;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.gui.table.ITableDataSource;
import rjc.jplanner.gui.table.Table.Alignment;
import rjc.jplanner.gui.table.TableCanvas;
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/**************************** Table data source for showing day-types ****************************/
/*************************************************************************************************/

public class DaysData implements ITableDataSource
{

  /************************************** getColumnCount *****************************************/
  @Override
  public int getColumnCount()
  {
    // table column count is max number of periods * 2 + SECTION_START1
    int max = 0;
    for ( int i = 0; i < getRowCount(); i++ )
      if ( JPlanner.plan.day( i ).numPeriods() > max )
        max = JPlanner.plan.day( i ).numPeriods();

    return max * 2 + Day.SECTION_START1;
  }

  /**************************************** getRowCount ******************************************/
  @Override
  public int getRowCount()
  {
    return JPlanner.plan.daysCount();
  }

  /************************************** getColumnTitle *****************************************/
  @Override
  public String getColumnTitle( int columnIndex )
  {
    return Day.sectionName( columnIndex );
  }

  /**************************************** getRowTitle ******************************************/
  @Override
  public String getRowTitle( int rowIndex )
  {
    // display row number plus one, so row index zero is displayed as "1" etc
    return Integer.toString( rowIndex + 1 );
  }

  /**************************************** getCellText ******************************************/
  @Override
  public String getCellText( int columnIndex, int rowIndex )
  {
    return JPlanner.plan.day( rowIndex ).toString( columnIndex );
  }

  /************************************* getCellAlignment ****************************************/
  @Override
  public Alignment getCellAlignment( int columnIndex, int rowIndex )
  {
    // all cells are middle aligned except name which is left aligned
    if ( columnIndex == Day.SECTION_NAME )
      return Alignment.LEFT;

    return Alignment.MIDDLE;
  }

  /************************************* getCellBackground ***************************************/
  @Override
  public Paint getCellBackground( int columnIndex, int rowIndex )
  {
    // all cells are normal coloured except unused start/end
    Day day = JPlanner.plan.day( rowIndex );
    if ( columnIndex >= day.numPeriods() * 2 + Day.SECTION_START1 )
      return TableCanvas.COLOR_DISABLED_CELL;

    return TableCanvas.COLOR_NORMAL_CELL;
  }

  /***************************************** getEditor *******************************************/
  @Override
  public CellEditor getEditor( int columnIndex, int rowIndex )
  {
    // return null if cell is not editable, unused start/end
    Day day = JPlanner.plan.day( rowIndex );
    if ( columnIndex >= day.numPeriods() * 2 + Day.SECTION_START1 )
      return null;

    // return editor for table body cell
    return new EditorText( columnIndex, rowIndex );
  }

  /****************************************** setValue *******************************************/
  @Override
  public void setValue( int columnIndex, int rowIndex, Object newValue )
  {
    // if new value equals old value, exit with no command
    Object oldValue = getValue( columnIndex, rowIndex );
    if ( newValue.equals( oldValue ) )
      return;

    // special command for setting number of work periods, otherwise generic
    if ( columnIndex == Day.SECTION_PERIODS )
      JPlanner.plan.undostack().push( new CommandDaySetNumPeriods( rowIndex, newValue, oldValue ) );
    else
      JPlanner.plan.undostack().push( new CommandDaySetValue( rowIndex, columnIndex, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int columnIndex, int rowIndex )
  {
    return getCellText( columnIndex, rowIndex );
  }

}
