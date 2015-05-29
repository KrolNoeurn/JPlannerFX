/**************************************************************************
 *  Copyright (C) 2015 by Richard Crook                                   *
 *  https://github.com/dazzle50/JPlanner                                  *
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

/*************************************************************************************************/
/***************************** Single day type as used by calendars ******************************/
/*************************************************************************************************/

public class Day
{
  private String                   m_name;   // name of day type
  private double                   m_work;   // equivalent days worked (typically 1.0 or 0.0)
  private ArrayList<DayWorkPeriod> m_periods; // list of work periods

  public enum DefaultDayTypes
  {
    NONWORK, STANDARDWORK, SHORT, EVENING, TWENTYFOURHOURS
  }

  public static final int     SECTION_NAME    = 0;
  public static final int     SECTION_WORK    = 1;
  public static final int     SECTION_PERIODS = 2;
  public static final int     SECTION_START1  = 3;
  public static final int     SECTION_END1    = 4;

  public static final String  XML_DAY         = "day";
  private static final String XML_ID          = "id";
  private static final String XML_NAME        = "name";
  private static final String XML_WORK        = "work";
  private static final String XML_PERIOD      = "period";
  private static final String XML_START       = "start";
  private static final String XML_END         = "end";

  /**************************************** constructor ******************************************/
  public Day()
  {
    // construct empty but usable day type
    m_name = "Null";
    m_work = 0.0;
    m_periods = new ArrayList<DayWorkPeriod>();
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
  }

  /**************************************** constructor ******************************************/
  public Day( XMLStreamReader xsr ) throws XMLStreamException
  {
    this();
    // read XML day attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XML_NAME:
          m_name = xsr.getAttributeValue( i );
          break;
        case XML_WORK:
          m_work = Double.parseDouble( xsr.getAttributeValue( i ) );
          break;
      }

    // check for any work-periods
    while ( xsr.hasNext() )
    {
      // if reached end of day, return
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XML_DAY ) )
        return;

      // if a work-period element, construct a period from it
      if ( xsr.isStartElement() && xsr.getLocalName().equals( XML_PERIOD ) )
      {
        Double start = -1.0;
        Double end = -1.0;
        Time time;

        for ( int i = 0; i < xsr.getAttributeCount(); i++ )
          switch ( xsr.getAttributeLocalName( i ) )
          {
            case XML_START:
              time = Time.fromString( xsr.getAttributeValue( i ) );
              start = time.getMilliseconds() / 3600_000.0;
              break;
            case XML_END:
              time = Time.fromString( xsr.getAttributeValue( i ) );
              end = time.getMilliseconds() / 3600_000.0;
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

  /****************************************** toString *******************************************/
  public String toString( int section )
  {
    // return display string for given section
    if ( section == SECTION_NAME )
      return m_name;

    if ( section == SECTION_WORK )
      return String.format( "%.2f", m_work );

    if ( section == SECTION_PERIODS )
      return String.format( "%d", numPeriods() );

    section -= SECTION_START1;
    try
    {
      if ( section % 2 == 0 )
        return start( section / 2 ).toString().substring( 0, 5 );
      else
        return end( section / 2 ).toString().substring( 0, 5 );
    }
    catch ( IndexOutOfBoundsException e )
    {
      // if no work period, return blank
      return "";
    }
  }

  /****************************************** setData ********************************************/
  @SuppressWarnings( "unchecked" )
  public void setData( int section, Object newValue )
  {
    // update day with new value
    if ( section == SECTION_NAME )
      m_name = (String) newValue;

    else if ( section == SECTION_WORK )
      m_work = Double.parseDouble( (String) newValue );

    else if ( section == SECTION_PERIODS )
      m_periods = (ArrayList<DayWorkPeriod>) newValue;

    else if ( section >= SECTION_START1 )
    {
      section -= SECTION_START1;
      if ( section % 2 == 0 )
        m_periods.get( section / 2 ).m_start = Time.fromString( (String) newValue );
      else
        m_periods.get( section / 2 ).m_end = Time.fromString( (String) newValue );
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
    int ms = time.getMilliseconds();
    Time answer = null;

    for ( DayWorkPeriod period : m_periods )
    {
      if ( ms <= period.m_start.getMilliseconds() )
        return answer;

      if ( ms <= period.m_end.getMilliseconds() )
        return time;

      answer = period.m_end;
    }

    return answer;
  }

  /******************************************* workUp ********************************************/
  public Time workUp( Time time )
  {
    // if in work period, return original time, otherwise start of later period, or null
    int ms = time.getMilliseconds();

    for ( DayWorkPeriod period : m_periods )
    {
      if ( ms < period.m_start.getMilliseconds() )
        return period.m_start;

      if ( ms < period.m_end.getMilliseconds() )
        return time;
    }

    return null;
  }

  /****************************************** saveToXML ******************************************/
  public void saveToXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write day-type data to XML stream
    xsw.writeStartElement( XML_DAY );
    xsw.writeAttribute( XML_ID, Integer.toString( JPlanner.plan.index( this ) ) );
    xsw.writeAttribute( XML_NAME, m_name );
    xsw.writeAttribute( XML_WORK, Double.toString( m_work ) );

    for ( int p = 0; p < m_periods.size(); p++ )
    {
      xsw.writeEmptyElement( XML_PERIOD );
      xsw.writeAttribute( XML_ID, Integer.toString( p ) );
      xsw.writeAttribute( XML_START, m_periods.get( p ).m_start.toString() );
      xsw.writeAttribute( XML_END, m_periods.get( p ).m_end.toString() );
    }

    xsw.writeEndElement(); // XML_DAY
  }

}
