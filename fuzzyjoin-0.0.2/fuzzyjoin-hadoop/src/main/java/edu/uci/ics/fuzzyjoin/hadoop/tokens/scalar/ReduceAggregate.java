/**
 * Copyright 2010-2011 The Regents of the University of California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS"; BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under
 * the License.
 * 
 * Author: Rares Vernica <rares (at) ics.uci.edu>
 */

package edu.uci.ics.fuzzyjoin.hadoop.tokens.scalar;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

/**
 * @author rares
 * 
 *         KEY1: token
 * 
 *         VALUE1: count
 * 
 */
public class ReduceAggregate extends MapReduceBase implements
        Reducer<Text, IntWritable, Text, IntWritable> {

    private final IntWritable countWritable = new IntWritable();

    private FSDataOutputStream out;

    public void configure(JobConf job) {
        // Setup code...
        try {
            FileSystem fs = FileSystem.get(job);
            Path path = new Path("dblp-small/debug-reduce-tokensbasic-phase1.txt");
            out = fs.create(path, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reduce(Text key, Iterator<IntWritable> values,
            OutputCollector<Text, IntWritable> output, Reporter reporter)
            throws IOException {
        int count = 0;

        while (values.hasNext()) {
            count += values.next().get();
        }

        countWritable.set(count);
        output.collect(key, countWritable);

        // Write the output to HDFS for debugging
        out.writeBytes("Key: " + key + " Value: " + countWritable + "\n");
    }

    @Override
    public void close() throws IOException {
        if (out != null) {
            out.close();
        }
    }
}
