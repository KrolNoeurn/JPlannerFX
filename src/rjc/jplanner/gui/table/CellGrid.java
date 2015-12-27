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

import java.util.HashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/*************************************************************************************************/
/************************ Abstract gui node for displaying grid of cells *************************/
/*************************************************************************************************/

public abstract class CellGrid extends Pane
{
  protected Table                m_table;

  private HashMap<Integer, Cell> m_cells = new HashMap<Integer, Cell>();

  /**************************************** constructor ******************************************/
  public CellGrid( Table table )
  {
    // construct grid drawing area
    super();
    setClip( new Rectangle( 9999999, 9999999 ) );
    m_table = table;

    // add listener for width change
    widthProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        setClip();
        int oldWidth = oldValue.intValue();
        int newWidth = newValue.intValue();

        // if new width not greater, can ignore
        if ( oldWidth >= newWidth )
          return;

        // width has increased, so add any extra new columns needed
        int min = m_table.getColumnAtX( oldWidth - getTranslateX() );
        int max = m_table.getColumnAtX( newWidth + TableScrollBar.SIZE - getTranslateX() );
        addColumns( min, max );
      }
    } );

    // add listener for height change
    heightProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        setClip();
        int oldHeight = oldValue.intValue();
        int newHeight = newValue.intValue();

        // if new height not greater, can ignore
        if ( oldHeight >= newHeight )
          return;

        // height has increased, so add any extra new rows needed
        int min = m_table.getRowAtY( oldHeight - getTranslateY() );
        int max = m_table.getRowAtY( newHeight + TableScrollBar.SIZE - getTranslateY() );
        addRows( min, max );
      }
    } );

    // add listener for horizontal scrolling
    translateXProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        setClip();
        int oldX = oldValue.intValue();
        int newX = newValue.intValue();

        if ( oldX > newX )
        {
          int min = m_table.getColumnAtX( getWidth() + TableScrollBar.SIZE - oldX );
          int max = m_table.getColumnAtX( getWidth() + TableScrollBar.SIZE - newX );
          addColumns( min, max );
        }
        else
        {
          int min = m_table.getColumnAtX( 0.0 - newX );
          int max = m_table.getColumnAtX( 0.0 - oldX );
          addColumns( min, max );
        }
      }
    } );

    // add listener for vertical scrolling
    translateYProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        setClip();
        int oldY = oldValue.intValue();
        int newY = newValue.intValue();

        if ( oldY > newY )
        {
          int min = m_table.getRowAtY( getHeight() + TableScrollBar.SIZE - oldY );
          int max = m_table.getRowAtY( getHeight() + TableScrollBar.SIZE - newY );
          addRows( min, max );
        }
        else
        {
          int min = m_table.getRowAtY( 0.0 - newY );
          int max = m_table.getRowAtY( 0.0 - oldY );
          addRows( min, max );
        }
      }
    } );

  }

  /****************************************** setClip ********************************************/
  private void setClip()
  {
    // set clipping rectangle position & dimensions
    Rectangle rect = (Rectangle) getClip();
    rect.setLayoutX( -getTranslateX() );
    rect.setLayoutY( -getTranslateY() );

    double w = getWidth();
    if ( !m_table.getVerticalScrollBar().isVisible() )
      w += TableScrollBar.SIZE;

    double h = getHeight();
    if ( !m_table.getHorizontalScrollBar().isVisible() )
      h += TableScrollBar.SIZE;

    rect.setWidth( w );
    rect.setHeight( h );
  }

  /****************************************** getCell ********************************************/
  private Cell getCell( int column, int row )
  {
    // return cell for specified column & row, or null if does not exist
    int hash = column * 9999 + row;
    return m_cells.get( hash );
  }

  /***************************************** cellExists ******************************************/
  private boolean cellExists( int column, int row )
  {
    // return true if a cell already registered for specified column & row
    int hash = column * 9999 + row;
    return m_cells.containsKey( hash );
  }

  /**************************************** cellRegister *****************************************/
  private void cellRegister( int column, int row, Cell cell )
  {
    // register cell as existing
    int hash = column * 9999 + row;
    m_cells.put( hash, cell );
  }

  /***************************************** addColumns ******************************************/
  private void addColumns( int min, int max )
  {
    // create columns of visible body cells
    int startRow = m_table.getRowAtY( 0.0 - getTranslateY() );
    int endRow = m_table.getRowAtY( getHeight() + TableScrollBar.SIZE - getTranslateY() );
    int startY = m_table.getRowStartY( startRow );
    int x = m_table.getColumnStartX( min );

    for ( int column = min; column <= max; column++ )
    {
      int width = m_table.getColumnWidth( column );
      int y = startY;

      for ( int row = startRow; row <= endRow; row++ )
      {
        int height = m_table.getRowHeight( row );

        if ( !cellExists( column, row ) )
        {
          Cell cell = createCell( column, row, x, y, width, height );
          if ( cell == null )
            continue;
          cellRegister( column, row, cell );
          getChildren().add( cell );
        }
        y += height;
      }
      x += width;
    }

  }

  /******************************************* addRows *******************************************/
  private void addRows( int min, int max )
  {
    // create row of visible body cells
    int startColumn = m_table.getColumnAtX( 0.0 - getTranslateX() );
    int endColumn = m_table.getColumnAtX( getWidth() + TableScrollBar.SIZE - getTranslateX() );
    int startX = m_table.getColumnStartX( startColumn );
    int y = m_table.getRowStartY( min );

    for ( int row = min; row <= max; row++ )
    {
      int height = m_table.getRowHeight( row );
      int x = startX;

      for ( int column = startColumn; column <= endColumn; column++ )
      {
        int width = m_table.getColumnWidth( column );

        if ( !cellExists( column, row ) )
        {
          Cell cell = createCell( column, row, x, y, width, height );
          if ( cell == null )
            continue;
          cellRegister( column, row, cell );
          getChildren().add( cell );
        }
        x += width;
      }
      y += height;
    }

  }

  /***************************************** createCell ******************************************/
  abstract Cell createCell( int column, int row, int x, int y, int w, int h );

}
