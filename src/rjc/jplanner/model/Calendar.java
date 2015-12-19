/**************************************************************************
 *  Copyright (C) 2015 by Richard Crook                                   *
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
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.model.Day.DefaultDayTypes;

/*************************************************************************************************/
/********************************* Single calendar for planning **********************************/
/*************************************************************************************************/

public class Calendar
{
  private String             m_name;        // name of calendar
  private Date               m_cycleAnchor; // anchor date of calendar cycle
  private ArrayList<Day>     m_normal;      // normal basic cycle days
  private HashMap<Date, Day> m_exceptions;  // exceptions override normal days

  enum DefaultCalendarTypes
  {
    STANDARD, FULLTIME, FANCY
  };

  public static final int     SECTION_NAME       = 0;
  public static final int     SECTION_ANCHOR     = 1;
  public static final int     SECTION_EXCEPTIONS = 2;
  public static final int     SECTION_CYCLE      = 3;
  public static final int     SECTION_NORMAL1    = 4;

  public static final String  XML_CALENDAR       = "calendar";
  private static final String XML_ID             = "id";
  private static final String XML_NAME           = "name";
  private static final String XML_ANCHOR         = "anchor";
  private static final String XML_NORMAL         = "normal";
  private static final String XML_DAY            = "day";
  private static final String XML_EXCEPTION      = "exception";
  private static final String XML_DATE           = "date";

  /**************************************** constructor ******************************************/
  public Calendar()
  {
    // construct empty but usable calendar
    m_name = "Null";
    m_cycleAnchor = new Date( 2000, 1, 1 );
    m_normal = new ArrayList<Day>();
    m_exceptions = new HashMap<Date, Day>();
  }

  /**************************************** constructor ******************************************/
  public Calendar( DefaultCalendarTypes type )
  {
    // construct default calendar
    Day working = JPlanner.plan.day( DefaultDayTypes.STANDARDWORK.ordinal() );
    Day nonWorking = JPlanner.plan.day( DefaultDayTypes.NONWORK.ordinal() );
    Day fullTime = JPlanner.plan.day( DefaultDayTypes.TWENTYFOURHOURS.ordinal() );
    Day evening = JPlanner.plan.day( DefaultDayTypes.EVENING.ordinal() );
    Day shortDay = JPlanner.plan.day( DefaultDayTypes.SHORT.ordinal() );

    m_normal = new ArrayList<Day>();
    m_exceptions = new HashMap<Date, Day>();

    if ( type == DefaultCalendarTypes.STANDARD )
    {
      m_name = "Standard";
      m_cycleAnchor = new Date( 2000, 1, 1 );

      for ( int n = 0; n < 7; n++ )
      {
        if ( n < 2 ) // Sat + Sun
          m_normal.add( nonWorking );
        else
          // Mon to Fri
          m_normal.add( working );
      }

      m_exceptions.put( new Date( 2014, 12, 25 ), nonWorking );
      m_exceptions.put( new Date( 2014, 12, 26 ), nonWorking );
      m_exceptions.put( new Date( 2015, 1, 1 ), nonWorking );
      m_exceptions.put( new Date( 2015, 4, 3 ), nonWorking );
      m_exceptions.put( new Date( 2015, 4, 6 ), nonWorking );
      m_exceptions.put( new Date( 2015, 5, 4 ), nonWorking );
      m_exceptions.put( new Date( 2015, 5, 25 ), nonWorking );
      m_exceptions.put( new Date( 2015, 8, 31 ), nonWorking );
      m_exceptions.put( new Date( 2015, 12, 25 ), nonWorking );
      m_exceptions.put( new Date( 2015, 12, 28 ), nonWorking );
    }
    else if ( type == DefaultCalendarTypes.FULLTIME )
    {
      m_name = "Full time";
      m_cycleAnchor = new Date( 2015, 1, 1 );

      m_normal.add( fullTime );
    }
    else if ( type == DefaultCalendarTypes.FANCY )
    {
      m_name = "Fancy";
      m_cycleAnchor = new Date( 2015, 1, 1 );

      m_normal.add( nonWorking );
      m_normal.add( nonWorking );
      m_normal.add( nonWorking );
      m_normal.add( shortDay );
      m_normal.add( shortDay );
      m_normal.add( evening );
      m_normal.add( evening );
      m_normal.add( fullTime );
      m_normal.add( nonWorking );
      m_normal.add( fullTime );

      m_exceptions.put( new Date( 2015, 12, 25 ), nonWorking );
      m_exceptions.put( new Date( 2015, 12, 28 ), nonWorking );
    }
    else
    {
      throw new IllegalArgumentException( "Unhandled DefaultCalendarTypes=" + type );
    }

  }

  /**************************************** constructor ******************************************/
  public Calendar( XMLStreamReader xsr ) throws XMLStreamException
  {
    this();
    // read XML calendar attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XML_NAME:
          m_name = xsr.getAttributeValue( i );
          break;
        case XML_ANCHOR:
          m_cycleAnchor = Date.fromString( xsr.getAttributeValue( i ) );
          break;
      }

    // check for any normals or exceptions
    while ( xsr.hasNext() )
    {
      // if reached end of calendar, return
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XML_CALENDAR ) )
        return;

      // if a normal element, add it to list
      if ( xsr.isStartElement() && xsr.getLocalName().equals( XML_NORMAL ) )
        for ( int i = 0; i < xsr.getAttributeCount(); i++ )
          switch ( xsr.getAttributeLocalName( i ) )
          {
            case XML_DAY:
              int dayIndex = Integer.parseInt( xsr.getAttributeValue( i ) );
              m_normal.add( JPlanner.plan.day( dayIndex ) );
              break;
          }

      // if an exception element, add it to list
      if ( xsr.isStartElement() && xsr.getLocalName().equals( XML_EXCEPTION ) )
      {
        Date date = null;
        int dayIndex = -1;

        for ( int i = 0; i < xsr.getAttributeCount(); i++ )
          switch ( xsr.getAttributeLocalName( i ) )
          {
            case XML_DATE:
              date = Date.fromString( xsr.getAttributeValue( i ) );
              break;
            case XML_DAY:
              dayIndex = Integer.parseInt( xsr.getAttributeValue( i ) );
              break;
          }

        m_exceptions.put( date, JPlanner.plan.day( dayIndex ) );
      }

      xsr.next();
    }
  }

  /***************************************** toString ********************************************/
  @Override
  public String toString()
  {
    return "Calendar[" + m_name + ", " + m_cycleAnchor + ", " + m_normal + "]";
  }

  /******************************************* name **********************************************/
  public String name()
  {
    return m_name;
  }

  /****************************************** anchor *********************************************/
  public Date anchor()
  {
    return m_cycleAnchor;
  }

  /**************************************** numNormals *******************************************/
  public int numNormals()
  {
    return m_normal.size();
  }

  /*************************************** numExceptions *****************************************/
  public int numExceptions()
  {
    return m_exceptions.size();
  }

  /****************************************** normal *********************************************/
  public Day normal( int index )
  {
    return m_normal.get( index );
  }

  /******************************************* normals *******************************************/
  public ArrayList<Day> normals()
  {
    return m_normal;
  }

  /****************************************** toString *******************************************/
  public String toString( int section )
  {
    // return display string for given section
    if ( section == SECTION_NAME )
      return m_name;

    if ( section == SECTION_ANCHOR )
      return m_cycleAnchor.toString();

    if ( section == SECTION_EXCEPTIONS )
      return String.format( "%d", m_exceptions.size() );

    if ( section == SECTION_CYCLE )
      return String.format( "%d", m_normal.size() );

    // if row beyond normals handle index out of bounds
    try
    {
      return normal( section - SECTION_NORMAL1 ).name();
    }
    catch ( IndexOutOfBoundsException e )
    {
      return "";
    }
  }

  /****************************************** setData ********************************************/
  @SuppressWarnings( "unchecked" )
  public void setData( int section, Object newValue )
  {
    // set calendar data for given section
    if ( section == SECTION_NAME )
      m_name = (String) newValue;

    else if ( section == SECTION_ANCHOR )
      m_cycleAnchor = Date.fromString( (String) newValue );

    else if ( section == SECTION_CYCLE )
      m_normal = (ArrayList<Day>) newValue;

    else if ( section == SECTION_EXCEPTIONS )
      m_exceptions = (HashMap<Date, Day>) newValue;

    else if ( section >= SECTION_NORMAL1 )
      m_normal.set( section - SECTION_NORMAL1, (Day) newValue );

    else
      throw new IllegalArgumentException( "Section=" + section );
  }

  /***************************************** isWorking *******************************************/
  public boolean isWorking( Date date )
  {
    // return whether date is working or not
    return day( date ).isWorking();
  }

  /********************************************* day *********************************************/
  private Day day( Date date )
  {
    // if exception exists return it, otherwise return normal cycle day
    if ( m_exceptions.containsKey( date ) )
      return m_exceptions.get( date );

    int normal = ( date.getEpochday() - m_cycleAnchor.getEpochday() ) % m_normal.size();
    if ( normal < 0 )
      normal += m_normal.size();

    return m_normal.get( normal );
  }

  /**************************************** sectionName ******************************************/
  public static String sectionName( int num )
  {
    // return section title
    if ( num == SECTION_NAME )
      return "Name";

    if ( num == SECTION_ANCHOR )
      return "Anchor";

    if ( num == SECTION_EXCEPTIONS )
      return "Exceptions";

    if ( num == SECTION_CYCLE )
      return "Cycle";

    return "Normal " + ( num + 1 - SECTION_NORMAL1 );
  }

  /****************************************** workDown *******************************************/
  public DateTime workDown( DateTime dt )
  {
    // return date-time if working, otherwise next future working date-time
    Date date = dt.getDate();
    Time time = dt.getTime();
    Day day = day( date );

    Time newTime = day.workDown( time );
    while ( newTime == null )
    {
      date = date.addDays( -1 );
      day = day( date );

      if ( day.isWorking() )
        newTime = day.end();
    }

    return new DateTime( date, newTime );
  }

  /******************************************** workUp *********************************************/
  public DateTime workUp( DateTime dt )
  {
    // return date-time if working, otherwise last past working date-time
    Date date = dt.getDate();
    Time time = dt.getTime();
    Day day = day( date );

    Time newTime = day.workUp( time );
    while ( newTime == null )
    {
      date = date.addDays( 1 );
      day = day( date );

      if ( day.isWorking() )
        newTime = day.start();
    }

    return new DateTime( date, newTime );
  }

  /****************************************** saveToXML ******************************************/
  public void saveToXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write calendar data to XML stream
    xsw.writeStartElement( XML_CALENDAR );
    xsw.writeAttribute( XML_ID, Integer.toString( JPlanner.plan.index( this ) ) );
    xsw.writeAttribute( XML_NAME, m_name );
    xsw.writeAttribute( XML_ANCHOR, m_cycleAnchor.toString() );

    for ( int p = 0; p < m_normal.size(); p++ )
    {
      xsw.writeEmptyElement( XML_NORMAL );
      xsw.writeAttribute( XML_ID, Integer.toString( p ) );
      xsw.writeAttribute( XML_DAY, Integer.toString( JPlanner.plan.index( m_normal.get( p ) ) ) );
    }

    for ( HashMap.Entry<Date, Day> except : m_exceptions.entrySet() )
    {
      xsw.writeEmptyElement( XML_EXCEPTION );
      xsw.writeAttribute( XML_DATE, except.getKey().toString() );
      xsw.writeAttribute( XML_DAY, Integer.toString( JPlanner.plan.index( except.getValue() ) ) );
    }

    xsw.writeEndElement(); // XML_CALENDAR
  }
}
