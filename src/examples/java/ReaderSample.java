/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.samples;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.spotfire.sbdf.BinaryReader;
import com.spotfire.sbdf.ColumnMetadata;
import com.spotfire.sbdf.FileHeader;
import com.spotfire.sbdf.MetadataProperty;
import com.spotfire.sbdf.TableMetadata;
import com.spotfire.sbdf.TableReader;

/**
 * This example is a simple command line tool that reads an SBDF file
 * and writes displays the content in the console.
 */
public class ReaderSample {

    public static void main(String[] args) throws FileNotFoundException {
        // The command line application requires one argument which is supposed to be
        // the name of the SBDF file to read.
        if (args.length != 1) {
            System.out.println("Syntax: ReaderSample inputfile.sbdf");
            return;
        }

        String inputFile = args[0];
        
        // First we just open the file as usual and then we need to wrap the stream
        // in a binary reader.
        InputStream inputStream = new FileInputStream(inputFile);
        BinaryReader reader = new BinaryReader(inputStream);
          
        // We need to start with reading the file header.
        FileHeader.read(reader);

        // Next we need to read the table metadata.
        TableMetadata tableMetadata = TableMetadata.read(reader);
          
        // Write all table metadata. This is what you would see as table properties in Spotfire.          
        for (MetadataProperty property : tableMetadata) {
            System.out.print("Table property \"");
            System.out.print(property.getName());
            System.out.print("\" (type ");
            System.out.print(property.getValueType());
            System.out.print("), value = \"");
            System.out.print(property.getValue());
            System.out.println("\"");
        }

        // From the table metadata we can also receive metadata about the columns in the table.         
        ColumnMetadata[] columns = tableMetadata.getColumns();          
        for (ColumnMetadata column : columns) {    
            // All columns always have a name and a data type.
            System.out.print("Column :");
            System.out.print(column.getName());
            System.out.print(", type: ");
            System.out.println(column.getDataType());
                           
            // Optionally a column may have additional metadata. This is what you will see
            // as column properties in Spotfire. While you can iterate directly over all
            // properties of the column metadata in this case we use the AssignedProperties
            // property instead since that one ignores name and data type which we have
            // already written.               
            for (MetadataProperty property : column.getAssignedProperties()) {
                System.out.print("Column property \"");
                System.out.print(property.getName());
                System.out.print("\" (type ");
                System.out.print(property.getValueType());
                System.out.print("), value = \"");
                System.out.print(property.getValue());
                System.out.println("\"");           
            }
        }

        // Now we can read the data using a table reader. Since the API has a single ReadValue
        // call which reads the next cell we need to keep track of the current column/row index.
        int rowIndex = 0;
        int columnIndex = 0;
        TableReader tableReader = new TableReader(reader, tableMetadata);
        
        while (true)
        {
            // Read the next cell.
            Object value = tableReader.readValue();
            if (value == null)
            {
                // The read value call returns null when we have reached end of file.
                break;
            }

            // Get the metadata for the current column.
            ColumnMetadata column = columns[columnIndex];

            // Since null is indicating end of file we instead need to use the InvalidValue
            // for the columns data type to check for null values.
            if (value == column.getDataType().getInvalidValue()) {
                System.out.print("(null);");
            } else {
                System.out.print(value);
                System.out.print(";");
            }

            columnIndex++;
            if (columnIndex == columns.length) {
                // We've reached the last column so this is the end of a row, start
                // over with the first column again.
                System.out.println();
                columnIndex = 0;
                rowIndex++;
            }
        }

            
        System.out.print(rowIndex);
        System.out.println(" rows read.");
    }     
}
