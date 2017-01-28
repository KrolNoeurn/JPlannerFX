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

/*************************************************************************************************/
/************************** Data to support the drawing of a gantt task **************************/
/*************************************************************************************************/

public class GanttData
{
  public DateTime            start; // start time of gantt task
  public ArrayList<DateTime> end;   // list of changes until end
  public ArrayList<Double>   value; // list of value change (-ve means summary)

  /***************************************** constructor *****************************************/
  public GanttData()
  {
    // nothing to do
  }

  /****************************************** setSummary *****************************************/
  public void setSummary( DateTime summaryStart, DateTime summaryEnd )
  {
    // set gantt data for summary
    start = summaryStart;
    end = new ArrayList<DateTime>();
    end.add( summaryEnd );
    value = new ArrayList<Double>();
    value.add( -1.0 );
  }

  /******************************************* setTask *******************************************/
  public void setTask( DateTime taskStart, DateTime taskEnd )
  {
    // set gantt data for simple task
    start = taskStart;

    // check if milestone or task bar needed
    if ( taskStart.getMilliseconds() < taskEnd.getMilliseconds() )
    {
      end = new ArrayList<DateTime>();
      end.add( taskEnd );
      value = new ArrayList<Double>();
      value.add( 1.0 );
    }
    else
    {
      end = null;
      value = null;
    }

  }

  /****************************************** isSummary ******************************************/
  public boolean isSummary()
  {
    // return true of gantt-data indicates summary
    if ( value == null )
      return false;
    return value.get( 0 ) < 0;
  }

  /***************************************** isMilestone *****************************************/
  public boolean isMilestone()
  {
    // return true of gantt-data indicates milestone
    return end == null;
  }

}
