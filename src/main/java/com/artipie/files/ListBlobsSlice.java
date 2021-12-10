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

import com.artipie.asto.Content;
import com.artipie.asto.Key;
import com.artipie.asto.Storage;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.headers.ContentType;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rs.RsFull;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.slice.KeyFromPath;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.reactivestreams.Publisher;

/**
 * This slice lists blobs contained in given path.
 * <p>
 * It formats response content according to {@link Function}
 * formatter.
 * It also converts URI path to storage {@link com.artipie.asto.Key}
 * and use it to access storage.
 * </p>
 *
 * @since 0.8
 */
public final class ListBlobsSlice implements Slice {

    /**
     * Storage.
     */
    private final Storage storage;

    /**
     * Blob list format.
     */
    private final BlobListFormat format;

    /**
     * Mime type.
     */
    private final String mtype;

    /**
     * Path to key transformation.
     */
    private final Function<String, Key> transform;

    /**
     * Slice by key from storage.
     *
     * @param storage Storage
     * @param format Blob list format
     * @param mtype Mime type
     */
    public ListBlobsSlice(
        final Storage storage,
        final BlobListFormat format,
        final String mtype
    ) {
        this(storage, format, mtype, KeyFromPath::new);
    }

    /**
     * Slice by key from storage using custom URI path transformation.
     *
     * @param storage Storage
     * @param format Blob list format
     * @param mtype Mime type
     * @param transform Transformation
     * @checkstyle ParameterNumberCheck (20 lines)
     */
    public ListBlobsSlice(
        final Storage storage,
        final BlobListFormat format,
        final String mtype,
        final Function<String, Key> transform
    ) {
        this.storage = storage;
        this.format = format;
        this.mtype = mtype;
        this.transform = transform;
    }

    @Override
    public Response response(final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        return new AsyncResponse(
            CompletableFuture
                .supplyAsync(new RequestLineFrom(line)::uri)
                .thenCompose(
                    uri -> {
                        final Key key = this.transform.apply(uri.getPath());
                        return this.storage.list(key)
                            .thenApply(
                                keys -> {
                                    final String text = this.format.apply(keys);
                                    return new RsFull(
                                        RsStatus.OK,
                                        new Headers.From(new ContentType(this.mtype)),
                                        new Content.From(text.getBytes(StandardCharsets.UTF_8))
                                    );
                                }
                            );
                    }
                )
        );
    }
}
