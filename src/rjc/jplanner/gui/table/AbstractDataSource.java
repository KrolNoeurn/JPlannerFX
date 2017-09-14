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

package rjc.jplanner.gui.table;

import java.util.Set;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.gui.table.Table.Alignment;
import rjc.jplanner.model.Calendar;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.DateTime;
import rjc.jplanner.model.Day;
import rjc.jplanner.model.Time;

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
  public Object getValue( int columnIndex, int row )
  {
    // return cell value for specified cell index
    return "?";
  }

  /****************************************** setValue *******************************************/
  public void setValue( int columnIndex, int row, Object newValue )
  {
    // set cell value for specified cell index
  }

  protected boolean equal( Object newValue, Object oldValue )
  {
    if ( newValue != null && newValue.equals( oldValue ) )
      return true;
    if ( newValue == null && oldValue == null )
      return true;
    return false;
  }

  /****************************************** getEditor ******************************************/
  public AbstractCellEditor getEditor( int columnIndex, int row )
  {
    // return editor to use for specified cell index
    return null;
  }

  /************************************** getCellAlignment ***************************************/
  public Alignment getCellAlignment( int columnIndex, int row )
  {
    // return cell contents alignment for specified cell index
    return Alignment.LEFT;
  }

  /************************************** getCellBackground **************************************/
  public Paint getCellBackground( int columnIndex, int row )
  {
    // return cell background colour for specified cell index
    return Colors.NORMAL_CELL;
  }

  /***************************************** getCellText *****************************************/
  public String getCellText( int columnIndex, int row )
  {
    // return cell display text for specified cell index
    Object value = getValue( columnIndex, row );
    if ( value == null )
      return null;

    // convert common data types into strings using plan formats
    if ( value instanceof Day )
      return ( (Day) value ).getName();
    if ( value instanceof Calendar )
      return ( (Calendar) value ).getName();
    if ( value instanceof DateTime )
      return ( (DateTime) value ).toFormat();
    if ( value instanceof Date )
      return ( (Date) value ).toFormat();
    if ( value instanceof Time )
      return ( (Time) value ).toStringShort();

    // convert doubles without unnecessary decimal 0
    if ( value instanceof Double )
      if ( (double) value == Math.rint( (double) value ) )
        return String.format( "%d", (long) ( (double) value ) );

    // return cell display text
    return value.toString();
  }

  /***************************************** getCellFont *****************************************/
  public Font getCellFont( int columnIndex, int row )
  {
    // return cell display font for specified cell index
    return Font.getDefault();
  }

  /**************************************** getCellIndent ****************************************/
  public int getCellIndent( int columnIndex, int row )
  {
    // return cell display indent level for specified cell index
    return 0;
  }

  /************************************** getSummaryEndRow ***************************************/
  public int getSummaryEndRow( int columnIndex, int row )
  {
    // return cell summary end row for specified cell index (or -1 if not summary)
    return -1;
  }

  /**************************************** isCellSummary ****************************************/
  public boolean isCellSummary( int columnIndex, int row )
  {
    // return true is cell is a collapsible summary
    return getSummaryEndRow( columnIndex, row ) > 0;
  }

  /***************************************** indentRows ******************************************/
  public Set<Integer> indentRows( Set<Integer> rows )
  {
    // return the subset of rows indented
    return null;
  }

  /***************************************** outdentRows *****************************************/
  public Set<Integer> outdentRows( Set<Integer> rows )
  {
    // return the subset of rows outdented
    return null;
  }

  /********************************** defaultTableModifications **********************************/
  public void defaultTableModifications( Table table )
  {
    // perform table data source default table modifications
  }

  /**************************************** getMoveEndRow ****************************************/
  public int getMoveEndRow( int startRow )
  {
    // return move end row (for example bottom of summary) for given start row, or -1 if moving not allowed 
    return -1;
  }

  /******************************************* setNull *******************************************/
  public Set<Integer> setNull( Set<Integer> cells )
  {
    // return set of cell hashes ( columnIndex * SELECT_HASH + row ) set to null
    return null;
  }

}