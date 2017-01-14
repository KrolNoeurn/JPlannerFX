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

package rjc.jplanner.model;

import java.util.ArrayList;
import java.util.Iterator;

import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/********************** Task predecessors shows dependencies on other tasks **********************/
/*************************************************************************************************/

public class Predecessors
{
  public static final String TYPE_FINISH_START  = "FS";
  public static final String TYPE_START_START   = "SS";
  public static final String TYPE_START_FINISH  = "SF";
  public static final String TYPE_FINISH_FINISH = "FF";
  public static final String TYPE_DEFAULT       = TYPE_FINISH_START;

  public class Predecessor
  {
    public Task     task;
    public String   type;
    public TimeSpan lag;

    @Override
    public String toString()
    {
      String str = Integer.toString( task.index() );
      if ( type != TYPE_DEFAULT || lag.getNumber() != 0.0 )
      {
        str += type;
        if ( lag.getNumber() > 0.0 )
          str += "+";
        if ( lag.getNumber() != 0.0 )
          str += lag.toString();
      }
      return str;
    }
  }

  private ArrayList<Predecessor> m_preds = new ArrayList<Predecessor>();

  /**************************************** constructor ******************************************/
  public Predecessors()
  {
    // nothing needs to be done
  }

  /**************************************** constructor ******************************************/
  public Predecessors( String text )
  {
    // split text into individual predecessors
    for ( String part : text.split( "," ) )
    {
      // split part into task, predecessor type and lag
      part = part.trim();
      if ( part.isEmpty() )
        continue;

      int digit = 0;
      while ( part.length() > digit && Character.isDigit( part.charAt( digit ) ) )
        digit++;

      int taskNum = Integer.parseInt( part.substring( 0, digit ) );
      String type = TYPE_DEFAULT;
      TimeSpan lag = new TimeSpan();

      part = part.substring( digit );
      part = part.trim();
      if ( !part.isEmpty() )
      {
        String start = part.substring( 0, 2 );

        if ( start.equalsIgnoreCase( TYPE_FINISH_START ) )
          type = TYPE_FINISH_START;
        if ( start.equalsIgnoreCase( TYPE_START_START ) )
          type = TYPE_START_START;
        if ( start.equalsIgnoreCase( TYPE_START_FINISH ) )
          type = TYPE_START_FINISH;
        if ( start.equalsIgnoreCase( TYPE_FINISH_FINISH ) )
          type = TYPE_FINISH_FINISH;

        lag = new TimeSpan( part.substring( 2 ) );
      }

      Predecessor pred = new Predecessor();
      pred.task = JPlanner.plan.task( taskNum );
      pred.type = type;
      pred.lag = lag;
      m_preds.add( pred );
    }

  }

  /***************************************** toString ********************************************/
  @Override
  public String toString()
  {
    String str = "";

    // build up string equivalent
    for ( Predecessor pred : m_preds )
      str += pred + ", ";

    // remove final ", " and return string equivalent
    if ( str.length() > 1 )
      str = str.substring( 0, str.length() - 2 );
    return str;
  }

  /************************************** hasPredecessor *****************************************/
  public boolean hasPredecessor( Task other )
  {
    // return true if task is a predecessor
    for ( Predecessor pred : m_preds )
    {
      if ( pred.task == other )
        return true;

      if ( pred.task.hasPredecessor( other ) )
        return true;
    }

    return false;
  }

  /******************************************** errors *******************************************/
  public static String errors( String text, int thisTaskNum )
  {
    // split text into individual predecessors
    StringBuilder error = new StringBuilder();
    for ( String part : text.split( "," ) )
    {
      // if blank part, skip
      if ( part.length() == 0 )
        continue;

      // split part into task, predecessor type and lag
      part = part.trim();
      int digit = 0;
      while ( part.length() > digit && Character.isDigit( part.charAt( digit ) ) )
        digit++;

      // check start is number
      if ( digit == 0 )
      {
        error.append( '\'' ).append( part ).append( "' does not start with a valid task number.  " );
        continue;
      }

      // check number is non-null task
      int taskNum = Integer.parseInt( part.substring( 0, digit ) );
      if ( taskNum >= JPlanner.plan.tasksCount() || JPlanner.plan.task( taskNum ).isNull() )
      {
        error.append( '\'' ).append( taskNum ).append( "' is a null task.  " );
        continue;
      }

      // check number is not zero
      if ( taskNum == 0 )
      {
        error.append( '\'' ).append( part ).append( "' does not start with a valid task number.  " );
        continue;
      }

      // check number is not sub-task if this task is a summary
      if ( JPlanner.plan.task( thisTaskNum ).isSummary() && taskNum > thisTaskNum
          && taskNum <= JPlanner.plan.task( thisTaskNum ).summaryEnd() )
      {
        error.append( '\'' ).append( taskNum ).append( "' is a sub-task of this summary.  " );
        continue;
      }

      // check number is not summary containing this task
      if ( JPlanner.plan.task( taskNum ).isSummary() && thisTaskNum > taskNum
          && thisTaskNum <= JPlanner.plan.task( taskNum ).summaryEnd() )
      {
        error.append( '\'' ).append( taskNum ).append( "' is a summary containing this sub-task.  " );
        continue;
      }

      // check number is not this task
      if ( taskNum == thisTaskNum )
      {
        error.append( '\'' ).append( taskNum ).append( "' is a reference to this task.  " );
        continue;
      }

      // check number is does not cause circular reference
      if ( JPlanner.plan.task( taskNum ).hasPredecessor( JPlanner.plan.task( thisTaskNum ) ) )
      {
        error.append( '\'' ).append( taskNum ).append( "' gives a circular reference to this task.  " );
        continue;
      }

      // check nothing remains or is a valid type
      part = part.substring( digit ).trim();
      if ( part.isEmpty() )
        continue;

      String start;
      if ( part.length() < 2 )
        start = part;
      else
        start = part.substring( 0, 2 );

      if ( !start.equalsIgnoreCase( TYPE_FINISH_START ) && !start.equalsIgnoreCase( TYPE_START_START )
          && !start.equalsIgnoreCase( TYPE_START_FINISH ) && !start.equalsIgnoreCase( TYPE_FINISH_FINISH ) )
      {
        error.append( '\'' ).append( part ).append( "' is not a valid dependency type.  " );
        continue;
      }

      // check nothing remains or is valid time-span
      part = part.substring( 2 ).trim();
      if ( part.isEmpty() )
        continue;
      try
      {
        new TimeSpan( part );
      }
      catch ( Exception e )
      {
        error.append( '\'' ).append( part ).append( "' is not a valid time span.  " );
      }
    }

    return error.toString();
  }

  /******************************************** clean ********************************************/
  public void clean( int thisTaskNum )
  {
    // remove any forbidden predecessors
    Iterator<Predecessor> iter = m_preds.iterator();
    while ( iter.hasNext() )
    {
      Task pred = iter.next().task;
      int predNum = pred.index();

      // sub-tasks not allowed to depend on their summaries
      if ( pred.isSummary() && predNum < thisTaskNum && pred.summaryEnd() >= thisTaskNum )
        iter.remove();

      // summaries not allowed to depend on a sub-task
      Task thisTask = JPlanner.plan.task( thisTaskNum );
      if ( thisTask.isSummary() && thisTaskNum < predNum && thisTask.summaryEnd() >= predNum )
        iter.remove();
    }
  }

  /****************************************** hasToStart *****************************************/
  public boolean hasToStart()
  {
    // return true if has Finish-To-Start or Start-to-Start predecessor
    for ( Predecessor pred : m_preds )
    {
      if ( pred.type == TYPE_FINISH_START )
        return true;
      if ( pred.type == TYPE_START_START )
        return true;
    }

    return false;
  }

  /****************************************** hasToFinish ****************************************/
  public boolean hasToFinish()
  {
    // return true if has Finish-To-Finish or Start-to-Finish predecessor
    for ( Predecessor pred : m_preds )
    {
      if ( pred.type == TYPE_FINISH_FINISH )
        return true;
      if ( pred.type == TYPE_START_FINISH )
        return true;
    }

    return false;
  }

  /******************************************** start ********************************************/
  public DateTime start()
  {
    // return task start based on predecessors
    Calendar cal = JPlanner.plan.calendar();
    DateTime start = DateTime.MIN_VALUE;
    for ( Predecessor pred : m_preds )
    {
      if ( pred.type == TYPE_FINISH_START )
      {
        DateTime check = cal.workTimeSpan( pred.task.end(), pred.lag );
        if ( check.milliseconds() > start.milliseconds() )
          start = check;
      }

      if ( pred.type == TYPE_START_START )
      {
        DateTime check = cal.workTimeSpan( pred.task.start(), pred.lag );
        if ( check.milliseconds() > start.milliseconds() )
          start = check;
      }
    }

    return start;
  }

  /********************************************* end *********************************************/
  public DateTime end()
  {
    // return task end based on predecessors
    Calendar cal = JPlanner.plan.calendar();
    DateTime end = DateTime.MAX_VALUE;
    for ( Predecessor pred : m_preds )
    {
      if ( pred.type == TYPE_FINISH_FINISH )
      {
        DateTime check = cal.workTimeSpan( pred.task.end(), pred.lag );
        if ( check.milliseconds() < end.milliseconds() )
          end = check;
      }

      if ( pred.type == TYPE_START_FINISH )
      {
        DateTime check = cal.workTimeSpan( pred.task.start(), pred.lag );
        if ( check.milliseconds() < end.milliseconds() )
          end = check;
      }
    }

    return end;
  }

  /******************************************** count ********************************************/
  public int count()
  {
    return m_preds.size();
  }

  /********************************************* get *********************************************/
  public Predecessor get( int index )
  {
    return m_preds.get( index );
  }

}
