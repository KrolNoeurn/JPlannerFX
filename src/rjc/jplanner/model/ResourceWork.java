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

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/*************************************************************************************************/
/************************** Resource work (quantity and time) on tasks ***************************/
/*************************************************************************************************/

class ResourceWork
{
  // struct that contains date-time and number
  private class DateTimeNumber
  {
    public DateTime dt;  // date-time
    public double   num; // number

    public DateTimeNumber( DateTime dt, double num )
    {
      this.dt = dt;
      this.num = num;
    }

    @Override
    public String toString()
    {
      return "DateTimeNumber [" + dt + ", " + num + "]";
    }
  }

  // struct that contains one resource allocation record
  private class Alloc
  {
    public DateTime end;  // date-time
    public double   num;  // number
    public Task     task; // task

    @Override
    public String toString()
    {
      return "Alloc [" + end + ", " + num + ", " + task + "]";
    }
  }

  Resource                   m_res;    // associated resource
  SortedMap<DateTime, Alloc> m_allocs; // key is 'start', value is 'end/quantity/task' 

  /**************************************** constructor ******************************************/
  public ResourceWork( Resource res )
  {
    // initialise private variables
    m_res = res;
    m_allocs = new TreeMap<DateTime, Alloc>();
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    // short string summary
    StringBuilder str = new StringBuilder();
    str.append( "ResourceWork@" ).append( Integer.toHexString( hashCode() ) );
    str.append( " [" ).append( m_res.toStringShort() ).append( " " ).append( m_allocs.size() );

    for ( Map.Entry<DateTime, Alloc> alloc : m_allocs.entrySet() )
      str.append( "\n\t" ).append( alloc.getKey() ).append( " " ).append( alloc.getValue() );

    str.append( "]" );
    return str.toString();
  }

  /******************************************** clear ********************************************/
  public void clear()
  {
    m_allocs.clear();
  }

  /******************************************** alloc ********************************************/
  public void alloc( DateTime start, DateTime end, double quantity, Task task )
  {
    // check inputs (PROBABLY CAN BE REMOVED TO AVOID DOUBLE CHECKING)
    if ( start == null )
      throw new NullPointerException( "start is null" );
    if ( end == null )
      throw new NullPointerException( "end is null" );
    if ( task == null )
      throw new NullPointerException( "task is null" );
    if ( quantity <= 0.0 )
      throw new IllegalArgumentException( "quantity must be greater than zero " + quantity );
    if ( !start.isLessThan( end ) )
      throw new IllegalArgumentException( "start " + start + " >= end " + end );
    if ( start.isLessThan( m_res.getStart() ) )
      throw new IllegalArgumentException( "start " + start + " < resource start " + m_res.getStart() );
    if ( m_res.getEnd().isLessThan( end ) )
      throw new IllegalArgumentException( "resource end " + m_res.getEnd() + " < end " + end );

    // check this won't cause over-allocation (PROBABLY CAN BE REMOVED TO AVOID DOUBLE CHECKING)

    // add resource allocation
    Alloc alloc = new Alloc();
    alloc.end = end;
    alloc.num = quantity;
    alloc.task = task;
    m_allocs.put( start, alloc );
  }

  /****************************************** available ******************************************/
  public DateTimeNumber available( DateTime from )
  {
    // return available quantity of resource at date-time from, and when this changes

    // if before resource start date
    if ( from.isLessThan( m_res.getStart() ) )
      return new DateTimeNumber( m_res.getStart(), 0.0 );

    // if after resource end date
    if ( m_res.getEnd().isLessThan( from ) )
      return new DateTimeNumber( m_res.getEnd(), m_res.getAvailable() );

    // okay, so in resource available period
    DateTime change = DateTime.MAX_VALUE;
    double avail = m_res.getAvailable();

    // loop thru each existing allocation and deduct any for specified date-time
    for ( Map.Entry<DateTime, Alloc> alloc : m_allocs.entrySet() )
    {
      DateTime start = alloc.getKey();
      if ( from.isLessThan( start ) )
      {
        if ( start.isLessThan( change ) )
          change = start;
        break;
      }

      DateTime end = alloc.getValue().end;
      if ( from.isLessThan( end ) )
      {
        avail -= alloc.getValue().num;
        if ( end.isLessThan( change ) )
          change = end;
      }
    }

    return new DateTimeNumber( change, avail );
  }

}
