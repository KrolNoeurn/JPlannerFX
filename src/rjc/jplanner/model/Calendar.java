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
import java.util.Comparator;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
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

  public enum DefaultCalendarTypes
  {
    STANDARD, FULLTIME, FANCY
  };

  public static final int SECTION_NAME       = 0;
  public static final int SECTION_ANCHOR     = 1;
  public static final int SECTION_EXCEPTIONS = 2;
  public static final int SECTION_CYCLE_LEN  = 3;
  public static final int SECTION_NORMAL1    = 4;

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

      m_exceptions.put( new Date( 2016, 12, 26 ), nonWorking );
      m_exceptions.put( new Date( 2016, 12, 27 ), nonWorking );
      m_exceptions.put( new Date( 2017, 1, 2 ), nonWorking );
      m_exceptions.put( new Date( 2017, 4, 14 ), nonWorking );
      m_exceptions.put( new Date( 2017, 4, 17 ), nonWorking );
      m_exceptions.put( new Date( 2017, 5, 1 ), nonWorking );
      m_exceptions.put( new Date( 2017, 5, 29 ), nonWorking );
      m_exceptions.put( new Date( 2017, 8, 28 ), nonWorking );
      m_exceptions.put( new Date( 2017, 12, 25 ), nonWorking );
      m_exceptions.put( new Date( 2017, 12, 26 ), nonWorking );
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

      m_exceptions.put( new Date( 2016, 12, 26 ), nonWorking );
      m_exceptions.put( new Date( 2016, 12, 27 ), nonWorking );
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
        case XmlLabels.XML_ID:
          break;
        case XmlLabels.XML_NAME:
          m_name = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_ANCHOR:
          m_cycleAnchor = Date.fromString( xsr.getAttributeValue( i ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    // check for any normals or exceptions
    while ( xsr.hasNext() )
    {
      // if reached end of calendar, return
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_CALENDAR ) )
        return;

      // if a normal element, add it to list
      if ( xsr.isStartElement() && xsr.getLocalName().equals( XmlLabels.XML_NORMAL ) )
        for ( int i = 0; i < xsr.getAttributeCount(); i++ )
          switch ( xsr.getAttributeLocalName( i ) )
          {
            case XmlLabels.XML_ID:
              break;
            case XmlLabels.XML_DAY:
              int dayIndex = Integer.parseInt( xsr.getAttributeValue( i ) );
              m_normal.add( JPlanner.plan.day( dayIndex ) );
              break;
            default:
              JPlanner.trace( "Normal - unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
              break;
          }

      // if an exception element, add it to list
      if ( xsr.isStartElement() && xsr.getLocalName().equals( XmlLabels.XML_EXCEPTION ) )
      {
        Date date = null;
        int dayIndex = -1;

        for ( int i = 0; i < xsr.getAttributeCount(); i++ )
          switch ( xsr.getAttributeLocalName( i ) )
          {
            case XmlLabels.XML_DATE:
              date = Date.fromString( xsr.getAttributeValue( i ) );
              break;
            case XmlLabels.XML_DAY:
              dayIndex = Integer.parseInt( xsr.getAttributeValue( i ) );
              break;
            default:
              JPlanner.trace( "Exception - unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
              break;
          }

        m_exceptions.put( date, JPlanner.plan.day( dayIndex ) );
      }

      xsr.next();
    }
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    return "Calendar[" + m_name + ", " + m_cycleAnchor + ", " + m_normal + "]";
  }

  /******************************************* getName *******************************************/
  public String getName()
  {
    return m_name;
  }

  /****************************************** getAnchor ******************************************/
  public Date getAnchor()
  {
    return m_cycleAnchor;
  }

  /******************************************* normal ********************************************/
  public Day getNormal( int index )
  {
    return m_normal.get( index );
  }

  /***************************************** getNormals ******************************************/
  public ArrayList<Day> getNormals()
  {
    return m_normal;
  }

  /***************************************** setNormals ******************************************/
  public void setNormals( ArrayList<Day> normals )
  {
    m_normal = normals;
  }

  /**************************************** getExceptions ****************************************/
  public HashMap<Date, Day> getExceptions()
  {
    return m_exceptions;
  }

  /****************************************** getValue *******************************************/
  public Object getValue( int section )
  {
    // return value for given section
    if ( section == SECTION_NAME )
      return m_name;

    if ( section == SECTION_ANCHOR )
      return m_cycleAnchor;

    if ( section == SECTION_EXCEPTIONS )
      return m_exceptions.size();

    if ( section == SECTION_CYCLE_LEN )
      return m_normal.size();

    // if row beyond normals handle index out of bounds
    try
    {
      return getNormal( section - SECTION_NORMAL1 );
    }
    catch ( IndexOutOfBoundsException exception )
    {
      return null;
    }
  }

  /****************************************** setValue ******************************************/
  public void setValue( int section, Object newValue )
  {
    // set calendar value for given section (except cycle-length + exceptions-count)
    if ( section == SECTION_NAME )
      m_name = (String) newValue;

    else if ( section == SECTION_ANCHOR )
      m_cycleAnchor = (Date) newValue;

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
  public Day day( Date date )
  {
    // if exception exists return it, otherwise return normal cycle day
    if ( m_exceptions.containsKey( date ) )
      return m_exceptions.get( date );

    int normal = ( date.epochday() - m_cycleAnchor.epochday() ) % m_normal.size();
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

    if ( num == SECTION_CYCLE_LEN )
      return "Cycle";

    return "Normal " + ( num + 1 - SECTION_NORMAL1 );
  }

  /***************************************** roundDown *******************************************/
  public DateTime roundDown( DateTime dt )
  {
    // return date-time if working, otherwise last past working date-time
    Date date = dt.date();
    Time time = dt.time();
    Day day = day( date );

    Time newTime = day.workDown( time );
    while ( newTime == null )
    {
      date.decrement();
      day = day( date );

      if ( day.isWorking() )
        newTime = day.end();
    }

    return new DateTime( date, newTime );
  }

  /******************************************* roundUp *******************************************/
  public DateTime roundUp( DateTime dt )
  {
    // return date-time if working, otherwise next future working date-time
    Date date = dt.date();
    Time time = dt.time();
    Day day = day( date );

    Time newTime = day.workUp( time );
    while ( newTime == null )
    {
      date.increment();
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
    xsw.writeStartElement( XmlLabels.XML_CALENDAR );
    xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( this.index() ) );
    xsw.writeAttribute( XmlLabels.XML_NAME, m_name );
    xsw.writeAttribute( XmlLabels.XML_ANCHOR, m_cycleAnchor.toString() );

    for ( int p = 0; p < m_normal.size(); p++ )
    {
      xsw.writeEmptyElement( XmlLabels.XML_NORMAL );
      xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( p ) );
      xsw.writeAttribute( XmlLabels.XML_DAY, Integer.toString( m_normal.get( p ).index() ) );
    }

    // generate sorted list of exception keys so order always same in XML file
    ArrayList<Date> keys = new ArrayList<Date>( m_exceptions.keySet() );
    keys.sort( new Comparator<Date>()
    {
      @Override
      public int compare( Date date1, Date date2 )
      {
        return Integer.compare( date1.epochday(), date2.epochday() );
      }
    } );

    for ( Date date : keys )
    {
      xsw.writeEmptyElement( XmlLabels.XML_EXCEPTION );
      xsw.writeAttribute( XmlLabels.XML_DATE, date.toString() );
      xsw.writeAttribute( XmlLabels.XML_DAY, Integer.toString( m_exceptions.get( date ).index() ) );
    }

    xsw.writeEndElement(); // XML_CALENDAR
  }

  /**************************************** workTimeSpan *****************************************/
  public DateTime workTimeSpan( DateTime start, TimeSpan ts )
  {
    // if time-span is zero length return original start
    if ( ts.number() == 0.0 )
      return start;

    // return date-time moved by TimeSpan
    if ( ts.units() == TimeSpan.UNIT_SECONDS )
      return workSeconds( start, ts.number() );

    if ( ts.units() == TimeSpan.UNIT_MINUTES )
      return workSeconds( start, ts.number() * 60.0 );

    if ( ts.units() == TimeSpan.UNIT_HOURS )
      return workSeconds( start, ts.number() * 3600.0 );

    if ( ts.units() == TimeSpan.UNIT_DAYS )
      return workDays( start, ts.number() );

    if ( ts.units() == TimeSpan.UNIT_WEEKS )
      return workWeeks( start, ts.number() );

    if ( ts.units() == TimeSpan.UNIT_MONTHS )
      return workMonths( start, ts.number() );

    if ( ts.units() == TimeSpan.UNIT_YEARS )
      return workYears( start, ts.number() );

    // unknown time-span units - should never happen!
    throw new IllegalArgumentException( ts.toString() );
  }

  /**************************************** workSeconds ******************************************/
  private DateTime workSeconds( DateTime start, double secs )
  {
    // return date-time from start by specified number for worked seconds
    Date date = start.date();
    Time fromTime = start.time();
    Day day = day( date );
    int ms = (int) Math.round( secs * 1000.0 );

    if ( ms > 0 )
    {
      // milliseconds is positive, so go forwards in time
      Time time = day.millisecondsForward( fromTime, ms );

      // if valid time then finished in day
      if ( time != null )
        return new DateTime( date, time );

      // if not valid time, move to next date
      ms -= day.millisecondsToGo( fromTime );
      date.increment();
      day = day( date );
      while ( ms >= day.milliseconds() )
      {
        ms -= day.milliseconds();
        date.increment();
        day = day( date );
      }

      if ( ms == 0 )
        return new DateTime( date, day.start() );
      else
        return new DateTime( date, day.millisecondsForward( ms ) );
    }
    else
    {
      // milliseconds is negative, so go backwards in time
      ms = -ms;
      Time time = day.millisecondsBackward( fromTime, ms );

      // if valid time then finished in day
      if ( time != null )
        return new DateTime( date, time );

      // if not valid time, move to previous date
      ms -= day.millisecondsDone( fromTime );
      date.decrement();
      day = day( date );
      while ( ms >= day.milliseconds() )
      {
        ms -= day.milliseconds();
        date.decrement();
        day = day( date );
      }

      if ( ms == 0 )
        return new DateTime( date, day.end() );
      else
        return new DateTime( date, day.millisecondsBackward( ms ) );
    }
  }

  /****************************************** workDays *******************************************/
  private DateTime workDays( DateTime start, double work )
  {
    // return date-time from start by specified number for work equivalent days
    Date date = start.date();
    Time fromTime = start.time();
    Day day = day( date );

    if ( work > 0 )
    {
      // work is positive, so go forwards in time
      Time time = day.workForward( fromTime, work );

      // if valid time then finished in day
      if ( time != null )
        return new DateTime( date, time );

      // if not valid time, move to next date
      work -= day.workToGo( fromTime );
      date.increment();
      day = day( date );
      while ( work >= day.work() )
      {
        work -= day.work();
        date.increment();
        day = day( date );
      }

      if ( work == 0 )
        return new DateTime( date, day.start() );
      else
        return new DateTime( date, day.workForward( work ) );
    }
    else
    {
      // work is negative, so go backwards in time
      work = -work;
      Time time = day.workBackward( fromTime, work );

      // if valid time then finished in day
      if ( time != null )
        return new DateTime( date, time );

      // if not valid time, move to previous date
      work -= day.workDone( fromTime );
      date.decrement();
      day = day( date );
      while ( work >= day.work() )
      {
        work -= day.work();
        date.decrement();
        day = day( date );
      }

      if ( work == 0 )
        return new DateTime( date, day.end() );
      else
        return new DateTime( date, day.workBackward( work ) );
    }
  }

  /****************************************** workWeeks ******************************************/
  private DateTime workWeeks( DateTime start, double weeks )
  {
    // return date-time moved by weeks (ignoring non-working days)
    return start.plusDays( (int) ( 7.0 * weeks ) );
  }

  /***************************************** workMonths ******************************************/
  private DateTime workMonths( DateTime start, double months )
  {
    // return date-time moved by months
    double whole = Math.floor( months );
    double fraction = months - whole;

    // if no fraction, just add months
    if ( fraction == 0.0 )
      return start.plusMonths( (int) whole );

    // if fraction, add months and days
    start = start.plusMonths( (int) months );
    int days1 = start.date().epochday();
    int days2 = start.plusMonths( 1 ).date().epochday();
    int daysInMonth = days2 - days1;
    return start.plusDays( (int) ( daysInMonth * fraction ) );
  }

  /****************************************** workYears ******************************************/
  private DateTime workYears( DateTime start, double years )
  {
    // return date-time moved by years
    double whole = Math.floor( years );
    double fraction = years - whole;

    // if no fraction, just add years
    if ( fraction == 0.0 )
      return start.plusYears( (int) whole );

    // if fraction, add years and months
    return workMonths( start.plusYears( (int) whole ), 12.0 * fraction );
  }

  /***************************************** workBetween *****************************************/
  public TimeSpan workBetween( DateTime start, DateTime end )
  {
    // return number of work equivalent days between the two date-times
    Date sd = start.date();
    Time st = start.time();
    Day day = day( sd );

    Date ed = end.date();
    Time et = end.time();

    // if start date same as end date, just work in day
    if ( sd.equals( ed ) )
      return new TimeSpan( day.workDone( et ) - day.workDone( st ), TimeSpan.UNIT_DAYS );

    // add together work across the days
    double work = day.workToGo( st );
    sd.increment();
    while ( !sd.equals( ed ) )
    {
      work += day( sd ).work();
      sd.increment();
    }
    work += day( ed ).workDone( et );

    return new TimeSpan( work, TimeSpan.UNIT_DAYS );
  }

  /******************************************** index ********************************************/
  public int index()
  {
    return JPlanner.plan.index( this );
  }

}
