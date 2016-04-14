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

package rjc.jplanner.gui;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import javafx.scene.control.TabPane;
import rjc.jplanner.JPlanner;
import rjc.jplanner.XmlLabels;
import rjc.jplanner.gui.calendars.CalendarsTab;
import rjc.jplanner.gui.days.DaysTab;
import rjc.jplanner.gui.plan.PlanTab;
import rjc.jplanner.gui.resources.ResourcesTab;
import rjc.jplanner.gui.tasks.TasksTab;

/*************************************************************************************************/
/*************** Widget showing the Plan/Tasks&Gantt/Resources/Calendars/Days tabs ***************/
/*************************************************************************************************/

public class MainTabWidget extends TabPane
{
  private PlanTab      m_tabPlan;
  private TasksTab     m_tabTasks;
  private ResourcesTab m_tabResources;
  private CalendarsTab m_tabCalendars;
  private DaysTab      m_tabDays;

  /**************************************** constructor ******************************************/
  public MainTabWidget()
  {
    // construct main tab widget
    super();

    // create and add the five tabs
    m_tabPlan = new PlanTab( "Plan" );
    m_tabTasks = new TasksTab( "Tasks & Gantt" );
    m_tabResources = new ResourcesTab( "Resources" );
    m_tabCalendars = new CalendarsTab( "Calendars" );
    m_tabDays = new DaysTab( "Days" );

    getTabs().addAll( m_tabPlan, m_tabTasks, m_tabResources, m_tabCalendars, m_tabDays );
  }

  /******************************************* select ********************************************/
  public void select( int tabNumber )
  {
    getSelectionModel().select( tabNumber );
  }

  /***************************************** getPlanTab ******************************************/
  public PlanTab getPlanTab()
  {
    return m_tabPlan;
  }

  /**************************************** getTasksTab ******************************************/
  public TasksTab getTasksTab()
  {
    return m_tabTasks;
  }

  /*************************************** getResourcesTab ***************************************/
  public ResourcesTab getResourcesTab()
  {
    return m_tabResources;
  }

  /*************************************** getCalendarsTab ***************************************/
  public CalendarsTab getCalendarsTab()
  {
    return m_tabCalendars;
  }

  /***************************************** getDaysTab ******************************************/
  public DaysTab getDaysTab()
  {
    return m_tabDays;
  }

  /****************************************** writeXML *******************************************/
  public boolean writeXML( XMLStreamWriter xsw )
  {
    // write display data to XML stream
    try
    {
      // write tasks-gantt display data
      xsw.writeStartElement( XmlLabels.XML_TASKS_GANTT_TAB );
      xsw.writeAttribute( XmlLabels.XML_SPLITTER, Integer.toString( m_tabTasks.getSplitPosition() ) );
      m_tabTasks.getGantt().writeXML( xsw );
      m_tabTasks.getTable().writeXML( xsw );
      xsw.writeEndElement(); // XML_TASKS_GANTT_TAB

      // write resources display data
      xsw.writeStartElement( XmlLabels.XML_RESOURCES_TAB );
      m_tabResources.getTable().writeXML( xsw );
      xsw.writeEndElement(); // XML_RESOURCES_TAB

      // write calendars display data
      xsw.writeStartElement( XmlLabels.XML_CALENDARS_TAB );
      m_tabCalendars.getTable().writeXML( xsw );
      xsw.writeEndElement(); // XML_CALENDARS_TAB

      // write day-types display data
      xsw.writeStartElement( XmlLabels.XML_DAYS_TAB );
      m_tabDays.getTable().writeXML( xsw );
      xsw.writeEndElement(); // XML_DAYS_TAB
    }
    catch ( XMLStreamException exception )
    {
      // some sort of exception thrown
      exception.printStackTrace();
      return false;
    }

    return true;
  }

  /************************************** loadXmlTasksGantt **************************************/
  public void loadXmlTasksGantt( XMLStreamReader xsr ) throws XMLStreamException
  {
    // adopt tasks-gantt tab data from XML stream, starting with the attributes
    for ( int i = 0; i < xsr.getAttributeCount(); i++ )
      switch ( xsr.getAttributeLocalName( i ) )
      {
        case XmlLabels.XML_SPLITTER:
          m_tabTasks.setSplitPosition( Integer.parseInt( xsr.getAttributeValue( i ) ) );
          break;
        default:
          JPlanner.trace( "Unhandled attribute '" + xsr.getAttributeLocalName( i ) + "'" );
          break;
      }

    // read tasks-gantt tab XML elements
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of tab data, break
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_TASKS_GANTT_TAB ) )
        break;

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_GANTT:
            m_tabTasks.getGantt().loadXML( xsr );
            break;
          case XmlLabels.XML_COLUMNS:
            m_tabTasks.getTable().loadColumns( xsr );
            break;
          case XmlLabels.XML_ROWS:
            m_tabTasks.getTable().loadRows( xsr );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }

  }

  /************************************** loadXmlResources ***************************************/
  public void loadXmlResources( XMLStreamReader xsr ) throws XMLStreamException
  {
    // read resources tab XML elements
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of tab data, break
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_RESOURCES_TAB ) )
        break;

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_COLUMNS:
            m_tabResources.getTable().loadColumns( xsr );
            break;
          case XmlLabels.XML_ROWS:
            m_tabResources.getTable().loadRows( xsr );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }
  }

  /************************************** loadXmlCalendars ***************************************/
  public void loadXmlCalendars( XMLStreamReader xsr ) throws XMLStreamException
  {
    // read calendars tab XML elements
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of tab data, break
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_CALENDARS_TAB ) )
        break;

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_COLUMNS:
            m_tabCalendars.getTable().loadColumns( xsr );
            break;
          case XmlLabels.XML_ROWS:
            m_tabCalendars.getTable().loadRows( xsr );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }
  }

  /*************************************** loadXmlDayTypes ***************************************/
  public void loadXmlDayTypes( XMLStreamReader xsr ) throws XMLStreamException
  {
    // read day-types tab XML elements
    while ( xsr.hasNext() )
    {
      xsr.next();

      // if reached end of tab data, break
      if ( xsr.isEndElement() && xsr.getLocalName().equals( XmlLabels.XML_DAYS_TAB ) )
        break;

      if ( xsr.isStartElement() )
        switch ( xsr.getLocalName() )
        {
          case XmlLabels.XML_COLUMNS:
            m_tabDays.getTable().loadColumns( xsr );
            break;
          case XmlLabels.XML_ROWS:
            m_tabDays.getTable().loadRows( xsr );
            break;
          default:
            JPlanner.trace( "Unhandled start element '" + xsr.getLocalName() + "'" );
            break;
        }
    }
  }

}
