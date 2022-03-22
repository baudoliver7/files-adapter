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
import com.artipie.asto.Meta;
import com.artipie.asto.Storage;
import com.artipie.http.Headers;
import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.async.AsyncResponse;
import com.artipie.http.rq.RequestLineFrom;
import com.artipie.http.rq.RqParams;
import com.artipie.http.rs.RsWithHeaders;
import com.artipie.http.slice.KeyFromPath;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;

/**
 * Slice that returns metadata of a file when user requests it.
 *
 * @since 1.0
 * @todo #107:30min Add test coverage for `FileMetaSlice`
 *  We should test that this slice return expected metadata (`X-Artipie-MD5`,
 *  `X-Artipie-Size` and `X-Artipie-CreatedAt`) when an user specify URL parameter
 *  `meta` to true. We should also check that nothing is return in the opposite case.
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class FileMetaSlice implements Slice {

    /**
     * Meta parameter.
     */
    private static final String META_PARAM = "meta";

    /**
     * Storage.
     */
    private final Storage storage;

    /**
     * Slice to wrap.
     */
    private final Slice origin;

    /**
     * Ctor.
     * @param origin Slice to wrap
     * @param storage Storage where to find file
     */
    public FileMetaSlice(final Slice origin, final Storage storage) {
        this.origin = origin;
        this.storage = storage;
    }

    @Override
    public Response response(
        final String line,
        final Iterable<Map.Entry<String, String>> iterable,
        final Publisher<ByteBuffer> publisher
    ) {
        final Response raw = this.origin.response(line, iterable, publisher);
        final URI uri = new RequestLineFrom(line).uri();
        final Optional<String> meta = new RqParams(uri).value(FileMetaSlice.META_PARAM);
        final Response response;
        if (meta.isPresent() && Boolean.parseBoolean(meta.get())) {
            final Key key = new KeyFromPath(uri.getPath());
            response = new AsyncResponse(
                this.storage.exists(key)
                    .thenCompose(
                        exist -> {
                            final CompletionStage<Response> result;
                            if (exist) {
                                result = this.storage.metadata(key)
                                    .thenApply(
                                        mtd -> new RsWithHeaders(
                                            raw,
                                            new FileHeaders(mtd)
                                        )
                                    );
                            } else {
                                result = CompletableFuture.completedFuture(raw);
                            }
                            return result;
                        }
                    )
            );
        } else {
            response = raw;
        }
        return response;
    }

    /**
     * File headers from Meta.
     * @since 1.0
     */
    private static final class FileHeaders extends Headers.Wrap {

        /**
         * Ctor.
         * @param mtd Meta
         */
        FileHeaders(final Meta mtd) {
            super(FileHeaders.from(mtd));
        }

        /**
         * Headers from meta.
         * @param mtd Meta
         * @return Headers
         */
        private static Headers from(final Meta mtd) {
            final Map<Meta.OpRWSimple<?>, String> fmtd = new HashMap<>();
            fmtd.put(Meta.OP_MD5, "X-Artipie-MD5");
            fmtd.put(Meta.OP_CREATED_AT, "X-Artipie-CreatedAt");
            fmtd.put(Meta.OP_SIZE, "X-Artipie-Size");
            final Map<String, String> hdrs = new HashMap<>();
            for (final Map.Entry<Meta.OpRWSimple<?>, String> entry : fmtd.entrySet()) {
                hdrs.put(entry.getValue(), mtd.read(entry.getKey()).get().toString());
            }
            return new Headers.From(hdrs.entrySet());
        }
    }
}
