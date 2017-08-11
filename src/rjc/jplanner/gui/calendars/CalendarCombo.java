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

package rjc.jplanner.gui.calendars;

import rjc.jplanner.JPlanner;
import rjc.jplanner.gui.AbstractComboEditor;
import rjc.jplanner.model.Calendar;

/*************************************************************************************************/
/**************** Extended version of AbstractComboEditor with list of calendars *****************/
/*************************************************************************************************/

public class CalendarCombo extends AbstractComboEditor
{

  /**************************************** getItemCount *****************************************/
  @Override
  public int getItemCount()
  {
    // return number of calendars
    return JPlanner.plan.getCalendarsCount();
  }

  /******************************************* getItem *******************************************/
  @Override
  public String getItem( int num )
  {
    // return calendar name item for list
    return JPlanner.plan.getCalendar( num ).getName();
  }

  /***************************************** getCalendar *****************************************/
  public Calendar getCalendar()
  {
    // return plan calendar which is currently selected
    return JPlanner.plan.getCalendar( getSelectedIndex() );
  }

  /***************************************** setCalendar *****************************************/
  public void setCalendar( Calendar calendar )
  {
    // set editor to specified plan calendar
    setText( calendar.getName() );
  }

}
