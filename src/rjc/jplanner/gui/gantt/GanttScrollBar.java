/**************************************************************************
 *  Copyright (C) 2018 by Richard Crook                                   *
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

import javafx.scene.control.ScrollBar;

/*************************************************************************************************/
/*************** Extended version of ScrollBar with special increment & decrement ****************/
/*************************************************************************************************/

class GanttScrollBar extends ScrollBar
{
  private Gantt               m_gantt;        // gantt definition

  private final static double INCREMENT = 8.0;

  /**************************************** constructor ******************************************/
  public GanttScrollBar( Gantt gantt )
  {
    // create scroll bar and record table
    m_gantt = gantt;

    // prevent value becoming min or max because this prevents decrement() or increment() being called
    setMin( -1e-6 );
    valueProperty().addListener( ( observable, oldValue, newValue ) ->
    {
      if ( getValue() == getMax() )
        setValue( getMax() - 1e-6 );
      if ( getValue() == getMin() )
        setValue( getMin() + 1e-6 );
    } );
  }

  /****************************************** increment ******************************************/
  @Override
  public void increment()
  {
    // make gantt end later if scroll-bar at maximum
    if ( getValue() + INCREMENT > getMax() )
    {
      long ms = (long) ( ( getValue() + INCREMENT - getMax() ) * m_gantt.getMsPP() );
      m_gantt.setEnd( m_gantt.getEnd().plusMilliseconds( ms ) );
      m_gantt.checkScrollbar();
      setValue( getMax() );
    }
    else
      setValue( getValue() + INCREMENT );
  }

  /****************************************** decrement ******************************************/
  @Override
  public void decrement()
  {
    // make gantt start earlier if scroll-bar at minimum
    if ( getValue() - INCREMENT < getMin() )
    {
      long ms = (long) ( ( getValue() - INCREMENT - getMin() ) * m_gantt.getMsPP() );
      m_gantt.setStart( m_gantt.getStart().plusMilliseconds( ms ) );
      m_gantt.checkScrollbar();
      setValue( getMin() );
    }
    else
      setValue( getValue() - INCREMENT );
  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    return "VAL=" + getValue() + " MIN=" + getMin() + " MAX=" + getMax() + " VIS=" + getVisibleAmount() + " BLK"
        + getBlockIncrement() + " UNIT" + getUnitIncrement() + " ORIENT=" + getOrientation();
  }

}
