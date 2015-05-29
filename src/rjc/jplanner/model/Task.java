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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/******************************** Single task within overall plan ********************************/
/*************************************************************************************************/

public class Task
{
  private String              m_title;                       // free text title
  private TimeSpan            m_duration;                    // duration of task
  private DateTime            m_start;                       // start date-time of task
  private DateTime            m_end;                         // end date-time of task
  private TimeSpan            m_work;                        // work effort for task
  private Predecessors        m_predecessors;                // task predecessors
  private TaskResources       m_resources;                   // resources allocated to task
  private TaskType            m_type;                        // task type
  private int                 m_priority;                    // overall task priority (0 to 999 times one million)
  private DateTime            m_deadline;                    // task warning deadline
  private String              m_cost;                        // calculated cost based on resource use
  private String              m_comment;                     // free text comment

  public static final int     SECTION_TITLE    = 0;
  public static final int     SECTION_DURATION = 1;
  public static final int     SECTION_START    = 2;
  public static final int     SECTION_END      = 3;
  public static final int     SECTION_WORK     = 4;
  public static final int     SECTION_PRED     = 5;
  public static final int     SECTION_RES      = 6;
  public static final int     SECTION_TYPE     = 7;
  public static final int     SECTION_PRIORITY = 8;
  public static final int     SECTION_DEADLINE = 9;
  public static final int     SECTION_COST     = 10;
  public static final int     SECTION_COMMENT  = 11;
  public static final int     SECTION_MAX      = 11;

  public static final String  XML_TASK         = "task";
  private static final String XML_ID           = "id";
  private static final String XML_TITLE        = "title";
  private static final String XML_DURATION     = "duration";
  private static final String XML_START        = "start";
  private static final String XML_END          = "end";
  private static final String XML_WORK         = "work";
  private static final String XML_RESOURCES    = "resources";
  private static final String XML_TYPE         = "type";
  private static final String XML_PRIORITY     = "priority";
  private static final String XML_DEADLINE     = "deadline";
  private static final String XML_COST         = "cost";
  private static final String XML_COMMENT      = "comment";

  /**************************************** constructor ******************************************/
  public Task()
  {
    // initialise private variables
    m_predecessors = new Predecessors( "" );
  }

  /**************************************** constructor ******************************************/
  public Task( XMLStreamReader xsr ) throws XMLStreamException
  {
    this();
    // read XML resource attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch (xsr.getAttributeLocalName( i )) {
        case XML_TITLE:
          m_title = xsr.getAttributeValue( i );
          break;
        case XML_DURATION:
          m_duration = new TimeSpan( xsr.getAttributeValue( i ) );
          break;
        case XML_START:
          m_start = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XML_END:
          m_end = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XML_WORK:
          m_work = new TimeSpan( xsr.getAttributeValue( i ) );
          break;
        case XML_RESOURCES:
          m_resources = new TaskResources( xsr.getAttributeValue( i ) );
          break;
        case XML_TYPE:
          m_type = new TaskType( xsr.getAttributeValue( i ) );
          break;
        case XML_PRIORITY:
          m_priority = 1_000_000 * Integer.parseInt( xsr.getAttributeValue( i ) );
          break;
        case XML_DEADLINE:
          m_deadline = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XML_COST:
          m_cost = xsr.getAttributeValue( i );
          break;
        case XML_COMMENT:
          m_comment = xsr.getAttributeValue( i );
          break;
      }
  }

  /***************************************** initialise ******************************************/
  public void initialise()
  {
    // initialise private variables
    m_duration = new TimeSpan( "1d" );
    m_work = new TimeSpan( "0d" );
    m_start = JPlanner.plan.start();
    m_end = JPlanner.plan.start();
    m_predecessors = new Predecessors( "" );
    m_resources = new TaskResources();
    m_type = new TaskType( TaskType.ASAP_FDUR );
    m_priority = 100 * 1_000_000;
  }

  /****************************************** toString *******************************************/
  public String toString( int section )
  {
    // return display string for given section
    if ( section == SECTION_TITLE )
      return m_title;

    // if task is null return blank for all other sections
    if ( isNull() )
      return "";

    if ( section == SECTION_DURATION )
      return m_duration.toString();

    if ( section == SECTION_START )
      return m_start.toString( JPlanner.plan.datetimeFormat() );

    if ( section == SECTION_END )
      return m_end.toString( JPlanner.plan.datetimeFormat() );

    if ( section == SECTION_WORK )
      return m_work.toString();

    if ( section == SECTION_PRED )
      return m_predecessors.toString();

    if ( section == SECTION_RES )
      return m_resources.toString();

    if ( section == SECTION_TYPE )
      return m_type.toString();

    if ( section == SECTION_PRIORITY )
      return String.format( "%d", m_priority / 1_000_000 );

    if ( section == SECTION_DEADLINE )
    {
      if ( m_deadline == null )
        return "NA";

      return m_deadline.toString( JPlanner.plan.datetimeFormat() );
    }

    if ( section == SECTION_COST )
      return m_cost;

    if ( section == SECTION_COMMENT )
      return m_comment;

    throw new IllegalArgumentException( "Section=" + section );
  }

  /****************************************** setData ********************************************/
  public void setData( int section, Object newValue )
  {
    // set task data for given section 
    if ( section == SECTION_TITLE )
    {
      if ( isNull() )
        initialise();

      m_title = (String) newValue;
    }

    else if ( section == SECTION_DURATION )
      m_duration = new TimeSpan( (String) newValue );

    else if ( section == SECTION_START )
      m_start = (DateTime) newValue;

    else if ( section == SECTION_END )
      m_end = (DateTime) newValue;

    else if ( section == SECTION_WORK )
      m_work = new TimeSpan( (String) newValue );

    else if ( section == SECTION_PRED )
      m_predecessors = new Predecessors( (String) newValue );

    else if ( section == SECTION_TYPE )
      m_type = new TaskType( (String) newValue );

    else if ( section == SECTION_PRIORITY )
      m_priority = 1_000_000 * (int) newValue;

    else if ( section == SECTION_DEADLINE )
      m_deadline = (DateTime) newValue;

    else if ( section == SECTION_COMMENT )
      m_comment = (String) newValue;

    // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!

    else
      throw new IllegalArgumentException( "Section=" + section );
  }

  /****************************************** isNull *********************************************/
  public boolean isNull()
  {
    // task is considered null if title not set
    return ( m_title == null );
  }

  /**************************************** sectionName ******************************************/
  public static String sectionName( int num )
  {
    // return section title
    if ( num == SECTION_TITLE )
      return "Title";

    if ( num == SECTION_DURATION )
      return "Duration";

    if ( num == SECTION_START )
      return "Start";

    if ( num == SECTION_END )
      return "End";

    if ( num == SECTION_WORK )
      return "Work";

    if ( num == SECTION_PRED )
      return "Predecessors";

    if ( num == SECTION_RES )
      return "Resources";

    if ( num == SECTION_TYPE )
      return "Type";

    if ( num == SECTION_PRIORITY )
      return "Priority";

    if ( num == SECTION_DEADLINE )
      return "Deadline";

    if ( num == SECTION_COST )
      return "Cost";

    if ( num == SECTION_COMMENT )
      return "Comment";

    throw new IllegalArgumentException( "Section=" + num );
  }

  /******************************************* type **********************************************/
  public TaskType type()
  {
    return m_type;
  }

  /****************************************** saveToXML ******************************************/
  public void saveToXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write task data to XML stream (except predecessors)
    xsw.writeStartElement( XML_TASK );
    xsw.writeAttribute( XML_ID, Integer.toString( JPlanner.plan.index( this ) ) );

    if ( !isNull() )
    {
      xsw.writeAttribute( XML_TITLE, m_title );
      xsw.writeAttribute( XML_DURATION, m_duration.toString() );
      xsw.writeAttribute( XML_START, m_start.toString() );
      xsw.writeAttribute( XML_END, m_end.toString() );
      xsw.writeAttribute( XML_WORK, m_work.toString() );
      xsw.writeAttribute( XML_RESOURCES, m_resources.toString() );
      xsw.writeAttribute( XML_TYPE, m_type.toString() );
      xsw.writeAttribute( XML_PRIORITY, Integer.toString( m_priority / 1_000_000 ) );
      if ( m_deadline != null )
        xsw.writeAttribute( XML_DEADLINE, m_deadline.toString() );
      if ( m_cost != null )
        xsw.writeAttribute( XML_COST, m_cost );
      if ( m_comment != null )
        xsw.writeAttribute( XML_COMMENT, m_comment.toString() );
    }

    xsw.writeEndElement(); // XML_TASK
  }

}
