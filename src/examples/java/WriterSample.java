/*
 * Copyright © 2024 Cloud Software Group, Inc.
 * This file is subject to the license terms contained in the
 * license file that is distributed with this file.
 */

package com.spotfire.samples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;

import com.spotfire.sbdf.BinaryWriter;
import com.spotfire.sbdf.ColumnMetadata;
import com.spotfire.sbdf.FileHeader;
import com.spotfire.sbdf.TableMetadata;
import com.spotfire.sbdf.TableMetadataBuilder;
import com.spotfire.sbdf.TableWriter;
import com.spotfire.sbdf.ValueType;

/**
 * This example is a simple command line tool that writes a simple SBDF file
 * with random data.
 */
public class WriterSample {

    public static void main(String[] args) throws IOException {
        // The command line application requires one argument which is supposed to be
        // the name of the SBDF file to write.
        if (args.length != 1)
        {
            System.out.println("Syntax: WriterSample output.sbdf");
            return;
        }

        String outputFile = args[0];
        
        // First we just open the file as usual and then we need to wrap the stream
        // in a binary writer.
        OutputStream outputStream = new FileOutputStream(outputFile);
        BinaryWriter writer = new BinaryWriter(outputStream);
          
        // When writing an SBDF file you first need to write the file header.
        FileHeader.writeCurrentVersion(writer);

        // The second part of the SBDF file is the metadata, in order to create
        // the table metadata we need to use the builder class.
        TableMetadataBuilder tableMetadataBuilder = new TableMetadataBuilder();

        // The table can have metadata properties defined. Here we add a custom
        // property indicating the producer of the file. This will be imported as 
        // a table property in Spotfire.
        tableMetadataBuilder.addProperty("GeneratedBy", "WriterSample.exe");
        
        // All columns in the table needs to be defined and added to the metadata builder,
        // the required information is the name of the column and the data type.
        ColumnMetadata col1 = new ColumnMetadata("Category", ValueType.STRING);
        tableMetadataBuilder.addColumn(col1);

        // Similar to tables, columns can also have metadata properties defined. Here
        // we add another custom property. This will be imported as a column property
        // in Spotfire.
        col1.addProperty("SampleProperty", "col1");

        ColumnMetadata col2 = new ColumnMetadata("Value", ValueType.DOUBLE);
        tableMetadataBuilder.addColumn(col2);
        col2.addProperty("SampleProperty", "col2");

        ColumnMetadata col3 = new ColumnMetadata("TimeStamp", ValueType.DATETIME);
        tableMetadataBuilder.addColumn(col3);
        col3.addProperty("SampleProperty", "col3");

        // We need to call the build function in order to get an object that we can
        // write to the file.
        TableMetadata tableMetadata = tableMetadataBuilder.build();
        tableMetadata.write(writer);

        int rowCount = 10000;
        Random random = new Random();

        // Now that we have written all the metadata we can start writing the actual data.
        // Here we use a TableWriter to write the data, remember to close the table writer
        // otherwise you will not generate a correct SBDF file.
        TableWriter tableWriter = new TableWriter(writer, tableMetadata);          
        for (int i = 0; i < rowCount; ++i) {            
            // You need to perform one addValue call for each column, for each row in the
            // same order as you added the columns to the table metadata object.             
            // In this example we just generate some random values of the appropriate types.
            // Here we write the first string column.
            String[] col1Values = new String[] {"A", "B", "C", "D", "E"};               
            tableWriter.addValue(col1Values[random.nextInt(5)]);

            // Next we write the second double column.
            double doubleValue = random.nextDouble();               
            if (doubleValue < 0.5) {
                // Note that if you want to write a null value you shouldn't send null to
                // addValue, instead you should use theInvalidValue property of the columns
                // ValueType.                   
                tableWriter.addValue(ValueType.DOUBLE.getInvalidValue());
            } else {                    
                tableWriter.addValue(random.nextDouble());                                            
            }
              
            // And finally the third date time column.              
            tableWriter.addValue(new Date());           
        }
        
        // Finally we need to close the file and write the end of table marker.
        tableWriter.writeEndOfTable();
        writer.close();
        outputStream.close();
            
        System.out.print("Wrote file: ");            
        System.out.println(outputFile);
    }
}
