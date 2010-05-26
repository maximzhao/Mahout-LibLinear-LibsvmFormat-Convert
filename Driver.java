/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.utils.vectors.libsvm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.mahout.common.CommandLineUtil;
import org.apache.mahout.math.VectorWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class Driver.
 */
public class Driver {
  
  /** The Constant log. */
  private static final Logger log = LoggerFactory
      .getLogger(Driver.class);
  
  /**
   * Instantiates a new driver.
   */
  private Driver() {}
  
  /**
   * The main method.
   * 
   * @param args the arguments
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void main(String[] args) throws IOException {
    DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    ArgumentBuilder abuilder = new ArgumentBuilder();
    GroupBuilder gbuilder = new GroupBuilder();
    
    Option inputOpt = obuilder
        .withLongName("input")
        .withRequired(true)
        .withArgument(
          abuilder.withName("input").withMinimum(1).withMaximum(1).create())
        .withDescription(
          "The file or directory containing the ARFF files.  If it is a directory, all .arff files will be converted")
        .withShortName("d").create();
    
    Option outputOpt = obuilder
        .withLongName("output")
        .withRequired(true)
        .withArgument(
          abuilder.withName("output").withMinimum(1).withMaximum(1).create())
        .withDescription(
          "The output directory.  Files will have the same name as the input, but with the extension .mvc")
        .withShortName("o").create();
    
    Option maxOpt = obuilder
        .withLongName("max")
        .withRequired(false)
        .withArgument(
          abuilder.withName("max").withMinimum(1).withMaximum(1).create())
        .withDescription(
          "The maximum number of vectors to output.  If not specified, then it will loop over all docs")
        .withShortName("m").create();
    
    Option dictOutOpt = obuilder.withLongName("dictOut").withRequired(true)
        .withArgument(
          abuilder.withName("dictOut").withMinimum(1).withMaximum(1).create())
        .withDescription("The file to output the label bindings")
        .withShortName("t").create();
    
    Option outWriterOpt = obuilder
        .withLongName("outputWriter")
        .withRequired(false)
        .withArgument(
          abuilder.withName("outputWriter").withMinimum(1).withMaximum(1)
              .create())
        .withDescription(
          "The VectorWriter to use, either seq (SequenceFileVectorWriter - default) or file (Writes to a File using JSON format)")
        .withShortName("e").create();
    
    Option helpOpt = obuilder.withLongName("help").withDescription(
      "Print out help").withShortName("h").create();
    Group group = gbuilder.withName("Options").withOption(inputOpt).withOption(
      outputOpt).withOption(maxOpt).withOption(helpOpt).withOption(dictOutOpt)
        .withOption(outWriterOpt).create();
    try {
      Parser parser = new Parser();
      parser.setGroup(group);
      CommandLine cmdLine = parser.parse(args);
      
      if (cmdLine.hasOption(helpOpt)) {
        
        CommandLineUtil.printHelp(group);
        return;
      }
      
      if (cmdLine.hasOption(inputOpt)) {// Lucene case
        File input = new File(cmdLine.getValue(inputOpt).toString());
        long maxDocs = Long.MAX_VALUE;
        if (cmdLine.hasOption(maxOpt)) {
          maxDocs = Long.parseLong(cmdLine.getValue(maxOpt).toString());
        }
        if (maxDocs < 0) {
          throw new IllegalArgumentException("maxDocs must be >= 0");
        }
        String outDir = cmdLine.getValue(outputOpt).toString();
        Driver.log.info("Output Dir: {}", outDir);
        String outWriter = null;
        if (cmdLine.hasOption(outWriterOpt)) {
          outWriter = cmdLine.getValue(outWriterOpt).toString();
        }
        File dictOut = new File(cmdLine.getValue(dictOutOpt).toString());
        List<Double> labels = new ArrayList<Double>();
        if (input.exists() && input.isDirectory()) {
          File[] files = input.listFiles();
          
          for (File file : files) {
//            Driver.writeFile(outWriter, outDir, file, maxDocs, labels);
          }
        } else {
//          Driver.writeFile(outWriter, outDir, input, maxDocs, labels);
        }
        Driver.log.info("Dictionary Output file: {}", dictOut);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
            new FileOutputStream(dictOut), Charset.forName("UTF8")));
        for (Double label : labels) {
          writer.append(label.toString()).append('\n');
        }
        writer.close();
        
      }
      
    } catch (OptionException e) {
      Driver.log.error("Exception", e);
      CommandLineUtil.printHelp(group);
    }
  }
  
//  /**
//   * Write file.
//   * 
//   * @param outWriter the out writer
//   * @param outDir the out dir
//   * @param file the file
//   * @param maxDocs the max docs
//   * @param labels the labels
//   * @throws IOException Signals that an I/O exception has occurred.
//   */
//  private static void writeFile(String outWriter,
//                                String outDir,
//                                File file,
//                                long maxDocs,
//                                List<Double> labels) throws IOException {
//    Driver.log.info("Converting File: {}", file);
//    LibsvmVectorIterable iteratable = new LibsvmVectorIterable(file, labels);
//    String outFile = outDir + '/' + file.getName() + ".mvc";
//    
//    VectorWriter vectorWriter;
//    if (outWriter != null) {
//      if (outWriter.equals("file")) {
//        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
//        vectorWriter = new JWriterVectorWriter(writer);
//      } else {
//        vectorWriter = Driver.getSeqFileWriter(outFile);
//      }
//    } else {
//      vectorWriter = Driver.getSeqFileWriter(outFile);
//    }
//    
//    long numDocs = vectorWriter.write(iteratable, maxDocs);
//    vectorWriter.close();
//    Driver.log.info("Wrote: {} vectors", numDocs);
//  }
  
  /**
   * Gets the seq file writer.
   * 
   * @param outFile the out file
   * @return the seq file writer
   * @throws IOException Signals that an I/O exception has occurred.
   */
//  private static VectorWriter getSeqFileWriter(String outFile) throws IOException {
//    Path path = new Path(outFile);
//    Configuration conf = new Configuration();
//    FileSystem fs = FileSystem.get(conf);
//    SequenceFile.Writer seqWriter = SequenceFile.createWriter(fs, conf, path,
//      LongWritable.class, VectorWritable.class);
//    return new SequenceFileVectorWriter(seqWriter);
//  }
  
}
