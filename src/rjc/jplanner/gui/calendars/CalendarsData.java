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

package rjc.jplanner.gui.calendars;

import javafx.scene.paint.Paint;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandCalendarSetCycleLength;
import rjc.jplanner.command.CommandCalendarSetExceptions;
import rjc.jplanner.command.CommandCalendarSetValue;
import rjc.jplanner.gui.table.Body;
import rjc.jplanner.gui.table.Cell.Alignment;
import rjc.jplanner.gui.table.CellEditor;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.gui.table.ITableDataSource;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.model.Calendar;

/*************************************************************************************************/
/**************************** Table data source for showing calendars ****************************/
/*************************************************************************************************/

public class CalendarsData implements ITableDataSource
{

  /************************************** getColumnCount *****************************************/
  @Override
  public int getColumnCount()
  {
    // return number of calendars in plan
    return JPlanner.plan.calendarsCount();
  }

  /**************************************** getRowCount ******************************************/
  @Override
  public int getRowCount()
  {
    // table row count is max number of normals + SECTION_NORMAL1
    int max = 0;
    for ( int i = 0; i < getColumnCount(); i++ )
      if ( JPlanner.plan.calendar( i ).numNormals() > max )
        max = JPlanner.plan.calendar( i ).numNormals();

    return max + Calendar.SECTION_NORMAL1;
  }

  /************************************** getColumnTitle *****************************************/
  @Override
  public String getColumnTitle( int column )
  {
    return "Calendar " + ( column + 1 );
  }

  /**************************************** getRowTitle ******************************************/
  @Override
  public String getRowTitle( int row )
  {
    return Calendar.sectionName( row );
  }

  /**************************************** getCellText ******************************************/
  @Override
  public String getCellText( int column, int row )
  {
    return JPlanner.plan.calendar( column ).toString( row );
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
    // all cells are normal coloured except unused normal section cells
    Calendar cal = JPlanner.plan.calendar( column );
    if ( row >= cal.numNormals() + Calendar.SECTION_NORMAL1 )
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

    // special command for setting exceptions & cycle-length, otherwise generic
    if ( row == Calendar.SECTION_EXCEPTIONS )
      JPlanner.plan.undostack().push( new CommandCalendarSetExceptions( column, newValue, oldValue ) );
    else if ( row == Calendar.SECTION_CYCLE )
      JPlanner.plan.undostack().push( new CommandCalendarSetCycleLength( column, newValue, oldValue ) );
    else
      JPlanner.plan.undostack().push( new CommandCalendarSetValue( column, row, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int column, int row )
  {
    return getCellText( column, row );
  }

}
