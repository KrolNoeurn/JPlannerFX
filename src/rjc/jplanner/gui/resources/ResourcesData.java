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

package rjc.jplanner.gui.resources;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandResourceSetValue;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.gui.table.ITableDataSource;
import rjc.jplanner.gui.table.Table.Alignment;
import rjc.jplanner.gui.table.TableCanvas;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.Resource;

/*************************************************************************************************/
/**************************** Table data source for showing resources ****************************/
/*************************************************************************************************/

public class ResourcesData implements ITableDataSource
{

  /************************************** getColumnCount *****************************************/
  @Override
  public int getColumnCount()
  {
    return Resource.SECTION_MAX + 1;
  }

  /**************************************** getRowCount ******************************************/
  @Override
  public int getRowCount()
  {
    return JPlanner.plan.resourcesCount();
  }

  /************************************** getColumnTitle *****************************************/
  @Override
  public String getColumnTitle( int columnIndex )
  {
    return Resource.sectionName( columnIndex );
  }

  /**************************************** getRowTitle ******************************************/
  @Override
  public String getRowTitle( int rowIndex )
  {
    return Integer.toString( rowIndex );
  }

  /************************************* getCellAlignment ****************************************/
  @Override
  public Alignment getCellAlignment( int columnIndex, int rowIndex )
  {
    return Alignment.MIDDLE;
  }

  /************************************* getCellBackground ***************************************/
  @Override
  public Paint getCellBackground( int columnIndex, int rowIndex )
  {
    // all cells are normal coloured except if null resource
    Resource res = JPlanner.plan.resource( rowIndex );
    if ( columnIndex != Resource.SECTION_INITIALS && res.isNull() )
      return TableCanvas.COLOR_DISABLED_CELL;

    return TableCanvas.COLOR_NORMAL_CELL;
  }

  /***************************************** getEditor *******************************************/
  @Override
  public AbstractCellEditor getEditor( int columnIndex, int rowIndex )
  {
    // return null if cell is not editable
    Resource res = JPlanner.plan.resource( rowIndex );
    if ( columnIndex != Resource.SECTION_INITIALS && res.isNull() )
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

    Resource res = JPlanner.plan.resource( rowIndex );
    JPlanner.plan.undostack().push( new CommandResourceSetValue( res, columnIndex, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int columnIndex, int rowIndex )
  {
    return JPlanner.plan.resource( rowIndex ).getValue( columnIndex );
  }

  /***************************************** getCellText *****************************************/
  @Override
  public String getCellText( int columnIndex, int rowIndex )
  {
    // get value to be displayed
    Object value = getValue( columnIndex, rowIndex );

    // convert date and date-times into strings using plan formats
    if ( value instanceof DateTime )
      return ( (DateTime) value ).toString( JPlanner.plan.datetimeFormat() );
    if ( value instanceof Date )
      return ( (Date) value ).toString( JPlanner.plan.dateFormat() );

    // return cell display text
    return ( value == null ? null : value.toString() );
  }

  /***************************************** getCellFont *****************************************/
  @Override
  public Font getCellFont( int columnIndex, int rowIndex )
  {
    // return cell display font
    return null;
  }

}
