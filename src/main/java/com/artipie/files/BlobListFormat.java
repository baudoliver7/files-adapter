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
import java.util.Collection;
import java.util.stream.Collectors;
import javax.json.Json;

/**
 * Format of a blob list.
 *
 * @since 0.8
 */
@FunctionalInterface
interface BlobListFormat {

    /**
     * Stamdard format implementations.
     * @since 1.0
     * @checkstyle IndentationCheck (30 lines)
     */
    enum Standard implements BlobListFormat {

        /**
         * Text format renders keys as a list of strings
         * separated by newline char {@code \n}.
         */
        TEXT(
            keys -> keys.stream().map(Key::string).collect(Collectors.joining("\n"))
        ),

        /**
         * Json format renders keys as JSON array with
         * keys items.
         */
        JSON(
            keys -> Json.createArrayBuilder(
                keys.stream().map(Key::string).collect(Collectors.toList())
            ).build().toString()
        );

        /**
         * Format.
         */
        private final BlobListFormat fmt;

        /**
         * Enum instance.
         * @param fmt Format
         */
        Standard(final BlobListFormat fmt) {
            this.fmt = fmt;
        }

        @Override
        public String apply(final Collection<? extends Key> blobs) {
            return this.fmt.apply(blobs);
        }
    }

    /**
     * Apply the format to the list of blobs.
     * @param blobs List of blobs
     * @return Text formatted
     */
    String apply(Collection<? extends Key> blobs);
}
