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

package rjc.jplanner.gui.days;

import javafx.scene.paint.Paint;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandDaySetNumPeriods;
import rjc.jplanner.command.CommandDaySetValue;
import rjc.jplanner.gui.table.Body;
import rjc.jplanner.gui.table.Cell.Alignment;
import rjc.jplanner.gui.table.CellEditor;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.gui.table.ITableDataSource;
import rjc.jplanner.gui.table.Table;
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
  public String getColumnTitle( int column )
  {
    return Day.sectionName( column );
  }

  /**************************************** getRowTitle ******************************************/
  @Override
  public String getRowTitle( int row )
  {
    return Integer.toString( row + 1 );
  }

  /**************************************** getCellText ******************************************/
  @Override
  public String getCellText( int column, int row )
  {
    return JPlanner.plan.day( row ).toString( column );
  }

  /************************************* getCellAlignment ****************************************/
  @Override
  public Alignment getCellAlignment( int column, int row )
  {
    // all cells are middle aligned except name which is left aligned
    if ( column == Day.SECTION_NAME )
      return Alignment.LEFT;

    return Alignment.MIDDLE;
  }

  /************************************* getCellBackground ***************************************/
  @Override
  public Paint getCellBackground( int column, int row )
  {
    // all cells are normal coloured except unused start/end
    Day day = JPlanner.plan.day( row );
    if ( column >= day.numPeriods() * 2 + Day.SECTION_START1 )
      return Table.COLOR_DISABLED_CELL;

    return Table.COLOR_NORMAL_CELL;
  }

  /***************************************** getEditor *******************************************/
  @Override
  public CellEditor getEditor( Body body )
  {
    // return editor for the table body cell with focus
    return new EditorText( body );
  }

  /****************************************** setValue *******************************************/
  @Override
  public void setValue( int column, int row, Object newValue )
  {
    // if new value equals old value, exit with no command
    Object oldValue = getValue( column, row );
    if ( newValue.equals( oldValue ) )
      return;

    // special command for setting number of work periods, otherwise generic
    if ( column == Day.SECTION_PERIODS )
      JPlanner.plan.undostack().push( new CommandDaySetNumPeriods( row, newValue, oldValue ) );
    else
      JPlanner.plan.undostack().push( new CommandDaySetValue( row, column, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int column, int row )
  {
    return getCellText( column, row );
  }

}
