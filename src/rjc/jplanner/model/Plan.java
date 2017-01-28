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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
import rjc.jplanner.command.UndoStack;

/*************************************************************************************************/
/************************** Holds the complete data model for the plan ***************************/
/*************************************************************************************************/

public class Plan
{
  private String              m_title;                  // plan title as set in properties
  private DateTime            m_start;                  // plan start date-time as set in properties
  private Calendar            m_calendar;               // plan's default calendar
  private String              m_datetimeFormat;         // format to display date-times
  private String              m_dateFormat;             // format to display dates
  private String              m_filename;               // filename when saved or loaded
  private String              m_fileLocation;           // file location
  private String              m_savedBy;                // who saved last
  private DateTime            m_savedWhen;              // when was last saved
  private String              m_notes;                  // plan notes as on plan tab

  private UndoStack           m_undostack;              // undo stack for plan editing

  public Tasks                tasks;                    // list of plan tasks
  public Resources            resources;                // list of plan resources
  public Calendars            calendars;                // list of plan calendars
  public Days                 daytypes;                 // list of plan day types

  private static final String NOTES_CR  = "#%NCR!£%#";  // because XMLStreamWriter doesn't encode new-lines correctly
  private static final String NOTES_TAB = "#%NTAB!£%#"; // because XMLStreamWriter doesn't encode tabs correctly

  /**************************************** constructor ******************************************/
  public Plan()
  {
    // construct empty but usable Plan
    tasks = new Tasks();
    resources = new Resources();
    calendars = new Calendars();
    daytypes = new Days();

    m_undostack = new UndoStack();

    m_title = "";
    m_datetimeFormat = "EEE dd/MM/yyyy HH:mm";
    m_dateFormat = "dd/MM/yyyy";
    m_filename = "";
    m_fileLocation = "";
    m_savedBy = "";
    m_notes = "";
  }

  /***************************************** toString ********************************************/
  @Override
  public String toString()
  {
    // convert to string
    String hash = super.toString();
    String id = hash.substring( hash.lastIndexOf( '.' ) + 1 );
    return id + "[" + m_title + ", " + m_start + ", " + tasks.size() + " Tasks, " + resources.size() + " Resources, "
        + calendars.size() + " Calendars, " + daytypes.size() + " DayTypes]";
  }

  /**************************************** initialise *******************************************/
  public void initialise()
  {
    // initialise plan with default settings and contents
    daytypes.initialise();
    calendars.initialise();
    resources.initialise();
    tasks.initialise();

    m_calendar = getCalendar( 0 );
    m_start = m_calendar.getWorkDateTimeUp( new DateTime( Date.now(), Time.MIN_VALUE ) );
  }

  /************************************ getTasksNotNullCount *************************************/
  public int getTasksNotNullCount()
  {
    // return number of not-null tasks in plan (skipping special task 0)
    int count = 0;
    for ( int id = 1; id < tasks.size(); id++ )
      if ( !tasks.get( id ).isNull() )
        count++;

    return count;
  }

  /********************************** getResourcesNotNullCount ***********************************/
  public int getResourcesNotNullCount()
  {
    // return number of not-null resources in plan (skipping special resource 0)
    int count = 0;
    for ( int id = 1; id < resources.size(); id++ )
      if ( !resources.get( id ).isNull() )
        count++;

    return count;
  }

  /**************************************** getTasksCount ****************************************/
  public int getTasksCount()
  {
    // return number of tasks in plan (including null tasks)
    return tasks.size();
  }

  /************************************** getResourcesCount **************************************/
  public int getResourcesCount()
  {
    // return number of resources in plan (including null resources)
    return resources.size();
  }

  /************************************** getCalendarsCount **************************************/
  public int getCalendarsCount()
  {
    // return number of calendars in plan
    return calendars.size();
  }

  /***************************************** getDaysCount ****************************************/
  public int getDaysCount()
  {
    // return number of day-types in plan
    return daytypes.size();
  }

  /******************************************** task *********************************************/
  public Task getTask( int index )
  {
    // return task corresponding to index
    return tasks.get( index );
  }

  public int getIndex( Task task )
  {
    return tasks.indexOf( task );
  }

  /****************************************** resource *******************************************/
  public Resource getResource( int index )
  {
    // return resource corresponding to index
    return resources.get( index );
  }

  public int getIndex( Resource res )
  {
    return resources.indexOf( res );
  }

  /****************************************** calendar *******************************************/
  public Calendar getCalendar( int index )
  {
    // return calendar corresponding to index
    return calendars.get( index );
  }

  public int getIndex( Calendar cal )
  {
    return calendars.indexOf( cal );
  }

  /********************************************* day *********************************************/
  public Day getDay( int index )
  {
    // return day-type corresponding to index
    return daytypes.get( index );
  }

  public int getIndex( Day day )
  {
    return daytypes.indexOf( day );
  }

  /****************************************** getTitle *******************************************/
  public String getTitle()
  {
    return m_title;
  }

  /****************************************** getNotes *******************************************/
  public String getNotes()
  {
    return m_notes;
  }

  /****************************************** getStart *******************************************/
  public DateTime getStart()
  {
    return m_start;
  }

  /************************************ getEarliestTaskStart *************************************/
  public DateTime getEarliestTaskStart()
  {
    // return start date-time of earliest starting plan task, or null if no tasks
    DateTime earliest = DateTime.MAX_VALUE;

    for ( Task task : tasks )
      if ( !task.isNull() && !task.isSummary() && task.getStart().isLessThan( earliest ) )
        earliest = task.getStart();

    if ( earliest == DateTime.MAX_VALUE )
      return null;
    return earliest;
  }

  /************************************** getLatestTaskEnd ***************************************/
  public DateTime getLatestTaskEnd()
  {
    // return end date-time of latest ending plan task, or null if no tasks
    DateTime latest = DateTime.MIN_VALUE;

    for ( Task task : tasks )
      if ( !task.isNull() && !task.isSummary() && latest.isLessThan( task.getEnd() ) )
        latest = task.getEnd();

    if ( latest == DateTime.MIN_VALUE )
      return null;
    return latest;
  }

  /************************************** getDefaultcalendar *************************************/
  public Calendar getDefaultcalendar()
  {
    return m_calendar;
  }

  /************************************** getDateTimeFormat **************************************/
  public String getDateTimeFormat()
  {
    return m_datetimeFormat;
  }

  /**************************************** getDateFormat ****************************************/
  public String getDateFormat()
  {
    return m_dateFormat;
  }

  /***************************************** getFilename *****************************************/
  public String getFilename()
  {
    return m_filename;
  }

  /*************************************** getFileLocation ***************************************/
  public String getFileLocation()
  {
    return m_fileLocation;
  }

  /***************************************** getSavedBy ******************************************/
  public String getSavedBy()
  {
    return m_savedBy;
  }

  /**************************************** getSavedWhen *****************************************/
  public DateTime getSavedWhen()
  {
    return m_savedWhen;
  }

  /**************************************** getUndostack *****************************************/
  public UndoStack getUndostack()
  {
    return m_undostack;
  }

  /******************************************* setNotes ******************************************/
  public void setNotes( String notes )
  {
    m_notes = notes;
  }

  /******************************************* setTitle ******************************************/
  public void setTitle( String title )
  {
    m_title = title;
  }

  /******************************************* setStart ******************************************/
  public void setStart( DateTime start )
  {
    m_start = start;
  }

  /************************************** setDefaultCalendar *************************************/
  public void setDefaultCalendar( Calendar cal )
  {
    m_calendar = cal;
  }

  /*************************************** setDateTimeFormat *************************************/
  public void setDateTimeFormat( String DTformat )
  {
    m_datetimeFormat = DTformat;
  }

  /**************************************** setDateFormat ****************************************/
  public void setDateFormat( String Dformat )
  {
    m_dateFormat = Dformat;
  }

  /*************************************** setFileDetails ****************************************/
  public void setFileDetails( String name, String loc, String user, DateTime when )
  {
    // set details related to file 
    m_filename = name;
    m_fileLocation = loc;
    m_savedBy = user;
    m_savedWhen = when;
  }

  /****************************************** savePlan *******************************************/
  public boolean savePlan( XMLStreamWriter xsw )
  {
    // save plan to specified XML stream
    try
    {
      // write plan data to XML stream
      xsw.writeStartElement( XmlLabels.XML_PLAN_DATA );
      xsw.writeAttribute( XmlLabels.XML_TITLE, m_title );
      xsw.writeAttribute( XmlLabels.XML_START, m_start.toString() );
      xsw.writeAttribute( XmlLabels.XML_CALENDAR, Integer.toString( getIndex( m_calendar ) ) );
      xsw.writeAttribute( XmlLabels.XML_DT_FORMAT, m_datetimeFormat );
      xsw.writeAttribute( XmlLabels.XML_D_FORMAT, m_dateFormat );

      // because XMLStreamWriter doesn't encode new-lines & tabs correctly 
      xsw.writeAttribute( XmlLabels.XML_NOTES, m_notes.replaceAll( "\\n", NOTES_CR ).replaceAll( "\\t", NOTES_TAB ) );

      // write day, calendar, resource, and task data to XML stream
      daytypes.writeXML( xsw );
      calendars.writeXML( xsw );
      resources.writeXML( xsw );
      tasks.writeXML( xsw );

      xsw.writeEndElement(); // XML_PLAN_DATA
      return true;
    }
    catch ( XMLStreamException exception )
    {
      // some sort of exception thrown
      exception.printStackTrace();
      return false;
    }
  }

  /******************************************* loadXML *******************************************/
  public void loadXML( XMLStreamReader xsr, String filename, String fileloc ) throws XMLStreamException
  {
    // as id of plan-calendar read before the calendars, need temporary store
    int calendarId = -1;

    // load plan from XML stream
    while ( xsr.hasNext() )
    {
      // if reached end of plan data, exit loop
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_PLAN_DATA ) )
        break;

      // if start element read data
      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_JPLANNER:
            loadXmlJPlanner( xsr );
            break;
          case XmlLabels.XML_PLAN_DATA:
            calendarId = loadXmlPlan( xsr );
            break;
          case XmlLabels.XML_DAY_DATA:
            daytypes.loadXML( xsr );
            break;
          case XmlLabels.XML_CAL_DATA:
            calendars.loadXML( xsr );
            break;
          case XmlLabels.XML_RES_DATA:
            resources.loadXML( xsr );
            break;
          case XmlLabels.XML_TASK_DATA:
            tasks.loadXML( xsr );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }

      xsr.next();
    }

    // if calendar-id still negative, default to first calendar
    if ( calendarId < 0 )
      m_calendar = getCalendar( 0 );
    else
      m_calendar = getCalendar( calendarId );

    m_filename = filename;
    m_fileLocation = fileloc;
  }

  /***************************************** loadXmlPlan *****************************************/
  private int loadXmlPlan( XMLStreamReader xsr ) throws XMLStreamException
  {
    // as calendars not yet loaded just keep calendar-id
    int calendarId = -1;

    // read XML plan attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_TITLE:
          m_title = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_START:
          m_start = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_DT_FORMAT:
          m_datetimeFormat = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_D_FORMAT:
          m_dateFormat = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_CALENDAR:
          calendarId = Integer.parseInt( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_NOTES:
          m_notes = xsr.getAttributeValue( i ).replaceAll( NOTES_CR, "\n" ).replaceAll( NOTES_TAB, "\t" );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    // return calendar-id to be set as default calendar
    return calendarId;
  }

  /*************************************** loadXmlJPlanner ***************************************/
  private void loadXmlJPlanner( XMLStreamReader xsr ) throws XMLStreamException
  {
    // read XML JPlanner attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_SAVEUSER:
          m_savedBy = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_SAVEWHEN:
          m_savedWhen = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_FORMAT:
        case XmlLabels.XML_SAVENAME:
        case XmlLabels.XML_SAVEWHERE:
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }
  }

  /*************************************** checkForErrors ****************************************/
  public String checkForErrors()
  {
    // check what errors plan may have
    if ( getDaysCount() == 0 )
      return "No day-types";

    if ( getCalendarsCount() == 0 )
      return "No calendars";

    if ( getIndex( getDefaultcalendar() ) == -1 )
      return "Invalid default calendar";

    return null;
  }

  /******************************************* stretch *******************************************/
  public DateTime stretch( DateTime dt, boolean stretchTasks )
  {
    // if input date-time is null return null
    if ( dt == null )
      return dt;

    // return date-time stretched across full 24 hrs if plan stretchTasks flag is true
    if ( stretchTasks )
    {
      Time time = m_calendar.getDay( dt.getDate() ).stretch( dt.getTime() );
      return new DateTime( dt.getDate(), time );
    }

    // plan stretchTasks flag not true, so return original date-time
    return dt;
  }

  /****************************************** schedule *******************************************/
  public void schedule()
  {
    // schedule the plan!
    JPlanner.trace( "============================== SCHEDULE started ==============================" );
    resources.clearAllocations();
    tasks.schedule();
    JPlanner.trace( "============================== SCHEDULE finished ==============================" );
  }

}