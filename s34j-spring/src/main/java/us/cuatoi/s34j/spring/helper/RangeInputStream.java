/*
 * Copyright (C) 2018 dungviettran89@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package us.cuatoi.s34j.spring.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RangeInputStream extends InputStream {
    public static final Logger logger = LoggerFactory.getLogger(RangeInputStream.class);
    private final InputStream is;
    private final String rangeHeader;
    private final List<Range> ranges;
    private final long length;
    private long index = 0;

    public RangeInputStream(InputStream is, long length, String rangeHeader) {
        this.is = is;
        this.length = length;
        this.rangeHeader = rangeHeader;
        ranges = decodeRange(rangeHeader);
        logger.info("New range input stream. Header={} Range={}", rangeHeader, ranges);
    }

    public static List<Range> decodeRange(String rangeHeader) {
        List<Range> ranges = new ArrayList<>();
        String byteRangeSetRegex = "(((?<byteRangeSpec>(?<firstBytePos>\\d+)-(?<lastBytePos>\\d+)?)|(?<suffixByteRangeSpec>-(?<suffixLength>\\d+)))(,|$))";
        String byteRangesSpecifierRegex = "bytes=(?<byteRangeSet>" + byteRangeSetRegex + "{1,})";
        Pattern byteRangeSetPattern = Pattern.compile(byteRangeSetRegex);
        Pattern byteRangesSpecifierPattern = Pattern.compile(byteRangesSpecifierRegex);
        Matcher byteRangesSpecifierMatcher = byteRangesSpecifierPattern.matcher(rangeHeader);
        if (byteRangesSpecifierMatcher.matches()) {
            String byteRangeSet = byteRangesSpecifierMatcher.group("byteRangeSet");
            Matcher byteRangeSetMatcher = byteRangeSetPattern.matcher(byteRangeSet);
            while (byteRangeSetMatcher.find()) {
                Range range = new Range();
                if (byteRangeSetMatcher.group("byteRangeSpec") != null) {
                    String start = byteRangeSetMatcher.group("firstBytePos");
                    String end = byteRangeSetMatcher.group("lastBytePos");
                    range.start = Integer.valueOf(start);
                    range.end = end == null ? null : Integer.valueOf(end);
                } else if (byteRangeSetMatcher.group("suffixByteRangeSpec") != null) {
                    range.suffixLength = Integer.valueOf(byteRangeSetMatcher.group("suffixLength"));
                } else {
                    throw new RuntimeException("Invalid range header");
                }
                ranges.add(range);
            }
        } else {
            throw new RuntimeException("Invalid range header");
        }
        return ranges;
    }

    @Override
    public int read() throws IOException {
        long skipped = 0;
        while (!inRange(index++)) {
            if (index >= length) {
                return -1;
            }
            skipped += is.skip(1);
        }
        if (skipped > 0) {
            logger.debug("Skipped {} bytes", skipped);
        }
        return is.read();
    }

    private boolean inRange(long index) {
        for (Range range : ranges) {
            if (range.suffixLength != null && index >= length - range.suffixLength) {
                return true;
            } else if (range.start != null && range.end != null && index >= range.start && index < range.end) {
                return true;
            } else if (range.start != null && range.end == null && index >= range.start) {
                return true;
            }
        }
        return false;
    }

    static class Range {
        Integer start;
        Integer end;
        Integer suffixLength;

        @Override
        public String toString() {
            return "Range{" +
                    "start=" + start +
                    ", end=" + end +
                    ", suffixLength=" + suffixLength +
                    '}';
        }
    }
}
