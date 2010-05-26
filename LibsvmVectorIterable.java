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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.apache.mahout.common.IOUtils;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;

/**
 * The Class LibsvmVectorIterable.
 */
public class LibsvmVectorIterable implements Iterable<Vector> {
  
  /** The buff. */
  private final BufferedReader buff;
  private final List<Double> labels;
  /**
   * Instantiates a new libsvm vector iterable.
   * 
   * @param file
   *          the file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public LibsvmVectorIterable(File file, List<Double> labels) throws IOException {
    this(new FileReader(file), labels);
  }
  
  /**
   * Instantiates a new libsvm vector iterable.
   * 
   * @param file
   *          the file
   * @param encoding
   *          the encoding
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public LibsvmVectorIterable(File file, Charset encoding, List<Double> labels) throws IOException {
    this(new InputStreamReader(new FileInputStream(file), encoding), labels);
  }
  
  /**
   * Instantiates a new libsvm vector iterable.
   * 
   * @param file
   *          the file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public LibsvmVectorIterable(String file, List<Double> labels) throws IOException {
    this(new StringReader(file), labels);
  }
  
  /**
   * Instantiates a new libsvm vector iterable.
   * 
   * @param reader the reader
   * @param labels the labels
   */
  public LibsvmVectorIterable(Reader reader, List<Double> labels) {
    if (reader instanceof BufferedReader) {
      buff = (BufferedReader) reader;
    } else {
      buff = new BufferedReader(reader);
    }
    
    this.labels = labels;
  }
  
  
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Iterable#iterator()
   */
  public Iterator<Vector> iterator() {
    LibsvmIterator libInterator = new LibsvmIterator(this.getLabels());
    return libInterator;
  }
  
  public List<Double> getLabels() {
    return labels;
  }

  /**
   * The Class LibsvmIterator.
   */
  private class LibsvmIterator implements Iterator<Vector> {
    
    /** The line. */
    private String line;
    
    private List<Double> labels;
    
    /**
     * Instantiates a new libsvm iterator.
     */
    private LibsvmIterator(List<Double> label) {
      this.labels = label; 
      goToNext();
    }
    
    /**
     * Go to next line of file
     */
    private void goToNext() {
      line = null;
      try {
        while ((line = buff.readLine()) != null) {
          line = line.trim();
          if (line.length() > 0
              && line.startsWith(LibsvmModel.Libsvm_COMMENT) == false) {
            break;
          }
        }
      } catch (IOException e) {
        line = null;
      }
      if (line == null) {
        IOUtils.quietClose(buff);
      }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
      return null != line;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#next()
     */
    @Override
    public Vector next() {
      if (null == line) {
        throw new NoSuchElementException();
      }
      
      Vector result = new RandomAccessSparseVector(Integer.MAX_VALUE, 12);
      this.labels.add(str2Vector(line, result));
      
      goToNext();
      return result;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove not supported");
    }
    
    /**
     * String to SparseVector converter for sample string to vector converting.
     * 
     * @param str
     *          sample's string ( label index:value ... )
     * @param row
     *          SparseVector.
     * @return label label of this sample, if the sample does NOT have label,
     *         return -Double.MAX_VALUE
     */
    private double str2Vector(String str, Vector row) {
      
      String temp;
      String[] array;
      String[] labelTest;
      int key;
      double v;
      double label;
      int i = 0;
      
      Pattern splitter = Pattern.compile("\\s++");
      if (0 == str.indexOf("#")) {
        return 0;
      } else if (2 == str.split("#").length) {
        temp = str.split("#")[0];
      } else {
        temp = str;
      }
      
      temp.trim();
      // handle matlab data set :( e.g. 2: .4
      array = splitter.split(temp.replace(": .", ":0."));
      
      // skip the empty header.
      while (array[i].isEmpty()) {
        i++;
      }
      
      labelTest = array[i].replace("+", "").replace(" ", "").split(":");
      if (1 == labelTest.length) {
        label = Double.parseDouble(labelTest[0]);
      } else { // do NOT have label
        label = -Double.MAX_VALUE;
        i--;
      }
      
      for (int j = i + 1; j < array.length; j++) {
        key = Integer.parseInt(array[j].split(":")[0]);
        v = Double.parseDouble(array[j].split(":")[1]);
        row.setQuick(key, v);
      }
      return label;
    }
    
  }
  
}
