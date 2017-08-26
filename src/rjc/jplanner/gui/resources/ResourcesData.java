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

package rjc.jplanner.gui.resources;

import javafx.scene.paint.Paint;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandResourceSetValue;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.gui.calendars.EditorSelectCalendar;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.gui.table.AbstractDataSource;
import rjc.jplanner.gui.table.EditorDate;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.gui.table.Table.Alignment;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.Resource;

/*************************************************************************************************/
/**************************** Table data source for showing resources ****************************/
/*************************************************************************************************/

class ResourcesData extends AbstractDataSource
{

  /************************************** getColumnCount *****************************************/
  @Override
  public int getColumnCount()
  {
    // return number of columns
    return Resource.SECTION_MAX + 1;
  }

  /**************************************** getRowCount ******************************************/
  @Override
  public int getRowCount()
  {
    // return number of rows
    return JPlanner.plan.getResourcesCount();
  }

  /************************************** getColumnTitle *****************************************/
  @Override
  public String getColumnTitle( int columnIndex )
  {
    // return column title
    return Resource.getSectionName( columnIndex );
  }

  /**************************************** getRowTitle ******************************************/
  @Override
  public String getRowTitle( int row )
  {
    // return row title
    return Integer.toString( row );
  }

  /************************************* getCellAlignment ****************************************/
  @Override
  public Alignment getCellAlignment( int columnIndex, int row )
  {
    // return alignment based on column
    switch ( columnIndex )
    {
      case Resource.SECTION_COMMENT:
        return Alignment.LEFT;
      default:
        return Alignment.MIDDLE;
    }
  }

  /************************************* getCellBackground ***************************************/
  @Override
  public Paint getCellBackground( int columnIndex, int row )
  {
    // all cells are normal coloured except if null resource
    Resource res = JPlanner.plan.getResource( row );
    if ( columnIndex != Resource.SECTION_INITIALS && res.isNull() )
      return Colors.DISABLED_CELL;

    return Colors.NORMAL_CELL;
  }

  /***************************************** getEditor *******************************************/
  @Override
  public AbstractCellEditor getEditor( int columnIndex, int row )
  {
    // return null if cell is not editable
    Resource res = JPlanner.plan.getResource( row );
    if ( columnIndex != Resource.SECTION_INITIALS && res.isNull() )
      return null;

    // return editor for table body cell
    switch ( columnIndex )
    {
      case Resource.SECTION_INITIALS:
        return new EditorResourceInitials( columnIndex, row );
      case Resource.SECTION_CALENDAR:
        return new EditorSelectCalendar( columnIndex, row );
      case Resource.SECTION_COMMENT:
        return new EditorText( columnIndex, row );
      case Resource.SECTION_AVAIL:
        return new EditorAvailable( columnIndex, row );

      case Resource.SECTION_START:
        EditorDate dateEditor = new EditorDate( columnIndex, row );
        dateEditor.addListener( ( property, oldText, newText ) ->
        {
          // only check start <= end if not already in error state
          if ( !JPlanner.isError( dateEditor.getControl() ) )
          {
            Date start = (Date) dateEditor.getValue();
            Date end = JPlanner.plan.getResource( row ).getEnd().getDate().plusDays( -1 );
            if ( start != null && end.isLessThan( start ) )
              JPlanner.setError( dateEditor.getControl(), "Start is after end '" + end.toFormat() + "'" );
          }
        } );
        return dateEditor;

      case Resource.SECTION_END:
        dateEditor = new EditorDate( columnIndex, row );
        dateEditor.addListener( ( property, oldText, newText ) ->
        {
          // only check end >= start if not already in error state
          if ( !JPlanner.isError( dateEditor.getControl() ) )
          {
            Date end = (Date) dateEditor.getValue();
            Date start = JPlanner.plan.getResource( row ).getStart().getDate();
            if ( end != null && end.isLessThan( start ) )
              JPlanner.setError( dateEditor.getControl(), "End is before start '" + start.toFormat() + "'" );
          }
        } );
        return dateEditor;

      default:
        // default text editor is fine for other columns except do not allow initials duplicates, square brackets or comma
        EditorText textEditor = new EditorText( columnIndex, row );
        textEditor.setAllowed( "^[^\\[\\],]*$" );
        textEditor.addListener( ( observable, oldText, newText ) ->
        {
          // must not be an initials duplicate
          String error = JPlanner.plan.resources.initialsClash( newText, -1 );
          JPlanner.setError( textEditor.getControl(), error );
        } );
        return textEditor;
    }

  }

  /****************************************** setValue *******************************************/
  @Override
  public void setValue( int columnIndex, int row, Object newValue )
  {
    // if new value equals old value, exit with no command
    Resource res = JPlanner.plan.getResource( row );
    Object oldValue = res.getValue( columnIndex );
    if ( equal( newValue, oldValue ) )
      return;

    JPlanner.plan.getUndostack().push( new CommandResourceSetValue( res, columnIndex, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int columnIndex, int row )
  {
    // return cell value
    return JPlanner.plan.getResource( row ).getValue( columnIndex );
  }

  /********************************** defaultTableModifications **********************************/
  @Override
  public void defaultTableModifications( Table table )
  {
    // default resource table modifications
    table.setDefaultColumnWidth( 100 );

    table.setWidthByColumnIndex( Resource.SECTION_INITIALS, 100 );
    table.setWidthByColumnIndex( Resource.SECTION_AVAIL, 70 );
    table.setWidthByColumnIndex( Resource.SECTION_COST, 70 );
    table.setWidthByColumnIndex( Resource.SECTION_COMMENT, 250 );

    table.hideRow( 0 ); // hide row 0 (the special 'unassigned' resource)
  }

}
