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

package rjc.jplanner.model;

import java.util.ArrayList;

import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Work.Effort;

/*************************************************************************************************/
/***************************** ?????? resource work on a plan ?????? *****************************/
/*************************************************************************************************/

public class Work extends ArrayList<Effort>
{
  private static final long serialVersionUID = 1L;

  // structure that contains one work record
  class Effort
  {
    public Task     task;
    public Resource resource;
    public double   num;     // quantity >0, or <=0 means all of available resource
    public long     startMS;
    public long     endMS;

    private Effort( Task task, Resource resource, double num, DateTime start, DateTime end )
    {
      this.task = task;
      this.resource = resource;
      this.num = num;
      this.startMS = start.getMilliseconds();
      this.endMS = end.getMilliseconds();
    }

    @Override
    public String toString()
    {
      String hash = super.toString();
      String id = hash.substring( hash.lastIndexOf( '.' ) + 1 );
      DateTime start = new DateTime( startMS );
      DateTime end = new DateTime( endMS );
      return id + "[" + task + ", " + resource + ", " + num + ", " + start + ", " + end + "]";
    }
  }

  // structure to return resource usage information
  class DateTimeNumber
  {
    public DateTime dt;
    public double   num;

    public DateTimeNumber( DateTime dt, double num )
    {
      this.dt = dt;
      this.num = num;
    }
  }

  /********************************************* add *********************************************/
  public Effort add( Task task, Resource resource, double num, DateTime start, DateTime end )
  {
    // check input parameters
    if ( task == null )
      throw new NullPointerException( "Task must not be null" );
    if ( resource == null )
      throw new NullPointerException( "Resource must not be null" );
    if ( start == null )
      throw new NullPointerException( "Start must not be null" );
    if ( end == null )
      throw new NullPointerException( "End must not be null" );
    if ( num <= 0.0 )
      throw new IllegalArgumentException( "Number must be greater than zero" );
    if ( end.getMilliseconds() <= start.getMilliseconds() )
      throw new IllegalArgumentException( "Start must be before End" );

    // ensure start is not before resource start
    DateTime rs = resource.getStart();
    if ( end.isLessThan( rs ) )
      return null;
    if ( start.isLessThan( rs ) )
      start = new DateTime( rs );

    // ensure end is not after resource end
    DateTime re = resource.getEnd();
    if ( re.isLessThan( start ) )
      return null;
    if ( re.isLessThan( end ) )
      end = new DateTime( re );

    // ensure effort is not greater than resource availability
    Double avail = resource.getAvailable();
    DateTimeNumber usage = getResourceUsage( resource, start );
    if ( num > avail - usage.num )
      num = avail - usage.num;

    // add effort with specified parameters
    Effort effort = new Effort( task, resource, num, start, end );

    // do checks to ensure resource is not over allocated and reduce effort appropriately !!!

    add( effort );

    // return effort actually accepted, or null if no effort accepted
    return effort;
  }

  /************************************** getResourceUsage ***************************************/
  public DateTimeNumber getResourceUsage( Resource resource, DateTime datetime )
  {
    JPlanner.trace( "REQUESTING RESOURCE USAGE FOR " + resource, datetime );

    // return resource usage at specified date-time and when usage next changes
    long ms = datetime.getMilliseconds();
    double use = 0.0;
    long change = DateTime.MAX_VALUE.getMilliseconds();

    // loop around each effort record checking for resource and date-time range
    for ( Effort effort : this )
      if ( effort.resource == resource && effort.startMS <= ms && effort.endMS >= ms )
      {
        use += effort.num;
        if ( effort.endMS < change )
          change = effort.endMS;
      }

    return new DateTimeNumber( new DateTime( change ), use );
  }

  /******************************************* getWork *******************************************/
  public TimeSpan getWork( Task task )
  {
    //JPlanner.trace( "REQUESTING WORK FOR " + task );

    // return work for specified task
    TimeSpan work = new TimeSpan();
    forEach( effort ->
    {
      JPlanner.trace( effort );
    } );

    return work;
  }

}
