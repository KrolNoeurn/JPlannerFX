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

package rjc.jplanner.gui.plan;

import rjc.jplanner.JPlanner;

/*************************************************************************************************/
/******************* Extended version of AbstractCombo with list of calendars ********************/
/*************************************************************************************************/

public class CalendarCombo extends AbstractCombo
{

  /**************************************** refreshItems *****************************************/
  @Override
  void refreshList()
  {
    // refresh items that user can select from
    m_items.clear();
    int count = JPlanner.plan.calendarsCount();
    for ( int index = 0; index < count; index++ )
      m_items.add( JPlanner.plan.calendar( index ).name() );
  }

}
