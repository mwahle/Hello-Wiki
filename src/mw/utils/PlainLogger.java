/* Copyright 2011 Manuel Wahle
 *
 * This file is part of Hello-Wiki.
 *
 *    Hello-Wiki is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Hello-Wiki is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with Hello-Wiki.  If not, see <http://www.gnu.org/licenses/>.
 */

package mw.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This is a simple class to log text to a file, to the console, or to both. It logs strings as they are passed, not adding any information or formatting. A lock file is created upon instantiating
 * with a log filename. Whenever <code>close()</code> is called, the log file is removed.
 * 
 * @author mwahle
 */
public class PlainLogger {
    private boolean _bLog2console = true;
    private boolean _bLog2file = false;
    private boolean _bFlushLinewise = false;
    private String _logLine = "";
    private String _logFileName = "";
    private BufferedWriter _bufferedWriter = null;
    private File _lockFile = null;

    /**
     * Constructor for logging to console only.
     */
    public PlainLogger() {
    }

    /**
     * Constructor for logging to a file and to the console. Console or file logging can be selectively disabled with {@link #setLog2Console(boolean)} and {@link #setLog2File(boolean)}. Falls back to console logging if given filename is empty.
     * 
     * @param logFileName the name of the output file
     * @throws IOException
     */
    public PlainLogger(String logFileName) throws IOException {
        if (logFileName != null && !logFileName.isEmpty()) {
            int i = 0;
            _lockFile = new File(logFileName + ".lck");
            while (_lockFile.exists()) {
                _lockFile = new File(logFileName + "." + (++i) + ".lck");
            }

            _lockFile.createNewFile();

            if (i == 0) {
                _bufferedWriter = new BufferedWriter(new FileWriter(logFileName));
            } else {
                _bufferedWriter = new BufferedWriter(new FileWriter(logFileName + "." + i));
            }
            _bLog2file = true;
            _logFileName = logFileName;
        }
    }

    /**
     * Add the fragment to the current line without ending the line. Call {@link #log(String)} or {@link #flush()} to end the line.
     * 
     * @param fragment a string that is to be added to the current line
     */
    public void add(String fragment) {
	_logLine += fragment;
    }

    /**
     * Add line to the output and end the line.
     * 
     * @param message a string with the message text to be logged
     */
    public void log(String message) {
	if (_bLog2console)
	    System.out.println(_logLine + message);

	if (_bLog2file) {
	    try {
		_bufferedWriter.write(_logLine + message);
		_bufferedWriter.newLine();
		if (_bFlushLinewise)
		    _bufferedWriter.flush();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

	_logLine = "";
    }

    /**
     * Set if logging to the console is enabled.
     * 
     * @param bLog2console a boolean
     */
    public void setLog2Console(boolean bLog2console) {
	// write current line if not empty
	if (_bLog2console != bLog2console)
	    if (!_logLine.isEmpty())
		log("");

	_bLog2console = bLog2console;
    }

    /**
     * Get if logging to the console is enabled.
     * 
     * @return true or false
     */
    public boolean getLog2Console() {
	return _bLog2console;
    }

    /**
     * Set if logging to the file is enabled. Will have no effect if this logger was constructed without a filename.
     * 
     * @param bLog2file a boolean
     */
    public void setLog2File(boolean bLog2file) {
	if (_bufferedWriter != null) {
	    // flush current line if not empty
	    if (_bLog2file != bLog2file)
		if (!_logLine.isEmpty()) {
		    log("");
		    flush();
		}

	    _bLog2file = bLog2file;
	}
    }

    /**
     * Get if logging to the file is enabled.
     * 
     * @return true or false
     */
    public boolean getLog2File() {
	return _bLog2file;
    }

    /**
     * By default, the underlying BufferedReader decides when to write the lines into the logfile. This behavior can be changed by calling this method with
     * <code>true</code> as parameter, which will cause every line to be written to the logfile immediately. This is handy if you're watching the logfile as
     * your program progresses, but may be inefficient if you write large amounts of text. Calling this method with <code>false</code> give control back to the
     * BufferedReader to decide when to write into the file.
     * 
     * @param bFlushLinewise boolean to determine if each line should be flushed immediately
     */
    public void setFlushLinewise(boolean bFlushLinewise) {
        _bFlushLinewise = bFlushLinewise;
    }

    /**
     * Get if flushing per line is enabled
     * @return true or false
     */
    public boolean getFlushLinewise() {
        return _bFlushLinewise;
    }


    /**
     * Flush the current output to console and file, if enabled. See also {@link #setFlushLinewise(boolean)}.
     */
    public void flush() {
	// flush current line if not empty
	if (!_logLine.isEmpty())
	    log("");

	if (_bufferedWriter != null) {
	    try {
		_bufferedWriter.flush();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * Close the output file if there is one.
     */
    public void close() {
	if (!_logLine.isEmpty())
	    log("");

	if (_logFileName != null)
	    System.out.println("Closing logger.");
	else
	    System.out.print("Closing log" + _logFileName);
	    
	if (_bufferedWriter != null) {
	    try {
		_bufferedWriter.close();
		if (_lockFile != null)
		    _lockFile.delete();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
     * If instance was created with a filename, return it. Otherwise, return empty string.
     * @return the log filename
     */
    public String getFileName() {
        return _logFileName;
    }
}
