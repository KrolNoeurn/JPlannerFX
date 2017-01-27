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

/*************************************************************************************************/
/******************************** Single task within overall plan ********************************/
/*************************************************************************************************/

public class Task implements Comparable<Task>
{
  private String          m_title;              // free text title
  private TimeSpan        m_duration;           // duration of task
  private DateTime        m_start;              // start date-time of task
  private DateTime        m_end;                // end date-time of task
  private TimeSpan        m_work;               // work effort for task
  private Predecessors    m_predecessors;       // task predecessors
  private TaskResources   m_resources;          // resources allocated to task
  private TaskType        m_type;               // task type
  private int             m_priority;           // overall task priority (0 to 999)
  private DateTime        m_deadline;           // task warning deadline
  private String          m_cost;               // calculated cost based on resource use
  private String          m_comment;            // free text comment

  private int             m_indent;             // task indent level, zero for no indent
  private int             m_summaryStart;       // index of this task's summary, ultimately task 0
  private int             m_summaryEnd;         // if summary, index of summary end, otherwise -1 
  private GanttData       m_gantt;              // data for gantt bar display

  public static final int SECTION_TITLE    = 0;
  public static final int SECTION_DURATION = 1;
  public static final int SECTION_START    = 2;
  public static final int SECTION_END      = 3;
  public static final int SECTION_WORK     = 4;
  public static final int SECTION_PRED     = 5;
  public static final int SECTION_RES      = 6;
  public static final int SECTION_TYPE     = 7;
  public static final int SECTION_PRIORITY = 8;
  public static final int SECTION_DEADLINE = 9;
  public static final int SECTION_COST     = 10;
  public static final int SECTION_COMMENT  = 11;
  public static final int SECTION_MAX      = 11;

  /**************************************** constructor ******************************************/
  public Task()
  {
    // do nothing
  }

  /**************************************** constructor ******************************************/
  public Task( XMLStreamReader xsr ) throws XMLStreamException
  {
    this();
    initialise();
    // read XML task attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_ID:
          break;
        case XmlLabels.XML_TITLE:
          m_title = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_DURATION:
          m_duration = new TimeSpan( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_START:
          m_start = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_END:
          m_end = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_WORK:
          m_work = new TimeSpan( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_RESOURCES:
          m_resources = new TaskResources( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_TYPE:
          m_type = new TaskType( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_PRIORITY:
          m_priority = Integer.parseInt( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_DEADLINE:
          m_deadline = new DateTime( xsr.getAttributeValue( i ) );
          break;
        case XmlLabels.XML_COST:
          m_cost = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_COMMENT:
          m_comment = xsr.getAttributeValue( i );
          break;
        case XmlLabels.XML_INDENT:
          m_indent = Integer.parseInt( xsr.getAttributeValue( i ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }
  }

  /***************************************** initialise ******************************************/
  public void initialise()
  {
    // initialise private variables
    m_duration = new TimeSpan( "1d" );
    m_work = new TimeSpan( "0d" );
    m_start = JPlanner.plan.getStart();
    m_end = JPlanner.plan.getStart();
    m_predecessors = new Predecessors( "" );
    m_resources = new TaskResources();
    m_type = new TaskType( TaskType.ASAP_FDUR );
    m_predecessors = new Predecessors();
    m_gantt = new GanttData();
    m_priority = 100;
    m_indent = 0;
    m_summaryStart = 0;
    m_summaryEnd = -1;
  }

  /****************************************** getValue *******************************************/
  public Object getValue( int section )
  {
    // return value for given section
    if ( section == SECTION_TITLE )
      return m_title;

    // if task is null return blank for all other sections
    if ( isNull() )
      return null;

    if ( section == SECTION_DURATION )
      return duration();

    if ( section == SECTION_START )
      return start();

    if ( section == SECTION_END )
      return end();

    if ( section == SECTION_WORK )
      return work();

    if ( section == SECTION_PRED )
      return m_predecessors;

    if ( section == SECTION_RES )
      return m_resources;

    if ( section == SECTION_TYPE )
      return m_type;

    if ( section == SECTION_PRIORITY )
      return m_priority;

    if ( section == SECTION_DEADLINE )
      return m_deadline;

    if ( section == SECTION_COST )
      return m_cost;

    if ( section == SECTION_COMMENT )
      return m_comment;

    throw new IllegalArgumentException( "Section=" + section );
  }

  /****************************************** setValue ******************************************/
  public void setValue( int section, Object newValue )
  {
    // set task value for given section
    if ( section == SECTION_TITLE )
    {
      if ( isNull() )
        initialise();

      m_title = (String) newValue;
    }

    else if ( section == SECTION_DURATION )
      m_duration = (TimeSpan) newValue;

    else if ( section == SECTION_START )
      m_start = (DateTime) newValue;

    else if ( section == SECTION_END )
      m_end = (DateTime) newValue;

    else if ( section == SECTION_WORK )
      m_work = (TimeSpan) newValue;

    else if ( section == SECTION_PRED )
      m_predecessors = (Predecessors) newValue;

    else if ( section == SECTION_RES )
      m_resources = (TaskResources) newValue;

    else if ( section == SECTION_TYPE )
      m_type = (TaskType) newValue;

    else if ( section == SECTION_PRIORITY )
      m_priority = (int) newValue;

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
    xsw.writeStartElement( XmlLabels.XML_TASK );
    xsw.writeAttribute( XmlLabels.XML_ID, Integer.toString( this.index() ) );

    if ( !isNull() )
    {
      xsw.writeAttribute( XmlLabels.XML_INDENT, Integer.toString( m_indent ) );
      xsw.writeAttribute( XmlLabels.XML_TITLE, m_title );
      xsw.writeAttribute( XmlLabels.XML_DURATION, m_duration.toString() );
      xsw.writeAttribute( XmlLabels.XML_START, m_start.toString() );
      xsw.writeAttribute( XmlLabels.XML_END, m_end.toString() );
      xsw.writeAttribute( XmlLabels.XML_WORK, m_work.toString() );
      xsw.writeAttribute( XmlLabels.XML_RESOURCES, m_resources.toString() );
      xsw.writeAttribute( XmlLabels.XML_TYPE, m_type.toString() );
      xsw.writeAttribute( XmlLabels.XML_PRIORITY, Integer.toString( m_priority ) );
      if ( m_deadline != null )
        xsw.writeAttribute( XmlLabels.XML_DEADLINE, m_deadline.toString() );
      if ( m_cost != null )
        xsw.writeAttribute( XmlLabels.XML_COST, m_cost );
      if ( m_comment != null )
        xsw.writeAttribute( XmlLabels.XML_COMMENT, m_comment.toString() );
    }

    xsw.writeEndElement(); // XML_TASK
  }

  /************************************ savePredecessorToXML *************************************/
  public void savePredecessorToXML( XMLStreamWriter xsw ) throws XMLStreamException
  {
    // write task predecessor data to XML stream
    if ( m_predecessors == null )
      return;
    String preds = m_predecessors.toString();

    if ( preds.length() > 0 )
    {
      xsw.writeStartElement( XmlLabels.XML_PREDECESSORS );
      xsw.writeAttribute( XmlLabels.XML_TASK, Integer.toString( this.index() ) );
      xsw.writeAttribute( XmlLabels.XML_PREDS, preds );
      xsw.writeEndElement(); // XML_PREDECESSORS
    }
  }

  /****************************************** compareTo ******************************************/
  @Override
  public int compareTo( Task other )
  {
    // sort comparison first check for predecessors
    if ( this.hasPredecessor( other ) )
      return 1;
    if ( other.hasPredecessor( this ) )
      return -1;

    // then by priority
    if ( m_priority < other.m_priority )
      return 1;
    if ( m_priority > other.m_priority )
      return -1;

    // finally by index
    return this.index() - other.index();
  }

  /***************************************** toString ********************************************/
  @Override
  public String toString()
  {
    // convert to string
    String hash = super.toString();
    String id = hash.substring( hash.lastIndexOf( '.' ) + 1 );
    return id + "['" + m_title + "' " + m_type + " " + m_priority + "]";
  }

  /**************************************** hasPredecessor ***************************************/
  public boolean hasPredecessor( Task other )
  {
    // return true if task is predecessor
    if ( m_predecessors.hasPredecessor( other ) )
      return true;

    // if task is summary, then sub-tasks are implicit predecessors
    if ( m_summaryEnd > 0 )
    {
      int thisNum = this.index();
      int otherNum = other.index();
      if ( otherNum > thisNum && otherNum <= m_summaryEnd )
        return true;
    }

    return false;
  }

  /***************************************** isSummary *******************************************/
  public boolean isSummary()
  {
    return m_summaryEnd > 0;
  }

  /***************************************** summaryEnd ******************************************/
  public int summaryEnd()
  {
    return m_summaryEnd;
  }

  /**************************************** setSummaryEnd ****************************************/
  public void setSummaryEnd( int index )
  {
    m_summaryEnd = index;
  }

  /*************************************** setSummaryStart ***************************************/
  public void setSummaryStart( int index )
  {
    m_summaryStart = index;
  }

  /****************************************** schedule *******************************************/
  public void schedule()
  {
    // TODO Auto-generated method stub
    JPlanner.trace( "Scheduling " + this );

    if ( m_type.toString() == TaskType.ASAP_FDUR )
    {
      schedule_ASAP_FDUR();
      return;
    }

    if ( m_type.toString() == TaskType.ASAP_FWORK )
    {
      schedule_ASAP_FWORK();
      return;
    }

    if ( m_type.toString() == TaskType.FIXED_PERIOD )
    {
      schedule_FIXED_PERIOD();
      return;
    }

    if ( m_type.toString() == TaskType.SON_FDUR )
    {
      schedule_SON_FDUR();
      return;
    }

    if ( m_type.toString() == TaskType.SON_FWORK )
    {
      schedule_SON_FWORK();
      return;
    }

    throw new UnsupportedOperationException( "Task type = " + m_type );
  }

  /************************************* schedule_SON_FWORK **************************************/
  private void schedule_SON_FWORK()
  {
    // TODO Auto-generated method stub
    JPlanner.trace( "NOT IMPLEMENTED YET !!!" );
  }

  /************************************* schedule_SON_FDUR ***************************************/
  private void schedule_SON_FDUR()
  {
    // TODO Auto-generated method stub
    JPlanner.trace( "NOT IMPLEMENTED YET !!!" );
  }

  /*********************************** schedule_FIXED_PERIOD *************************************/
  private void schedule_FIXED_PERIOD()
  {
    // TODO Auto-generated method stub
    JPlanner.trace( "NOT IMPLEMENTED YET !!!" );
  }

  /************************************* schedule_ASAP_FWORK *************************************/
  private void schedule_ASAP_FWORK()
  {
    // TODO Auto-generated method stub
    JPlanner.trace( "NOT IMPLEMENTED YET !!!" );
  }

  /************************************* schedule_ASAP_FDUR **************************************/
  private void schedule_ASAP_FDUR()
  {
    // depending on predecessors determine task start & end
    boolean hasToStart = m_predecessors.hasToStart();
    boolean hasToFinish = m_predecessors.hasToFinish();

    // if this task doesn't have predecessors, does a summary?
    if ( !hasToStart && !hasToFinish )
    {
      Task task = this;
      for ( int indent = m_indent; indent > 0; indent-- )
      {
        task = JPlanner.plan.getTask( task.m_summaryStart );

        hasToStart = task.m_predecessors.hasToStart();
        if ( hasToStart )
          break;

        hasToFinish = task.m_predecessors.hasToFinish();
        if ( hasToFinish )
          break;
      }
    }

    Calendar planCal = JPlanner.plan.getDefaultcalendar();
    if ( m_duration.getNumber() == 0.0 )
    {
      // milestone
      if ( hasToStart )
        m_start = planCal.roundDown( startDueToPredecessors() );
      else if ( hasToFinish )
        m_start = planCal.roundDown( endDueToPredecessors() );
      else
        m_start = planCal.roundUp( JPlanner.plan.getStart() );

      m_end = m_start;
    }
    else
    {
      // not milestone
      if ( hasToStart )
      {
        m_start = planCal.roundUp( startDueToPredecessors() );
        m_end = planCal.roundDown( planCal.workTimeSpan( m_start, m_duration ) );
      }
      else if ( hasToFinish )
      {
        m_end = planCal.roundDown( endDueToPredecessors() );
        m_start = planCal.roundUp( planCal.workTimeSpan( m_end, m_duration.minus() ) );
      }
      else
      {
        m_start = planCal.roundUp( JPlanner.plan.getStart() );
        m_end = planCal.roundDown( planCal.workTimeSpan( m_start, m_duration ) );
      }
    }

    // ensure end is always greater or equal to start
    if ( m_end.isLessThan( m_start ) )
      m_end = m_start;

    // set gantt task bar data
    if ( isSummary() )
      m_gantt.setSummary( start(), end() );
    else
      m_gantt.setTask( m_start, m_end );

    // set resource allocations
    m_resources.assign( this );
  }

  /********************************************* end *********************************************/
  public DateTime end()
  {
    // return task or summary end date-time
    if ( isSummary() )
    {
      // loop through each subtask
      DateTime end = DateTime.MIN_VALUE;
      for ( int id = index() + 1; id <= m_summaryEnd; id++ )
      {
        // if task isn't summary & isn't null, check if its end is after current latest
        Task task = JPlanner.plan.getTask( id );
        if ( !task.isNull() && !task.isSummary() && end.isLessThan( task.m_end ) )
          end = task.m_end;
      }

      return end;
    }

    return m_end;
  }

  /******************************************** start ********************************************/
  public DateTime start()
  {
    // return task or summary start date-time
    if ( isSummary() )
    {
      // loop through each subtask
      DateTime start = DateTime.MAX_VALUE;
      for ( int id = index() + 1; id <= m_summaryEnd; id++ )
      {
        // if task isn't summary & isn't null, check if its start is before current earliest
        Task task = JPlanner.plan.getTask( id );
        if ( !task.isNull() && !task.isSummary() && task.m_start.isLessThan( start ) )
          start = task.m_start;
      }

      return start;
    }

    return m_start;
  }

  /******************************************** work *********************************************/
  public TimeSpan work()
  {
    // return task or summary work time-span
    if ( isSummary() )
    {
      // TODO return JPlanner.plan.resources.work( this );

      return new TimeSpan();
    }

    return m_work;
  }

  /****************************************** duration *******************************************/
  public TimeSpan duration()
  {
    // return task or summary work time-span
    if ( isSummary() )
      return JPlanner.plan.getDefaultcalendar().workBetween( start(), end() );

    return m_duration;
  }

  /************************************ startDueToPredecessors ***********************************/
  private DateTime startDueToPredecessors()
  {
    // get start based on this task's predecessors
    DateTime start = m_predecessors.start();

    // if indented also check start against summary(s) predecessors
    Task task = this;
    for ( int indent = m_indent; indent > 0; indent-- )
    {
      task = JPlanner.plan.getTask( task.m_summaryStart );

      // if start from summary predecessors is later, use it instead
      DateTime summaryStart = task.m_predecessors.start();
      if ( start.isLessThan( summaryStart ) )
        start = summaryStart;
    }

    return start;
  }

  /************************************* endDueToPredecessors ************************************/
  private DateTime endDueToPredecessors()
  {
    // get end based on this task's predecessors
    DateTime end = m_predecessors.end();

    // if indented also check end against summary(s) predecessors
    Task task = this;
    for ( int indent = m_indent; indent > 0; indent-- )
    {
      task = JPlanner.plan.getTask( task.m_summaryStart );

      // if end from summary predecessors is later, use it instead
      DateTime summaryEnd = task.m_predecessors.end();
      if ( summaryEnd.isLessThan( end ) )
        end = summaryEnd;
    }

    return end;
  }

  /**************************************** predecessors *****************************************/
  public Predecessors predecessors()
  {
    return m_predecessors;
  }

  /****************************************** ganttData ******************************************/
  public GanttData ganttData()
  {
    // return gantt-data associated with the task
    return m_gantt;
  }

  /******************************************** index ********************************************/
  public int index()
  {
    return JPlanner.plan.getIndex( this );
  }

  /****************************************** priority *******************************************/
  public int priority()
  {
    return m_priority;
  }

  /******************************************* indent ********************************************/
  public int indent()
  {
    return m_indent;
  }

  /****************************************** setIndent ******************************************/
  public void setIndent( int indent )
  {
    m_indent = indent;
  }

  /************************************** isSectionEditable **************************************/
  public boolean isSectionEditable( int section )
  {
    // return if section is enable for this task
    if ( section == SECTION_TITLE )
      return true;

    if ( isNull() )
      return false;

    if ( section == SECTION_COST )
      return false;

    if ( isSummary() )
      if ( section == SECTION_DURATION || section == SECTION_START || section == SECTION_END
          || section == SECTION_WORK )
        return false;

    return m_type.isSectionEditable( section );
  }

}