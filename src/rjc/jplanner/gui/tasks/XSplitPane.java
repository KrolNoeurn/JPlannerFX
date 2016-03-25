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

package rjc.jplanner.gui.tasks;

import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;

/*************************************************************************************************/
/***************** Extended version of SplitPane with preferred left node width ******************/
/*************************************************************************************************/

public class XSplitPane extends SplitPane
{
  public int               preferredLeftNodeWidth = 300;
  private boolean          m_ignore               = false;

  private static final int DIVIDER_WIDTH          = 6;

  /**************************************** constructor ******************************************/
  public XSplitPane( Region... regions )
  {
    super( regions );
    setMinWidth( 0.0 );

    // add listener to ensure divider is at preferred position when pane resized
    widthProperty().addListener( ( observable, oldValue, newValue ) ->
    {
      m_ignore = true;
      setDividerPosition( 0, preferredLeftNodeWidth / newValue.doubleValue() );
    } );

    // add listener to ensure preferred position is updated when divider manually moved
    getDividers().get( 0 ).positionProperty().addListener( ( observable, oldValue, newValue ) ->
    {
      if ( !m_ignore )
      {
        // don't confuse divider movement due to pane resize
        if ( regions[1].getWidth() == 0.0 || getWidth() - regions[0].getWidth() < DIVIDER_WIDTH )
          return;

        preferredLeftNodeWidth = (int) ( getWidth() * newValue.doubleValue() );
      }
      m_ignore = false;
    } );
  }

}
