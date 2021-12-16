/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 artipie.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.files;

import com.artipie.asto.Key;
import java.util.Arrays;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import wtf.g4s8.hamcrest.json.JsonContains;
import wtf.g4s8.hamcrest.json.JsonValueIs;
import wtf.g4s8.hamcrest.json.StringIsJson;

/**
 * Test case for {@link BlobListFormat}.
 * @since 1.0
 */
final class BlobListFormatTest {

    @Test
    void formatTextKeys() {
        MatcherAssert.assertThat(
            BlobListFormat.Standard.TEXT.apply(
                Arrays.asList(
                    new Key.From("a", "file.txt"),
                    new Key.From("c.txt"),
                    new Key.From("b", "file2.txt")
                )
            ),
            Matchers.equalTo("a/file.txt\nc.txt\nb/file2.txt")
        );
    }

    @Test
    void formatTextEmptyKeys() {
        MatcherAssert.assertThat(
            BlobListFormat.Standard.TEXT.apply(Collections.emptyList()),
            Matchers.emptyString()
        );
    }

    @Test
    void formatJsonKeys() {
        MatcherAssert.assertThat(
            BlobListFormat.Standard.JSON.apply(
                Arrays.asList(
                    new Key.From("one", "file.bin"),
                    new Key.From("two", "file3.bin"),
                    new Key.From("three", "file2.bin")
                )
            ),
            new StringIsJson.Array(
                new JsonContains(
                    new JsonValueIs("one/file.bin"),
                    new JsonValueIs("two/file3.bin"),
                    new JsonValueIs("three/file2.bin")
                )
            )
        );
    }

    @Test
    void formatJsonEmptyKeys() {
        MatcherAssert.assertThat(
            BlobListFormat.Standard.JSON.apply(Collections.emptyList()),
            new StringIsJson.Array(new JsonContains())
        );
    }
}
