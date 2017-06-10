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

package rjc.jplanner.gui.tasks;

import java.util.Set;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import rjc.jplanner.JPlanner;
import rjc.jplanner.command.CommandTaskIndent;
import rjc.jplanner.command.CommandTaskOutdent;
import rjc.jplanner.command.CommandTaskSetValue;
import rjc.jplanner.gui.Colors;
import rjc.jplanner.gui.gantt.Gantt;
import rjc.jplanner.gui.table.AbstractCellEditor;
import rjc.jplanner.gui.table.AbstractDataSource;
import rjc.jplanner.gui.table.EditorDateTime;
import rjc.jplanner.gui.table.EditorText;
import rjc.jplanner.gui.table.EditorTimeSpan;
import rjc.jplanner.gui.table.Table;
import rjc.jplanner.gui.table.Table.Alignment;
import rjc.jplanner.model.Task;

/*************************************************************************************************/
/****************************** Table data source for showing tasks ******************************/
/*************************************************************************************************/

class TasksData extends AbstractDataSource
{

  /************************************** getColumnCount *****************************************/
  @Override
  public int getColumnCount()
  {
    // return number of columns
    return Task.SECTION_MAX + 1;
  }

  /**************************************** getRowCount ******************************************/
  @Override
  public int getRowCount()
  {
    // return number of rows
    return JPlanner.plan.getTasksCount();
  }

  /************************************** getColumnTitle *****************************************/
  @Override
  public String getColumnTitle( int columnIndex )
  {
    // return column title
    return Task.getSectionName( columnIndex );
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
      case Task.SECTION_DURATION:
      case Task.SECTION_WORK:
        return Alignment.RIGHT;
      case Task.SECTION_TITLE:
      case Task.SECTION_PRED:
      case Task.SECTION_RES:
      case Task.SECTION_COMMENT:
        return Alignment.LEFT;
      default:
        return Alignment.MIDDLE;
    }
  }

  /************************************* getCellBackground ***************************************/
  @Override
  public Paint getCellBackground( int columnIndex, int row )
  {
    // cell colour determined by if editable
    if ( JPlanner.plan.getTask( row ).isSectionEditable( columnIndex ) )
      return Colors.NORMAL_CELL;

    return Colors.DISABLED_CELL;

  }

  /***************************************** getEditor *******************************************/
  @Override
  public AbstractCellEditor getEditor( int columnIndex, int row )
  {
    // return null if cell is not editable
    if ( !JPlanner.plan.getTask( row ).isSectionEditable( columnIndex ) )
      return null;

    // return editor for table body cell
    switch ( columnIndex )
    {
      case Task.SECTION_DURATION:
      case Task.SECTION_WORK:
        return new EditorTimeSpan( columnIndex, row );
      case Task.SECTION_START:
      case Task.SECTION_END:
        return new EditorDateTime( columnIndex, row );
      case Task.SECTION_PRIORITY:
        return new EditorTaskPriority( columnIndex, row );
      case Task.SECTION_TYPE:
        return new EditorTaskType( columnIndex, row );
      case Task.SECTION_PRED:
        return new EditorTaskPredecessors( columnIndex, row );
      case Task.SECTION_RES:
        return new EditorTaskResources( columnIndex, row );
      default:
        return new EditorText( columnIndex, row );
    }
  }

  /****************************************** setValue *******************************************/
  @Override
  public void setValue( int columnIndex, int row, Object newValue )
  {
    // if new value equals old value, exit with no command
    Task task = JPlanner.plan.getTask( row );
    Object oldValue = task.getValue( columnIndex );
    if ( newValue.equals( oldValue ) )
      return;

    JPlanner.plan.getUndostack().push( new CommandTaskSetValue( task, columnIndex, newValue, oldValue ) );
  }

  /****************************************** getValue *******************************************/
  @Override
  public Object getValue( int columnIndex, int row )
  {
    // return cell value
    return JPlanner.plan.getTask( row ).getValue( columnIndex );
  }

  /***************************************** getCellFont *****************************************/
  @Override
  public Font getCellFont( int columnIndex, int row )
  {
    // return cell display font, bold for summary tasks
    if ( JPlanner.plan.getTask( row ).isSummary() )
      return Font.font( Font.getDefault().getFamily(), FontWeight.BOLD, Font.getDefault().getSize() );

    return Font.getDefault();
  }

  /**************************************** getCellIndent ****************************************/
  @Override
  public int getCellIndent( int columnIndex, int row )
  {
    // return cell indent level (0 = no indent)
    if ( columnIndex == Task.SECTION_TITLE )
      return JPlanner.plan.getTask( row ).getIndent() + 1;

    return 0;
  }

  /************************************** getSummaryEndRow ***************************************/
  @Override
  public int getSummaryEndRow( int columnIndex, int row )
  {
    // return cell summary end row for specified cell index (or -1 if not summary)
    if ( columnIndex == Task.SECTION_TITLE )
      return JPlanner.plan.getTask( row ).getSummaryEnd();

    return -1;
  }

  /********************************** defaultTableModifications **********************************/
  @Override
  public void defaultTableModifications( Table table )
  {
    // default task table modifications
    table.setHorizontalHeaderHeight( Gantt.GANTTSCALE_HEIGHT * 2 );
    table.setVerticalHeaderWidth( 34 );
    table.setDefaultColumnWidth( 110 );

    table.setWidthByColumnIndex( Task.SECTION_TITLE, 200 );
    table.setWidthByColumnIndex( Task.SECTION_DURATION, 60 );
    table.setWidthByColumnIndex( Task.SECTION_START, 140 );
    table.setWidthByColumnIndex( Task.SECTION_END, 140 );
    table.setWidthByColumnIndex( Task.SECTION_WORK, 60 );
    table.setWidthByColumnIndex( Task.SECTION_PRIORITY, 60 );
    table.setWidthByColumnIndex( Task.SECTION_DEADLINE, 140 );
    table.setWidthByColumnIndex( Task.SECTION_COMMENT, 140 );

    table.hideRow( 0 ); // hide row 0 (the overall project summary)
  }

  /***************************************** indentRows ******************************************/
  @Override
  public Set<Integer> indentRows( Set<Integer> rows )
  {
    // return the subset of rows indented
    Set<Integer> indent = JPlanner.plan.tasks.canIndent( rows );
    if ( !indent.isEmpty() )
      JPlanner.plan.getUndostack().push( new CommandTaskIndent( indent ) );

    return indent;
  }

  /***************************************** outdentRows *****************************************/
  @Override
  public Set<Integer> outdentRows( Set<Integer> rows )
  {
    // return the subset of rows outdented
    Set<Integer> outdent = JPlanner.plan.tasks.canOutdent( rows );
    if ( !outdent.isEmpty() )
      JPlanner.plan.getUndostack().push( new CommandTaskOutdent( outdent ) );

    return outdent;
  }

  /**************************************** getMoveEndRow ****************************************/
  @Override
  public int getMoveEndRow( int startRow )
  {
    // if summary return summary end, otherwise same row
    int end = JPlanner.plan.getTask( startRow ).getSummaryEnd();
    if ( end > 0 )
      return end;

    return startRow;
  }

  /***************************************** getCellText *****************************************/
  @Override
  public String getCellText( int columnIndex, int row )
  {
    // if summary return special cell text
    if ( columnIndex == Task.SECTION_TYPE && JPlanner.plan.getTask( row ).isSummary() )
      return "Summary";
    if ( columnIndex == Task.SECTION_PRIORITY && JPlanner.plan.getTask( row ).isSummary() )
      return "-";

    // otherwise return default cell text
    return super.getCellText( columnIndex, row );
  }

}
