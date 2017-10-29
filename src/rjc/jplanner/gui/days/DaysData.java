/**************************************************************************
 *  Copyright (C) 2017 by Richard Crook                                   *
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
import rjc.jplanner.gui.Colors;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.gui.table.AbstractDataSource;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.gui.table.Table.Alignment;
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/**************************** Table data source for showing day-types ****************************/
/*************************************************************************************************/

class DaysData extends AbstractDataSource
{

  /************************************** getColumnCount *****************************************/
  @Override
  public int getColumnCount()
  {
    // table column count is max number of periods * 2 + SECTION_START1
    int max = 0;
    for ( int i = 0; i < getRowCount(); i++ )
      if ( JPlanner.plan.getDay( i ).getNumberOfPeriods() > max )
        max = JPlanner.plan.getDay( i ).getNumberOfPeriods();

    return max * 2 + Day.SECTION_START1;
  }

  /**************************************** getRowCount ******************************************/
  @Override
  public int getRowCount()
  {
    // return number of rows
    return JPlanner.plan.getDaysCount();
  }

  /************************************** getColumnTitle *****************************************/
  @Override
  public String getColumnTitle( int columnIndex )
  {
    // return column title
    return Day.getSectionName( columnIndex );
  }

  /**************************************** getRowTitle ******************************************/
  @Override
  public String getRowTitle( int row )
  {
    // display row number plus one, so row index zero is displayed as "1" etc
    return Integer.toString( row + 1 );
  }

  /************************************* getCellAlignment ****************************************/
  @Override
  public Alignment getCellAlignment( int columnIndex, int row )
  {
    // all cells are middle aligned except name which is left aligned
    if ( columnIndex == Day.SECTION_NAME )
      return Alignment.LEFT;

    return Alignment.MIDDLE;
  }

  /************************************* getCellBackground ***************************************/
  @Override
  public Paint getCellBackground( int columnIndex, int row )
  {
    // all cells are normal coloured except unused start/end
    Day day = JPlanner.plan.getDay( row );
    if ( columnIndex >= day.getNumberOfPeriods() * 2 + Day.SECTION_START1 )
      return Colors.DISABLED_CELL;

    // if no work periods, work is disabled
    if ( columnIndex == Day.SECTION_WORK && day.getNumberOfPeriods() == 0 )
      return Colors.DISABLED_CELL;

    return Colors.NORMAL_CELL;
  }

  /***************************************** getEditor *******************************************/
  @Override
  public AbstractCellEditor getEditor( int columnIndex, int row )
  {
    // return null if cell is disabled
    if ( getCellBackground( columnIndex, row ) == Colors.DISABLED_CELL )
      return null;

    // return editor for table body cell
    switch ( columnIndex )
    {
      case Day.SECTION_NAME:
        return new EditorDayName( columnIndex, row );
      case Day.SECTION_WORK:
        return new EditorDayWork( columnIndex, row );
      case Day.SECTION_PERIODS:
        return new EditorDayNumPeriods( columnIndex, row );
      default:
        return new EditorDayTime( columnIndex, row );
    }
  }

  /****************************************** setValue *******************************************/
  @Override
  public void setValue( int columnIndex, int row, Object newValue )
  {
    // if new value equals old value, exit with no command
    Day day = JPlanner.plan.getDay( row );
    Object oldValue = day.getValue( columnIndex );
    if ( JPlanner.equal( newValue, oldValue ) )
      return;

    // special command for setting number of work periods, otherwise generic
    if ( columnIndex == Day.SECTION_PERIODS )
      JPlanner.plan.getUndostack().push( new CommandDaySetNumPeriods( day, (int) newValue, (int) oldValue ) );
    else
      JPlanner.plan.getUndostack().push( new CommandDaySetValue( day, columnIndex, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int columnIndex, int row )
  {
    // return null if cell is disabled
    if ( getCellBackground( columnIndex, row ) == Colors.DISABLED_CELL )
      return null;

    // return cell value
    return JPlanner.plan.getDay( row ).getValue( columnIndex );
  }

  /***************************************** getCellText *****************************************/
  @Override
  public String getCellText( int columnIndex, int row )
  {
    // get value to be displayed
    Object value = getValue( columnIndex, row );
    if ( value == null )
      return null;

    // display work with two decimal places
    if ( columnIndex == Day.SECTION_WORK )
      return String.format( "%.2f", value );

    // otherwise return default cell text
    return super.getCellText( columnIndex, row );
  }

  /********************************** defaultTableModifications **********************************/
  @Override
  public void defaultTableModifications( Table table )
  {
    // default table modifications
    table.setDefaultColumnWidth( 60 );
    table.setWidthByColumnIndex( Day.SECTION_NAME, 150 );
  }

}
