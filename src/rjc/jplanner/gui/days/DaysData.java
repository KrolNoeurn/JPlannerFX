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
import rjc.jplanner.model.Time;

/*************************************************************************************************/
/**************************** Table data source for showing day-types ****************************/
/*************************************************************************************************/

public class DaysData extends AbstractDataSource
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
  public String getRowTitle( int rowIndex )
  {
    // display row number plus one, so row index zero is displayed as "1" etc
    return Integer.toString( rowIndex + 1 );
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
    Day day = JPlanner.plan.getDay( rowIndex );
    if ( columnIndex >= day.getNumberOfPeriods() * 2 + Day.SECTION_START1 )
      return Colors.DISABLED_CELL;

    return Colors.NORMAL_CELL;
  }

  /***************************************** getEditor *******************************************/
  @Override
  public AbstractCellEditor getEditor( int columnIndex, int rowIndex )
  {
    // return null if cell is not editable, unused start/end
    Day day = JPlanner.plan.getDay( rowIndex );
    if ( columnIndex >= day.getNumberOfPeriods() * 2 + Day.SECTION_START1 )
      return null;

    // return editor for table body cell
    switch ( columnIndex )
    {
      case Day.SECTION_NAME:
        return new EditorDayName( columnIndex, rowIndex );
      case Day.SECTION_WORK:
        return new EditorDayWork( columnIndex, rowIndex );
      case Day.SECTION_PERIODS:
        return new EditorDayNumPeriods( columnIndex, rowIndex );
      default:
        return new EditorDayTime( columnIndex, rowIndex );
    }
  }

  /****************************************** setValue *******************************************/
  @Override
  public void setValue( int columnIndex, int rowIndex, Object newValue )
  {
    // if new value equals old value, exit with no command
    Day day = JPlanner.plan.getDay( rowIndex );
    Object oldValue = day.getValue( columnIndex );
    if ( newValue.equals( oldValue ) )
      return;

    // special command for setting number of work periods, otherwise generic
    if ( columnIndex == Day.SECTION_PERIODS )
      JPlanner.plan.getUndostack().push( new CommandDaySetNumPeriods( day, (int) newValue, (int) oldValue ) );
    else
      JPlanner.plan.getUndostack().push( new CommandDaySetValue( day, columnIndex, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int columnIndex, int rowIndex )
  {
    // return cell value
    return JPlanner.plan.getDay( rowIndex ).getValue( columnIndex );
  }

  /***************************************** getCellText *****************************************/
  @Override
  public String getCellText( int columnIndex, int rowIndex )
  {
    // get value to be displayed
    Object value = getValue( columnIndex, rowIndex );

    // convert times into string using "HH:MM" formats
    if ( value instanceof Time )
      return ( (Time) value ).toStringShort();

    // display work with two decimal places
    if ( columnIndex == Day.SECTION_WORK )
      return String.format( "%.2f", value );

    // return cell display text
    return ( value == null ? null : value.toString() );
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
