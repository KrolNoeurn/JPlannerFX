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

package rjc.jplanner.gui.table;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.gui.table.Table.Alignment;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.Day;

/*************************************************************************************************/
/***************************** Abstract class for table data source ******************************/
/*************************************************************************************************/

abstract public class AbstractDataSource
{
  /*************************************** getColumnCount ****************************************/
  public int getColumnCount()
  {
    // return number of columns to be displayed in table
    return 3;
  }

  /**************************************** getRowCount ******************************************/
  public int getRowCount()
  {
    // return number of rows to be displayed in table
    return 3;
  }

  /************************************** getColumnTitle *****************************************/
  public String getColumnTitle( int columnIndex )
  {
    // return column title for specified column index
    return "?";
  }

  /**************************************** getRowTitle ******************************************/
  public String getRowTitle( int row )
  {
    // return row title for specified row
    return "?";
  }

  /****************************************** getValue *******************************************/
  public Object getValue( int columnIndex, int rowIndex )
  {
    // return cell value for specified cell index
    return "?";
  }

  /****************************************** setValue *******************************************/
  public void setValue( int columnIndex, int rowIndex, Object newValue )
  {
    // set cell value for specified cell index
  }

  /***************************************** getEditor *******************************************/
  public AbstractCellEditor getEditor( int columnIndex, int rowIndex )
  {
    // return editor to use for specified cell index
    return null;
  }

  /************************************* getCellAlignment ****************************************/
  public Alignment getCellAlignment( int columnIndex, int rowIndex )
  {
    // return cell contents alignment for specified cell index
    return Alignment.LEFT;
  }

  /************************************* getCellBackground ***************************************/
  public Paint getCellBackground( int columnIndex, int rowIndex )
  {
    // return cell background colour for specified cell index
    return Colors.NORMAL_CELL;
  }

  /***************************************** getCellText *****************************************/
  public String getCellText( int columnIndex, int rowIndex )
  {
    // return cell display text for specified cell index
    Object value = getValue( columnIndex, rowIndex );

    // convert date and date-times into strings using plan formats
    if ( value instanceof Day )
      return ( (Day) value ).name();
    if ( value instanceof Calendar )
      return ( (Calendar) value ).getName();
    if ( value instanceof DateTime )
      return ( (DateTime) value ).toString( JPlanner.plan.getDateTimeFormat() );
    if ( value instanceof Date )
      return ( (Date) value ).toString( JPlanner.plan.getDateFormat() );

    // return cell display text
    return ( value == null ? null : value.toString() );
  }

  /***************************************** getCellFont *****************************************/
  public Font getCellFont( int columnIndex, int rowIndex )
  {
    // return cell display font for specified cell index
    return Font.getDefault();
  }

  /**************************************** getCellIndent ****************************************/
  public int getCellIndent( int columnIndex, int rowIndex )
  {
    // return cell display indent level for specified cell index
    return 0;
  }

  /************************************** getSummaryEndRow ***************************************/
  public int getSummaryEndRow( int columnIndex, int rowIndex )
  {
    // return cell summary end row for specified cell index (or -1 if not summary)
    return -1;
  }

  /**************************************** isCellSummary ****************************************/
  public boolean isCellSummary( int columnIndex, int rowIndex )
  {
    // return true is cell is a collapsible summary
    return getSummaryEndRow( columnIndex, rowIndex ) > 0;
  }

  /********************************** defaultTableModifications **********************************/
  public void defaultTableModifications( Table table )
  {
    // perform table data source default table modifications
  }

}