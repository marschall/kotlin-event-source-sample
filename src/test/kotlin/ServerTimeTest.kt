package com.github.marschall;

import org.junit.Test
import org.junit.Assert.*
import java.util.regex.Pattern

class ServerTimeTest {

    Test fun formatting() {
        assertTrue(Pattern.matches("\\d\\d:\\d\\d:\\d\\d\r\n", formattedTime()))
    }

}
