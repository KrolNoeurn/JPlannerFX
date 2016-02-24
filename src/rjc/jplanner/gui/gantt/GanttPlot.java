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

package rjc.jplanner.gui.gantt;

import rjc.jplanner.model.DateTime;

/*************************************************************************************************/
/***************** GanttPlot provides a view of the plan tasks and dependencies ******************/
/*************************************************************************************************/

public class GanttPlot
{
  private DateTime      m_start;
  private long          m_millisecondsPP;

  private static int    m_taskHeight = 6;
  private static int    m_arrowSize  = 4;

  public static boolean ganttStretch;

  /****************************************** setStart *******************************************/
  public void setStart( DateTime start )
  {
    m_start = start;
  }

  /****************************************** setMsPP ********************************************/
  public void setMsPP( long mspp )
  {
    m_millisecondsPP = mspp;
  }

}
