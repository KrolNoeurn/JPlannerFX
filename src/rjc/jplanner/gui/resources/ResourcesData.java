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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Paint;
import javafx.stage.Window;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandDeleteMultipleValues;
import rjc.jplanner.command.CommandResourceSetValue;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.gui.calendars.EditorSelectCalendar;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.gui.table.AbstractDataSource;
import rjc.jplanner.gui.table.EditorDate;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.gui.table.Table.Alignment;
import rjc.jplanner.gui.table.TableSelection;
import rjc.jplanner.model.Date;
import rjc.jplanner.model.Resource;
import rjc.jplanner.model.Task;
import rjc.jplanner.model.TaskResources;

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
        dateEditor.addEpochdayListener( ( property, oldNumber, newNumber ) ->
        {
          // only check start <= end if not already in error state
          if ( !JPlanner.isError( dateEditor.getControl() ) )
          {
            int start = newNumber.intValue();
            Date end = JPlanner.plan.getResource( row ).getEnd().getDate().plusDays( -1 );
            if ( start > end.getEpochday() )
              JPlanner.setError( dateEditor.getControl(), "Start is after end '" + end.toFormat() + "'" );
          }
        } );
        return dateEditor;

      case Resource.SECTION_END:
        dateEditor = new EditorDate( columnIndex, row );
        dateEditor.addEpochdayListener( ( property, oldNumber, newNumber ) ->
        {
          // only check end >= start if not already in error state
          if ( !JPlanner.isError( dateEditor.getControl() ) )
          {
            int end = newNumber.intValue();
            Date start = JPlanner.plan.getResource( row ).getStart().getDate();
            if ( end < start.getEpochday() )
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
          if ( error == null )
            JPlanner.setNoError( textEditor.getControl(), "" );
          else
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

  /******************************************* setNull *******************************************/
  @Override
  public Set<Integer> setNull( Set<Integer> cells )
  {
    // if initials being deleted, also delete name, org, group, role and alias
    for ( int hash : cells )
      if ( hash / TableSelection.SELECT_HASH == Resource.SECTION_INITIALS )
      {
        int base = hash - Resource.SECTION_INITIALS * TableSelection.SELECT_HASH;
        cells.add( base + Resource.SECTION_NAME * TableSelection.SELECT_HASH );
        cells.add( base + Resource.SECTION_ORG * TableSelection.SELECT_HASH );
        cells.add( base + Resource.SECTION_GROUP * TableSelection.SELECT_HASH );
        cells.add( base + Resource.SECTION_ROLE * TableSelection.SELECT_HASH );
        cells.add( base + Resource.SECTION_ALIAS * TableSelection.SELECT_HASH );
      }

    // create undo command to set permitted values to null (delete contents)
    HashSet<Integer> allowed = new HashSet<Integer>();
    HashMap<String, ArrayList<Integer>> tags = new HashMap<String, ArrayList<Integer>>();

    // go through set of cells to determine which allowed 
    for ( int hash : cells )
    {
      int row = hash % TableSelection.SELECT_HASH;
      int columnIndex = hash / TableSelection.SELECT_HASH;

      switch ( columnIndex )
      {
        case Resource.SECTION_INITIALS:
        case Resource.SECTION_NAME:
        case Resource.SECTION_ORG:
        case Resource.SECTION_GROUP:
        case Resource.SECTION_ROLE:
        case Resource.SECTION_ALIAS:
          // add to list of tags to check later
          String tag = (String) JPlanner.plan.getResource( row ).getValue( columnIndex );
          ArrayList<Integer> list = tags.get( tag );
          if ( list == null )
          {
            list = new ArrayList<Integer>();
            tags.put( tag, list );
          }
          list.add( hash );
          break;

        case Resource.SECTION_START:
        case Resource.SECTION_END:
        case Resource.SECTION_COMMENT:
          // always allow
          allowed.add( hash );
          break;

        default:
          // do now allow
      }
    }

    // check tags
    Window focusedWindow = JPlanner.gui.getFocusWindow();
    for ( String tag : tags.keySet() )
    {
      // if not all occurrences of tag are being deleted, then deletion allowed without further checks
      ArrayList<Integer> list = tags.get( tag );
      if ( list.size() < JPlanner.plan.resources.tagCount( tag ) )
        allowed.addAll( list );
      else
      {
        // if tag not used in task resources, then deletion allowed without further checks
        HashMap<Task, TaskResources> use = JPlanner.plan.tasks.getTaskResources( tag );
        if ( use.isEmpty() )
          allowed.addAll( list );
        else
        {
          // build sorted list of task indexes
          TreeSet<Integer> tasks = new TreeSet<Integer>();
          for ( Task task : use.keySet() )
            tasks.add( task.getIndex() );

          // build string showing task indexes
          StringBuilder useString = new StringBuilder( "Task" );
          useString.append( tasks.size() > 1 ? "s " : " " );
          for ( int index : tasks )
            useString.append( index + ", " );
          useString.setLength( useString.length() - 2 );

          // ask user if deleting permitted or skip or cancel whole
          Alert dialog = new Alert( AlertType.CONFIRMATION );
          dialog.initOwner( focusedWindow );
          dialog.setHeaderText( "Do you want to delete used resource name '" + tag + "' ?" );
          dialog.setContentText( "'" + tag + "' will be removed from " + useString + " resourcing." );
          ButtonType delete = new ButtonType( "_Delete", ButtonData.YES );
          ButtonType skip = new ButtonType( "_Skip", ButtonData.NO );
          dialog.getButtonTypes().setAll( delete, skip, ButtonType.CANCEL );
          Optional<ButtonType> result = dialog.showAndWait();
          focusedWindow.requestFocus();

          if ( result.get() == delete ) // deleting tag is permitted
            allowed.addAll( list );
          if ( result.get() == ButtonType.CANCEL ) // cancel so abandon all deleting
            return null;
        }
      }
    }

    // if one or more cells are allowed to be set to null, create undo command
    if ( !allowed.isEmpty() )
      JPlanner.plan.getUndostack().push( new CommandDeleteMultipleValues( this, allowed ) );

    return cells;
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
