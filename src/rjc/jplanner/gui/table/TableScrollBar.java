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

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;

/*************************************************************************************************/
/*************************** Scroll bar for tables that self-manage ******************************/
/*************************************************************************************************/

public class TableScrollBar extends ScrollBar
{
  public static final double SIZE = 18.0;

  private Table              m_table;
  private TableScrollBar     m_other;

  /**************************************** constructor ******************************************/
  public TableScrollBar( Table table, Orientation orientation )
  {
    // create table scroll bar
    super();
    m_table = table;
    setOrientation( orientation );
    setMinWidth( SIZE );
    setMinHeight( SIZE );

    // add listener to ensure scroll bar appropriate for table body size
    ReadOnlyDoubleProperty property = m_table.getBody().heightProperty();
    if ( orientation == Orientation.HORIZONTAL )
      property = m_table.getBody().widthProperty();
    property.addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        checkSettings();
        scrollTable( (int) getValue() );
      }
    } );

    // add listener to scroll table when scroll bar thumb moved
    valueProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        scrollTable( newValue.intValue() );
      }
    } );

  }

  /****************************************** toString *******************************************/
  private void scrollTable( int value )
  {
    // scroll table body and header
    if ( getOrientation() == Orientation.HORIZONTAL )
    {
      // horizontal scroll bar
      m_table.getBody().setTranslateX( -value );
      m_table.getHorizontalHeader().setTranslateX( -value );
    }
    else
    {
      // vertical scroll bar
      m_table.getBody().setTranslateY( -value );
      m_table.getVerticalHeader().setTranslateY( -value );
    }

  }

  /****************************************** toString *******************************************/
  @Override
  public String toString()
  {
    return "TableScrollBar " + getOrientation().toString().charAt( 0 ) + " value=" + getValue() + " thumb="
        + getVisibleAmount() + " min=" + getMin() + " max=" + getMax();
  }

  /****************************************** setOther *******************************************/
  public void setOther( TableScrollBar other )
  {
    m_other = other;
  }

  /****************************************** setSpan ********************************************/
  private void setSpan( int size )
  {
    // set scroll-bar span across table grid cells
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

  /****************************************** setSpans *******************************************/
  private void setSpans()
  {
    // ensure scroll bar spans are correct for those visible
    if ( isVisible() && m_other.isVisible() )
    {
      // both visible so both need length of 2
      setSpan( 2 );
      m_other.setSpan( 2 );
    }
    else
    {
      // if visible set length to 3
      if ( isVisible() )
        setSpan( 3 );
      if ( m_other.isVisible() )
        m_other.setSpan( 3 );
    }
  }

  /**************************************** checkSettings ****************************************/
  void checkSettings()
  {
    // check ensure scroll bar appropriate for table body size
    double size = 0.0;
    int index, max;
    if ( !m_other.isVisible() )
      size += SIZE;
    if ( getOrientation() == Orientation.HORIZONTAL )
    {
      size += m_table.getBody().getWidth();
      index = m_table.getColumnExactAtX( size );
      max = m_table.getColumnStartX( Integer.MAX_VALUE );
    }
    else
    {
      size += m_table.getBody().getHeight();
      index = m_table.getRowExactAtY( size );
      max = m_table.getRowStartY( Integer.MAX_VALUE );
    }

    // scroll bar needed if not beyond normal column/row index at edge
    boolean needed = index != Integer.MAX_VALUE;
    if ( isVisible() != needed )
    {
      setVisible( needed );
      setSpans();
      m_other.checkSettings();
    }

    // if visible, check thumb size
    if ( isVisible() )
    {
      if ( getValue() > max - size )
        setValue( max - size );
      setMax( max - size );
      setVisibleAmount( size / max * ( max - size ) );
    }
    else
      setValue( 0.0 );

  }

}
