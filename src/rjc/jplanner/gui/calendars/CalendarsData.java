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
import rjc.jplanner.gui.table.Cell.Alignment;
import rjc.jplanner.gui.table.ITableDataSource;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.model.Calendar;

/*************************************************************************************************/
/****************************** Table data source for showing tasks ******************************/
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
    // all cells are white except unused normal cells
    Calendar cal = JPlanner.plan.calendar( column );
    if ( row >= cal.numNormals() + Calendar.SECTION_NORMAL1 )
      return Table.COLOR_DISABLED_CELL;

    return Table.COLOR_NORMAL_CELL;
  }

}