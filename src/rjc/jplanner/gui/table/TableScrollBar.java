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

package rjc.jplanner.gui.table;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;

/*************************************************************************************************/
/*************************** Scroll bar for tables that self-manage ******************************/
/*************************************************************************************************/

public class TableScrollBar extends ScrollBar
{
  private Table          m_table;
  private TableScrollBar m_other;

  /**************************************** constructor ******************************************/
  public TableScrollBar( Table table, Orientation orientation )
  {
    // create table scroll bar
    super();
    m_table = table;
    setOrientation( orientation );
    setMinWidth( 18.0 );
    setMinHeight( 18.0 );

    if ( orientation == Orientation.HORIZONTAL )
      addHorizontalListeners();
    else
      addVerticalListeners();
  }

  /****************************************** setOther *******************************************/
  public void setOther( TableScrollBar other )
  {
    m_other = other;
  }

  /***************************************** setLength *******************************************/
  private void setLength( int size )
  {
    // set scroll-bar length
    if ( getOrientation() == Orientation.HORIZONTAL )
    {
      // horizontal scroll bar
      m_table.getChildren().remove( this );
      m_table.add( this, 0, 2, size, 1 );
    }
    else
    {
      // vertical scroll bar
      m_table.getChildren().remove( this );
      m_table.add( this, 2, 0, 1, size );
    }
  }

  /***************************************** setLengths ******************************************/
  private void setLengths()
  {
    // ensure scroll bar lengths are correct for those visible
    if ( isVisible() && m_other.isVisible() )
    {
      // both visible so both need length of 2
      setLength( 2 );
      m_other.setLength( 2 );
    }
    else
    {
      // if visible set length to 3
      if ( isVisible() )
        setLength( 3 );
      if ( m_other.isVisible() )
        m_other.setLength( 3 );
    }
  }

  /******************************************** check ********************************************/
  private void check( int index )
  {
    // if change needed, hide/show scroll bar and reset scroll bar lengths 
    boolean needed = index != Integer.MAX_VALUE;
    if ( isVisible() != needed )
    {
      setVisible( needed );
      setLengths();
    }
  }

  /************************************ addVerticalListeners *************************************/
  private void addVerticalListeners()
  {
    // add listener for height change
    m_table.getBody().heightProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        // check if vertical scroll bar needed
        int y = newValue.intValue();
        if ( !m_other.isVisible() )
          y += TableScrollBar.this.getWidth();
        int row = m_table.getRowExactAtY( y );
        check( row );
      }
    } );

  }

  /*********************************** addHorizontalListeners ************************************/
  private void addHorizontalListeners()
  {
    // add listener for width change
    m_table.getBody().widthProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        // check if horizontal scroll bar needed
        int x = newValue.intValue();
        if ( !m_other.isVisible() )
          x += TableScrollBar.this.getHeight();
        int column = m_table.getColumnExactAtX( x );
        check( column );
      }
    } );

  }

}
