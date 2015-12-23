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

  private int                    m_maxShownColumn = -1;
  private int                    m_maxShownRow    = -1;
  private HashMap<Integer, Cell> m_cells          = new HashMap<Integer, Cell>();

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
        int oldWidth = oldValue.intValue();
        int newWidth = newValue.intValue();

        // if new width not greater, can ignore
        if ( oldWidth >= newWidth )
          return;

        // width has increased, so add any extra new columns needed
        int newMax = m_table.getColumnAtX( newWidth );
        for ( int column = m_maxShownColumn + 1; column <= newMax; column++ )
        {
          addColumn( column );
          m_maxShownColumn = column;
        }
      }
    } );

    // add listener for height change
    heightProperty().addListener( new ChangeListener<Number>()
    {
      @Override
      public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue )
      {
        int oldHeight = oldValue.intValue();
        int newHeight = newValue.intValue();

        // if new height not greater, can ignore
        if ( oldHeight >= newHeight )
          return;

        // height has increased, so add any extra new rows needed
        int newMax = m_table.getRowAtY( newHeight );
        for ( int row = m_maxShownRow + 1; row <= newMax; row++ )
        {
          addRow( row );
          m_maxShownRow = row;
        }
      }
    } );

  }

  /****************************************** getCell ********************************************/
  public Cell getCell( int column, int row )
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

  /***************************************** addColumn *******************************************/
  private void addColumn( int column )
  {
    // create column of visible body cells
    int startRow = m_table.getRowAtY( 0.0 );
    int endRow = m_table.getRowAtY( getHeight() );
    int x = m_table.getColumnStartX( column );
    int y = m_table.getRowStartY( startRow );
    int width = m_table.getColumnWidth( column );

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

  }

  /******************************************* addRow ********************************************/
  private void addRow( int row )
  {
    // create row of visible body cells 
    int startColumn = m_table.getColumnAtX( 0.0 );
    int endColumn = m_table.getColumnAtX( getWidth() );
    int height = m_table.getRowHeight( row );
    int x = m_table.getColumnStartX( startColumn );
    int y = m_table.getRowStartY( row );

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

  }

  /***************************************** createCell ******************************************/
  abstract Cell createCell( int column, int row, int x, int y, int w, int h );

}
