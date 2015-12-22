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

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.table.Cell.Alignment;
import rjc.jplanner.gui.table.ITableDataSource;
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
    return "TBD";
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
    return Color.WHITE;
  }

}
