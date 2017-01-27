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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;

/*************************************************************************************************/
/***************************** Single day type as used by calendars ******************************/
/*************************************************************************************************/

public class Day
{
  private String                   m_name;    // name of day type
  private double                   m_work;    // equivalent days worked (typically 1.0 or 0.0)
  private ArrayList<DayWorkPeriod> m_periods; // list of work periods

  private int                      m_workMS;  // pre-calculated number of worked milliseconds in day-type

  public enum DefaultDayTypes
  {
    NONWORK, STANDARDWORK, SHORT, EVENING, TWENTYFOURHOURS
  }

  public static final int SECTION_NAME    = 0;
  public static final int SECTION_WORK    = 1;
  public static final int SECTION_PERIODS = 2;
  public static final int SECTION_START1  = 3;
  public static final int SECTION_END1    = 4;

  /**************************************** constructor ******************************************/
  public Day()
  {
    // construct empty but usable day type
    m_name = "Null";
    m_work = 0.0;
    m_periods = new ArrayList<DayWorkPeriod>();
    m_workMS = 0;
  }

  /**************************************** constructor ******************************************/
  public Day( DefaultDayTypes type )
  {
    // construct default day type
    m_periods = new ArrayList<DayWorkPeriod>();

    if ( type == DefaultDayTypes.NONWORK )
    {
      m_name = "Non Working";
      m_work = 0.0;
    }
    else if ( type == DefaultDayTypes.STANDARDWORK )
    {
      m_name = "Standard work day";
      m_work = 1.0;
      m_periods.add( new DayWorkPeriod( 9.0, 13.0 ) );
      m_periods.add( new DayWorkPeriod( 14.0, 18.0 ) );
    }
    else if ( type == DefaultDayTypes.SHORT )
    {
      m_name = "Morning only";
      m_work = 0.5;
      m_periods.add( new DayWorkPeriod( 9.0, 13.0 ) );
    }
    else if ( type == DefaultDayTypes.EVENING )
    {
      m_name = "Evening shift";
      m_work = 0.6;
      m_periods.add( new DayWorkPeriod( 18.0, 22.0 ) );
    }
    else if ( type == DefaultDayTypes.TWENTYFOURHOURS )
    {
      m_name = "24H day";
      m_work = 1.5;
      m_periods.add( new DayWorkPeriod( 0.0, 24.0 ) );
    }
    else
    {
      throw new IllegalArgumentException( "Unhandled DefaultDayType=" + type );
    }

    calcWorkMS();
  }

  /**************************************** constructor ******************************************/
  public Day( XMLStreamReader xsr ) throws XMLStreamException
  {
    this();
    // read XML day attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_ID:
          break;
        case XmlLabels.XML_NAME:
          m_name = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_WORK:
          m_work = Double.parseDouble( xsr.getAttributeValue( i ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    // check for any work-periods
    while ( xsr.hasNext() )
    {
      // if reached end of day, return
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_DAY ) )
      {
        calcWorkMS();
        return;
      }

      // if a work-period element, construct a period from it
      if ( xsr.isStartElement() && xsr.getLocalName().equals( XmlLabels.XML_PERIOD ) )
      {
        Double start = -1.0;
        Double end = -1.0;
        Time time;

        for ( int i = 0; i < xsr.getAttributeCount(); i++ )
          switch ( xsr.getAttributeLocalName( i ) )
          {
            case XmlLabels.XML_ID:
              break;
            case XmlLabels.XML_START:
              time = Time.fromString( xsr.getAttributeValue( i ) );
              start = time.milliseconds() / 3600_000.0;
              break;
            case XmlLabels.XML_END:
              time = Time.fromString( xsr.getAttributeValue( i ) );
              end = time.milliseconds() / 3600_000.0;
              break;
            default:
              JPlanner.trace( "Period - unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
              break;
          }

        m_periods.add( new DayWorkPeriod( start, end ) );
      }

      xsr.next();
    }
  }

  /***************************************** toString ********************************************/
  @Override
  public String toString()
  {
    return "DayType[" + m_name + ", " + m_work + ", " + m_periods + "]";
  }

  /******************************************* name **********************************************/
  public String name()
  {
    return m_name;
  }

  /******************************************* work **********************************************/
  public double work()
  {
    return m_work;
  }

  /**************************************** numPeriods *******************************************/
  public int numPeriods()
  {
    return m_periods.size();
  }

  /**************************************** workPeriods ******************************************/
  public ArrayList<DayWorkPeriod> workPeriods()
  {
    return m_periods;
  }

  /******************************************** end **********************************************/
  public Time end( int num )
  {
    return m_periods.get( num ).m_end;
  }

  /******************************************* start *********************************************/
  public Time start( int num )
  {
    return m_periods.get( num ).m_start;
  }

  /******************************************** end **********************************************/
  public Time end()
  {
    // return end of working day
    return end( m_periods.size() - 1 );
  }

  /******************************************* start *********************************************/
  public Time start()
  {
    // return start of working day
    return start( 0 );
  }

  /****************************************** getValue *******************************************/
  public Object getValue( int section )
  {
    // return display string for given section
    if ( section == SECTION_NAME )
      return m_name;

    if ( section == SECTION_WORK )
      return m_work;

    if ( section == SECTION_PERIODS )
      return numPeriods();

    section -= SECTION_START1;
    try
    {
      if ( section % 2 == 0 )
        return start( section / 2 );
      else
        return end( section / 2 );
    }
    catch ( IndexOutOfBoundsException e )
    {
      // if no work period, return blank
      return null;
    }
  }

  /****************************************** setValue ******************************************/
  @SuppressWarnings( "unchecked" )
  public void setValue( int section, Object newValue )
  {
    // update day with new value
    if ( section == SECTION_NAME )
      m_name = (String) newValue;

    else if ( section == SECTION_WORK )
      m_work = (double) newValue;

    else if ( section == SECTION_PERIODS )
      m_periods = (ArrayList<DayWorkPeriod>) newValue;

    else if ( section >= SECTION_START1 )
    {
      section -= SECTION_START1;
      if ( section % 2 == 0 )
        m_periods.get( section / 2 ).m_start = (Time) newValue;
      else
        m_periods.get( section / 2 ).m_end = (Time) newValue;
    }

    else
      throw new IllegalArgumentException( "Section=" + section );
  }

  /***************************************** isWorking *******************************************/
  public boolean isWorking()
  {
    // return if day has work periods
    return m_periods.size() > 0;
  }

  /**************************************** sectionName ******************************************/
  public static String sectionName( int num )
  {
    // return section title
    if ( num == SECTION_NAME )
      return "Name";

    if ( num == SECTION_WORK )
      return "Work";

    if ( num == SECTION_PERIODS )
      return "Periods";

    if ( num % 2 == 0 )
      return "End " + ( num / 2 - 1 );
    else
      return "Start " + ( num / 2 );
  }

  /****************************************** workDown *******************************************/
  public Time workDown( Time time )
  {
    // if in work period, return original time, otherwise end of earlier period, or null
    int ms = time.milliseconds();
    Time answer = null;

    for ( DayWorkPeriod period : m_periods )
    {
      if ( ms <= period.m_start.milliseconds() )
        return answer;

      if ( ms <= period.m_end.milliseconds() )
        return time;

      answer = period.m_end;
    }

    return answer;
  }

  /******************************************* workUp ********************************************/
  public Time workUp( Time time )
  {
    // if in work period, return original time, otherwise start of later period, or null
    int ms = time.milliseconds();

    for ( DayWorkPeriod period : m_periods )
    {
      if ( ms < period.m_start.milliseconds() )
        return period.m_start;

      if ( ms < period.m_end.milliseconds() )
        return time;
    }

    return null;
  }

  /****************************************** saveToXML ******************************************/
  public void saveToXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write day-type data to XML stream
    xsw.writeStartElement( XmlLabels.XML_DAY );
    xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( this.index() ) );
    xsw.writeAttribute( XmlLabels.XML_NAME, m_name );
    xsw.writeAttribute( XmlLabels.XML_WORK, Double.toString( m_work ) );

    for ( int p = 0; p < m_periods.size(); p++ )
    {
      xsw.writeEmptyElement( XmlLabels.XML_PERIOD );
      xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( p ) );
      xsw.writeAttribute( XmlLabels.XML_START, m_periods.get( p ).m_start.toString() );
      xsw.writeAttribute( XmlLabels.XML_END, m_periods.get( p ).m_end.toString() );
    }

    xsw.writeEndElement(); // XML_DAY
  }

  /***************************************** calcWorkMS ******************************************/
  private void calcWorkMS()
  {
    // calculate pre-calculated worked milliseconds in day-type
    m_workMS = 0;

    for ( DayWorkPeriod period : m_periods )
      m_workMS += period.m_end.milliseconds() - period.m_start.milliseconds();
  }

  /*************************************** millisecondsDone **************************************/
  public int millisecondsDone( Time time )
  {
    // return number of ms done from 00:00 to specified time
    int ms = time.milliseconds();
    int done = 0;

    for ( DayWorkPeriod period : m_periods )
    {
      if ( ms <= period.m_start.milliseconds() )
        return done;

      if ( ms < period.m_end.milliseconds() )
        return done + ms - period.m_start.milliseconds();

      done += period.m_end.milliseconds() - period.m_start.milliseconds();
    }

    return done;
  }

  /*************************************** millisecondsToGo **************************************/
  public int millisecondsToGo( Time time )
  {
    // return number of ms work remaining from specified time to 24:00
    return m_workMS - millisecondsDone( time );
  }

  /********************************************* work ********************************************/
  public Time work( Time from, double work )
  {
    // work number of ms from specified time
    if ( work > 0 )
      return workForward( from, work );

    if ( work < 0 )
      return workBackward( from, -work );

    return from;
  }

  /******************************************* workToGo ******************************************/
  public double workToGo( Time from )
  {
    // return number of work equivalent days remaining from specified time to 24:00
    return m_work - workDone( from );
  }

  /******************************************* workDone ******************************************/
  public double workDone( Time from )
  {
    // return number of work equivalent days done from 00:00 to specified time
    return m_work * millisecondsDone( from ) / m_workMS;
  }

  /***************************************** workForward *****************************************/
  public Time workForward( Time from, double work )
  {
    return workForward( from.milliseconds(), work );
  }

  public Time workForward( double work )
  {
    return workForward( 0, work );
  }

  public Time workForward( int from, double work )
  {
    return millisecondsForward( from, (int) ( work * m_workMS / m_work ) );
  }

  /***************************************** workBackward ****************************************/
  public Time workBackward( Time from, double work )
  {
    return workBackward( from.milliseconds(), work );
  }

  public Time workBackward( double work )
  {
    return workBackward( Time.MILLISECONDS_IN_DAY, work );
  }

  public Time workBackward( int from, double work )
  {
    return millisecondsBackward( from, (int) ( work * m_workMS / m_work ) );
  }

  /*************************************** workMilliseconds **************************************/
  public Time workMilliseconds( Time from, int ms )
  {
    // work number of ms from specified time
    if ( ms > 0 )
      return millisecondsForward( from, ms );

    if ( ms < 0 )
      return millisecondsBackward( from, -ms );

    return from;
  }

  /************************************* millisecondsForward *************************************/
  public Time millisecondsForward( Time from, int ms )
  {
    return millisecondsForward( from.milliseconds(), ms );
  }

  public Time millisecondsForward( int ms )
  {
    return millisecondsForward( 0, ms );
  }

  public Time millisecondsForward( int from, int ms )
  {
    if ( ms > m_workMS )
      return null;

    // work forwards specified number of ms
    for ( DayWorkPeriod period : m_periods )
    {
      if ( period.m_end.milliseconds() < from )
        continue;

      if ( from < period.m_start.milliseconds() )
        from = period.m_start.milliseconds();

      if ( from + ms <= period.m_end.milliseconds() )
        return Time.fromMilliseconds( from + ms );

      ms -= period.m_end.milliseconds() - from;
    }

    return null;
  }

  /************************************* millisecondsBackward ************************************/
  public Time millisecondsBackward( Time from, int ms )
  {
    return millisecondsBackward( from.milliseconds(), ms );
  }

  public Time millisecondsBackward( int ms )
  {
    return millisecondsBackward( Time.MILLISECONDS_IN_DAY, ms );
  }

  public Time millisecondsBackward( int from, int ms )
  {
    if ( ms > m_workMS )
      return null;

    // work backwards specified number of ms
    for ( int p = m_periods.size() - 1; p >= 0; p-- )
    {
      DayWorkPeriod period = m_periods.get( p );

      if ( period.m_start.milliseconds() > from )
        continue;

      if ( from > period.m_end.milliseconds() )
        from = period.m_end.milliseconds();

      if ( from - ms >= period.m_start.milliseconds() )
        return Time.fromMilliseconds( from - ms );

      ms -= from - period.m_start.milliseconds();
    }

    return null;
  }

  /***************************************** milliseconds ****************************************/
  public int milliseconds()
  {
    // return number for milliseconds worked in day-type
    return m_workMS;
  }

  /******************************************* stretch *******************************************/
  public Time stretch( Time time )
  {
    // if no working times return original date-time
    if ( m_periods.size() == 0 )
      return time;

    int now = time.milliseconds();
    int s = m_periods.get( 0 ).m_start.milliseconds();
    if ( now <= s )
      return Time.MIN_VALUE;

    int e = m_periods.get( m_periods.size() - 1 ).m_end.milliseconds();
    if ( now >= e )
      return Time.MAX_VALUE;

    // stretch time component so it uses whole 24 hours
    double scale = (double) Time.MILLISECONDS_IN_DAY / ( e - s );
    return Time.fromMilliseconds( (int) ( scale * ( now - s ) ) );
  }

  /******************************************** index ********************************************/
  public int index()
  {
    return JPlanner.plan.getIndex( this );
  }

}