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

package rjc.jplanner.gui;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import javafx.scene.control.TabPane;
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
  public MainTabWidget( boolean showPlanTab )
  {
    // construct main tab widget
    super();

    // create and add plan tab only if requested
    if ( showPlanTab )
    {
      m_tabPlan = new PlanTab( "Plan" );
      getTabs().add( m_tabPlan );
    }

    // create and add the other tabs
    m_tabTasks = new TasksTab( "Tasks & Gantt" );
    m_tabResources = new ResourcesTab( "Resources" );
    m_tabCalendars = new CalendarsTab( "Calendars" );
    m_tabDays = new DaysTab( "Days" );

    getTabs().addAll( m_tabTasks, m_tabResources, m_tabCalendars, m_tabDays );
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
      // write gantt display data
      xsw.writeStartElement( XmlLabels.XML_GANTT );
      xsw.writeAttribute( XmlLabels.XML_SPLITTER, Integer.toString( m_tabTasks.getSplitPosition() ) );
      m_tabTasks.getGantt().writeXML( xsw );
      xsw.writeEndElement(); // XML_GANTT

      // write tasks table data
      xsw.writeStartElement( XmlLabels.XML_TASKS_TABLE );
      m_tabTasks.getTable().writeXML( xsw );
      xsw.writeEndElement(); // XML_TASKS_TABLE

      // write resources table data
      xsw.writeStartElement( XmlLabels.XML_RESOURCES_TABLE );
      m_tabResources.getTable().writeXML( xsw );
      xsw.writeEndElement(); // XML_RESOURCES_TABLE

      // write calendars table data
      xsw.writeStartElement( XmlLabels.XML_CALENDARS_TABLE );
      m_tabCalendars.getTable().writeXML( xsw );
      xsw.writeEndElement(); // XML_CALENDARS_TABLE

      // write day-types table data
      xsw.writeStartElement( XmlLabels.XML_DAYTYPES_TABLE );
      m_tabDays.getTable().writeXML( xsw );
      xsw.writeEndElement(); // XML_DAYTYPES_TABLE
    }
    catch ( XMLStreamException exception )
    {
      // some sort of exception thrown
      exception.printStackTrace();
      return false;
    }

    return true;
  }

}
