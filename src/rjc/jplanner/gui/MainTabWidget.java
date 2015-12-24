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

package rjc.jplanner.gui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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

  /**************************************** constructor ******************************************/
  public MainTabWidget()
  {
    // construct main tab widget
    super();

    // create and add the five tabs
    Tab tabPlan = new PlanTab( "Plan" );
    Tab tabTasks = new TasksTab( "Tasks & Gantt" );
    Tab tabResources = new ResourcesTab( "Resources" );
    Tab tabCalendars = new CalendarsTab( "Calendars" );
    Tab tabDays = new DaysTab( "Days" );

    getTabs().addAll( tabPlan, tabTasks, tabResources, tabCalendars, tabDays );
  }

}
