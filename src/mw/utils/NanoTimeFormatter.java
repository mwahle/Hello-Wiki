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

/**
 * A simple class with static methods that convert a number of nanoseconds into a formatted string.
 * 
 * @author mwahle
 */
public class NanoTimeFormatter {

    /**
     * Private constructor. This class should not be instantiated.
     */
    private NanoTimeFormatter() {
    }

    /**
     * Convert the given number of nano-seconds to hours and minutes with number of minutes rounded.
     * 
     * @param iNanoSeconds a long with the number of nano-seconds
     * @return formatted string like h:mm
     */
    public static String getHMM(long iNanoSeconds) {
	double fHours, fMinutes;

	fHours = Math.floor(iNanoSeconds / 3600e9);
	iNanoSeconds -= (long) fHours * 3600e9;

	fMinutes = Math.round(iNanoSeconds / 60e9);

	return String.format("%.0f:%02.0f", fHours, fMinutes);
    }

    /**
     * Convert the given number of nano-seconds to minutes and seconds (with number of seconds rounded).
     * 
     * @param iNanoSeconds a long with the number of nano-seconds
     * @return formatted string like m:ss
     */
    public static String getMSS(long iNanoSeconds) {
	double fMinutes = Math.floor(iNanoSeconds / 60e9);
	iNanoSeconds -= (long) fMinutes * 60e9;

	double fSeconds = Math.round(iNanoSeconds / 1e9);

	return String.format("%.0f:%02.0f", fMinutes, fSeconds);
    }

    /**
     * Convert the given number of nano-seconds to hours, minutes, and seconds (with number of seconds rounded).
     * 
     * @param iNanoSeconds a long with the number of nano-seconds
     * @return formatted string like h:mm:ss
     */
    public static String getHMMSS(long iNanoSeconds) {
	double fHours = Math.floor(iNanoSeconds / 3600e9);
	iNanoSeconds -= (long) fHours * 3600e9;

	double fMinutes = Math.floor(iNanoSeconds / 60e9);
	iNanoSeconds -= (long) fMinutes * 60e9;

	double fSeconds = Math.round(iNanoSeconds / 1e9);

	return String.format("%.0f:%02.0f:%02.0f", fHours, fMinutes, fSeconds);
    }

    /**
     * Convert the given number of nano-seconds to seconds. Result is rounded.
     * 
     * @param iNanoSeconds a long with the number of nano-seconds
     * @return number of seconds as a string
     */
    public static String getS(long iNanoSeconds) {
	double fSeconds = (double) iNanoSeconds / (double) 1e9;
	fSeconds = Math.round(fSeconds);

	return String.format("%.0f", fSeconds);
    }

    /**
     * Convert the given number of nano-seconds to seconds. Result is rounded.
     * 
     * @param iNanoSeconds a long with the number of nano-seconds
     * @return number of seconds as a string
     */
    public static String getSf(long iNanoSeconds, int iFractionalDigits) {
	double fSeconds = (double) iNanoSeconds / (double) 1e9;
	fSeconds = Math.floor(fSeconds);

	iNanoSeconds -= fSeconds * (double) 1e9;

	String fraction = String.format("%09d", iNanoSeconds).substring(0, iFractionalDigits);

	return String.format("%.0f.%s", fSeconds, fraction);
    }

    /**
     * Convert the given number of nano-seconds to minutes. Result is rounded.
     * 
     * @param iNanoSeconds a long with the number of nano-seconds
     * @return number of minutes as a string
     */
    public static String getM(long iNanoSeconds) {
	double fMinutes = (double) iNanoSeconds / (double) 60e9;
	fMinutes = Math.round(fMinutes);

	return String.format("%.0f", fMinutes);
    }
}
